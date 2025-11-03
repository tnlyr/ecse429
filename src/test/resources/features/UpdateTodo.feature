Feature: Update Existing Todo Item
  As a user, I want to edit an existing todo entry
  So that I can update its title or description when needed

  Background:
    Given the todo service is active
    And the following todo items exist in the system:
      | id | title          | doneStatus | description |
      | 1  | scan paperwork | false      |              |
      | 2  | file paperwork | false      |              |

  # Normal Flow
  Scenario Outline: Successfully update a todo item’s title and description using PUT
    When I make a PUT request to "todos/<ID>" with title "<title>" and description "<description>"
    Then the response status code should be 200
    And the response should include a todo item with title "<title>" and description "<description>"

    Examples:
      | ID | title   | description               |
      | 1  | Todo 1  | Updated description 1     |
      | 2  | Todo 2  | Updated description 2     |

  # Alternate Flow
  Scenario Outline: Successfully update a todo item’s title and description using POST
    When I make a POST request to "todos/<ID>" with title "<title>" and description "<description>"
    Then the response status code should be 200
    And the response should include a todo item with title "<title>" and description "<description>"

    Examples:
      | ID | title        | description                   |
      | 1  | Todo 1 POST  | Updated description for POST 1 |
      | 2  | Todo 2 POST  | Updated description for POST 2 |

  # Error Flow
  Scenario Outline: Attempt to update a todo item with an invalid ID using PUT
    When I make a PUT request to "todos/-1" with title "<title>" and description "<description>"
    Then the response status code should be 404
    And the response should include the error message "[Invalid resource identifier: -1 for todo]"

    Examples:
      | title         | description                  |
      | Todo 1 Error  | Updated description Error 1   |
      | Todo 2 Error  | Updated description Error 2   |
