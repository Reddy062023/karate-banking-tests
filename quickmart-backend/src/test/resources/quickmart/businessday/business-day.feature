Feature: Business Day API Tests

  Background:
    * url baseUrl

  @smoke @regression
  Scenario: Create Business Day successfully
    * def uniqueStore = 'STORE-BD-' + java.lang.System.currentTimeMillis()
    Given path '/api/business-day/create'
    And request { "storeId": "#(uniqueStore)", "createdBy": "MANAGER-01", "openingFloat": 500.00 }
    When method POST
    Then status 201
    And match response.businessDayId == '#notnull'
    And match response.status == 'OPEN'
    And match response.openingFloat == 500.00
    And match response.tradingDate == '#notnull'
    * def businessDayId = response.businessDayId
    * print '>>> Business Day created:', businessDayId

  @smoke @regression
  Scenario: Create and End Business Day flow
    * def uniqueStore = 'STORE-BD-END-' + java.lang.System.currentTimeMillis()
    Given path '/api/business-day/create'
    And request { "storeId": "#(uniqueStore)", "createdBy": "MANAGER-01", "openingFloat": 500.00 }
    When method POST
    Then status 201
    And match response.status == 'OPEN'
    * def businessDayId = response.businessDayId
    * print '>>> Business Day opened:', businessDayId

    Given path '/api/business-day/' + businessDayId + '/end'
    And request { "closingFloat": 450.00, "totalSales": 1500.00, "totalRefunds": 50.00, "closedBy": "MANAGER-01" }
    When method POST
    Then status 200
    And match response.status == 'CLOSED'
    And match response.totalSales == 1500.00
    And match response.totalRefunds == 50.00
    * print '>>> Business Day closed:', businessDayId

  @regression
  Scenario: Get current Business Day for store
    * def uniqueStore = 'STORE-BD-GET-' + java.lang.System.currentTimeMillis()
    Given path '/api/business-day/create'
    And request { "storeId": "#(uniqueStore)", "createdBy": "MANAGER-02", "openingFloat": 300.00 }
    When method POST
    Then status 201
    * def businessDayId = response.businessDayId

    Given path '/api/business-day/store/' + uniqueStore + '/current'
    When method GET
    Then status 200
    And match response.status == 'OPEN'
    And match response.storeId == uniqueStore
    * print '>>> Current Business Day:', response.businessDayId