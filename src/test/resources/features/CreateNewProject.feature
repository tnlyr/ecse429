Feature: Create New Project
  As a user, I want to create a project without specifying an ID so that I can quickly add new projects using minimal input.

  Background:
    Given the service is running

  # Normal Flow
  Scenario Outline: Successfully create a new project using title and active status
    When I send a POST request to "/projects" with title: "<title>" and active flag: "<active>"
    Then the project response code should be 201
    And the response must include a project titled "<title>" with active flag "<active>"

    Examples:
      | title           | active |
      | New Project     | true   |
      | Second Project | false  |

  # Alternate Flow
  Scenario: Create a project with no fields specified
    When I send a POST request to "/projects" with an empty request body
    Then the project response code should be 201
    And the returned project should use default values for all attributes

  # Error Flow
  Scenario: Attempt to create a project with invalid data type for active field
    When I send a POST request to "/projects" where active field is "notABoolean"
    Then the project response code should be 400
    And the response must include the error message: "Failed Validation: active should be BOOLEAN"