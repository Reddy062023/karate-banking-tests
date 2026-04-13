Feature: Create Account API

  Background:
    * url wireMockUrl

  @smoke @regression
  Scenario: Create new savings account successfully
    Given path '/accounts'
    And request
    """
    {
      "accountType": "SAVINGS",
      "currency": "USD",
      "owner": {
        "name": "Jane Doe",
        "email": "jane.doe@example.com"
      }
    }
    """
    When method POST
    Then status 201
    And match response.accountId == '#notnull'
    And match response.accountType == 'SAVINGS'
    And match response.balance == 0.00
    And match response.currency == 'USD'
    And match response.status == 'ACTIVE'
    And match response.owner.name == '#string'
    And match response.owner.email == '#string'
    And match response.createdAt == '#notnull'
    And match response == read('../../shared/schema/account-schema.json')
    * def newAccountId = response.accountId
    * print '>>> New account created:', newAccountId

  @regression
  Scenario: Create account with missing required fields returns 400
    Given path '/accounts/validate'
    And request
    """
    {
      "accountType": "SAVINGS",
      "currency": "USD"
    }
    """
    When method POST
    Then status 400

  @regression
  Scenario: Create account with invalid currency returns 400
    Given path '/accounts'
    And request
    """
    {
      "accountType": "SAVINGS",
      "currency": "INVALID",
      "owner": {
        "name": "Jane Doe",
        "email": "jane.doe@example.com"
      }
    }
    """
    When method POST
    Then status 400