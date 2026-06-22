← [Back to main README](../README.md)

## Tips, Tricks & The Reasoning Behind Them

This page documents the specific decisions made throughout this template and basically describes:

- Why they were made
- What the pattern is 
- What problem it solves
- What happens if you skip it

Think of it as the "read this before you copy anything" companion to the code and javadocs.

---

### 🗄️ Data Layer, Schema & JPA Mapping

#### 1. Normalize everything before turning it into a database table and Java entity

Translating a conceptual domain model directly into tables, before normalization, produces schemas that seem fine on a whiteboard but break down the moment JPA tries to map them. 

Common results from translating conceptual domain model without doing normalization first are:
- Redundant indexes
- Many-to-many relationships that can't physically exist as drawn (a single column can't hold multiple foreign key values without violating 1NF)
- No clear owner for a relation

The cost of fixing a bad schema after entity classes exist is high, you're changing both the migration script and the mapping annotations at the same time. Normalizing first (identifying the real cardinality, where the FK lives, whether a junction table is needed) means entity classes are modeling something that already makes structural sense.

[See the use-case writeup for a concrete before/after of what this looks like in practice →](usecase.md)

---

#### 2. Prefer unidirectional relationships in a database wrapper service

In a bidirectional relationship, both sides know about each other, the parent has a `List<Child>`, the child has a reference back to the parent. In a unidirectional relationship, only the owning side (usually the child) knows about the other.

**Why unidirectional is easier to manage here:**
- You don't have to manually keep both sides in sync (`parent.addChild(child); child.setParent(parent)`), forgetting one side leaves the in-memory object graph inconsistent even before a flush.
- Jackson won't accidentally serialize both sides and hit infinite recursion (`Parent → children → each Child's parent → back to Parent`), which is a common silent failure in auto-generated REST endpoints where you don't control the serialization.
- It's architecturally honest: in a database wrapper, each entity resource exposes its own data. If you need "all enrollments for a student", that's a query on `Enrollment`, not navigation through `Student.enrollments`.

If you do need bidirectional (e.g. for a specific query pattern), treat it as a deliberate exception, always set `mappedBy` on the non-owning side and `@JsonIgnore` on one direction.

---

#### 3. Use `@MapsId` for strict 1:1 relationships, don't give the child its own auto-increment key

When two entities have a strict 1:1 relationship (one `Profile` per `Student`, always), the child entity doesn't need its own independently generated primary key. `@MapsId` lets the child derive its primary key directly from the parent's, `Profile.id` becomes both the PK and the FK, shared with `Student.id`.

**What this eliminates:**
- A redundant auto-increment sequence for `profiles.id` that exists purely as a technical artifact.
- A separate index on the FK column (`student_id`) alongside the PK index, they're the same column now, so there's only one index to maintain.
- The possibility of a `Profile` row existing with a `student_id` that doesn't match its own `id` (impossible by construction when they're the same column).

**The trade-off to be aware of:** this makes `Profile` impossible to create without an already-persisted `Student`. The auto-generated `add`/`update`/`delete` endpoints from REST Data Panache don't handle this correctly, see point 8 below.

---

#### 4. Deconstruct many-to-many into a junction entity, never put FKs directly in the main tables

A direct many-to-many (placing `course_id` in `students` or `student_id` in `courses`) can't work in a relational database: a single column can't hold multiple values without breaking first normal form. The correct model is a dedicated junction entity (`Enrollment`) that turns the M:M into two 1:M relationships.

**Beyond just "it works":**
- The junction entity becomes a first-class domain object you can query, paginate, and filter independently (`give me all enrollments for course X`), not just a hidden join table.
- You can add attributes to the relationship itself (enrollment date, grade, status) without restructuring the main tables.
- Bulk operations (delete all enrollments for a student before deleting the student) are straightforward queries on the junction entity, not cascades that could ripple unpredictably.

---

### 🔄 Lazy Loading & Serialization

#### 5. Lazy-loaded relations don't stay lazy through auto-generated REST Data Panache endpoints, `@JsonIgnore` the relation field on the child

When REST Data Panache serializes an entity to JSON, it calls all getters. If a getter touches a `FetchType.LAZY` field, Hibernate initializes the proxy right there, firing an extra query per object, per request. Whether this causes a `LazyInitializationException` or a silent N+1 depends on whether the Hibernate session is still open at that point, which isn't guaranteed.

**The pattern that reliably works:**
- Mark the relation field `@JsonIgnore` on the child entity, Jackson never touches it, so the lazy proxy is never triggered through the serialization path.
- If you need the related data in a response, expose it explicitly via a DTO (see point 7) or a custom endpoint with `JOIN FETCH` (see point 6).

This is one of the most common ways auto-generated CRUD silently misbehaves, the endpoint returns data, tests pass, and the N+1 only surfaces in production under load.

[See the N+1 demo for a measured side-by-side →](n1-query-demo.md)

---

#### 6. Use `JOIN FETCH` from the child entity to get a complete parent+child record in a single query

When you need both a child entity and its parent in a single response, don't rely on lazy loading to fetch the parent after the child is loaded. Fetch them together explicitly:

```java
public static Profile findByIdWithStudent(Long id) {
    return find("FROM Profile p LEFT JOIN FETCH p.student WHERE p.id = ?1", id)
            .firstResult();
}
```

**Why this matters:**
- One SQL query instead of two (or N+1 in a list context).
- The parent is fully initialized before serialization begins, no risk of proxy initialization issues.
- The intent is explicit in the code: anyone reading this knows the parent will always be present, not "maybe loaded depending on session state".

The `LEFT JOIN` (not `INNER JOIN`) is intentional, it handles the case where the relation might be nullable without silently dropping the child from the result set.

---

#### 7. Expose flat IDs via custom getter/setter instead of nested objects, leverage Jackson's property accessor convention

For junction entities like `Enrollment` that have FK relations as their core data, exposing the full `Student` or `Course` object in the request/response body is both verbose and problematic (triggers lazy loading, exposes data the consumer didn't ask for). Instead:

- Mark the actual relation field (`student`, `course`) with `@JsonIgnore`, hidden from both request and response.
- Add a custom getter that reads the FK id off the relation: `getStudentId()` → `this.student.id`.
- Add a custom setter that wraps an incoming id into a lazy-reference stub: `setStudentId(Long id)` sets `this.student = new Student(); this.student.id = id;`.

Jackson treats any `getX()`/`setX()` pair as a property named `x`, so `studentId` and `courseId` appear naturally in the Swagger schema as plain `Long` fields, exactly matching what the database table actually stores, and what the consumer actually needs to send.

**Important:** the getter must be read-only, don't initialize `this.student` inside the getter as a side effect. Getters are called by serializers, debuggers, and logging tools; a getter that mutates state will create `Student` stubs in unexpected places and cause silent persistence bugs.

---

### ⚙️ REST Data Panache & Swagger

#### 8. Auto-generated `add`/`update`/`delete` don't work for shared-primary-key entities, use `getReference()` for the fix

For child entities using `@MapsId`, the auto-generated endpoints try to persist the child with an independently assigned ID, which conflicts with the derived PK strategy and fails at the database level. The fix for `add` and `update` is set a reference for the parent entity field:

```java
profile.student = Profile.getEntityManager().getReference(Student.class, id);
profile.persist();
```

`getReference()` builds a lazy proxy using only the ID, no `SELECT` against the parent table, just the FK value needed to satisfy `@MapsId`. The trade-off: if the ID doesn't correspond to a real parent row, the failure surfaces at `persist()` time as a `ConstraintViolationException` (FK violation), not at the point where the reference is created. That returns a raw `500` in this template since there's no exception mapper, a known limitation documented in the relevant javadocs.

For delete, a plain `deleteById` on the child also won't work if the parent must be deleted together (the shared PK means "delete just the child, keep the parent" is structurally valid but semantically wrong here). Use an aggregate endpoint that deletes both in one transaction, see `ProfileResource#deleteAggregate`.

---

#### 9. Disable `count()`, unbounded COUNT is expensive and out of scope for a database wrapper

REST Data Panache exposes a `GET /entity/count` endpoint by default. For large tables, an unbounded `COUNT(*)` can be slow and tie up a connection thread for the duration of the scan. More importantly, in a database wrapper service, the consumer shouldn't be making aggregate analytical queries, those belong in reporting layers or directly in the DBMS.

Disabling it is one line:

```java
@Override
@MethodProperties(exposed = false)
@Operation(hidden = true)
long count();
```

If you genuinely need a count, query it directly via your DBMS rather than exposing it as an API endpoint.

---

#### 10. Swagger is for testing, not full error documentation, use an OAS filter to fix the status codes that REST Data Panache gets wrong

The auto-generated OpenAPI spec from REST Data Panache is approximate: for example it documents status codes that never actually occur (e.g. `201` on a `PUT` that only returns `204`) and omits codes that do occur (e.g. `404` on a single-resource `GET`). Left as-is, Swagger becomes misleading as a contract reference.

An `OASFilter` (see `OASFilter.java` + `OASFilterHelperPanache.java`) post-processes the generated spec at startup to correct these. It doesn't fix everything, structured error response bodies (validation failures, constraint violations) still can't be documented from a filter like this, since REST Data Panache doesn't expose typed error responses through the OpenAPI generation pipeline. That's a known limitation: **Swagger here is reliable for testing happy-path CRUD, not as an exhaustive error contract.**

---

#### 11. When to stop using `PanacheEntityResource`, API versioning is the signal

`PanacheEntityResource` trades architectural separation for speed. That trade-off pays off in a narrow, stable database wrapper service. The moment you need to version your API (e.g. `/v1/students` vs `/v2/students` with a different response shape), the trade-off stops paying:

- You can't change the response shape without breaking consumers, because there's no DTO layer to absorb the change.
- You can't evolve the database schema independently of the API, because the entity *is* the API contract.

At that point, move to standard Jakarta REST endpoints (`@GET`, `@POST`, etc.) with Java records as DTOs. This decouples the database schema from the HTTP contract, you can rename a column without changing the API, or change the API response shape without touching the database.

If you're not worried about versioning yet, `PanacheEntityResource` is fine. The moment that concern appears, it's time to migrate.

---

### ✅ Bean Validation

#### 12. `quarkus-hibernate-validator` is a required dependency, without it, `@NotNull`/`@Size` are inert metadata

The `jakarta.validation.constraints.*` annotations (`@NotNull`, `@Size`, `@NotBlank`, `@Positive`, etc.) are just annotations, they don't do anything without a validation engine to read and enforce them. Quarkus doesn't bundle Hibernate Validator by default; it's a separate extension:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>
```

Without this dependency, a `@NotBlank String name` will happily persist a blank string with a `201` and no error. With it, the same request returns a `400` with a structured violation body, automatically, without any custom exception mapper.

---

#### 13. `@NotNull` alone doesn't cascade into nested objects, add `@Valid` too for that

When validating a record or object that contains other objects, `@NotNull` only checks that the field itself isn't null. It doesn't trigger validation of the nested object's own constraints. To cascade:

```java
public record ProfileStudentDto(
        @NotNull @Valid Profile profile,
        @NotNull @Valid Student student
) implements Serializable {}
```

`@NotNull` → "this field must be present".
`@Valid` → "and once confirmed present, also validate its internal constraints".

Without `@Valid`, a `ProfileStudentDto` with a non-null `Student` whose `name` is blank will pass the DTO-level validation and only fail later at `persist()`, deeper in the call stack, with a less helpful error context.

---

← [Back to main README](../README.md)
