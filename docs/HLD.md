# High-Level Design

Relay demonstrates durable orchestration through a concrete order fulfillment flow.

```mermaid
flowchart LR
 Customer --> Dashboard[Thymeleaf order operations]
 Dashboard --> API[Spring REST API]
 API --> W[(workflows + steps)]
 WorkerA[Bounded worker pool] -->|atomic SKIP LOCKED claim| W
 WorkerA --> Inventory[(inventory items + reservations)]
 WorkerA --> Payments[(payment records)]
 WorkerA --> Shipments[(shipment labels)]
 WorkerA --> Notifications[(notification outbox records)]
 WorkerA --> Events[(append-only execution history)]
 Dashboard --> Events
```

The API accepts an order with an idempotency key and creates a durable workflow and ordered activity definitions. PostgreSQL stores workflow state, lease owner/expiry and the fencing counter. Worker threads claim work through a single `FOR UPDATE SKIP LOCKED` statement. Domain activities write business effects in PostgreSQL transactions. Their primary keys are workflow IDs, which make redelivery safe. On terminal payment failure, a compensation releases reserved inventory. Flyway evolves the schema; Thymeleaf presents an operations view over live workflow and business state.

Durability applies to engine state and the included PostgreSQL effects. Payment and carrier behavior are simulations; customer notification is recorded rather than sent to an email provider.
