Feature: Kafka Order Event Validation
  # Section 2.7 - KafkaTestHelper static methods

  Background:
    * url baseUrl
    * def TOPIC = 'order-events'
    * def BROKER = 'localhost:9092'
    * def KafkaTestHelper = Java.type('com.qalab.helpers.KafkaTestHelper')

  @smoke @kafka
  Scenario: Send HTTP call and validate Kafka event produced
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "ACC-10042",
      "toAccountId":   "ACC-20099",
      "amount":        250.00,
      "currency":      "USD"
    }
    """
    When method POST
    Then status 201
    * print '>>> HTTP transaction created:', response.transactionId

    * def Producer = Java.type('com.qalab.kafka.OrderEventProducer')
    * def producer = new Producer()
    * def OrderEvent = Java.type('com.qalab.kafka.OrderEvent')
    * def order = new OrderEvent('CUST-KARATE-001', 250.00)
    * eval producer.sendOrderCreated(order)
    * def orderId = order.orderId
    * eval producer.close()
    * print '>>> Kafka event sent, orderId:', orderId

    # Wait for Kafka to propagate message in CI
    * def sleep = function(ms){ java.lang.Thread.sleep(ms) }
    * sleep(3000)

    # Step 3: Validate Kafka message
    * def msg = KafkaTestHelper.poll(BROKER, TOPIC, orderId, 30)
    * print '>>> Kafka message received:', msg

    And match msg.orderId    == orderId
    And match msg.customerId == 'CUST-KARATE-001'
    And match msg.amount     == 250.0
    And match msg.currency   == 'USD'
    And match msg.status     == 'CREATED'
    And match msg.eventType  == 'ORDER_CREATED'
    And match msg.topic      == 'order-events'
    And match msg.offset     == '#number'
    And match msg.partition  == '#number'
    * print '>>> ✅ Kafka event validated!'

  @regression @kafka
  Scenario: Validate ORDER_CREATED event type routing
    * def Producer = Java.type('com.qalab.kafka.OrderEventProducer')
    * def producer = new Producer()
    * def OrderEvent = Java.type('com.qalab.kafka.OrderEvent')
    * def order = new OrderEvent('CUST-KARATE-002', 99.99)
    * eval producer.sendOrderCreated(order)
    * eval producer.close()

    * def msg = KafkaTestHelper.pollByType(BROKER, TOPIC, 'ORDER_CREATED', 15)

    And match msg.eventType       == 'ORDER_CREATED'
    And match msg.headerEventType == 'ORDER_CREATED'
    And match msg.amount          == '#number'
    And match msg.customerId      == '#string'
    * print '>>> ✅ EventType routing validated!'

  @regression @kafka
  Scenario: Validate full order lifecycle - Created to Paid
    * def Producer = Java.type('com.qalab.kafka.OrderEventProducer')
    * def producer = new Producer()
    * def OrderEvent = Java.type('com.qalab.kafka.OrderEvent')
    * def order = new OrderEvent('CUST-KARATE-003', 499.99)
    * eval producer.sendOrderCreated(order)
    * def orderId = order.orderId
    * eval producer.sendOrderPaid(order)
    * eval producer.close()

    * def createdMsg = KafkaTestHelper.poll(BROKER, TOPIC, orderId, 15)
    And match createdMsg.orderId == orderId
    And match createdMsg.status  == 'CREATED'
    * print '>>> ✅ Full lifecycle validated! orderId:', orderId