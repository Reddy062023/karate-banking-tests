Feature: First Karate Test

  Scenario: Verify WireMock is running
    Given url 'http://localhost:8090/__admin/mappings'
    When method GET
    Then status 200
    And print 'WireMock is running!'