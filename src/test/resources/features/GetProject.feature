Feature: Retrieve a Specific Project
  As a user, I want to access a particular project by ID or title so that I can review its details.

  Background:
    Given the service is running
    And the following projects exist in the system:
      | id | title       | active |
      | 1  | Office Work | false  |

  # Normal Flow
  Scenario Outline: Retrieve a project by its ID
    When we send a GET request to "projects/<id>"
    Then the project response code should be 200
    And the response must include a project with ID "<id>" and title "<title>"

    Examples:
      | id | title       |
      | 1  | Office Work |

  # Alternate Flow
  Scenario Outline: Retrieve a project by its title
    When we send a GET request to "projects" with title parameter "<title>" applied
    Then the project response code should be 200
    And the response must include a project with ID "<id>" and title "<title>"

    Examples:
      | id | title       |
      | 1  | Office Work |

  # Error Flow
  Scenario Outline: Attempt to retrieve a project with an invalid ID
    When I send a GET request to project endpoint "projects/<invalid_id>"
    Then the project response code should be 404
    And the response must include the error message: "Could not find an instance with projects/<invalid_id>"

    Examples:
      | invalid_id |
      | 9999       |
      | abc123     |
