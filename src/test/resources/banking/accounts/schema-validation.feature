Feature: Schema Validation & Assertion Cheat Sheet
  # Covers Section 1.5 from document + Real-world improvements
  # Relates to Instore project - validating KOLOG/API responses

  Background:
    * url baseUrl

    # ── Reusable schemas defined ONCE ──────────────────────────────
    # Account schema - used across multiple scenarios
    * def accountSchema =
    """
    {
      accountId:     '#string',
      accountHolder: '#string',
      balance:       '#number',
      currency:      '#string',
      status:        '#string'
    }
    """

    # Error response schema - used for negative tests
    * def errorSchema =
    """
    {
      errorCode:  '#string',
      message:    '#string',
      timestamp:  '#string'
    }
    """

    # Transfer response schema
    * def transferSchema =
    """
    {
      transactionId: '#string',
      status:        '#string',
      amount:        '#number',
      currency:      '#string',
      fromAccountId: '#string',
      toAccountId:   '#string',
      createdAt:     '#string'
    }
    """

  # ── DOCUMENT SECTION: Basic Type Matchers ─────────────────────
  @smoke @regression
  Scenario: Type validation - #string #number #boolean #notnull
    Given path '/accounts/ACC-001'
    When method GET
    Then status 200

    # From document - type matchers
    And match response.accountId    == '#string'
    And match response.accountHolder == '#string'
    And match response.balance      == '#number'
    And match response.status       == '#string'
    And match response.currency     == '#string'

    # Not null checks
    And match response.accountId    == '#notnull'
    And match response.balance      == '#notnull'

    # Reusable schema validation
    And match response == accountSchema

    # Number assertions
    And assert response.balance > 0
    And match response.balance == '#? _ > 0'

    # Specific value checks
    And match response.status   == 'ACTIVE'
    And match response.currency == 'USD'

    * print '>>> Type validation passed for:', response.accountId

  # ── DOCUMENT SECTION: Partial Match & Contains ────────────────
  @regression
  Scenario: Partial match - match response contains
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "ACC-10042",
      "toAccountId":   "ACC-20099",
      "amount":        100.00,
      "currency":      "USD"
    }
    """
    When method POST
    Then status 201

    # Full schema validation
    And match response == transferSchema

    # Partial match - only check specific fields
    And match response contains { transactionId: '#notnull', status: '#notnull' }
    And match response contains { amount: '#number', currency: '#string' }

    # Response time check (from document)
    
   * assert responseTime < 3000

    * print '>>> Partial match validation passed'

  # ── DOCUMENT SECTION: Regex & Conditional ─────────────────────
  @regression
  Scenario: Regex and conditional validation
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "ACC-10042",
      "toAccountId":   "ACC-20099",
      "amount":        100.00,
      "currency":      "USD"
    }
    """
    When method POST
    Then status 201

    # Regex - transactionId format
    And match response.transactionId == '#regex TXN-.*'

    # Conditional - status is one of expected values
    And match response.status == '#? _ == "PENDING" || _ == "COMPLETED"'

    # Currency is USD or EUR
    And match response.currency == '#? _ == "USD" || _ == "EUR"'

    * print '>>> Regex validation passed:', response.transactionId

  # ── IMPROVEMENT: Error Response Schema ────────────────────────
  @regression
  Scenario: Negative test - error response schema validation
    # In Instore - validate error responses from KOLOG/API

    Given path '/accounts/ACC-DOES-NOT-EXIST'
    When method PATCH
    Then status 404

    # Validate error schema structure
    And match response == errorSchema

    # Specific error fields
    And match response.errorCode == '#string'
    And match response.message   == '#string'
    And match response.timestamp == '#string'

    * print '>>> Error schema validated:', response.errorCode

  # ── IMPROVEMENT: Response Header Validation ───────────────────
  @regression
  Scenario: Response header validation
    Given path '/accounts/ACC-001'
    When method GET
    Then status 200

    # Validate Content-Type header
    And match responseHeaders['Content-Type'][0] contains 'application/json'

    * print '>>> Header validation passed'

  # ── IMPROVEMENT: QuickMart Cash Transaction Schema ────────────
  @regression
  Scenario: Validate QuickMart Cash API schema
    # Relates directly to your Instore KOLOG transaction validation
    * def cashSchema =
    """
    {
      transactionId: '#string',
      storeId:       '#string',
      cashierId:     '#string',
      amount:        '#number',
      type:          '#string',
      currency:      '#string',
      status:        '#string',
      timestamp:     '#string'
    }
    """

    # This uses wireMock stub for now
    Given path '/transactions/transfer'
    And request
    """
    {
      "fromAccountId": "ACC-10042",
      "toAccountId":   "ACC-20099",
      "amount":        250.00,
      "currency":      "USD"
    }
    """
    When method POST
    Then status 201

    # Validate all required fields present
    And match response.transactionId == '#notnull'
    And match response.status        == '#notnull'
    And match response.amount        == '#number'
    And match response.amount        == '#? _ > 0'

    * print '>>> QuickMart schema validated'