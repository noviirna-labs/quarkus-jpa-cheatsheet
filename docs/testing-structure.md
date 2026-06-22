← [Back to README](../README.md)

## Testing Structure

```
src/test/java/<your.package>/
├── resource/
│ ├── panacheentity/
│ │ ├── RestPanacheTest.java # Base class for all Panache entity integration tests
│ │ ├── RestPanacheTestFactory.java # Factory interface, implement this per entity
│ │ ├── factory/
│ │ │ ├── CourseFactory.java # Implements RestPanacheTestFactory<Course>
│ │ │ ├── EnrollmentFactory.java # Implements RestPanacheTestFactory<Enrollment>
│ │ │ ├── ProfileFactory.java # Implements RestPanacheTestFactory<Profile>
│ │ │ └── StudentFactory.java # Implements RestPanacheTestFactory<Student>
│ │ └── integration/
│ │   ├── CourseIT.java # Integration tests for Course resource
│ │   ├── EnrollmentIT.java # Integration tests for Enrollment resource
│ │   ├── ProfileIT.java # Integration tests for Profile resource
│ │   └── StudentIT.java # Integration tests for Student resource
│ │  
│ └── custom/ # Your custom resources go here (not Panache auto-generated)
│   └── .gitkeep # Guidance notes for further extension
```

## Factory Pattern Setup

`RestPanacheTestFactory.java`  implement this for each entity:

```java
// RestPanacheTestFactory.java
public interface RestPanacheTestFactory<T> {
// ... Interface methods
}
```

Example implementation:

```java
// StudentFactory.java
@ApplicationScoped
public class StudentFactory implements RestPanacheTestFactory<Student> {
    // ... Override implementations
}
```

`RestPanacheTest.java` is the base class for all integration tests, it extends `BaseEntity` which inherit
`PanacheEntityBase` from panache library.

```java

@QuarkusTest
public abstract class RestPanacheTest<T extends BaseEntity> {
    // Shared test utilities (client setup, common assertions, etc.)
}
```

## Extending This Structure

### For PanacheResource Entity Tests

Follow the existing factory pattern: one factory per entity, and one `*IT.java` integration test class per resource. Only use `RestPanacheTest` as your base test class if your entities extend `BaseEntity`.

`BaseEntity` (a custom class in this project extended by both the base test class and all template entities) defines a custom deserialization behavior that excludes the id field from HTTP request payloads using `@JsonProperty(access = READ_ONLY`). 

If your entities do not extend `BaseEntity`, you must implement a factory pattern tailored to your entity design. Otherwise, you can simply reuse the provided test classes in this project.

### Adding Custom Resources (for testing the classes that do not extend PanacheEntityResource)

Before creating new test files, rename the `custom/` directory to match your business domain (e.g., `billing/`,
`product/`, `notification/`).
This keeps custom resources cleanly separated from the core Panache entity test suite.
This structure keeps your custom standard resources cleanly separated from the core PanacheEntity factory system.
(which is a custom class in this project, not from an external library)

Example:

```
resource/
├── panacheentity/ # Panache auto-generated resources
├── product/ # (renamed from custom/) Custom product service tests
└── billing/ # Another custom domain
```

← [Back to README](../README.md)