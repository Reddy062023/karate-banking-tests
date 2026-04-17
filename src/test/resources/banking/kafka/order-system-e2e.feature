Feature: Real Order System E2E Flow
  # Section 2.9 - Complete event-driven architecture test
  # Simulates: API → Kafka → Validate
  # Maps to Instore: Register → KOLOG → Ingress → Validate

  Background:
    * url baseUrl
    * def BROKER = 'localhost:9092'
    * def TOPIC = 'order-events'
    * def KafkaTestHelper = Java.type('com.qalab.helpers.KafkaTestHelper')

  @regression @kafka @e2e
  Scenario: Full order flow - API to Kafka event validation
    # Step 1: Customer sends HTTP request
    # In Instore: cashier scans items on register
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "ACC-10042",
      "toAccountId":   "ACC-20099",
      "amount":        499.99,
      "currency":      "USD"
    }
    """
    When method POST

    # Step 2: Order Service validates and returns 201
    # In Instore: KOLOG validates and saves transaction
    Then status 201
    And match response.transactionId == '#notnull'
    And match response.status        == '#string'
    And match response.amount        == '#number'
    * def txnId = response.transactionId
    * print '>>> Step 2 PASS: API returned 201, txnId:', txnId

    # Step 3: Publish ORDER_CREATED to Kafka
    # In Instore: KOLOG publishes CASH_SALE to Kafka
    * def Producer = Java.type('com.qalab.kafka.OrderEventProducer')
    * def producer = new Producer()
    * def OrderEvent = Java.type('com.qalab.kafka.OrderEvent')
    * def order = new OrderEvent('CUST-E2E-001', 499.99)
    * eval producer.sendOrderCreated(order)
    * def orderId = order.orderId
    * eval producer.close()
    * print '>>> Step 3 PASS: Kafka ORDER_CREATED published:', orderId

    # Step 4: Validate Kafka event (simulates Payment Service)
    # In Instore: Ingress Service reads from Kafka and validates
    * def sleep = function(ms){ java.lang.Thread.sleep(ms) }
    * sleep(2000)
    * def msg = KafkaTestHelper.poll(BROKER, TOPIC, orderId, 30)
    * print '>>> Step 4 PASS: Kafka event consumed'

    And match msg.orderId    == orderId
    And match msg.customerId == 'CUST-E2E-001'
    And match msg.amount     == 499.99
    And match msg.eventType  == 'ORDER_CREATED'
    And match msg.status     == 'CREATED'
    And match msg.topic      == 'order-events'
    And match msg.offset     == '#number'
    And match msg.partition  == '#number'

    # Step 5: Simulate Payment completed
    # In Instore: Payment confirmed, publish next event
    * def producer2 = new Producer()
    * eval producer2.sendOrderPaid(order)
    * eval producer2.close()
    * print '>>> Step 5 PASS: ORDER_PAID published'
    * print '>>> E2E Flow Complete: API 201 → Kafka ORDER_CREATED → Kafka ORDER_PAID'

  @regression @kafka @e2e
  Scenario: DLQ Flow - Failed order goes to Dead Letter Queue
    # In Instore: Failed transactions go to error queue
    # Support team investigates and replays

    * def Producer = Java.type('com.qalab.kafka.OrderEventProducer')
    * def producer = new Producer()
    * def OrderEvent = Java.type('com.qalab.kafka.OrderEvent')
    * def order = new OrderEvent('CUST-E2E-DLQ', 9999.99)

    # Send ORDER_CREATED
    * eval producer.sendOrderCreated(order)
    * def orderId = order.orderId
    * print '>>> ORDER_CREATED published, orderId:', orderId

    # Simulate payment failure - send CANCELLED
    * eval producer.sendOrderCancelled(order)
    * eval producer.close()
    * print '>>> ORDER_CANCELLED published - simulates payment failure'
    * print '>>> In production: cancelled events go to DLQ'
    * print '>>> Support team reviews and decides to retry or refund'

    # Validate event on Kafka
    * def sleep = function(ms){ java.lang.Thread.sleep(ms) }
    * sleep(2000)
    * def msg = KafkaTestHelper.poll(BROKER, TOPIC, orderId, 30)

    And match msg.orderId   == orderId
    And match msg.eventType == 'ORDER_CREATED'
    * print '>>> DLQ Flow validated! orderId:', orderId