# quarkus-panache-swagger-blueprint

A minimalist Quarkus template covering two things at once: clean, optimized Jakarta JPA/Hibernate entity relations (1:1,
1:M, M:M), and a reusable pattern for Quarkus REST Data Panache's auto-generated CRUD that's actually usable, ready to extend, and not just a disposable demo.

## 💡 Why Does This Repository Exist?

Most boilerplate projects out there fall into two extremes: they are either too complex (overloaded with multi-layer
architectures, DTO mappings, and service patterns) or too trivial (simplified apps that sometimes implement database
design that is not the best practices). This often obscures the core mechanics of JPA.

Built entirely using Quarkus Panache (Active Record pattern), this project keeps the code down to just a few core files
so you can focus 100% on database efficiency, clean mappings, and seeing the direct contrast between Bad Practices and
Best Practices.

This repository is an intended `template` for a microservice that act like a `"database wrapper"` which owns a few table
and just need other services read or write to them without complete business logic.

While Quarkus REST Data Panache Extension provides automatically generated CRUD for Developer velocity with
`PanacheEntityResource`, BUT, it is a deliberate trade-off, it sacrifices strict architectural separation to give you
instant zero-code HTTP endpoints. While it give you most CRUD API's automatically, most of the time not all of the API
works when tested, so you ends up customizing more of it or rewrite the code entirely.

Moreover, one of the most common issues when pairing JPA relationships with `PanacheEntityResource` is that
**lazy-loaded relations don't stay lazy**. Because the entity is serialized straight to JSON outside of your control, a
relation marked `FetchType.LAZY` can still get silently triggered (causing N+1 queries) or blow up with a
`LazyInitializationException`, depending on whether the Hibernate session is still open at serialization time.

This repo's entities, are written with that trade-off in mind. Each relation field has a javadoc comment explaining why
it's mapped the way it is. Don't want to take this on faith? I provide two demo endpoints in this repo with the same
response shape, same DTO, but one quietly fires an extra query per request and one doesn't.

[See the side-by-side demo with query counts →](docs/n1-query-demo.md)

To sum up, this boilerplate projects fall into the in-between, it leverage the rapid prototyping of Quarkus REST Data
Panache, while also sorta minimize the drawback from using it. It gives you examples how to utilise the leverage of
using the automated CRUD, while also avoid bad practices.

[To run this project, please see our guide →](docs/README-quarkus.md)

---

## 🎯 What You'll Learn Here

This repo covers two layers that are easy to get wrong independently, and worse when combined:

1. **The data layer, JPA relations done right.** How `1:1`, `1:M`, and `M:M` relations should actually be mapped in JPA,
   using a concrete before/after schema as proof, not just theory. → details in the next section.
2. **The API layer, making auto-generated CRUD reusable.** How to recognize where Quarkus REST Data Panache's
   `PanacheEntityResource` breaks down for relations like shared primary keys, and how to fix just those specific
   cases instead of abandoning the auto-generated CRUD entirely. → see the step-by-step below.

The two layers are connected on purpose: badly-mapped JPA relations are exactly what makes auto-generated CRUD endpoints
misbehave (silent N+1s, broken serialization, lazy exceptions). Get the data layer right first, and the CRUD layer on
top of it stays boring, in a good way.

---

## 🗂️ The Schema Behind the Examples

Every entity, relation, and javadoc in this repo is built on top of one concrete domain: a `Student` who has one
`Profile` and can enroll in many `Courses`. It's small enough to hold in your head, but it touches all three relation
types you'll actually run into, 1:1, 1:M, and M:M.

What makes it worth reading isn't the domain itself, it's the **before/after**. The pre-normalization version of this schema looks reasonable on a whiteboard, but breaks down the moment you try to map it with JPA, redundant indexes, a many-to-many
that can't physically exist as drawn, no clear owner for the relation. The normalized version fixes each of those with a
specific JPA technique (`@MapsId` for shared primary keys, a junction entity to deconstruct M:M into two 1:M's).

If you're about to copy a relation pattern from this template into your own entity, **read this first**, it explains
*why* the entities are shaped the way they are, not just *what* they are:

[Read the full use-case writeup →](docs/usecase.md)

---

## 🧩 Where This Template Fits

This template is **not** meant to be a general-purpose API starter. It is scoped specifically for one role inside a
microservice architecture: the **database wrapper service**.

A database wrapper service is a microservice that:

- Owns a small, well-defined set of tables (usually a single bounded context or aggregate).
- Exposes that data over REST so other internal services can read/write it.
- Does **not** carry significant business logic, orchestration, or cross-service workflows, that belongs to the services
  consuming this one.

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  Service A       │      │  Service B       │      │  Service C       │
│  (business logic)│      │  (business logic)│      │  (business logic)│
└────────┬─────────┘      └────────┬─────────┘      └────────┬─────────┘
         │                         │                         │
         └────────────┬────────────┴────────────┬────────────┘
                       ▼                         ▼
              ┌──────────────────────────────────────┐
              │   This repo: database wrapper service  │
              │   (REST Data Panache + Swagger)         │
              └──────────────────┬───────────────────┘
                                  ▼
                            ┌───────────┐
                            │  Database │
                            └───────────┘
```

Because the scope is intentionally narrow (own a few tables, expose CRUD, nothing fancier), this is exactly the
situation where `PanacheEntityResource`'s trade-off pays off the most, and where this template's conventions keep the
generated CRUD reusable instead of disposable.

> [!NOTE]
> "No significant business logic or orchestration" doesn't mean no logic at all. Handling constraints that come from the
> schema this service owns, a shared primary key needing its parent created first, a foreign key blocking a delete until
> dependents are cleared, is still this service's job, since nobody else owns that schema. That's why endpoints like
> addAggregate/deleteAggregate exist here: they're not business rules, they're the mechanics of keeping the data this
> service is responsible for in a valid state.
> What's out of scope is deciding when or why something gets deleted, or chaining that decision across unrelated domains
> that belongs to whichever service actually owns the business reason for the action.

> [!IMPORTANT]
> If your distributed system is growing to the point where you are actively worrying about API versioning and backward
> compatibility, **you have officially outgrown `PanacheEntityResource`.**
> The moment you need versioning, it is time to move away from the automatic CRUD generation, write standard Jakarta
> REST endpoints, and introduce Java records as DTOs. This gives you the decoupling you need to change your database
> whenever you want without breaking the other apps in your system.

> [!NOTE]
> This template does not include an exception handler or exception mapper. That's intentional, because error handling
> conventions (response shape, error codes, logging strategy) are typically standardized at the company or team level,
> not something a reusable template should prescribe. What you'll find instead is honest documentation of where raw
> errors surface (FK violations, `getReference()` failures, etc.) and what they look like, so you know exactly where
> to wire in your own exception handling when you adopt this template.

---

## 📁 Template Structure

This repo follows a per-layer package structure. Use it as the skeleton, wire in your own package name and entities on
top of it.

```
src/main/java/<your.package>/
├── dto/
│   └── YourDto.java              # Custom Dto class
├── entity/
│   └── YourEntity.java           # Panache entity (table mapping + relations)
├── resource/
│   └── YourEntityResource.java   # Custom Resource interface extending PanacheEntityResource
└── repository/
    └── (not used in this template, because the focus is on the Active Record pattern;
         add this back if you need custom queries beyond what Active Record offers)
```

> The exact package names are up to you, what matters is keeping **entity**, **resource**, and **dto** in
> separate, predictable locations, so that adding a new entity always follows the same pattern.

---

## 🛠️ Adding a New Entity (Step-by-Step)

This is the core promise of this template: adding a new CRUD resource should mostly be a matter of **copy, rename, adjust**, not rewriting from scratch.

### Step 1, Copy the reference entity

Duplicate an existing entity that's structurally closest to what you need (see [the use-case schema](docs/usecase.md)
for what each entity in this repo looks like, `Course` is the simplest starting point if you don't need any relations yet) into a new file under `entity/`.

```bash
cp src/main/java/<pkg>/entity/Course.java src/main/java/<pkg>/entity/Product.java
```

### Step 2, Rename the class and table mapping

In the new file, rename the class and update the `@Table` / `@Entity` mapping to match your new domain object. for example

```java

@Entity
@Table(name = "products")
public class Product extends PanacheEntity {
    public String name;
    public BigDecimal price;
    // add fields + relations as needed (see entity examples for 1:1, 1:M, M:M)
}
```

### Step 3, Copy and rename the Resource interface

Duplicate the matching Resource interface, and point it at the new entity.

```bash
cp src/main/java/<pkg>/resource/CourseResource.java src/main/java/<pkg>/resource/ProductResource.java
```

```java

@Path("/products")
public interface ProductResource extends PanacheEntityResource<Product, Long> {
    // exclude/override methods here if a field shouldn't be exposed,
    // or if pagination/sorting defaults need to change
}
```

### Step 4, Adjust what shouldn't be auto-exposed

This is the step most tutorials skip, and the one that actually determines whether your generated CRUD survives contact
with real usage. Start with `ProfileResource` and `Profile`, they have the most thoroughly javadoc'd examples in this
repo, covering:

- Excluding a method (e.g. disabling `DELETE` on a shared-primary-key entity).
- Adding `@Valid` / Bean Validation annotations on the entity fields (see `Student.name` for a worked example, and note
  it needs the `quarkus-hibernate-validator` dependency to actually be enforced, because the annotations are silently
  inert without it).
- Adding OpenAPI annotations (`@Schema`, `@Operation`) so Swagger reflects the real contract, not just the generated
  default.

This template already hides some of the API that normally shouldn't be exposed in a database wrapper microservice, or
that was generated but can't be used correctly due to the `PanacheEntityResource` limitations covered in the sections
above.

### Step 5, Run and verify in Swagger

```bash
./mvnw quarkus:dev
```

Open `http://localhost:8080/q/swagger-ui` and confirm:

- [ ] The new entity's endpoints appear under their own tag.
- [ ] Request/response schemas match what you defined (not raw entity dumps).
- [ ] CRUD operations actually work end-to-end via "Try it out", not just appear in the docs.

That last checkbox is the whole point of this template: **generated ≠ working**, and this checklist exists so "ready to re-use" actually means ready.

> [!NOTE]
> **Swagger here is for testing, not full error documentation.** The auto-generated request bodies are reliable as-is,
> for most entities, you can hit "Try it out" and just fill in values, no extra setup needed. What it does **not** give
> you is a documented error response object: Quarkus's default error payloads from `PanacheEntityResource` aren't
> reflected in the OpenAPI schema, so things like validation failures or constraint violations won't show up as a typed
> response in Swagger. You'll still see the actual error when you call the endpoint, it's just not documented there. If
> you need that documented for consumers, you're back to writing a custom endpoint + exception mapper with
`@APIResponse`
> annotations.

---

## 📚 Further Reading

- [Tips, tricks & the reasoning behind them](docs/cheatsheet.md), every design decision in this repo explained with "why"
- [The schema behind the examples](docs/usecase.md), full before/after normalization writeup
- [The N+1 problem, demonstrated](docs/n1-query-demo.md), two real endpoints, side by side, with query counts
- [How to run this project](docs/README-quarkus.md)