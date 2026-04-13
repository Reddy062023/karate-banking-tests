Feature: Shared Authentication

  Scenario: Get JWT Token
    Given url wireMockUrl + '/auth/token'
    And request { username: 'testuser', password: 'testpass' }
    When method POST
    Then status 200
    And def authToken = response.token