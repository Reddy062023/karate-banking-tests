Feature: Data-driven transfer tests from CSV file

  Background:
    * url wireMockUrl

  @regression @data-driven
  Scenario Outline: Transfer scenario: <expectedStatus> for amount <amount>
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "<fromAccountId>",
      "toAccountId": "<toAccountId>",
      "amount": <amount>,
      "currency": "<currency>"
    }
    """
    When method POST
    Then status <expectedStatus>

    Examples:
    | read('testdata/transfers.csv') |