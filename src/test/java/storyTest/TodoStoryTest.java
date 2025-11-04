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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class TodoStoryTest {

    private static Process process;
    private Response response;
    private RequestSpecification request;

    @Given("the service is running")
    public void ensureServiceRunning() {
        RestAssured.baseURI = "http://localhost:4567";

        try {
            // Check if service responds on base URI
            HttpURLConnection connection = (HttpURLConnection) new URL(RestAssured.baseURI).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // If reachable, shut it down gracefully before restarting
            if (connection.getResponseCode() == 200) {
                try { given().get("/shutdown"); } catch (Exception ignored) {}
                Thread.sleep(1500);
            }
        } catch (IOException | InterruptedException e) {
            // Service is not currently running — proceed to start it
        }

        // Start the REST API service
        try {
            process = new ProcessBuilder("java", "-jar", "runTodoManagerRestAPI-1.5.5.jar").start();
            Thread.sleep(1750); // give it a moment to initialize
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Failed to start the TodoManager REST API service.");
        }
    }

    @Given("the following todos exist in the system:")
    public void verifyTodosExistInSystem(DataTable dataTable) {
        int[] todoIds = {1, 2};
        String[] expectedTitles = {"scan paperwork", "file paperwork"};
        String expectedDoneStatus = "false";

        for (int i = 0; i < todoIds.length; i++) {
            Response res = given()
                    .pathParam("id", todoIds[i])
                    .when()
                    .get("/todos/{id}");

            // Ensure each record exists
            assertEquals(200, res.getStatusCode(),
                    "Todo with ID " + todoIds[i] + " should exist");

            // Extract actual data
            String actualTitle = res.jsonPath().getString("todos[0].title");
            String actualDone = res.jsonPath().getString("todos[0].doneStatus");
            String actualDesc = res.jsonPath().getString("todos[0].description");

            // Validate against expectations
            assertEquals(expectedTitles[i], actualTitle, "Unexpected title for todo " + todoIds[i]);
            assertEquals(expectedDoneStatus, actualDone, "Unexpected doneStatus for todo " + todoIds[i]);
            assertEquals("", actualDesc, "Description should be empty for todo " + todoIds[i]);
        }
    }

    @AfterAll
    public static void killServer() {
        try {
            given().when().get("/shutdown");
        }
        catch (Exception ignored) {
        }
    }

// -------------------------- CreateNewTodo.feature--------------------------
    // Normal flow
    @When("I send a POST request to {string} with title: {string} and description: {string}")
    public void postWithTitleAndDescription(String endpoint, String title, String description) {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, description);
        request = RestAssured.given().header("Content-Type", "application/json").body(body);
        response = request.post("/" + endpoint);
    }

    @Then("we get an HTTP response {int}")
    public void assertHttpStatus(int statusCode) {
        assertEquals(statusCode, response.getStatusCode());
    }

    @Then("the response should have a todo task with title: {string} and description: {string}")
    public void assertTodoWithTitleAndDescription(String expectedTitle, String expectedDescription) {
        String actualTitle = response.jsonPath().getString("title");
        String actualDescription = response.jsonPath().getString("description");
        assertEquals(expectedTitle, actualTitle);
        assertEquals(expectedDescription, actualDescription);
    }

    // Alternate flow
    @When("I send a POST request to {string} with title: {string} and empty description")
    public void postWithTitleOnly(String endpoint, String title) {
        String body = String.format("{\"title\":\"%s\", \"description\":null}", title);
        request = RestAssured.given().header("Content-Type", "application/json").body(body);
        response = request.post("/" + endpoint);
    }

    @Then("the response should have a todo task with title: {string} and empty description")
    public void assertTodoWithTitleAndEmptyDescription(String expectedTitle) {
        String actualTitle = response.jsonPath().getString("title");
        Object actualDescription = response.jsonPath().get("description");
        assertEquals(expectedTitle, actualTitle);
        // Accept null or empty string, depending on server behavior
        assertTrue(actualDescription == null || "".equals(actualDescription.toString()));
    }

    // Error flow
    @Then("the response should display the error message {string}")
    public void assertErrorMessage(String expectedErrorMessage) {
        String actualErrorMessage = response.jsonPath().getString("errorMessages");
        assertEquals(expectedErrorMessage, actualErrorMessage);
    }

// -------------------------- UpdateTodo.feature--------------------------
    // Normal flow
    @When("I send PUT request to {string} with title: {string} and description: {string}")
    public void putUpdateTodo(String endpoint, String title, String description) {
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        String payload = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", title, description);
        response = RestAssured
                .given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put(path);
    }

// -------------------------- DeleteTodo.feature--------------------------
    // Normal Flow
    @When("I send DELETE request to {string}")
    public void iSendADeleteRequestTo(String endpoint) {
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        response = RestAssured.given().when().delete(path);
    }

    // Alternate Flow
    @When("I send POST request to {string} with title: {string} and description: {string} then delete the todo")
    public void iSendAPostRequestToCreateTodoThenDelete(String endpoint, String title, String description) {
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        String body = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", title, description);

        response = RestAssured.given()
                .contentType("application/json")
                .body(body)
                .post(path);

        // Extract new id, then delete it
        String id = response.jsonPath().getString("id");
        response = RestAssured.given().delete("/todos/" + id);
    }

    @Then("the todo task located at {string} should be deleted")
    public void theTodoShouldBeDeleted(String endpoint) {
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        Response check = RestAssured.given().when().get(path);
        assertEquals(404, check.getStatusCode());
    }

// -------------------------- GetTodo.feature--------------------------
    // Normal Flow
    @When("I send GET request to {string}")
    public void aliasSendGet(String endpoint) {
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        response = io.restassured.RestAssured.given().get(path);
    }

    // Alternate Flow
    @When("I send GET request to {string} with title parameter {string}")
    public void aliasSendGetWithTitle(String endpoint, String title) { // <— renamed method
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        response = io.restassured.RestAssured.given()
                .queryParam("title", title)
                .get(path);
    }

    // Body Assertions
    @Then("the response should have a todo task ID {string} with title {string}")
    public void assertTodoContainsIdAndTitle(String expectedId, String expectedTitle) { // <— renamed method
        org.junit.jupiter.api.Assertions.assertNotNull(response, "Response was null — was the previous step executed?");
        String actualId = response.jsonPath().getString("todos[0].id");
        String actualTitle = response.jsonPath().getString("todos[0].title");
        org.junit.jupiter.api.Assertions.assertEquals(expectedId, actualId);
        org.junit.jupiter.api.Assertions.assertEquals(expectedTitle, actualTitle);
    }

// -------------------------- GetAllTodos.feature--------------------------
    // Alternate Flow (with filter)
    @When("I send GET request to {string} with filter {string}")
    public void sendGetWithFilter(String endpoint, String filter) {
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        String suffix = (filter == null || filter.isEmpty())
                ? ""
                : (filter.startsWith("?") || filter.startsWith("&")) ? filter : "?" + filter;
        response = io.restassured.RestAssured.given().get(path + suffix);
    }

    // Body Assertions
    @Then("the response should contain a list of all todo tasks")
    public void assertTodoListPresent() {
        org.junit.jupiter.api.Assertions.assertNotNull(response, "Response was null");
        List<Map<String, Object>> todos = response.jsonPath().getList("todos");
        org.junit.jupiter.api.Assertions.assertNotNull(todos, "todos list should not be null");
        org.junit.jupiter.api.Assertions.assertFalse(todos.isEmpty(), "todos list should not be empty");
    }

    @Then("the list should include the todo tasks with the following details:")
    public void assertTodoListMatches(io.cucumber.datatable.DataTable expectedTodos) {
        List<Map<String, String>> expected = expectedTodos.asMaps(String.class, String.class);
        List<Map<String, Object>> actual = response.jsonPath().getList("todos");

        // Index actual by id to avoid relying on order
        java.util.Map<String, Map<String, Object>> actualById = new java.util.HashMap<>();
        for (Map<String, Object> item : actual) {
            actualById.put(String.valueOf(item.get("id")), item);
        }

        // Ensure all expected rows are present and fields match
        for (Map<String, String> row : expected) {
            String id = row.get("id");
            Map<String, Object> found = actualById.get(id);
            org.junit.jupiter.api.Assertions.assertNotNull(found, "Missing todo with id=" + id);

            String actualTitle = String.valueOf(found.get("title"));
            String actualDone  = String.valueOf(found.get("doneStatus"));

            org.junit.jupiter.api.Assertions.assertEquals(row.get("title"), actualTitle, "Title mismatch for id=" + id);
            org.junit.jupiter.api.Assertions.assertEquals(row.get("doneStatus"), actualDone, "doneStatus mismatch for id=" + id);
        }
    }
}

