# Order Processing System
Event-driven Microservices architecture built with Kafka and Spring Boot. This project demonstrates a distributed Order Processing System using asynchronous messaging to decouple Order, Payment, Inventory, and Notification services. Services share a MySQL database with soft locking to prevent race conditions.

<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/1dc1d5a6-edc2-4220-aba6-1d589116c8c8" />

Flow evidence: this weekend 

## Services

**Order Service**

* Creates and cancels orders with soft locking for inventory (reserves stock on creation, releases on failure/cancel, finalizes on payment success)
* Publishes: `order-created`, `order-cancelled`, `order-success`
* Consumes: `payment-success`, `payment-failed`, `order-completed`

**Payment Service**

* Processes payment lifecycle with checkout creation and webhook callbacks
* Publishes: `payment-success`, `payment-failed`
* Consumes: `order-created`

**Inventory Service**

* Logs inventory reservation and finalization events
* Maintains stock history with idempotency checks
* Publishes: `order-completed`, `inventory-updated`
* Consumes: `order-created`, `payment-success`, `order-cancelled`

**PDF Service**

* Generates invoices for completed orders
* Publishes: (none)
* Consumes: `order-success`

**Notification Service**

* Sends notification emails for payment and order lifecycle events
* Publishes: (none)
* Consumes: `payment-success`, `payment-failed`, `order-completed`

## End-to-End Flow

1. **Create Order** (POST /orders)
   * Order Service validates order data and reserves inventory by incrementing `reservedQuantity`.
   * It publishes `order-created` with order details.
2. **OrderCreatedEvent**
   * Payment Service receives `order-created` and creates a payment entity in initial status.
   * It prepares a checkout URL and waits for webhook result.
   * Inventory Service receives `order-created` and logs the reservation in history without adjusting quantity.
3. **Checkout / Webhook**
   * When checkout succeeds, Payment Service publishes `payment-success`.
   * When checkout fails or is cancelled, Payment Service publishes `payment-failed`.
4. **PaymentSuccessEvent**
   * Order Service receives `payment-success`, finalizes the order, decrements stock and reserved quantity, and publishes `order-success`.
   * Inventory Service receives `payment-success`, logs the final inventory transition, and publishes `order-completed`.
   * Notification Service receives `payment-success` and sends a payment confirmation email.
5. **OrderSuccessEvent**
   * PDF Service receives `order-success` and generates the invoice document.
6. **OrderCompletedEvent**
   * Notification Service receives `order-completed` and sends the final order completion notification.
   * Order Service can also consume `order-completed` if the deployment is configured to treat inventory confirmation as the final completion signal.

## Tech Stack

* Java 17, Spring Boot 3.4.0
* Apache Kafka
* MySQL (shared database)
* Stripe (payment processing)
* Lombok

## Key Features

* Event-driven architecture with Kafka
* Optimistic locking (@Version) for concurrent updates
* Soft locking with reservedQuantity to prevent overselling
* Idempotent event processing (prevents duplicate updates)
* Shared database with coordinated stock management

## Future Improvements

* Outbox Pattern for Kafka reliability
* Dead Letter Queue for failed events
* Monitoring & alerting
* Saga pattern for distributed transactions
