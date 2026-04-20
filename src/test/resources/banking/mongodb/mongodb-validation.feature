Feature: MongoDB Kafka Event Validation
  # Section 5.5 - Kafka Message Validation in MongoDB
  # In Instore: validate KOLOG events stored in MongoDB

  Background:
    * def MongoHelper = Java.type('com.qalab.mongodb.MongoHelper')
    * def MONGO_URI  = 'mongodb://localhost:27017'
    * def DB         = 'banking_events'
    * def COLLECTION = 'order_events'

  @regression @mongodb
  Scenario: Validate ORDER_CREATED event stored in MongoDB
    * def event = MongoHelper.findByOrderId(MONGO_URI, DB, COLLECTION, 'ORD-1001')
    * print '>>> MongoDB event found:', event
    And match event.orderId    == 'ORD-1001'
    And match event.customerId == 'CUST-01'
    And match event.totalAmount == 1149.98
    And match event.eventType  == '#string'
    And match event.status     == '#string'
    * print '>>> ORDER event validated in MongoDB!'

  @regression @mongodb
  Scenario: Validate Kafka metadata stored correctly in MongoDB
    * def count = MongoHelper.validateKafkaMetadata(MONGO_URI, DB, COLLECTION, 'ORD-1001', 'order-events', 0, 1024)
    * print '>>> Kafka metadata match count:', count
    And assert count == 1
    * print '>>> Kafka metadata validated!'

  @regression @mongodb
  Scenario: Find events missing Kafka metadata
    * def missing = MongoHelper.findMissingKafka(MONGO_URI, DB, COLLECTION)
    * print '>>> Events missing Kafka metadata:', missing
    * def cnt = karate.sizeOf(missing)
    And assert cnt == 0
    * print '>>> All ORDER_CREATED events have Kafka metadata!'

  @regression @mongodb
  Scenario: Find DLQ events - failed transactions
    * def dlqEvents = MongoHelper.findDLQEvents(MONGO_URI, DB, COLLECTION)
    * print '>>> DLQ events:', dlqEvents
    * def cnt = karate.sizeOf(dlqEvents)
    And assert cnt > 0
    And match dlqEvents[0].orderId == 'ORD-FAIL-001'
    * print '>>> DLQ events found:', cnt, 'failed transactions'

  @regression @mongodb
  Scenario: Validate event audit trail for order
    * def trail = MongoHelper.getAuditTrail(MONGO_URI, DB, COLLECTION, 'ORD-1001')
    * print '>>> Audit trail:', trail
    * def cnt = karate.sizeOf(trail)
    And assert cnt >= 1
    * print '>>> Audit trail has', cnt, 'events for ORD-1001'

  @regression @mongodb
  Scenario: Count total events in MongoDB
    * def filter = {}
    * def total = MongoHelper.count(MONGO_URI, DB, COLLECTION, filter)
    * print '>>> Total events in MongoDB:', total
    And assert total == 4
    * print '>>> All 4 events stored correctly in MongoDB!'