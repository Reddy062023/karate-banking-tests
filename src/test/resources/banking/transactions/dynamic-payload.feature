Feature: Dynamic Payload Generation

  Background:
    * url baseUrl

  @regression
  Scenario: Generate unique transfer payload at runtime
    * def makePayload =
    """
    function() {
      var uuid = java.util.UUID.randomUUID().toString();
      var amount = Math.round(Math.random() * 1000 * 100) / 100;
      var ts = new Date().toISOString();
      return {
        fromAccountId: 'ACC-10042',
        toAccountId:   'ACC-20099',
        amount:        amount,
        currency:      'USD',
        description:   'Dynamic test - ' + ts,
        idempotencyKey: uuid
      };
    }
    """
    * def payload = call makePayload
    Given path '/transactions/transfer'
    And request payload
    When method POST
    Then status 201
    And match response.amount == '#notnull'
    And match response.transactionId == '#notnull'
    * print '>>> Created:', response.transactionId, 'amount:', payload.amount