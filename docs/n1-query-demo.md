[← back to README](../README.md)

## 🔍 The N+1 Problem, Demonstrated

This page is a companion to the [main README](../README.md) specifically the section on lazy loading silently breaking
through auto-generated endpoints. Rather than just describing the problem, this repo ships two real endpoints on the
`Profile` entity that do the exact same thing from the outside, but behave very differently underneath.

If you only read one part of this repo to understand *why* relation mapping matters before you copy an entity, make it
this one.

---

### The Two Endpoints

Both endpoints return the same `ProfileStudentDto`, for the same `Profile`. The only difference is how the `student`
relation is fetched.

#### ❌ Unoptimized: N+1

This happens when findById invoked on an entity that have FK and need to access the fk's fields triggers N+1

```java

@GET
@Path("/full/{id : \\d+}/demo/n+1problem")
@Produces("application/json")
default ProfileStudentDto getAggregateDemoN1Problem(@PathParam("id") long id) {
    Profile p = Profile.findById(id);
    return new ProfileStudentDto(p, p.student);
}
```

#### ✅ Optimized: single query via `JOIN FETCH`

```java

@GET
@Path("/full/{id : \\d+}")
@Produces(MediaType.APPLICATION_JSON)
default ProfileStudentDto getAggregate(@PathParam("id") long id) {
    Profile p = Profile.findByIdWithStudent(id);
    return new ProfileStudentDto(p, p.student);
}
```

```java
    public static Profile findByIdWithStudent(Long id) {
    return find("FROM Profile p LEFT JOIN FETCH p.student WHERE p.id = ?1", id)
            .firstResult();
}
```

From a Swagger / API-consumer point of view, **these two endpoints are indistinguishable.** Same response shape, same
status codes, same `ProfileStudentDto`. That's exactly what makes this class a bit tricky, nothing about the contract
tells
you one of them is doing extra work behind the scenes.

---

### What's Actually Happening Underneath

|                                                   | Unoptimized (`/demo/n1+query`)                                                                                                                                                                                                                        | Optimized (`/demo/`)                                                                                            |
|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| How `student` is fetched                          | `Profile.findById(id)` loads `Profile` only. `student` stays an uninitialized lazy proxy.                                                                                                                                                             | `findByIdWithStudent(id)` uses `LEFT JOIN FETCH` `Profile` and `Student` are loaded together in one round trip. |
| What triggers the extra query                     | The `ProfileDto` constructor reads `dao.student`, which forces Hibernate to initialize the proxy *right there* while the Hibernate session is still open (request-scoped), so it succeeds silently instead of throwing `LazyInitializationException`. | Nothing to trigger because `student` is already a fully initialized object by the time the DTO is built.        |
| Query count for 1 profile                         | 2 (1 for `Profile`, 1 for `Student`)                                                                                                                                                                                                                  | 1                                                                                                               |
| Query count for N profiles in a loop/list context | 1 + N                                                                                                                                                                                                                                                 | 1 (or 1 per page, if paginated)                                                                                 |

The unoptimized version doesn't fail and that's the trap. It returns the correct data, every time, in dev with a handful
of rows. The cost only shows up as query count grows linearly with result size, which is usually invisible until either
the
database starts throttling under load or someone's actually watching the SQL log.

---

### The Evidence (Query Log)

> [!NOTE]
> Reproduce this yourself, see steps below and fill in the actual numbers here. Placeholder values shown for format
> reference only.

**Setup:**

- add `quarkus.hibernate-orm.log.sql=true` to [application.properties](src/main/resources/application.properties) if the
  property not exists
- single `Profile` row with its `Student` relation populated.

| Endpoint                              | Queries executed |
|---------------------------------------|------------------|
| `GET /profile/full/1/demo/n+1problem` | 2 queries        |
| `GET /profile/full/1`                 | 1 query          |

#### The Unoptimized API test result

Call the API using swagger
![](docs/attachment/example-demo-n+1-problem_swagger-hit-to-api.png)

The log shown two SQL query from hibernate to database, 2 query call
![](docs/attachment/example-demo-n+1-problem_log.png)

```
[Hibernate] 
    select
        p1_0.student_id,
        p1_0.academic_level 
    from
        profiles p1_0 
    where
        p1_0.student_id=?

${timestamp} TRACE [org.hibernate.orm.jdbc.bind] (executor-thread-1) binding parameter (1:BIGINT) <- [1]
[Hibernate] 
    select
        s1_0.id,
        s1_0.name 
    from
        students s1_0 
    where
        s1_0.id=?

${timestamp} TRACE [org.hibernate.orm.jdbc.bind] (executor-thread-1) binding parameter (1:BIGINT) <- [1]



```

#### The Optimized API test result

Call the API using swagger
![](docs/attachment/example-demo-avoid-n+1_swagger-hit-to-api.png)

The log shown two SQL query from hibernate to database, 1 query call
![](docs/attachment/example-demo-avoid-n+1_log.png)

```
[Hibernate] 
    select
        p1_0.student_id,
        p1_0.academic_level,
        s1_0.id,
        s1_0.name 
    from
        profiles p1_0 
    left join
        students s1_0 
            on s1_0.id=p1_0.student_id 
    where
        p1_0.student_id=? 
    fetch
        first ? rows only


```

The gap above is per single record. It's worth re-running this against a list endpoint (N profiles, not just one) to see
the linear-vs-constant query growth directly, and that's where N+1 actually hurts in production, not on a single-record
GET.

---

### Reproduce This Yourself

1. Enable SQL logging in `application.properties`:
   ```properties
   quarkus.hibernate-orm.log.sql=true
   ```
   (it is already enabled in this repository if you haven't changed it)
2. Run the app: `./mvnw quarkus:dev`
3. Seed at least one `Profile` with a non-null `student` relation.
4. Hit both endpoints (via Swagger's "Try it out", or `curl`) and watch the console output between each call.
5. Count the `select` statements attributed to each request.

---

### Takeaway

This isn't an argument against `PanacheEntityResource` or against lazy loading, i am just sharing something i found out,
that **the moment your endpoint serializes a relation, you've made a fetching decision, whether you meant to or not.**
In a database wrapper service (the scope this template targets), where endpoints are often hit at high frequency by
other internal services, that decision compounds fast.

The fix demonstrated here: a custom finder with `JOIN FETCH` is deliberately small in scope. It doesn't require
abandoning Panache's Active Record style, just being explicit at the one point where it matters (relation fetching),
instead of trusting the default.

← [Back to main README](../README.md)
