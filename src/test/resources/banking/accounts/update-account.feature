Feature: Update Account API

  Background:
    * url wireMockUrl
    * def accountId = 'ACC-10042'

  @smoke @regression
  Scenario: Freeze account successfully
    Given path '/accounts/' + accountId
    And request
    """
    {
      "status": "FROZEN"
    }
    """
    When method PATCH
    Then status 200
    And match response.accountId == '#notnull'
    And match response.status == 'FROZEN'
    And match response.accountType == '#string'
    And match response.balance == '#number'
    And match response.currency == '#string'
    And match response.owner.name == '#string'
    And match response.owner.email == '#string'
    And match response == read('../../shared/schema/account-schema.json')
    * print '>>> Account frozen:', accountId

  @regression
  Scenario: Update account email successfully
    Given path '/accounts/' + accountId
    And request
    """
    {
      "owner": {
        "email": "newemail@example.com"
      }
    }
    """
    When method PATCH
    Then status 200
    And match response.accountId == '#notnull'
    And match response.owner.email == '#string'

  @regression
  Scenario: Update non-existent account returns 404
    Given path '/accounts/ACC-DOES-NOT-EXIST'
    And request
    """
    {
      "status": "FROZEN"
    }
    """
    When method PATCH
    Then status 404