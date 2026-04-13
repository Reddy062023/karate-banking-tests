Feature: Transfer Funds API

  Background:
    * url wireMockUrl
    * def fromAcc = 'ACC-10042'
    * def toAcc = 'ACC-20099'

  @smoke @regression
  Scenario: Successful transfer returns 201 with PENDING status
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "#(fromAcc)",
      "toAccountId": "#(toAcc)",
      "amount": 250.00,
      "currency": "USD",
      "description": "Rent payment April 2024"
    }
    """
    When method POST
    Then status 201
    And match response.transactionId == '#notnull'
    And match response.status == 'PENDING'
    And match response.amount == 250.00
    And match response.currency == 'USD'
    And match response.fromAccountId == fromAcc
    And match response.toAccountId == toAcc
    And match response.createdAt == '#notnull'
    And match response == read('../../shared/schema/transaction-schema.json')
    * def txnId = response.transactionId
    * print '>>> Transaction created:', txnId

  @regression
  Scenario: Insufficient funds returns 422
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "#(fromAcc)",
      "toAccountId": "#(toAcc)",
      "amount": 9999999.99,
      "currency": "USD"
    }
    """
    When method POST
    Then status 422

  @regression
  Scenario: Transfer to same account returns 400
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "#(fromAcc)",
      "toAccountId": "#(fromAcc)",
      "amount": 50.00,
      "currency": "USD"
    }
    """
    When method POST
    Then status 400

  @regression
  Scenario: Missing required fields returns 400
    Given path '/transactions/transfer/validate'
    And request
    """
    {
      "fromAccountId": null,
      "amount": -50.00
    }
    """
    When method POST
    Then status 400