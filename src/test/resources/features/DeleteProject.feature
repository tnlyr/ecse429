Feature: Delete Project
  Users must be able to permanently remove projects that are no longer needed.

  Background:
    Given the project API service is available
    And the following projects are already stored:
      | id | title         | active |
      | 1  | Demo Project  | false  |

  # --- Standard Deletion ---
  Scenario Outline: Remove a project using its identifier
    When a DELETE request is sent to "projects/<ID>"
    Then the server responds with status code 200
    And the project located at "projects/<ID>" should be gone

    Examples:
      | ID |
      | 1  |

  # --- Create and Delete ---
  Scenario Outline: Create a temporary project and remove it
    When a POST request is sent to "projects" with title "Temporary" and active status "true", then that project is deleted
    Then the server responds with status code 200
    And the project located at "projects/<ID>" should be gone

    Examples:
      | ID | title          | active |
      | 5  | Temporary Task | true   |
      | 6  | Archive File   | false  |

  # --- Invalid Deletion ---
  Scenario: Attempt to delete a project with an invalid ID
    When a DELETE request is sent to "projects/-1"
    Then the server responds with status code 404
    And the response message should include "[Could not find any instances with projects/-1]"
