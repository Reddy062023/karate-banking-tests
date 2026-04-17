Feature: Oracle DB Validation
  # Section 4.4 - Kafka Data Validation Queries
  # In Instore: QA validates KOLOG transactions in Oracle

  Background:
    * def OracleHelper = Java.type('com.qalab.oracle.OracleHelper')
    * def DB_URL  = 'jdbc:oracle:thin:@localhost:1521:XE'
    * def DB_USER = 'qalab'
    * def DB_PASS = 'qalab123'

  @regression @oracle
  Scenario: Validate sample data loaded correctly
    * def custCount = OracleHelper.scalar(DB_URL, DB_USER, DB_PASS, 'SELECT COUNT(*) FROM customers')
    * def ordCount  = OracleHelper.scalar(DB_URL, DB_USER, DB_PASS, 'SELECT COUNT(*) FROM orders')
    * def prodCount = OracleHelper.scalar(DB_URL, DB_USER, DB_PASS, 'SELECT COUNT(*) FROM products')
    * def itemCount = OracleHelper.scalar(DB_URL, DB_USER, DB_PASS, 'SELECT COUNT(*) FROM order_items')
    * def payCount  = OracleHelper.scalar(DB_URL, DB_USER, DB_PASS, 'SELECT COUNT(*) FROM payments')
    * print '>>> Customers:', custCount
    * print '>>> Orders:', ordCount
    * print '>>> Products:', prodCount
    * print '>>> Order Items:', itemCount
    * print '>>> Payments:', payCount
    And assert custCount == 4
    And assert ordCount  == 4
    And assert prodCount == 4
    And assert itemCount == 4
    And assert payCount  == 2
    * print '>>> All table counts validated!'

  @regression @oracle
  Scenario: Validate DELIVERED order exists in Oracle
    * def exists = OracleHelper.orderExists(DB_URL, DB_USER, DB_PASS, 'ORD-1001')
    * def status = OracleHelper.getOrderStatus(DB_URL, DB_USER, DB_PASS, 'ORD-1001')
    And assert exists == true
    And match status == 'DELIVERED'
    * print '>>> ORD-1001 exists in Oracle with status:', status

  @regression @oracle
  Scenario: Find orders not published to Kafka
    * def unpublished = OracleHelper.findUnpublishedOrders(DB_URL, DB_USER, DB_PASS)
    * print '>>> Unpublished orders:', unpublished
    * def cnt = karate.sizeOf(unpublished)
    And assert cnt > 0
    And match unpublished[0].order_id == 'ORD-1003'
    And match unpublished[0].status   == 'CREATED'
    * print '>>> Found', cnt, 'order(s) not published to Kafka!'

  @regression @oracle
  Scenario: Validate no duplicate orders - idempotency check
    * def duplicates = OracleHelper.findDuplicateOrders(DB_URL, DB_USER, DB_PASS)
    * print '>>> Duplicate orders:', duplicates
    * def cnt = karate.sizeOf(duplicates)
    And assert cnt == 0
    * print '>>> No duplicate orders - idempotency working!'

  @regression @oracle
  Scenario: Validate order item totals match order total
    * def mismatches = OracleHelper.findAmountMismatches(DB_URL, DB_USER, DB_PASS)
    * print '>>> Amount mismatches:', mismatches
    * def cnt = karate.sizeOf(mismatches)
    And assert cnt == 0
    * print '>>> All order amounts match item totals!'

  @regression @oracle
  Scenario: Kafka partition load balance check
    * def sql = 'SELECT kafka_partition, COUNT(*) AS message_count FROM orders WHERE kafka_offset IS NOT NULL GROUP BY kafka_partition ORDER BY kafka_partition'
    * def partitions = OracleHelper.query(DB_URL, DB_USER, DB_PASS, sql)
    * print '>>> Kafka partition distribution:', partitions
    * def cnt = karate.sizeOf(partitions)
    And assert cnt > 0
    * print '>>> Partition check complete - partitions:', cnt