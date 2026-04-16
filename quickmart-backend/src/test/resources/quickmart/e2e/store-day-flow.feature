Feature: QuickMart Store Day End to End Flow

  Background:
    * url baseUrl

  @e2e @smoke
  Scenario: Complete store trading day flow
    # This simulates a complete trading day
    # exactly like your Instore project flow!

    # STEP 1: Open Business Day
    * print '>>> STEP 1: Opening Business Day'
    Given path '/api/business-day/create'
    And request
    """
    {
      "storeId": "STORE-E2E",
      "createdBy": "MANAGER-01",
      "openingFloat": 500.00
    }
    """
    When method POST
    Then status 201
    And match response.status == 'OPEN'
    * def businessDayId = response.businessDayId
    * print '>>> Business Day opened:', businessDayId

    # STEP 2: Open Till
    * print '>>> STEP 2: Opening Till'
    Given path '/api/till/open'
    And request
    """
    {
      "storeId": "STORE-E2E",
      "cashierId": "CASHIER-01",
      "openingBalance": 200.00
    }
    """
    When method POST
    Then status 201
    And match response.status == 'OPEN'
    * def tillId = response.tillId
    * print '>>> Till opened:', tillId

    # STEP 3: Cash Inbound (customer pays)
    * print '>>> STEP 3: Cash Inbound - customer payment'
    Given path '/api/cash/inbound'
    And request
    """
    {
      "storeId": "STORE-E2E",
      "cashierId": "CASHIER-01",
      "amount": 75.00,
      "reason": "CASH_SALE",
      "currency": "GBP"
    }
    """
    When method POST
    Then status 201
    And match response.status == 'PROCESSED'
    * print '>>> Cash sale processed:', response.transactionId

    # STEP 4: Cash Loan (till needs more float)
    * print '>>> STEP 4: Cash Loan - extra float needed'
    Given path '/api/cash/loan'
    And request
    """
    {
      "storeId": "STORE-E2E",
      "cashierId": "CASHIER-01",
      "amount": 100.00,
      "currency": "GBP"
    }
    """
    When method POST
    Then status 201
    And match response.type == 'LOAN'
    * print '>>> Cash loan processed:', response.transactionId

    # STEP 5: Cash Pickup (security collects cash)
    * print '>>> STEP 5: Cash Pickup - security collection'
    Given path '/api/cash/pickup'
    And request
    """
    {
      "storeId": "STORE-E2E",
      "cashierId": "CASHIER-01",
      "amount": 300.00,
      "currency": "GBP"
    }
    """
    When method POST
    Then status 201
    And match response.type == 'PICKUP'
    * print '>>> Cash pickup processed:', response.transactionId

    # STEP 6: Close Till
    * print '>>> STEP 6: Closing Till'
    Given path '/api/till/' + tillId + '/close'
    And request
    """
    {
      "closingBalance": 175.00
    }
    """
    When method POST
    Then status 200
    And match response.status == 'CLOSED'
    * print '>>> Till closed:', tillId

    # STEP 7: End Business Day
    * print '>>> STEP 7: Ending Business Day'
    Given path '/api/business-day/' + businessDayId + '/end'
    And request
    """
    {
      "closingFloat": 175.00,
      "totalSales": 75.00,
      "totalRefunds": 0.00,
      "closedBy": "MANAGER-01"
    }
    """
    When method POST
    Then status 200
    And match response.status == 'CLOSED'
    And match response.totalSales == 75.00
    * print '>>> Business Day closed:', businessDayId
    * print '>>> Complete store day flow PASSED!'