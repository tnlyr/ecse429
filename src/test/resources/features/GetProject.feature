Feature: Retrieve Specific Project
  A user should be able to obtain details of a single project by either its ID or title.

  Background:
    Given the project API service is available
    And the following projects are already stored:
      | id | title        | active |
      | 1  | Health App   | true   |

  # --- Lookup by ID ---
  Scenario Outline: Retrieve a project using its unique identifier
    When a GET request is sent to "projects/<id>"
    Then the server responds with status code 200
    And the response contains project ID "<id>" and title "<title>"

    Examples:
      | id | title      |
      | 1  | Health App |

  # --- Lookup by Title ---
  Scenario Outline: Retrieve a project by searching with title
    When a GET request is sent to "projects" with query parameter title="<title>"
    Then the server responds with status code 200
    And the response contains project ID "<id>" and title "<title>"

    Examples:
      | id | title      |
      | 1  | Health App |

  # --- Invalid ID ---
  Scenario Outline: Try retrieving a project that does not exist
    When a GET request is sent to "projects/<invalid_id>"
    Then the server responds with status code 404
    And the response message should include "[Could not find an instance with projects/<invalid_id>]"

    Examples:
      | invalid_id |
      | 404        |
      | wrongID    |
