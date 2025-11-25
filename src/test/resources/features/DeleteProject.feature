Feature: Delete an Existing Project
  As a user, I want to delete a project by its ID so that it no longer appears in listings.

  Background:
    Given the service is running
    And the following projects exist in the system:
      | id | title       | active |
      | 1  | Office Work | false  |

  # Normal Flow
  Scenario Outline: Delete a project by ID
    When I delete the project at "projects/<ID>"
    Then the project response code should be 200
    And the project at "projects/<ID>" should no longer exist

    Examples:
      | ID |
      | 1  |

  # Alternate Flow
  Scenario Outline: Create a project and then delete it
    When I create a project via POST to "projects" with title "title" and active "false", then delete it
    And I send a DELETE request to project endpoint "projects/<ID>"
    Then the project response code should be 404
    And the project at "projects/<ID>" should no longer exist

    Examples:
      | ID | title     | active |
      | 5  | Project X | true   |
      | 6  | Project Y | false  |

  # Error Flow
  Scenario: Delete a project with an invalid ID
    When I delete the project at "projects/-1"
    Then the project response code should be 404
    And the response must include the error message: "Could not find any instances with projects/-1"
