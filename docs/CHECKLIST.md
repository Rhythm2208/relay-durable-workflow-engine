# Engineering Checklist

- [x] Java 21 / Spring Boot API and Thymeleaf operations dashboard
- [x] Concrete order fulfillment workflow with inventory, payment, shipment and notification records
- [x] PostgreSQL persistence, Flyway migrations and order idempotency
- [x] Atomic work claiming with `SKIP LOCKED`, expiring leases and fencing tokens
- [x] Bounded worker pool and exponential retry backoff
- [x] Retry-safe business effects and inventory compensation on terminal payment failure
- [x] OpenAPI/Swagger, Dockerfile, Compose and Java 21 GitHub Actions CI
- [x] Unit tests and PostgreSQL Testcontainers integration cases
- [ ] Authentication, authorization and tenant isolation
- [ ] Real payment, carrier and email provider adapters
- [ ] Transactional outbox for external side effects and durable cancellation semantics
- [ ] Metrics, traces, alerts, rate limits, retention and operator auditing
- [ ] Load, concurrency, chaos and recovery qualification
