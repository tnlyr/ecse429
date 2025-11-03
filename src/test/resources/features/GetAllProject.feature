Feature: Retrieve All Projects
  The system must return a list of all existing projects when requested by the user.

  Background:
    Given the project API service is available
    And the following projects are already stored:
      | id | title        | active |
      | 1  | Design Phase | true   |

  # --- Retrieve All Projects ---
  Scenario: Fetch every project in the database
    When a GET request is sent to "projects"
    Then the server responds with status code 200
    And the body contains a list of projects
    And that list should include:
      | id | title        | active |
      | 1  | Design Phase | true   |

  # --- Filtered Retrieval ---
  Scenario: Retrieve only inactive projects
    When a GET request is sent to "projects" with filter "?active=false"
    Then the server responds with status code 200
    And the body contains a list of projects
    And that list should include:
      | id | title        | active |
      | 1  | Design Phase | true   |

  # --- Invalid Endpoint ---
  Scenario: Handle invalid project endpoint
    When a GET request is sent to "projects/all"
    Then the server responds with status code 404
    And the response message should include "Could not find an instance with projects/all"
