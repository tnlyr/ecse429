Feature: Get a Specific Todo
  As a user, I want to retrieve a specific todo task so that I can view its details.

  Background:
    Given the service is running
    And the following todos exist in the system:
      | id | title          | doneStatus | description |
      | 1  | scan paperwork | false      |             |
      | 2  | file paperwork | false      |             |

  # Normal Flow
  Scenario Outline: Get a Todo task using its ID
    When I send a GET request to "todos/<id>"
    Then I should receive a response status code of 200
    And the response should contain a todo task with ID "<id>" and title "<title>"

    Examples:
      | id | title          |
      | 1  | scan paperwork |
      | 2  | file paperwork |

  # Alternate Flow
  Scenario Outline: Get a Todo task using its title
    When I send a GET request to "todos" with title parameter "<title>"
    Then I should receive a response status code of 200
    And the response should contain a todo task with ID "<id>" and title "<title>"

    Examples:
      | id | title           |
      | 1  | scan paperwork  |
      | 2  | file paperwork  |

  # Error Flow
  Scenario Outline: Get a Todo task with invalid ID
    When I send a GET request to "todos/<invalid_id>"
    Then I should receive a response status code of 404
    And the response should contain the error message "[Could not find an instance with todos/<invalid_id>]"

    Examples:
      | invalid_id |
      | 9999       |
      | abc123     |