Feature: Cash Management API Tests

  Background:
    * url baseUrl

  @smoke @regression
  Scenario: Cash Inbound - successful cash sale
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
    And match response.storeId == 'STORE-001'
    And match response.currency == 'GBP'
    * print '>>> Cash Inbound created:', response.transactionId

  @smoke @regression
  Scenario: Cash Outbound - successful refund
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
    And match response.type == 'OUTBOUND'
    And match response.status == 'PROCESSED'
    And match response.amount == 50.00
    * print '>>> Cash Outbound created:', response.transactionId

  @regression
  Scenario: Cash Loan - store needs extra float
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
    And match response.type == 'LOAN'
    And match response.reason == 'CASH_LOAN'
    And match response.status == 'PROCESSED'
    * print '>>> Cash Loan created:', response.transactionId

  @regression
  Scenario: Cash Pickup - end of day collection
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
    And match response.type == 'PICKUP'
    And match response.reason == 'CASH_PICKUP'
    And match response.status == 'PROCESSED'
    * print '>>> Cash Pickup created:', response.transactionId

  @regression
  Scenario: Get cash transactions by store
    Given path '/api/cash/store/STORE-001'
    When method GET
    Then status 200
    And match response == '#array'
    And match response == '#[_ > 0]'
    * print '>>> Total transactions:', response.length