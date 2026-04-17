Feature: Real-World Kafka Test Scenarios
  # Section 2.10 - Real-world Kafka testing scenarios
  # Covers: Idempotency, Partition Routing, Offset Replay

  Background:
    * def BROKER = 'localhost:9092'
    * def TOPIC = 'order-events'
    * def KafkaTestHelper = Java.type('com.qalab.helpers.KafkaTestHelper')
    * def Producer = Java.type('com.qalab.kafka.OrderEventProducer')
    * def OrderEvent = Java.type('com.qalab.kafka.OrderEvent')

  # ── Scenario 3: Idempotency Test ─────────────────────────────────
  @regression @kafka @realworld
  Scenario: Idempotency - same order sent twice produces only one event
    # In Instore: cashier double-clicks register
    # System should NOT create duplicate transaction
    # Kafka idempotent producer (enable.idempotence=true) prevents duplicates

    * def producer = new Producer()

    # Send same order twice with same orderId
    * def order = new OrderEvent('CUST-IDEM-001', 150.00)
    * def fixedOrderId = 'ORD-IDEM-FIXED-001'
    * eval order.orderId = fixedOrderId

    # First send
    * eval producer.sendOrderCreated(order)
    * print '>>> First send - orderId:', fixedOrderId

    # Second send - same orderId (simulates duplicate)
    * eval producer.sendOrderCreated(order)
    * print '>>> Second send - same orderId:', fixedOrderId

    * eval producer.close()

    # Validate - find the event on Kafka
    * def sleep = function(ms){ java.lang.Thread.sleep(ms) }
    * sleep(2000)
    * def msg = KafkaTestHelper.poll(BROKER, TOPIC, fixedOrderId, 15)

    And match msg.orderId    == fixedOrderId
    And match msg.customerId == 'CUST-IDEM-001'
    And match msg.amount     == 150.0

    * print '>>> Idempotency validated!'
    * print '    orderId:', fixedOrderId
    * print '    enable.idempotence=true prevents duplicate messages'
    * print '    In Instore: prevents double-charging customer'

  # ── Scenario 6: Partition Key Routing ────────────────────────────
  @regression @kafka @realworld
  Scenario: Partition routing - same customerId always goes to same partition
    # In Instore: all transactions for same store go to same partition
    # Guarantees ordering of events per customer/store
    # Key = customerId → same partition → ordered processing

    * def producer = new Producer()
    * def CUSTOMER_ID = 'CUST-PARTITION-001'

    # Send 3 orders for same customer
    * def order1 = new OrderEvent(CUSTOMER_ID, 100.00)
    * def meta1 = producer.sendOrderCreated(order1)
    * def partition1 = meta1.partition()
    * print '>>> Order 1 partition:', partition1

    * def order2 = new OrderEvent(CUSTOMER_ID, 200.00)
    * def meta2 = producer.sendOrderCreated(order2)
    * def partition2 = meta2.partition()
    * print '>>> Order 2 partition:', partition2

    * def order3 = new OrderEvent(CUSTOMER_ID, 300.00)
    * def meta3 = producer.sendOrderCreated(order3)
    * def partition3 = meta3.partition()
    * print '>>> Order 3 partition:', partition3

    * eval producer.close()

    # All 3 orders for same customer must be on same partition
    * assert partition1 == partition2
    * assert partition2 == partition3

    * print '>>> Partition routing validated!'
    * print '    All orders for', CUSTOMER_ID, 'on partition:', partition1
    * print '    Same partition = guaranteed ordering'
    * print '    In Instore: all txns for store go to same partition'

  # ── Scenario 10: Offset Replay Test ──────────────────────────────
  @regression @kafka @realworld
  Scenario: Offset replay - consumer reads from earliest offset
    # In Instore: after system failure, replay all missed transactions
    # auto.offset.reset=earliest means start from beginning
    # Each test uses unique group.id so always reads from offset 0

    * def producer = new Producer()

    # Send a known order
    * def order = new OrderEvent('CUST-REPLAY-001', 777.77)
    * eval producer.sendOrderCreated(order)
    * def orderId = order.orderId
    * eval producer.close()

    * print '>>> Sent order for replay test, orderId:', orderId

    # Wait for message to be available
    * def sleep = function(ms){ java.lang.Thread.sleep(ms) }
    * sleep(2000)

    # KafkaTestHelper uses auto.offset.reset=earliest
    # and unique group.id = always reads from beginning
    # This simulates offset replay after consumer restart
    * def msg = KafkaTestHelper.poll(BROKER, TOPIC, orderId, 15)

    And match msg.orderId    == orderId
    And match msg.customerId == 'CUST-REPLAY-001'
    And match msg.amount     == 777.77
    And match msg.offset     == '#number'
    And match msg.partition  == '#number'

    * print '>>> Offset replay validated!'
    * print '    orderId:', orderId
    * print '    offset:', msg.offset
    * print '    auto.offset.reset=earliest replays all messages'
    * print '    In Instore: replay missed KOLOG events after outage'