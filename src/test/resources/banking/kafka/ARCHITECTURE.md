\# Section 2.9 - Real Order System Event-Driven Architecture



\## Complete Order Processing Flow (10 Steps)



Step 1:  Customer sends POST /api/v1/orders

Step 2:  Order Service validates, saves to Oracle DB, returns 201 + orderId

Step 3:  Order Service publishes ORDER\_CREATED to Kafka topic: order-events

Step 4:  Payment Service consumes ORDER\_CREATED, charges card

Step 5:  Payment Service publishes PAYMENT\_COMPLETED to: payment-events

Step 6:  Inventory Service consumes PAYMENT\_COMPLETED, reserves stock

Step 7:  Inventory publishes STOCK\_RESERVED to: inventory-events

Step 8:  Notification Service sends email/SMS confirmation

Step 9:  All services write events to MongoDB for event sourcing/audit

Step 10: Any failure - message published to order-events-dlq



QA validates: API response == Kafka event == Oracle DB == MongoDB document



\## Architecture Diagram



Customer

&#x20; |

&#x20; | POST /orders

&#x20; v

Order Service

&#x20; |-- validates input

&#x20; |-- saves to Oracle DB

&#x20; |-- returns 201 + orderId

&#x20; |

&#x20; | publishes ORDER\_CREATED

&#x20; v

Kafka: order-events

&#x20; |

&#x20; |-- Payment Service consumes ORDER\_CREATED

&#x20; |     |-- charges card

&#x20; |     |-- publishes PAYMENT\_COMPLETED to payment-events

&#x20; |

&#x20; |-- Inventory Service consumes PAYMENT\_COMPLETED

&#x20; |     |-- reserves stock

&#x20; |     |-- publishes STOCK\_RESERVED to inventory-events

&#x20; |

&#x20; |-- Notification Service consumes STOCK\_RESERVED

&#x20;       |-- sends email/SMS to customer



MongoDB: all events stored for audit and event sourcing



Any failure: message goes to order-events-dlq (Dead Letter Queue)





\## QA Validation Points



API Response     == Kafka Event    == Oracle DB    == MongoDB

\-----------------------------------------------------------------

status=201       ORDER\_CREATED     ORDER saved     Event stored

orderId=ORD-001  orderId=ORD-001   id=ORD-001      orderId=ORD-001

amount=250.00    amount=250.00     amount=250.00   amount=250.00





\## How This Maps to Instore Project



Document                    Instore Project

\------------------------------------------------------------

Customer to Order Service   Store Register to KOLOG

ORDER\_CREATED event         CASH\_SALE event

Payment Service             Payment Processor

PAYMENT\_COMPLETED           PAYMENT\_CONFIRMED

Inventory Service           Stock Management System

Notification Service        Receipt Printer or Email

MongoDB audit               TLOG audit trail

DLQ Dead Letter Queue       Error queue for failed txns





\## Topics in This Architecture



Topic                Producer           Consumer

\------------------------------------------------------------

order-events         Order Service      Payment Service

payment-events       Payment Service    Inventory Service

inventory-events     Inventory Service  Notification Service

order-events-dlq     Any Service        Support or Ops Team





\## What We Built vs Full Architecture



Built: Order Service (HTTP to Kafka producer)

Built: KafkaTestHelper (Karate validates Kafka events)

Built: JSON producer and consumer

Built: Avro producer with Schema Registry

Next:  E2E test connecting API to Kafka to validation





\## Interview Key Points



Q: Why Kafka over REST?

A: Async processing - no blocking

&#x20;  Services decoupled - can scale independently

&#x20;  Event replay - can reprocess failed orders

&#x20;  Audit trail - every event stored



Q: What is DLQ?

A: Dead Letter Queue

&#x20;  Failed messages go here

&#x20;  Support team investigates

&#x20;  Can replay after fix



Q: How do you test event-driven systems?

A: Send HTTP request

&#x20;  Verify Kafka event published using KafkaTestHelper

&#x20;  Verify DB updated using JDBC or MongoDB helper

&#x20;  Verify downstream events produced



Q: What is event sourcing?

A: Store every state change as an event

&#x20;  Can replay events to rebuild state

&#x20;  MongoDB stores full event history

&#x20;  Used in Instore for TLOG audit



Q: What is the difference between BACKWARD and FORWARD compatibility?

A: BACKWARD - New schema can read data written by OLD schema

&#x20;  FORWARD  - Old schema can read data written by NEW schema

&#x20;  FULL     - Both BACKWARD and FORWARD



Q: How does Schema Registry prevent breaking changes?

A: When producer sends new schema version

&#x20;  Registry checks compatibility rules

&#x20;  If breaking change detected - rejects the schema

&#x20;  Producer gets error - deployment blocked

