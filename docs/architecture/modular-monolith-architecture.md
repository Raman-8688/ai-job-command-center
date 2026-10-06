# Modular Monolith Architecture

## 1. Modular Boundaries & Rules
A Modular Monolith provides the developer productivity and operational simplicity of a single deployment unit while preventing the structural chaos of a monolithic "Big Ball of Mud".

### Rule 1: Package-Private Internal Details
Each module is encapsulated within its own root package under `com.jobcommandcenter`:
- `com.jobcommandcenter.user`
- `com.jobcommandcenter.job`
- `com.jobcommandcenter.ai`
- `com.jobcommandcenter.resume`
- `com.jobcommandcenter.email`
- `com.jobcommandcenter.application`
- `com.jobcommandcenter.interview`
- `com.jobcommandcenter.automation`
- `com.jobcommandcenter.analytics`
- `com.jobcommandcenter.security`
- `com.jobcommandcenter.common`

Internal repository classes and entity persistence details are kept package-private or accessible only via designated public Service API contracts.

### Rule 2: No Direct Cross-Module Entity Associations
A JPA entity in the `job` module must not have a direct `@OneToMany` or `@ManyToOne` Hibernate relationship to an entity in the `resume` module. Cross-module references are maintained via foreign identifier values (e.g., `UUID resumeVersionId`), ensuring that no inadvertent cascading lazy-loading or cross-module database locking occurs.

### Rule 3: Event-Driven Module Communication
Cross-module side effects (e.g., when an incoming email in `email` triggers an application status change in `application`) are decoupled using Spring's in-memory `ApplicationEventPublisher`. Modules publish domain events:
```java
public record EmailClassifiedEvent(UUID emailId, EmailCategory category, UUID matchedApplicationId) {}
```
Listener modules handle events asynchronously (`@TransactionalEventListener`), preventing hard circular dependencies.
