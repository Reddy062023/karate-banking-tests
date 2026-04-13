Feature: Dynamic test data generated at runtime

  Background:
    * url wireMockUrl

  @regression
  Scenario: Generate unique transfer payload per test run
    * def makePayload =
    """
    function() {
      var uuid = java.util.UUID.randomUUID().toString();
      var amount = 100.00;
      var ts = new Date().toISOString();
      return {
        fromAccountId: 'ACC-10042',
        toAccountId: 'ACC-20099',
        amount: amount,
        currency: 'USD',
        description: 'Dynamic test - ' + ts,
        idempotencyKey: uuid
      };
    }
    """
    * def payload = call makePayload
    Given path '/transactions/transfer'
    And request payload
    When method POST
    Then status 201
    And match response.transactionId == '#notnull'
    And match response.status == 'PENDING'
    * print '>>> Created:', response.transactionId, 'amount:', payload.amount