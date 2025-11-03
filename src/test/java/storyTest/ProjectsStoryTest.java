package test.java.storyTests;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for project API scenarios.
 * Covers creating, listing, updating, retrieving and deleting projects.
 */
public class ProjectsStoryTest {

    private static Process serviceProcess;
    private Response apiResponse;
    private RequestSpecification httpRequest;

// setup and teardown
    @AfterAll
    public static void closeServer() {
        try {
            given().when().get("/shutdown");
        } catch (Exception ignored) {}
    }

    @Given("the project API service is available")
    public void confirmServiceAvailability() {
        Response res = given().get("/projects");
        assertTrue(res.statusCode() == 200 || res.statusCode() == 404,
                "Service did not respond as expected.");
    }

    // baseline data 
    @Given("the following projects are already stored:")
    public void verifyExistingProjects(DataTable table) {
        Map<String, String> project = table.asMaps(String.class, String.class).get(0);
        int projectId = Integer.parseInt(project.get("id"));

        Response res = given()
                .pathParam("id", projectId)
                .when()
                .get("/projects/{id}");

        assertEquals(200, res.getStatusCode(), "Expected existing project not found.");
        assertEquals(project.get("title"), res.jsonPath().getString("projects[0].title"));
        assertEquals(project.get("active"), res.jsonPath().getString("projects[0].active"));
    }

// create proj
    @When("a POST request is sent to {string} with title {string} and active status {string}")
    public void sendPostToCreate(String endpoint, String title, String active) {
        String json = String.format("{\"title\":\"%s\", \"active\":%s}", title, active);
        httpRequest = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(json);
        apiResponse = httpRequest.post(endpoint);
    }

    @When("a POST request is sent to {string} with an empty request body")
    public void sendEmptyPost(String endpoint) {
        httpRequest = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("{}");
        apiResponse = httpRequest.post(endpoint);
    }

    @When("a POST request is sent to {string} using an invalid active value {string}")
    public void sendInvalidPost(String endpoint, String value) {
        String payload = String.format("{\"title\":\"Invalid\", \"active\":\"%s\"}", value);
        httpRequest = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(payload);
        apiResponse = httpRequest.post(endpoint);
    }

    @Then("the server responds with status code {int}")
    public void verifyStatusCode(int expected) {
        assertEquals(expected, apiResponse.getStatusCode(),
                "Unexpected status code received from API.");
    }

    @Then("the project returned has title {string} and active status {string}")
    public void verifyProjectCreated(String expectedTitle, String expectedActive) {
        assertEquals(expectedTitle, apiResponse.jsonPath().getString("title"));
        assertEquals(expectedActive, apiResponse.jsonPath().getString("active"));
    }

    @Then("the returned project contains default values for every field")
    public void verifyDefaultProjectFields() {
        assertEquals("", apiResponse.jsonPath().getString("title"));
        assertEquals("", apiResponse.jsonPath().getString("description"));
        assertEquals("false", apiResponse.jsonPath().getString("active"));
    }

    @Then("the response message should include {string}")
    public void verifyErrorMessageContains(String expectedMsg) {
        String actual = apiResponse.jsonPath().getString("errorMessages[0]");
        assertTrue(actual.contains(expectedMsg), "Error message mismatch.");
    }

// Get all projs
    @When("a GET request is sent to {string}")
    public void getRequest(String endpoint) {
        apiResponse = RestAssured.given().get("/" + endpoint);
    }

    @When("a GET request is sent to {string} with filter {string}")
    public void getRequestWithFilter(String endpoint, String filter) {
        apiResponse = RestAssured.given().get("/" + endpoint + filter);
    }

    @Then("the body contains a list of projects")
    public void verifyListOfProjects() {
        List<Map<String, Object>> list = apiResponse.jsonPath().getList("projects");
        assertNotNull(list, "Project list is null.");
        assertFalse(list.isEmpty(), "Project list should not be empty.");
    }

    @Then("that list should include:")
    public void verifyProjectListMatches(DataTable expected) {
        List<Map<String, String>> expList = expected.asMaps(String.class, String.class);
        List<Map<String, Object>> actualList = apiResponse.jsonPath().getList("projects");

        assertEquals(expList.size(), actualList.size(), "List size mismatch.");

        // handle order if reversed
        if (actualList.get(0).get("id").equals("2")) {
            Collections.swap(actualList, 0, 1);
        }

        for (int i = 0; i < expList.size(); i++) {
            assertEquals(expList.get(i).get("id"), actualList.get(i).get("id").toString());
            assertEquals(expList.get(i).get("title"), actualList.get(i).get("title"));
            assertEquals(expList.get(i).get("active"), actualList.get(i).get("active").toString());
        }
    }

// Get single proj
    @When("a GET request is sent to {string} with query parameter title={string}")
    public void getProjectByTitle(String endpoint, String title) {
        apiResponse = given()
                .queryParam("title", title)
                .when()
                .get("/" + endpoint);
    }

    @Then("the response contains project ID {string} and title {string}")
    public void verifySingleProject(String id, String title) {
        assertEquals(id, apiResponse.jsonPath().getString("projects[0].id"));
        assertEquals(title, apiResponse.jsonPath().getString("projects[0].title"));
    }

// update proj
    @When("a PUT request is sent to {string} with title {string} and description {string}")
    public void putRequestUpdate(String endpoint, String title, String desc) {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, desc);
        httpRequest = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body);
        apiResponse = httpRequest.put("/" + endpoint);
    }

    @When("a POST request is sent to {string} with title {string} and description {string}")
    public void postRequestUpdate(String endpoint, String title, String desc) {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, desc);
        httpRequest = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body);
        apiResponse = httpRequest.post("/" + endpoint);
    }

    @Then("the updated project should have title {string} and description {string}")
    public void verifyUpdatedProject(String title, String desc) {
        assertEquals(title, apiResponse.jsonPath().getString("title"));
        assertEquals(desc, apiResponse.jsonPath().getString("description"));
    }

// Delete
    @When("a DELETE request is sent to {string}")
    public void deleteRequest(String endpoint) {
        apiResponse = given().when().delete("/" + endpoint);
    }

    @Then("the project located at {string} should be gone")
    public void verifyDeletion(String endpoint) {
        Response res = given().when().get(endpoint);
        assertEquals(404, res.getStatusCode(), "Expected project to be deleted.");
    }

    @When("a POST request is sent to {string} with title {string} and active status {string}, then that project is deleted")
    public void createThenDeleteProject(String endpoint, String title, String active) {
        String json = String.format("{\"title\":\"%s\", \"active\":%s}", title, active);
        httpRequest = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(json);
        apiResponse = httpRequest.post("/" + endpoint);

        String id = apiResponse.jsonPath().getString("id");
        RestAssured.given().delete("/" + endpoint + "/" + id);
    }
}
