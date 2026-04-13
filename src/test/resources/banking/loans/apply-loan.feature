Feature: Complete personal loan application flow

  Background:
    * url wireMockUrl
    * def accountId = 'ACC-10042'

  @smoke @regression @loan
  Scenario: Apply for personal loan - full happy path (5 steps)

    # STEP 1: Verify account is active
    Given path '/accounts/' + accountId
    When method GET
    Then status 200
    And match response.status == 'ACTIVE'
    * def currentBalance = response.balance
    * print '>>> Step 1 PASS: Account active, Balance =', currentBalance

    # STEP 2: Check loan eligibility
    Given path '/loans/eligibility'
    When method GET
    Then status 200
    And match response.interestRate == '#number'
    * def rate = response.interestRate
    * print '>>> Step 2 PASS: Eligible, rate =', rate

    # STEP 3: Submit loan application
    Given path '/loans/apply'
    And request
    """
    {
      "accountId": "#(accountId)",
      "loanType": "PERSONAL",
      "amount": 10000,
      "termMonths": 24,
      "purpose": "Home renovation"
    }
    """
    When method POST
    Then status 201
    And match response.loanId == '#notnull'
    And match response.status == 'APPROVED'
    And match response.amount == 10000
    * def loanId = response.loanId
    * print '>>> Step 3 PASS: loanId =', loanId

    # STEP 4: Verify loan status is APPROVED
    Given path '/loans/' + loanId
    When method GET
    Then status 200
    And match response.status == 'APPROVED'
    * print '>>> Step 4 PASS: Loan status =', response.status

    # STEP 5: Verify account balance
    Given path '/accounts/' + accountId
    When method GET
    Then status 200
    And match response.balance == '#number'
    * print '>>> Step 5 PASS: Final balance =', response.balance