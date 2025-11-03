Feature: Update Project Details
  A user should be able to modify an existing project's title or description.

  Background:
    Given the project API service is available
    And the following projects are already stored:
      | id | title          | description |
      | 1  | Draft Proposal |             |

  # --- PUT Update ---
  Scenario Outline: Modify title and description using PUT
    When a PUT request is sent to "projects/<ID>" with title "<title>" and description "<description>"
    Then the server responds with status code 200
    And the updated project should have title "<title>" and description "<description>"

    Examples:
      | ID | title             | description        |
      | 1  | Draft Revision    | Updated via PUT    |

  # --- POST Update ---
  Scenario Outline: Modify title and description using POST
    When a POST request is sent to "projects/<ID>" with title "<title>" and description "<description>"
    Then the server responds with status code 200
    And the updated project should have title "<title>" and description "<description>"

    Examples:
      | ID | title               | description        |
      | 1  | Draft Revision v2   | Updated via POST   |

  # --- Invalid Update ---
  Scenario Outline: Attempt to update a project with a bad ID
    When a PUT request is sent to "projects/-1" with title "<title>" and description "<description>"
    Then the server responds with status code 404
    And the response message should include "Invalid GUID for -1 entity project"

    Examples:
      | ID | title        | description        |
      | 1  | Broken Edit  | Invalid reference  |
