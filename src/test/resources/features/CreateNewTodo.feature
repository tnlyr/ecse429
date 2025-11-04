Feature: Create New Todo
  As a user, I want to create a new todo task so that I can add it to my todo list, and save it in the system.

  Background:
    Given the service is running

  # Normal Flow
  Scenario Outline: Make a new Todo task using title and description successfully
    When I send a POST request to "todos" with title: "<title>" and description: "<description>"
    Then we get an HTTP response 201
    And the response should have a todo task with title: "<title>" and description: "<description>"

    Examples:
      | title  | description           |
      | Todo 1 | Description of Todo 1 |
      | Todo 2 | Description of Todo 2 |

  # Alternate Flow
  Scenario Outline: Create a new Todo task using title and empty description successfully
    When I send a POST request to "todos" with title: "<title>" and empty description
    Then we get an HTTP response 201
    And the response should have a todo task with title: "<title>" and empty description

    Examples:
      | title  |
      | Todo 1 |
      | Todo 2 |

  # Error Flow
  Scenario: Create a new Todo task with empty title
    When I send a POST request to "todos" with title: "" and description: "description"
    Then we get an HTTP response 400
    And the response should display the error message "[Failed Validation: title : can not be empty]"