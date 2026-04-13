Feature: Get Account API

  Background:
    * url wireMockUrl
    * path '/accounts'

  @smoke
  Scenario: Get account by ID - success
    Given path '/ACC-001'
    When method GET
    Then status 200
    And match response.accountId == 'ACC-001'
    And match response.accountHolder == 'John Doe'
    And match response.balance == 5000.00
    And match response.status == 'ACTIVE'

  @smoke
  Scenario: Get account - verify schema
    Given path '/ACC-001'
    When method GET
    Then status 200
    And match response ==
    """
    {
      accountId: '#string',
      accountHolder: '#string',
      balance: '#number',
      currency: '#string',
      status: '#string'
    }
    """

  @regression
  Scenario: Get account - verify currency
    Given path '/ACC-001'
    When method GET
    Then status 200
    And match response.currency == 'USD'