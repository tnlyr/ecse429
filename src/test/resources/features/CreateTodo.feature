Feature: Create New Todo Item
  As a user, I want to be able to add a new todo entry
  So that it appears in my todo list for future tracking

  Background:
    Given the todo service is up and running

  # Normal Flow
  Scenario Outline: Successfully create a todo with a title and description
    When I make a POST request to "todos" with the title "<title>" and description "<description>"
    Then the response status code should be 201
    And the response body should include a todo item with the title "<title>" and description "<description>"

    Examples:
      | title   | description           |
      | Todo 1  | Details for Todo 1    |
      | Todo 2  | Details for Todo 2    |

  # Alternate Flow
  Scenario Outline: Successfully create a todo with only a title
    When I make a POST request to "todos" with the title "<title>" and description "<description>"
    Then the response status code should be 201
    And the response body should include a todo item with the title "<title>" and description "<description>"

    Examples:
      | title   | description |
      | Todo 1  |             |
      | Todo 2  |             |

  # Error Flow
  Scenario Outline: Attempt to create a todo without providing a title
    When I make a POST request to "todos" with the title "<title>" and description "<description>"
    Then the response status code should be 400
    And the response body should contain the error message "[Validation Failed: title cannot be empty]"

    Examples:
      | title | description         |
      |       | Some description    |