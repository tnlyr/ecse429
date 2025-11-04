Feature: Modify an Existing Project
  As a user, I want to update an existing project so that I can adjust its details.

  Background:
    Given the service is running
    And the following projects exist in the system:
      | id | title       | description |
      | 1  | Office Work |             |

  # Normal Flow
  Scenario Outline: Successfully update a project's title and description using PUT
    When I update the project at "projects/<ID>" via PUT with title "<title>" and description "<description>"
    Then the project response code should be 200
    And the response must show a project titled "<title>" with description "<description>"

    Examples:
      | ID | title     | description       |
      | 1  | Project 1 | description - PUT |

  # Alternate Flow
  Scenario Outline: Successfully update a project's title and description using POST
    When I update the project at "projects/<ID>" via POST with title "<title>" and description "<description>"
    Then the project response code should be 200
    And the response must show a project titled "<title>" with description "<description>"

    Examples:
      | ID | title            | description        |
      | 1  | Project 1 - Post | description - POST |

  # Error Flow
  Scenario Outline: Attempt to update with an invalid ID using PUT
    When I update the project at "projects/-1" via PUT with title "<title>" and description "<description>"
    Then the project response code should be 404
    And the response must include the error message: "Invalid GUID for -1 entity project"

    Examples:
      | ID | title             | description         |
      | 1  | Project 1 - Error | description - Error |
