Feature: Retrieve a Specific Todo
  As a user, I want to get a specific todo task so that I can retrieve its details.

  Background:
    Given the service is running
    And the following todos exist in the system:
      | id | title          | doneStatus | description |
      | 1  | scan paperwork | false      |             |
      | 2  | file paperwork | false      |             |

  # Normal Flow
  Scenario Outline: Retrieve a Todo task using its ID
    When I send GET request to "todos/<id>"
    Then we get an HTTP response 200
    And the response should have a todo task ID "<id>" with title "<title>"

    Examples:
      | id | title          |
      | 1  | scan paperwork |
      | 2  | file paperwork |

  # Alternate Flow
  Scenario Outline: Retrieve a Todo task using its title
    When I send GET request to "todos" with title parameter "<title>"
    Then we get an HTTP response 200
    And the response should have a todo task ID "<id>" with title "<title>"

    Examples:
      | id | title           |
      | 1  | scan paperwork  |
      | 2  | file paperwork  |

  # Error Flow
  Scenario Outline: Attempt to retrieve a Todo task using an invalid ID
    When I send GET request to "todos/<invalid_id>"
    Then we get an HTTP response 404
    And the response should display the error message "[Could not find an instance with todos/<invalid_id>]"

    Examples:
      | invalid_id |
      | 9999       |
      | abc123     |
