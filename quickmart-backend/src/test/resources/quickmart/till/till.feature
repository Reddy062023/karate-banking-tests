Feature: Till Management API Tests

  Background:
    * url baseUrl
    * def uniqueStore = 'STORE-TILL-' + java.lang.System.currentTimeMillis()

  @smoke @regression
  Scenario: Open Till successfully
    Given path '/api/till/open'
    And request { "storeId": "#(uniqueStore)", "cashierId": "CASHIER-01", "openingBalance": 200.00 }
    When method POST
    Then status 201
    And match response.tillId == '#notnull'
    And match response.status == 'OPEN'
    And match response.openingBalance == 200.00
    And match response.currentBalance == 200.00
    * def tillId = response.tillId
    * print '>>> Till opened:', tillId

  @smoke @regression
  Scenario: Open and Close Till flow
    * def storeId = 'STORE-TILL-CLOSE-' + java.lang.System.currentTimeMillis()
    Given path '/api/till/open'
    And request { "storeId": "#(storeId)", "cashierId": "CASHIER-01", "openingBalance": 300.00 }
    When method POST
    Then status 201
    And match response.status == 'OPEN'
    * def tillId = response.tillId
    * print '>>> Till opened:', tillId

    Given path '/api/till/' + tillId + '/close'
    And request { "closingBalance": 450.00 }
    When method POST
    Then status 200
    And match response.status == 'CLOSED'
    And match response.closingBalance == 450.00
    * print '>>> Till closed:', tillId

  @regression
  Scenario: Get Till Status
    * def storeId = 'STORE-TILL-STATUS-' + java.lang.System.currentTimeMillis()
    Given path '/api/till/open'
    And request { "storeId": "#(storeId)", "cashierId": "CASHIER-02", "openingBalance": 100.00 }
    When method POST
    Then status 201
    * def tillId = response.tillId

    Given path '/api/till/' + tillId + '/status'
    When method GET
    Then status 200
    And match response.status == 'OPEN'
    And match response.tillId == tillId
    * print '>>> Till status:', response.status