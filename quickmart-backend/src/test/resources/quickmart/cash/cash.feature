Feature: Cash Management API Tests with MongoDB Validation

  Background:
    * url baseUrl

  @smoke @regression
  Scenario: Cash Inbound - verify saved in MongoDB
    Given path '/api/cash/inbound'
    And request
    """
    {
      "storeId": "STORE-001",
      "cashierId": "CASHIER-01",
      "amount": 100.00,
      "reason": "CASH_SALE",
      "currency": "GBP"
    }
    """
    When method POST
    Then status 201
    And match response.transactionId == '#notnull'
    And match response.type == 'INBOUND'
    And match response.status == 'PROCESSED'
    And match response.amount == 100.00
    * def transactionId = response.transactionId
    * print '>>> Created:', transactionId

    # MongoDB validation - fetch back and verify
    Given path '/api/cash/' + transactionId
    When method GET
    Then status 200
    And match response.transactionId == transactionId
    And match response.type == 'INBOUND'
    And match response.status == 'PROCESSED'
    And match response.amount == 100.00
    And match response.storeId == 'STORE-001'
    And match response.currency == 'GBP'
    And match response.timestamp == '#notnull'
    And match response.processedAt == '#notnull'
    * print '>>> MongoDB verified for:', transactionId

  @smoke @regression
  Scenario: Cash Outbound - verify saved in MongoDB
    Given path '/api/cash/outbound'
    And request
    """
    {
      "storeId": "STORE-001",
      "cashierId": "CASHIER-01",
      "amount": 50.00,
      "reason": "REFUND",
      "currency": "GBP"
    }
    """
    When method POST
    Then status 201
    And match response.transactionId == '#notnull'
    * def transactionId = response.transactionId

    # MongoDB validation
    Given path '/api/cash/' + transactionId
    When method GET
    Then status 200
    And match response.transactionId == transactionId
    And match response.type == 'OUTBOUND'
    And match response.reason == 'REFUND'
    And match response.amount == 50.00
    * print '>>> MongoDB verified for:', transactionId

  @regression
  Scenario: Cash Loan - verify saved in MongoDB
    Given path '/api/cash/loan'
    And request
    """
    {
      "storeId": "STORE-001",
      "cashierId": "CASHIER-01",
      "amount": 200.00,
      "currency": "GBP"
    }
    """
    When method POST
    Then status 201
    And match response.transactionId == '#notnull'
    * def transactionId = response.transactionId

    # MongoDB validation
    Given path '/api/cash/' + transactionId
    When method GET
    Then status 200
    And match response.transactionId == transactionId
    And match response.type == 'LOAN'
    And match response.reason == 'CASH_LOAN'
    * print '>>> MongoDB verified for:', transactionId

  @regression
  Scenario: Cash Pickup - verify saved in MongoDB
    Given path '/api/cash/pickup'
    And request
    """
    {
      "storeId": "STORE-001",
      "cashierId": "CASHIER-01",
      "amount": 500.00,
      "currency": "GBP"
    }
    """
    When method POST
    Then status 201
    And match response.transactionId == '#notnull'
    * def transactionId = response.transactionId

    # MongoDB validation
    Given path '/api/cash/' + transactionId
    When method GET
    Then status 200
    And match response.transactionId == transactionId
    And match response.type == 'PICKUP'
    And match response.reason == 'CASH_PICKUP'
    * print '>>> MongoDB verified for:', transactionId

  @regression
  Scenario: Get all transactions for store
    Given path '/api/cash/store/STORE-001'
    When method GET
    Then status 200
    And match response == '#array'
    And match response == '#[_ > 0]'
    * print '>>> Total transactions:', response.length

  @regression
  Scenario: Get transaction by ID - not found returns 404
    Given path '/api/cash/INVALID-TXN-999'
    When method GET
    Then status 404
    * print '>>> 404 verified for invalid transaction'