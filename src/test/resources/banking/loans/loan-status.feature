@ignore
Feature: Get loan status - helper feature called by apply-loan

  Background:
    * url wireMockUrl

  Scenario: Get loan status by ID
    Given path '/loans/' + loanId
    When method GET
    Then status 200
    * def status = response.status