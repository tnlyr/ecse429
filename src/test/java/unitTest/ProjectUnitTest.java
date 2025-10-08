package unitTest;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.Random;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * BDD-style Project API tests (performance/csv logging removed).
 */
@TestMethodOrder(Random.class)
public class ProjectUnitTest {

    private int projectId;
    private JSONObject testProject;
    private static Process apiProcess;

    // Constants used in a couple of query tests
    private static final String TARGET_TITLE = "Introduction to Software Validation";
    private static final String TARGET_PARTIAL_DESCRIPTION = "Beginner";

    @BeforeAll
    public static void beforeAll_startApiAndVerify() throws Exception {
        // Boot the API under test
        try {
            apiProcess = Runtime.getRuntime().exec("java -jar runTodoManagerRestAPI-1.5.5.jar");
            sleep(750); // brief pause for server startup
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configure RestAssured base URI once
        RestAssured.baseURI = "http://localhost:4567";

        // Smoke check the server
        int code = 404;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:4567").openConnection();
            conn.setRequestMethod("GET");
            code = conn.getResponseCode();
            assertEquals(200, code);
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(200, code);
        }
    }

    @AfterAll
    public static void afterAll_shutdownApi() {
        try {
            given().when().get("/shutdown");
        } catch (Exception ignored) {}
    }

    @BeforeEach
    public void beforeEach_seedProject() {
        testProject = newUniqueProject();

        Response create = given()
                .body(testProject.toString())
                .when()
                .post("/projects");

        assertEquals(201, create.getStatusCode());
        assertEquals(testProject.getString("title"), create.jsonPath().getString("title"));
        assertEquals(testProject.getString("description"), create.jsonPath().getString("description"));
        assertEquals(String.valueOf(testProject.getBoolean("completed")), create.jsonPath().getString("completed"));
        assertEquals(String.valueOf(testProject.getBoolean("active")), create.jsonPath().getString("active"));

        projectId = create.jsonPath().getInt("id");
    }

    @AfterEach
    public void afterEach_cleanupSeedProject() {
        Response delete = given()
                .pathParam("id", projectId)
                .when()
                .delete("/projects/{id}");
        assertEquals(200, delete.getStatusCode());
    }

    // --- Create -----------------------------------------------------------------

    @Test
    public void givenValidJson_whenPostProject_thenCreatedAndFieldsPersisted() {
        JSONObject payload = new JSONObject()
                .put("title", "test title")
                .put("completed", false)
                .put("active", false)
                .put("description", "test description");

        Response resp = given().body(payload.toString()).when().post("/projects");

        assertEquals(201, resp.getStatusCode());
        assertEquals("test title", resp.jsonPath().getString("title"));
        assertEquals("false", resp.jsonPath().getString("completed"));
        assertEquals("false", resp.jsonPath().getString("active"));
        assertEquals("test description", resp.jsonPath().getString("description"));

        deleteById(resp.jsonPath().getInt("id"));
    }

    @Test
    public void givenValidXml_whenPostProject_thenCreatedAndFieldsPersisted() {
        String xml = "<project>" +
                "<title>xml title</title>" +
                "<completed>false</completed>" +
                "<active>false</active>" +
                "<description>xml description</description>" +
                "</project>";

        Response resp = given()
                .header("Accept", ContentType.XML)
                .contentType(ContentType.XML)
                .body(xml)
                .when()
                .post("/projects");

        assertEquals(201, resp.getStatusCode());
        assertEquals("xml title", resp.xmlPath().getString("project.title"));
        assertEquals("false", resp.xmlPath().getString("project.completed"));
        assertEquals("false", resp.xmlPath().getString("project.active"));
        assertEquals("xml description", resp.xmlPath().getString("project.description"));

        deleteById(resp.xmlPath().getInt("project.id"));
    }

    @Test
    public void givenDuplicateNameAndDescription_whenPostTwice_thenBothAreCreatedIndependently() {
        JSONObject payload = new JSONObject()
                .put("title", "same title")
                .put("completed", false)
                .put("active", false)
                .put("description", "same description");

        Response first = given().body(payload.toString()).when().post("/projects");
        Response second = given().body(payload.toString()).when().post("/projects");

        assertEquals(201, first.getStatusCode());
        assertEquals(201, second.getStatusCode());

        deleteById(first.jsonPath().getInt("id"));
        deleteById(second.jsonPath().getInt("id"));
    }

    @Test
    public void givenCompletedTrue_whenPostProject_thenCreatedWithCompletedTrue() {
        JSONObject payload = new JSONObject()
                .put("title", "done")
                .put("completed", true)
                .put("active", false)
                .put("description", "finished");

        Response resp = given().body(payload.toString()).when().post("/projects");

        assertEquals(201, resp.getStatusCode());
        assertEquals("true", resp.jsonPath().getString("completed"));
        deleteById(resp.jsonPath().getInt("id"));
    }

    @Test
    public void givenCompletedAndActiveTrue_whenPostProject_thenCreatedWithBothTrue() {
        JSONObject payload = new JSONObject()
                .put("title", "active and done")
                .put("completed", true)
                .put("active", true)
                .put("description", "both true");

        Response resp = given().body(payload.toString()).when().post("/projects");
        assertEquals(201, resp.getStatusCode());
        assertEquals("true", resp.jsonPath().getString("active"));
        assertEquals("true", resp.jsonPath().getString("completed"));
        deleteById(resp.jsonPath().getInt("id"));
    }

    @Test
    public void givenEmptyBody_whenPostProject_thenBadRequestExpected_butApiActuallyCreates() {
        JSONObject empty = new JSONObject();
        Response resp = given().body(empty.toString()).when().post("/projects");
        // Document both: expected vs observed
        assertEquals(201, resp.getStatusCode());
        deleteById(resp.jsonPath().getInt("id"));
    }

    @Test
    public void givenEmptyBody_whenPostProject_thenBadRequest() {
        JSONObject empty = new JSONObject();
        Response resp = given().body(empty.toString()).when().post("/projects");
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    public void givenMalformedJsonField_whenPostProject_then400WithFieldError() {
        JSONObject bad = new JSONObject()
                .put("title", "bad field")
                .put("complete", false) // should be "completed"
                .put("active", false)
                .put("description", "oops");

        Response resp = given().body(bad.toString()).when().post("/projects");

        assertEquals(400, resp.getStatusCode());
        assertEquals("[Could not find field: complete]", resp.jsonPath().getString("errorMessages"));
    }

    @Test
    public void givenMalformedXmlField_whenPostProject_then400WithFieldError() {
        String xml = "<project>" +
                "<title>bad field</title>" +
                "<complete>false</complete>" + // should be "completed"
                "<active>false</active>" +
                "<description>oops</description>" +
                "</project>";

        Response resp = given()
                .header("Accept", ContentType.XML)
                .contentType(ContentType.XML)
                .body(xml)
                .when()
                .post("/projects");

        assertEquals(400, resp.getStatusCode());
        assertEquals("Could not find field: complete", resp.xmlPath().getString("errorMessages"));
    }

    // --- Read -------------------------------------------------------------------

    @Test
    public void givenSeededProject_whenListProjectsJson_thenSeededTitleIsPresent() {
        Response resp = given().when().get("/projects");

        List<Map<String, Object>> list = resp.jsonPath().getList("projects");
        boolean present = list.stream().anyMatch(p -> testProject.getString("title").equals(p.get("title")));

        assertEquals(200, resp.getStatusCode());
        assertFalse(list.isEmpty());
        assertTrue(present);
    }

    @Test
    public void givenSeededProject_whenListProjectsXml_thenSeededTitleIsPresent() {
        Response resp = given()
                .header("Accept", ContentType.XML)
                .contentType(ContentType.XML)
                .when()
                .get("/projects");

        List<String> titles = resp.xmlPath().getList("projects.project.title");
        assertEquals(200, resp.getStatusCode());
        assertNotNull(titles);
        assertFalse(titles.isEmpty());
        assertTrue(titles.contains(testProject.getString("title")));
    }

    @Test
    public void givenSeededProjectId_whenGetProject_thenSeededFieldsMatch() {
        Response resp = given()
                .pathParam("id", projectId)
                .when()
                .get("/projects/{id}");

        assertEquals(200, resp.getStatusCode());
        assertEquals(testProject.getString("title"), resp.jsonPath().getString("projects[0].title"));
        assertEquals(testProject.getString("description"), resp.jsonPath().getString("projects[0].description"));
        assertEquals(String.valueOf(testProject.getBoolean("completed")), resp.jsonPath().getString("projects[0].completed"));
        assertEquals(String.valueOf(testProject.getBoolean("active")), resp.jsonPath().getString("projects[0].active"));
    }

    @Test
    public void givenInvalidProjectId_whenGetProject_then404AndHelpfulMessage() {
        int invalid = -1;
        Response resp = given().pathParam("id", invalid).when().get("/projects/{id}");
        assertEquals(404, resp.getStatusCode());
        assertEquals("[Could not find an instance with projects/" + invalid + "]",
                resp.jsonPath().getString("errorMessages"));
    }

    @Test
    public void givenTitleQuery_whenGetProjects_thenMatchingProjectReturned() {
        Response resp = given()
                .pathParam("projectTitle", testProject.getString("title"))
                .when()
                .get("/projects?title={projectTitle}");

        assertEquals(200, resp.getStatusCode());
        assertEquals(testProject.getString("title"), resp.jsonPath().getString("projects[0].title"));
        assertEquals(testProject.getString("description"), resp.jsonPath().getString("projects[0].description"));
    }

    @Test
    public void givenTitleAndPartialDescription_whenGetProjects_thenMatchOnBoth() {
        Response resp = given()
                .pathParam("projectTitle", TARGET_TITLE)
                .pathParam("partialProjectDescription", TARGET_PARTIAL_DESCRIPTION)
                .when()
                .get("/projects?title={projectTitle}&description={partialProjectDescription}");

        assertEquals(200, resp.getStatusCode());
        assertEquals(TARGET_TITLE, resp.jsonPath().getString("projects[0].title"));
        assertEquals(TARGET_PARTIAL_DESCRIPTION, resp.jsonPath().getString("projects[0].description"));
        assertEquals("false", resp.jsonPath().getString("projects[0].completed"));
        assertEquals("false", resp.jsonPath().getString("projects[0].active"));
    }

    // --- Headers & Options ------------------------------------------------------

    @Test
    public void givenProjectsEndpoint_whenHead_thenContentTypeAndTransferEncodingPresent() {
        Response resp = given().when().head("/projects");
        assertEquals(200, resp.getStatusCode());
        assertNotEquals(0, resp.getHeaders().size());
        assertEquals("application/json", resp.getHeaders().get("Content-Type").getValue());
        assertEquals("chunked", resp.getHeaders().get("Transfer-Encoding").getValue());
    }

    @Test
    public void givenProjectsEndpoint_whenOptions_thenAllowHeaderListsSupportedVerbs() {
        Response resp = given().when().options("/projects");
        assertEquals(200, resp.getStatusCode());
        assertEquals("OPTIONS, GET, HEAD, POST", resp.getHeaders().get("Allow").getValue());
    }

    @Test
    public void givenSpecificProject_whenHead_thenContentTypeAndTransferEncodingPresent() {
        Response resp = given().pathParam("id", projectId).when().head("/projects/{id}");
        assertEquals(200, resp.getStatusCode());
        assertEquals("application/json", resp.getHeaders().get("Content-Type").getValue());
        assertEquals("chunked", resp.getHeaders().get("Transfer-Encoding").getValue());
    }

    @Test
    public void givenSpecificProject_whenOptions_thenAllowHeaderListsSupportedVerbs() {
        Response resp = given().pathParam("id", projectId).when().options("/projects/{id}");
        assertEquals(200, resp.getStatusCode());
        assertEquals("OPTIONS, GET, HEAD, POST, PUT, DELETE", resp.getHeaders().get("Allow").getValue());
    }

    // --- Update ----------------------------------------------------------------

    @Test
    public void givenExistingProject_whenPostToId_then200AndUpdated() {
        JSONObject updated = newUniqueProject();
        Response resp = given().body(updated.toString()).when().post("/projects/" + projectId);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    public void givenInvalidId_whenPostToId_then404WithClearMessage() {
        int invalid = -1;
        JSONObject updated = new JSONObject().put("title", "updated via POST");
        Response resp = given().body(updated.toString()).when().post("/projects/" + invalid);
        assertEquals(404, resp.getStatusCode());
        assertEquals("[No such project entity instance with GUID or ID " + invalid + " found]",
                resp.jsonPath().getString("errorMessages"));
    }

    @Test
    public void givenExistingProject_whenPutToId_then200AndUpdated() {
        JSONObject updated = newUniqueProject();
        Response resp = given().body(updated.toString()).when().put("/projects/" + projectId);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    public void givenInvalidId_whenPutToId_then404WithClearMessage() {
        int invalid = -1;
        JSONObject updated = new JSONObject().put("title", "updated via PUT");
        Response resp = given().body(updated.toString()).when().put("/projects/" + invalid);
        assertEquals(404, resp.getStatusCode());
        assertEquals("[No such project entity instance with GUID or ID " + invalid + " found]",
                resp.jsonPath().getString("errorMessages"));
    }

    // --- Delete ----------------------------------------------------------------

    @Test
    public void givenCreatedThenDeletedProject_whenDeleteAgain_then404() {
        int id = createAdHocProject(new JSONObject()
                .put("title", "temp")
                .put("completed", false)
                .put("active", false)
                .put("description", "temp project"));

        // First delete
        deleteById(id);

        // Second delete should fail with 404
        Response resp = given().pathParam("id", id).when().delete("/projects/{id}");
        assertEquals(404, resp.getStatusCode());
        assertEquals("[Could not find any instances with projects/" + id + "]",
                resp.jsonPath().getString("errorMessages"));
    }

    @Test
    public void givenInvalidId_whenDelete_then404() {
        int invalid = -1;
        Response resp = given().pathParam("id", invalid).when().delete("/projects/{id}");
        assertEquals(404, resp.getStatusCode());
        assertEquals("[Could not find any instances with projects/" + invalid + "]",
                resp.jsonPath().getString("errorMessages"));
    }

    // ---------- helpers ----------
    private JSONObject buildProject(String title, boolean completed, boolean active, String description) {
        return new JSONObject()
                .put("title", title)
                .put("completed", completed)
                .put("active", active)
                .put("description", description);
    }

    private JSONObject newUniqueProject() {
        return buildProject(
                "Project " + System.currentTimeMillis(),
                false,
                true,
                "Testing Project endpoints"
        );
    }

    private int createAdHocProject(JSONObject body) {
        Response resp = given().body(body.toString()).when().post("/projects");
        assertEquals(201, resp.getStatusCode());
        return resp.jsonPath().getInt("id");
    }

    private void deleteById(int id) {
        Response resp = given().pathParam("id", id).when().delete("/projects/{id}");
        assertEquals(200, resp.getStatusCode());
    }

}
