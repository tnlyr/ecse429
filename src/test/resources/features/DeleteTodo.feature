Feature: Delete Existing Todo
  As a user, I want to remove a todo so it no longer appears in the list

  Background:
    Given the service is running
    And the following todos exist in the system:
      | id | title          | doneStatus | description |
      | 1  | scan paperwork | false      |             |
      | 2  | file paperwork | false      |             |

  # Normal Flow
  Scenario Outline: Delete a Todo using its ID
    When I send DELETE request to "todos/<ID>"
    Then we get an HTTP response 200
    And the todo task located at "todos/<ID>" should be deleted

    Examples:
      | ID |
      | 1  |
      | 2  |

  # Alternate Flow
  Scenario Outline: Create a Todo then proceed to delete it
    When I send POST request to "todos" with title: "<title>" and description: "<description>" then delete the todo
    Then we get an HTTP response 200

    Examples:
      | title          | description            |
      | temp item 1    | cleanup me please      |
      | temp item 2    | remove after creating  |

  # Error Flow
  Scenario: Attempt to delete a Todo with an invalid ID
    When I send DELETE request to "todos/-1"
    Then we get an HTTP response 404
    And the response should display the error message "[Could not find any instances with todos/-1]"
