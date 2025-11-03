Feature: Get All Todo Items
  As a user, I want to fetch all existing todo entries
  So that I can view their current details and statuses

  Background:
    Given the todo service is operational
    And the following todo records are present in the system:
      | id | title          | doneStatus | description |
      | 1  | scan paperwork | false      |              |
      | 2  | file paperwork | false      |              |

  # Normal Flow
  Scenario: Retrieve all todo items successfully
    When I make a GET request to "todos"
    Then the response status code should be 200
    And the response body should include a list of todos
    And the list should contain the following entries:
      | id | title          | doneStatus |
      | 1  | scan paperwork | false      |
      | 2  | file paperwork | false      |

  # Alternate Flow
  Scenario: Retrieve all incomplete todo items
    When I make a GET request to "todos" with the filter parameter "?doneStatus=false"
    Then the response status code should be 200
    And the response body should include a list of todos
    And the list should contain the following entries:
      | id | title          | doneStatus |
      | 1  | scan paperwork | false      |
      | 2  | file paperwork | false      |

  # Error Flow
  Scenario: Attempt to fetch todos from an invalid endpoint
    When I make a GET request to "todos/all"
    Then the response status code should be 404
    And the response body should include the error message "[No resource found for endpoint: todos/all]"
