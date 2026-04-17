Feature: Data-Driven Transfer Tests from CSV

  Background:
    * url baseUrl

  @regression @data-driven
  Scenario Outline: Transfer <expectedStatus> for amount <amount>
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "<fromAccountId>",
      "toAccountId":   "<toAccountId>",
      "amount":        <amount>,
      "currency":      "<currency>"
    }
    """
    When method POST
    Then status <expectedStatus>
    * if ('<expectedError>' != '') karate.match(response.errorCode, '<expectedError>')
    * print '>>> Transfer result:', responseStatus, 'amount:', <amount>

    Examples:
    | read('testdata/transfers.csv') |