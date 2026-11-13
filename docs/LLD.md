# Low-Level Design

## Order workflow

`reserve-inventory → charge-payment → create-shipment → send-confirmation`

The order reference is the workflow idempotency key. Repeated submissions return the existing workflow. Each side effect has a unique workflow key and is written transactionally, so an activity that is delivered again can safely detect its prior result.

## State machine

`PENDING → RUNNING → PENDING` between successful activities; the final success is `SUCCEEDED`. Failures move to `RETRY_WAIT` with `available_at`; after `max_attempts`, the workflow becomes `FAILED`. Eligible workflows can be cancelled or manually retried from step one. The manual retry event resets the step attempt counter; prior business effects remain idempotent. Terminal payment failure triggers inventory compensation and records a compensation event.

## Claim protocol

One SQL statement selects eligible work `FOR UPDATE SKIP LOCKED`, updates owner, lease and monotonically increasing fence, then returns the current step. Workers complete a step only when the workflow remains `RUNNING` with matching owner and fence. Lease recovery increments the fence, preventing stale execution from changing workflow state. A bounded Java executor caps in-process concurrency.

## Persistence

- `workflows`: workflow input, current state, idempotency key, attempts, lease, fencing token and timestamps.
- `workflow_steps`: immutable ordered workflow definition.
- `execution_history`: ordered transition and attempt events.
- `inventory_items`, `inventory_reservations`: available units and reversible reservations.
- `payments`: simulated captured payment records.
- `shipments`: simulated shipment labels and tracking references.
- `customer_notifications`: durable confirmation records (no real email is sent).

## Retry

Delay is `min(3600, baseSeconds * 2^(attempt-1))`. Attempt number is derived per step from failure history. `FAIL_ONCE` demonstrates recovery; `DECLINE` demonstrates terminal failure and stock release.
