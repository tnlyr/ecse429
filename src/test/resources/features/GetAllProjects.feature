Feature: Get All Projects
  As a user, I want to view all existing project entries so that I can review their current details.

  Background:
    Given the service is running
    And the following projects exist in the system:
      | id | title       | active |
      | 1  | Office Work | false  |

  # Normal Flow
  Scenario: Retrieve the full list of projects
    When we send a GET request to "projects"
    Then the project response code should be 200
    And the response must contain a non-empty list of projects
    And the list should include the following project details:
      | id | title       | active |
      | 1  | Office Work | false  |

  # Alternate Flow
  Scenario: Retrieve only inactive projects
    When I send a GET request to "projects" with filter "?active=false" applied
    Then the project response code should be 200
    And the response must contain a non-empty list of projects
    And the list should include the following project details:
      | id | title       | active |
      | 1  | Office Work | false  |

  # Error Flow
  Scenario: Try retrieving projects from an invalid endpoint
    When we send a GET request to "projects/all"
    Then the project response code should be 404
    And the response must include the error message: "Could not find an instance with projects/all"
