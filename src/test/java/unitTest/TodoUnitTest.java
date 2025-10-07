package unitTest;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.Random;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * JUnit tests for the Todo REST API.
 * Tests are randomized at runtime
 */
@TestMethodOrder(Random.class)
public class TodoUnitTest {

    private int currentTodoId;
    private JSONObject seedTodoObj;

    private final String defaultTitle = "Title Todo";
    private final Boolean defaultDoneStatus = false;

    private static Process apiProcess;

    // ---------- lifecycle ----------

    @BeforeAll
    public static void bootApi() throws Exception {
        // Start the sample API
        try {
            apiProcess = Runtime.getRuntime().exec("java -jar runTodoManagerRestAPI-1.5.5.jar");
            sleep(500); // small buffer for startup
        } catch (Exception e) {
            e.printStackTrace();
        }

        RestAssured.baseURI = "http://localhost:4567";

        // Sanity check: API should respond on the root
        int serverResponse = 404;
        try {
            HttpURLConnection conn =
                    (HttpURLConnection) new URL("http://localhost:4567").openConnection();
            conn.setRequestMethod("GET");
            serverResponse = conn.getResponseCode();
            assertEquals(200, serverResponse);
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(200, serverResponse);
        }
    }

    // Seed a to-do before each test
    @BeforeEach
    public void createToDo() {
        seedTodoObj = newUniqueTodo();
        Response response = given()
                .body(seedTodoObj.toString())
                .when()
                .post("/todos");

        assertEquals(201, response.getStatusCode());
        assertEquals(seedTodoObj.getString("title"), response.jsonPath().getString("title"));
        assertEquals(seedTodoObj.getString("description"), response.jsonPath().getString("description"));
        assertEquals(String.valueOf(seedTodoObj.getBoolean("doneStatus")), response.jsonPath().getString("doneStatus"));

        currentTodoId = response.jsonPath().getInt("id");
    }

    // Clean up the seeded to-do after each run
    @AfterEach
    public void removeSeedTodo() {
        Response response = given()
                .pathParam("id", currentTodoId)
                .when()
                .delete("/todos/{id}");
        assertEquals(200, response.getStatusCode());
    }

    @AfterAll
    public static void tearDownApi() {
        try {
            apiProcess.destroy();
            sleep(500);
        } catch (Exception ignored) {}
    }

    // ---------- Tests ----------

    @Test
    public void shouldExposeOptionsForTodos() {
        Response response = given().when().options("/todos");
        assertEquals(200, response.getStatusCode());
        assertEquals("OPTIONS, GET, HEAD, POST", response.getHeaders().get("Allow").getValue());
    }

    @Test
    public void shouldReturnHeadersForSpecificTodo() {
        Response response = given()
                .pathParam("id", currentTodoId)
                .when()
                .head("/todos/{id}");
        assertEquals(200, response.getStatusCode());
        assertNotEquals(0, response.getHeaders().size());
        assertEquals("application/json", response.getHeaders().get("Content-Type").getValue());
        assertEquals("chunked", response.getHeaders().get("Transfer-Encoding").getValue());
    }

    @Test
    public void shouldCreateTodoFromJson() {
        JSONObject payload = new JSONObject()
                .put("title", "test title")
                .put("doneStatus", false)
                .put("description", "test description");

        Response response = given().body(payload.toString()).when().post("/todos");
        assertEquals(201, response.getStatusCode());
        assertEquals("test title", response.jsonPath().getString("title"));
        assertEquals("false", response.jsonPath().getString("doneStatus"));
        assertEquals("test description", response.jsonPath().getString("description"));

        int createdId = response.jsonPath().getInt("id");
        Response deleteResp = given().pathParam("id", createdId).when().delete("/todos/{id}");
        assertEquals(200, deleteResp.getStatusCode());
    }

    @Test
    public void should404OnUnknownTodoId() {
        int invalidId = -1;
        Response response = given().pathParam("id", invalidId).when().get("/todos/{id}");
        assertEquals(404, response.getStatusCode());
        String expectedMessage = "[Could not find an instance with todos/" + invalidId + "]";
        assertEquals(expectedMessage, response.jsonPath().getString("errorMessages"));
    }

    // FIXME : BUG
    @Test
    public void shouldServeDocsPage() {
        Response response = given().when().get("/docs");
        String title = response.xmlPath().getString("html.head.title");
        assertEquals(200, response.getStatusCode());
        assertTrue(title.contains("API Documentation"));
    }

    @Test
    public void shouldRejectEmptyTodoOnPost() {
        JSONObject empty = new JSONObject();
        Response response = given().body(empty.toString()).when().post("/todos");
        assertEquals(400, response.getStatusCode());
        assertEquals("[title : field is mandatory]", response.jsonPath().getString("errorMessages"));
    }

    @Test
    public void shouldListTodosAsJson() {
        Response response = given().when().get("/todos");
        List<Map<String, Object>> todos = response.jsonPath().getList("todos");
        boolean found = false;
        for (Map<String, Object> todo : todos) {
            if (seedTodoObj.getString("title").equals(todo.get("title"))) {
                found = true; break;
            }
        }
        assertEquals(200, response.getStatusCode());
        assertFalse(todos.isEmpty());
        assertTrue(found);
    }

    @Test
    public void shouldUpdateTodoViaPost() {
        JSONObject updatedTodo = newUniqueTodo();
        Response responsePost = given().body(updatedTodo.toString()).when().post("/todos/" + currentTodoId);
        assertEquals(200, responsePost.getStatusCode());
    }

    @Test
    public void should400OnMalformedJsonPayload() {
        JSONObject bad = new JSONObject()
                .put("title", "test title")
                .put("done", false) // wrong key; should be doneStatus
                .put("description", "test description");

        Response response = given().body(bad.toString()).when().post("/todos");
        assertEquals(400, response.getStatusCode());
        assertEquals("[Could not find field: done]", response.jsonPath().getString("errorMessages"));
    }

    @Test
    public void shouldFilterTodosByTitle() {
        Response response = given()
                .queryParam("title", seedTodoObj.getString("title"))
                .when()
                .get("/todos");

        assertEquals(200, response.getStatusCode());
        assertEquals(seedTodoObj.getString("title"), response.jsonPath().getString("todos[0].title"));
        assertEquals(seedTodoObj.getString("description"), response.jsonPath().getString("todos[0].description"));
        assertEquals(String.valueOf(seedTodoObj.getBoolean("doneStatus")), response.jsonPath().getString("todos[0].doneStatus"));
    }

    @Test
    public void shouldRejectPostWithoutTitle() {
        JSONObject payload = new JSONObject()
                .put("doneStatus", false)
                .put("description", "test description");

        Response response = given().body(payload.toString()).when().post("/todos");
        assertEquals(400, response.getStatusCode());
        assertEquals("[title : field is mandatory]", response.jsonPath().getString("errorMessages"));
    }

    @Test
    public void shouldExposeOptionsForSpecificTodo() {
        Response response = given()
                .pathParam("id", currentTodoId)
                .when()
                .options("/todos/{id}");
        assertEquals(200, response.getStatusCode());
        assertEquals("OPTIONS, GET, HEAD, POST, PUT, DELETE", response.getHeaders().get("Allow").getValue());
    }

    @Test
    public void shouldListTodosAsXml() {
        Response response = given()
                .header("Accept", ContentType.XML)
                .contentType(ContentType.XML)
                .when()
                .get("/todos");

        List<String> titles = response.xmlPath().getList("todos.todo.title");
        boolean found = titles.contains(seedTodoObj.getString("title"));

        assertEquals(200, response.getStatusCode());
        assertNotNull(titles);
        assertFalse(titles.isEmpty());
        assertTrue(found);
    }

    @Test
    public void should400OnMalformedXmlPayload() {
        String xmlBody =
                "<todo>" +
                        "<title>test title</title>" +
                        "<done>false</done>" + // wrong key; should be doneStatus
                        "<description>test description</description>" +
                        "</todo>";

        Response response = given()
                .header("Accept", ContentType.XML)
                .contentType(ContentType.XML)
                .body(xmlBody)
                .when()
                .post("/todos");

        assertEquals(400, response.getStatusCode());
        assertEquals("Could not find field: done", response.xmlPath().getString("errorMessages"));
    }

    @Test
    public void shouldReturnHeadersForTodosCollection() {
        Response response = given().when().head("/todos");
        assertEquals(200, response.getStatusCode());
        assertNotEquals(0, response.getHeaders().size());
        assertEquals("application/json", response.getHeaders().get("Content-Type").getValue());
        assertEquals("chunked", response.getHeaders().get("Transfer-Encoding").getValue());
    }

    @Test
    public void shouldFetchTodoById() {
        Response response = given().pathParam("id", currentTodoId).when().get("/todos/{id}");
        assertEquals(200, response.getStatusCode());
        assertEquals(seedTodoObj.getString("title"), response.jsonPath().getString("todos[0].title"));
        assertEquals(seedTodoObj.getString("description"), response.jsonPath().getString("todos[0].description"));
        assertEquals(String.valueOf(seedTodoObj.getBoolean("doneStatus")), response.jsonPath().getString("todos[0].doneStatus"));
    }

    // FIXME
    // Known behavior discrepancy: PUT unexpectedly resets doneStatus/description if not provided.
    @Test
    public void shouldKeepFieldsWhenPutOnlyChangesTitle_expected() {
        JSONObject updated = new JSONObject().put("title", "updated test title - put");
        Response responsePut = given().body(updated.toString()).when().put("/todos/" + currentTodoId);

        assertEquals(200, responsePut.getStatusCode());
        assertEquals("updated test title - put", responsePut.jsonPath().getString("title"));
        assertEquals(seedTodoObj.getString("description"), responsePut.jsonPath().getString("description"));
        assertEquals(String.valueOf(seedTodoObj.getBoolean("doneStatus")), responsePut.jsonPath().getString("doneStatus"));
    }

    // Captures actual server behavior (reset of some fields)
    @Test
    public void shouldReflectActualPutBehavior_resetsFields() {
        JSONObject updatedTodo = newUniqueTodo();
        Response responsePut = given().body(updatedTodo.toString()).when().put("/todos/" + currentTodoId);
        assertEquals(200, responsePut.getStatusCode());
    }

    // FIXME
    // Expected vs actual: API demands title on PUT even for partial updates.
    @Test
    public void shouldAllowPutWithoutTitle_expected() {
        JSONObject updated = new JSONObject().put("description", "updated test description - put");
        Response responsePut = given().body(updated.toString()).when().put("/todos/" + currentTodoId);

        assertEquals(200, responsePut.getStatusCode());
        assertEquals("updated test description - put", responsePut.jsonPath().getString("description"));
        assertEquals(defaultTitle, responsePut.jsonPath().getString("title"));
        assertEquals(defaultDoneStatus.toString(), responsePut.jsonPath().getString("doneStatus"));
    }

    // Mirrors actual API (400 requiring title)
    @Test
    public void shouldRejectPutWithoutTitle_actual() {
        JSONObject updated = new JSONObject().put("description", "updated test description - put");
        Response responsePut = given().body(updated.toString()).when().put("/todos/" + currentTodoId);

        assertEquals(400, responsePut.getStatusCode());
        assertEquals("[title : field is mandatory]", responsePut.jsonPath().getString("errorMessages"));
    }

    @Test
    public void should404OnPutInvalidId() {
        int invalidId = -1;
        JSONObject updated = new JSONObject().put("title", "updated test title - put");
        Response responsePut = given().body(updated.toString()).when().put("/todos/" + invalidId);

        assertEquals(404, responsePut.getStatusCode());
        String expectedMessage = "[Invalid GUID for " + invalidId + " entity todo]";
        assertEquals(expectedMessage, responsePut.jsonPath().getString("errorMessages"));
    }

    @Test
    public void shouldPostUpdateWithoutTitleKeepOtherFields() {
        JSONObject updated = new JSONObject().put("description", "updated test description - post");
        Response responsePost = given().body(updated.toString()).when().post("/todos/" + currentTodoId);

        assertEquals(200, responsePost.getStatusCode());
        assertEquals("updated test description - post", responsePost.jsonPath().getString("description"));
        assertEquals(seedTodoObj.getString("title"), responsePost.jsonPath().getString("title"));
        assertEquals(String.valueOf(seedTodoObj.getBoolean("doneStatus")), responsePost.jsonPath().getString("doneStatus"));
    }

    @Test
    public void should404OnPostUpdateWithInvalidId() {
        int invalidId = -1;
        JSONObject updated = new JSONObject().put("title", "updated test title - post");
        Response responsePost = given().body(updated.toString()).when().post("/todos/" + invalidId);

        assertEquals(404, responsePost.getStatusCode());
        String expectedMessage = "[No such todo entity instance with GUID or ID " + invalidId + " found]";
        assertEquals(expectedMessage, responsePost.jsonPath().getString("errorMessages"));
    }

    @Test
    public void shouldCreateTodoFromXml() {
        String xmlBody =
                "<todo>" +
                        "<title>test title</title>" +
                        "<doneStatus>false</doneStatus>" +
                        "<description>test description</description>" +
                        "</todo>";

        Response response = given()
                .header("Accept", ContentType.XML)
                .contentType(ContentType.XML)
                .body(xmlBody)
                .when()
                .post("/todos");

        assertEquals(201, response.getStatusCode());
        assertEquals("test title", response.xmlPath().getString("todo.title"));
        assertEquals("false", response.xmlPath().getString("todo.doneStatus"));
        assertEquals("test description", response.xmlPath().getString("todo.description"));

        int createdId = response.xmlPath().getInt("todo.id");
        Response deleteResp = given().pathParam("id", createdId).when().delete("/todos/{id}");
        assertEquals(200, deleteResp.getStatusCode());
    }

    @Test
    public void should404OnDeleteInvalidId() {
        int invalidId = -1;
        Response response = given().pathParam("id", invalidId).when().delete("/todos/{id}");
        assertEquals(404, response.getStatusCode());
        String expectedMessage = "[Could not find any instances with todos/" + invalidId + "]";
        assertEquals(expectedMessage, response.jsonPath().getString("errorMessages"));
    }

    @Test
    public void should404WhenDeletingAlreadyDeletedTodo() {
        JSONObject payload = new JSONObject()
                .put("title", "test title")
                .put("doneStatus", false)
                .put("description", "test description");

        Response createResp = given().body(payload.toString()).when().post("/todos");
        assertEquals(201, createResp.getStatusCode());
        int createdId = createResp.jsonPath().getInt("id");

        Response firstDelete = given().pathParam("id", createdId).when().delete("/todos/{id}");
        assertEquals(200, firstDelete.getStatusCode());

        Response secondDelete = given().pathParam("id", createdId).when().delete("/todos/{id}");
        assertEquals(404, secondDelete.getStatusCode());

        String expectedMessage = "[Could not find any instances with todos/" + createdId + "]";
        assertEquals(expectedMessage, secondDelete.jsonPath().getString("errorMessages"));
    }

    @Test
    public void shouldCreateMultipleTodosAndCleanup() {
        for (int i = 0; i < 3; i++) {
            JSONObject todo = newUniqueTodo();
            Response response = given().body(todo.toString()).when().post("/todos");
            assertEquals(201, response.getStatusCode());

            int createdId = response.jsonPath().getInt("id");
            Response del = given().pathParam("id", createdId).when().delete("/todos/{id}");
            assertEquals(200, del.getStatusCode());
        }
    }

    @Test
    public void shouldDeleteBatchOfNewTodos() {
        int[] createdIds = new int[3];
        for (int i = 0; i < createdIds.length; i++) {
            JSONObject todo = newUniqueTodo();
            Response response = given().body(todo.toString()).when().post("/todos");
            assertEquals(201, response.getStatusCode());
            createdIds[i] = response.jsonPath().getInt("id");
        }
        for (int id : createdIds) {
            Response response = given().pathParam("id", id).when().delete("/todos/{id}");
            assertEquals(200, response.getStatusCode());
        }
    }

    // ---------- helpers ----------
    private JSONObject buildTodo(String title, boolean done, String description) {
        return new JSONObject()
                .put("title", title)
                .put("doneStatus", done)
                .put("description", description);
    }

    private JSONObject newUniqueTodo() {
        return buildTodo(
                "Todo " + System.currentTimeMillis(),
                false,
                "Testing Todo endpoints"
        );
    }
}