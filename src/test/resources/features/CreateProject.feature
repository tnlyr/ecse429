Feature: Create New Project
  Users should be able to add a new project without specifying an ID, providing only minimal information.

  Background:
    Given the project API service is available

  # --- Successful Creation ---
  Scenario Outline: Successfully create a project with a title and active flag
    When a POST request is sent to "/projects" with title "<title>" and active status "<active>"
    Then the server responds with status code 201
    And the project returned has title "<title>" and active status "<active>"

    Examples:
      | title             | active |
      | Website Redesign  | true   |
      | Internal Training | false  |

  # --- Default Creation ---
  Scenario: Create a project without specifying any fields
    When a POST request is sent to "/projects" with an empty request body
    Then the server responds with status code 201
    And the returned project contains default values for every field

  # --- Invalid Input ---
  Scenario: Attempt to create a project with an invalid active value
    When a POST request is sent to "/projects" using an invalid active value "yesplease"
    Then the server responds with status code 400
    And the response message should include "Failed Validation: active should be BOOLEAN"
