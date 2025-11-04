Feature: Update Existing Todo
  As a user, I want to modify details of an existing todo task so that I can keep my todo list up to date.

  Background:
    Given the service is running
    And the following todos exist in the system:
      | id | title          | doneStatus | description |
      | 1  | scan paperwork | false      |             |
      | 2  | file paperwork | false      |             |

  # Normal Flow
  Scenario Outline: PUT Update a todo task's title and description
    When I send PUT request to "todos/<ID>" with title: "<title>" and description: "<description>"
    Then we get an HTTP response 200
    And the response should have a todo task with title: "<title>" and description: "<description>"

    Examples:
      | ID | title  | description               |
      | 1  | Todo 1 | New description of Todo 1 |
      | 2  | Todo 2 | New description of Todo 2 |

  # Alternate Flow
  Scenario Outline: POST Update a todo task's title and description
    When I send a POST request to "todos/<ID>" with title: "<title>" and description: "<description>"
    Then we get an HTTP response 200
    And the response should have a todo task with title: "<title>" and description: "<description>"

    Examples:
      | ID | title       | description                    |
      | 1  | Todo 1 POST | New description of Todo 1 POST |
      | 2  | Todo 2 POST | New description of Todo 2 POST |

  # Error Flow
  Scenario Outline: PUT Attempt to update a todo task with an invalid ID
    When I send PUT request to "todos/-1" with title: "<title>" and description: "<description>"
    Then we get an HTTP response 404
    And the response should display the error message "[Invalid GUID for -1 entity todo]"

    Examples:
      | title        | description                      |
      | Todo 1 Error | New description of Todo 1 Error  |
      | Todo 2 Error | New description of Todo 2 Error  |
