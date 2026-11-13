# Relay, explained simply

## What is Relay?

Relay is a small backend system that makes sure a multi-step job keeps moving, even if one step fails.

This version uses **online order fulfillment** as its example. When someone places an order, several things need to happen: stock must be reserved, payment must be recorded, a shipment must be prepared, and the customer must be notified. Relay keeps track of each step and remembers where it left off.

You can think of Relay as a careful coordinator. It gives each job to a worker, writes down what happened, and tries again when a temporary problem gets in the way.

## Why build it?

A simple program might start an order, do a few steps, and lose track if it stops partway through. That can leave an order unfinished or cause a step to happen twice when the program is restarted.

Relay saves the workflow in a database. When a worker restarts, Relay can find unfinished work and continue. It also records an activity history so someone can see what happened.

## A sample order, step by step

For an order of headphones, Relay:

1. Checks the available stock and reserves one pair.
2. Records the payment as captured.
3. Creates a shipment record and tracking reference.
4. Saves a customer confirmation record.

The dashboard shows the order, its current state, payment, stock reservation, shipment reference, and the history of workflow events.

## What happens when something goes wrong?

The dashboard lets you choose a payment behavior for the demo:

- **Approve normally:** the order completes without a simulated failure.
- **Timeout once, then recover:** the first payment attempt fails. Relay records the failure, waits, retries, and then completes the order.
- **Decline every attempt:** Relay retries a few times, marks the workflow as failed, and releases the reserved stock.

You can also retry a failed workflow. Relay starts again from the first step. The saved records help it avoid repeating effects that have already happened.

## What can I try?

Start the project with Docker Compose, then open:

- Dashboard: http://localhost:8082/
- API explorer: http://localhost:8082/swagger-ui.html

On the dashboard, submit a demo order and select **Timeout once, then recover**. Open the order to follow its progress and see the failed attempt and retry in the history.

The app uses PostgreSQL on port **5434**. It does not use ports 80 or 81.

## What is real and what is simulated?

Relay really stores workflow state, stock reservations, payment records, shipment records, customer confirmations, and execution history in PostgreSQL. Its retry and recovery behavior runs in the application.

The payment gateway and shipping provider are examples stored locally in the database. The customer confirmation is saved as a record; Relay does not send an actual email. The project does not connect to a real store, bank, carrier, or customer account.

## Why is this a useful portfolio project?

This project shows how to build software that coordinates work reliably. It brings together a web API, a database, background workers, retries, safe handling of repeated work, and a dashboard for seeing what happened.

In simple terms, it demonstrates that the developer can build a system that remembers work, handles failures, and makes the result visible to an operator.

## Main technologies

- **Java 21 and Spring Boot** run the application and its web API.
- **PostgreSQL** stores orders, workflow progress, and activity records.
- **Docker Compose** starts the app and its database together.
- **Thymeleaf** builds the dashboard pages.
- **Flyway** applies database changes when the app starts.

## Current limits

Relay is a learning and portfolio project, not a ready-to-use store or payment platform. It does not yet include sign-in, user accounts, real payment processing, real shipping integration, or email delivery. It also needs more security, monitoring, and stress testing before it could be used for important production work.
