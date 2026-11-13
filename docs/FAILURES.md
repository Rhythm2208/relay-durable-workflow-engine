# Failure Scenarios

| Scenario | Expected behavior |
|---|---|
| Worker stops before an activity | Lease expires; another worker claims with a higher fencing token |
| Payment gateway times out once | Failure is recorded; retry backoff occurs; payment step succeeds on retry |
| Payment is declined on every attempt | Workflow reaches `FAILED`; inventory reservation is compensated and stock restored |
| Activity is delivered again after a crash | Workflow-keyed DB effect prevents a duplicate reservation, payment, shipment or confirmation record |
| Two workers poll at once | PostgreSQL `SKIP LOCKED` gives each worker a distinct row |
| Stale worker completes after reassignment | Owner/fence condition rejects the stale workflow update |
| Same order reference is submitted twice | Unique idempotency key returns the original workflow |
| Database is unavailable | No in-memory queue is treated as authoritative; workers resume from persisted rows after recovery |

External providers are not wired in. In a real integration, a crash after a remote side effect but before recording its result can still cause redelivery. Use provider idempotency keys, outbox/inbox patterns and downstream fencing support.
