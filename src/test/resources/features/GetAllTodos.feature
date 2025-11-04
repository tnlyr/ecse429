Feature: Get All Todos
  As a user, I want to fetch all todo tasks so that I can view my entire todo list.

  Background:
    Given the service is running
    And the following todos exist in the system:
      | id | title          | doneStatus | description |
      | 1  | scan paperwork | false      |             |
      | 2  | file paperwork | false      |             |

  # Normal Flow
  Scenario: Retrieve all Todo tasks successfully
    When I send GET request to "todos"
    Then we get an HTTP response 200
    And the response should contain a list of all todo tasks
    And the list should include the todo tasks with the following details:
      | id | title          | doneStatus |
      | 1  | scan paperwork | false      |
      | 2  | file paperwork | false      |

  # Alternate Flow
  Scenario: Retrieve all Todo tasks still to be done using filter "?doneStatus=false"
    When I send GET request to "todos" with filter "?doneStatus=false"
    Then we get an HTTP response 200
    And the response should contain a list of all todo tasks
    And the list should include the todo tasks with the following details:
      | id | title          | doneStatus |
      | 1  | scan paperwork | false      |
      | 2  | file paperwork | false      |

  # Error Flow
  Scenario: Attempt to retrieve all Todo tasks using an invalid endpoint
    When I send GET request to "todos/all"
    Then we get an HTTP response 404
    And the response should display the error message "[Could not find an instance with todos/all]"
