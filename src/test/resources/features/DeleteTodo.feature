Feature: Delete Existing Todo Item
  As a user, I want the ability to remove a todo entry
  So that it no longer appears in my list of tasks

  Background:
    Given the todo service is active
    And the following todo items already exist:
      | id | title          | doneStatus | description |
      | 1  | scan paperwork | false      |              |
      | 2  | file paperwork | false      |              |

  # Normal Flow
  Scenario Outline: Successfully delete a todo item by its ID
    When I issue a DELETE request to "todos/<ID>"
    Then the response status code should be 200
    And the todo with ID "<ID>" should no longer be available in "todos/<ID>"

    Examples:
      | ID |
      | 1  |
      | 2  |

  # Alternate Flow
  Scenario Outline: Delete a todo item that was just created
    When I send a POST request to "todos" with title "<title>" and description "<description>"
    And I issue a DELETE request to "todos/<ID>"
    Then the response status code should be 200
    And the todo with ID "<ID>" should be successfully removed

    Examples:
      | ID | title           | description |
      | 1  | scan paperwork  |             |
      | 2  | file paperwork  |             |

  # Error Flow
  Scenario: Attempt to delete a todo using an invalid ID
    When I issue a DELETE request to "todos/-1"
    Then the response status code should be 404
    And the response body should include the error message "[No todo found with id: -1]"
