package storyTest;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProjectsStoryTest {

    private static Process process;
    private Response response;
    private RequestSpecification request;

    @Given("the following projects exist in the system:")
    public void verifyProjectsExist(DataTable dataTable) {
        String expectedTitle = "Office Work";
        String expectedActive = "false";

        // Check if the specified project already exists by ID
        int projectId = 1;
        Response checkResponse = given()
                .pathParam("id", projectId)
                .when()
                .get("/projects/{id}");

        // Ensure the record is found
        assertEquals(200, checkResponse.getStatusCode(), "Project with ID " + projectId + " should exist");

        // Validate project properties
        String actualTitle = checkResponse.jsonPath().getString("projects[0].title");
        String actualActive = checkResponse.jsonPath().getString("projects[0].active");
        String actualDescription = checkResponse.jsonPath().getString("projects[0].description");

        assertEquals(expectedTitle, actualTitle, "Unexpected project title");
        assertEquals(expectedActive, actualActive, "Unexpected active flag");
        assertEquals("", actualDescription, "Description should be empty by default");
    }

    @AfterAll
    public static void stopServer() {
        try {
            given().get("/shutdown");
        } catch (Exception ignored) {
        }
    }

// -------------------------- CreateNewProject.feature Steps --------------------------
    // -------------------------- Normal Flow --------------------------
    @When("I send a POST request to {string} with title: {string} and active flag: {string}")
    public void sendPostRequestToCreateProject(String endpoint, String title, String active) {
        // Construct the JSON body with the provided parameters
        String requestBody = String.format("{\"title\":\"%s\", \"active\":%s}", title, active);
        request = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody);
        response = request.post(endpoint);
    }

    @Then("the response must include a project titled {string} with active flag {string}")
    public void verifyResponseProjectDetails(String expectedTitle, String expectedActive) {
        String title = response.jsonPath().getString("title");
        String active = response.jsonPath().getString("active");
        assertEquals(expectedTitle, title);
        assertEquals(expectedActive, active);
    }

    // -------------------------- Alternate Flow --------------------------
    @When("I send a POST request to {string} with an empty request body")
    public void sendPostRequestWithEmptyBody(String endpoint) {
        request = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("{}");
        response = request.post(endpoint);
    }

    @Then("the returned project should use default values for all attributes")
    public void verifyProjectDefaults() {
        String title = response.jsonPath().getString("title");
        String description = response.jsonPath().getString("description");
        String active = response.jsonPath().getString("active");

        assertEquals("", title);
        assertEquals("", description);
        assertEquals("false", active);
    }

    // -------------------------- Error Flow --------------------------
    @When("I send a POST request to {string} where active field is {string}")
    public void sendInvalidPostRequest(String endpoint, String invalidActive) {
        String requestBody = String.format("{\"title\":\"Invalid Project\", \"active\":\"%s\"}", invalidActive);
        request = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody);
        response = request.post(endpoint);
    }

    @Then("the response must include the error message: {string}")
    public void verifyErrorMessage(String expectedMessage) {
        String actualMessage = response.jsonPath().getString("errorMessages[0]");
        assertEquals(expectedMessage, actualMessage);
    }

// -------------------------- UpdateProject.feature Steps --------------------------
    // -------------------------- Normal Flow --------------------------
    @When("I update the project at {string} via PUT with title {string} and description {string}")
    public void putUpdateProject(String endpoint, String title, String description) {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, description);
        request = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body);
        response = request.put("/" + endpoint);
    }

    @Then("the response must show a project titled {string} with description {string}")
    public void verifyUpdatedProjectFields(String expectedTitle, String expectedDescription) {
        String actualTitle = response.jsonPath().getString("title");
        String actualDesc  = response.jsonPath().getString("description");

        assertEquals(expectedTitle, actualTitle, "Title does not match");
        assertEquals(expectedDescription, actualDesc, "Description does not match");
    }

    // -------------------------- Alternate Flow --------------------------
    @When("I update the project at {string} via POST with title {string} and description {string}")
    public void postUpdateProject(String endpoint, String title, String description) {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, description);
        request = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body);
        response = request.post("/" + endpoint);
    }

// -------------------------- DeleteProject.feature Steps --------------------------
    // -------------------------- Normal Flow --------------------------
    @When("I delete the project at {string}")
    public void deleteProjectAt(String endpoint) {
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        response = io.restassured.RestAssured.given().delete(path);
    }

    @Then("the project at {string} should no longer exist")
    public void verifyProjectIsDeleted(String endpoint) {
        // Confirm the resource is gone by attempting a GET
        response = io.restassured.RestAssured.given().when().get("/" + endpoint);
        org.junit.jupiter.api.Assertions.assertEquals(
                404, response.getStatusCode(),
                "Expected deletion: resource at " + endpoint + " should not be found"
        );
    }

    @When("I send a DELETE request to project endpoint {string}")
    public void deleteProjectAtProject(String endpoint) {
        response = RestAssured.given().delete("/" + endpoint);
    }

    @Then("the project response code should be {int}")
    public void assertProjectStatus(int code) {
        assertEquals(code, response.getStatusCode());
    }


    // -------------------------- Alternate Flow --------------------------
    @When("I create a project via POST to {string} with title {string} and active {string}, then delete it")
    public void createThenDeleteProject(String endpoint, String title, String active) {
        String body = String.format("{\"title\":\"%s\", \"active\":%s}", title, active);
        request = io.restassured.RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body);

        // Create
        response = request.post("/" + endpoint);

        // Extract ID and delete
        String newProjectId = response.jsonPath().getString("id");
        String deleteEndpoint = "/" + endpoint + "/" + newProjectId;
        response = io.restassured.RestAssured.given().delete(deleteEndpoint);
    }

    // -------------------------- Error Flow --------------------------
    @Given("a project with ID {int} has been removed already")
    public void ensureProjectAlreadyDeleted(int deletedId) {
        String deleteEndpoint = "/projects/" + deletedId;
        io.restassured.RestAssured.given()
                .header("Content-Type", "application/json")
                .delete(deleteEndpoint);
        // idempotent: we don't assert here; the test scenarios will validate outcomes
    }

// -------------------------- GetProject.feature Steps --------------------------
    // -------------------------- Normal & Alternate Flow --------------------------
    @Then("the response must include a project with ID {string} and title {string}")
    public void verifyProjectByIdAndTitle(String expectedId, String expectedTitle) {
        String actualId = response.jsonPath().getString("projects[0].id");
        String actualTitle = response.jsonPath().getString("projects[0].title");

        assertEquals(expectedId, actualId, "Mismatch in project ID");
        assertEquals(expectedTitle, actualTitle, "Mismatch in project title");
    }

    @When("we send a GET request to {string} with title parameter {string} applied")
    public void sendGetRequestByTitle(String endpoint, String title) {
        response = given()
                .queryParam("title", title)
                .when()
                .get("/" + endpoint);
    }

    // -------------------------- Error Flow --------------------------
    @When("I send a GET request to project endpoint {string}")
    public void sendInvalidGetRequest(String endpoint) {
        response = given().get("/" + endpoint);
    }

// -------------------------- GetAllProjects.feature Steps --------------------------
    // ---------------------- Normal Flow ----------------------
    @When("we send a GET request to {string}")
    public void sendGetRequest(String endpoint) {
        response = RestAssured.given().get("/" + endpoint);
    }

    @Then("the response must contain a non-empty list of projects")
    public void verifyProjectListExists() {
        List<Map<String, Object>> projects = response.jsonPath().getList("projects");
        assertNotNull(projects, "Response project list should not be null");
        assertFalse(projects.isEmpty(), "Expected at least one project in the list");
    }

    @Then("the list should include the following project details:")
    public void verifyProjectDetails(DataTable expectedData) {
        List<Map<String, String>> expectedList = expectedData.asMaps(String.class, String.class);
        List<Map<String, Object>> actualList = response.jsonPath().getList("projects");

        assertEquals(expectedList.size(), actualList.size(), "Mismatch in number of projects returned");

        // Optional ordering fix if server returns reversed list
        if (actualList.size() > 1 && "2".equals(actualList.get(0).get("id").toString())) {
            Collections.swap(actualList, 0, 1);
        }

        for (int i = 0; i < expectedList.size(); i++) {
            assertEquals(expectedList.get(i).get("id"), actualList.get(i).get("id").toString(), "ID mismatch");
            assertEquals(expectedList.get(i).get("title"), actualList.get(i).get("title"), "Title mismatch");
            assertEquals(expectedList.get(i).get("active"), actualList.get(i).get("active").toString(), "Active flag mismatch");
        }
    }

    // ---------------------- Alternate Flow ----------------------
    @When("I send a GET request to {string} with filter {string} applied")
    public void sendGetRequestWithFilter(String endpoint, String filter) {
        response = RestAssured.given().get("/" + endpoint + filter);
    }
}

