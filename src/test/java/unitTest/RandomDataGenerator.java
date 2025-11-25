package unitTest;

import com.github.javafaker.Faker;
import org.json.JSONObject;

/**
 * Utility class for creating randomized test data objects.
 */
public class RandomDataGenerator {

    private static final Faker FAKER = new Faker();

    /**
     * Builds a random TODO entry with simple generated fields.
     *
     * @return JSONObject representing a fake TODO.
     */
    public static JSONObject createRandomTodo() {
        String title = FAKER.lorem().sentence();
        boolean isDone = FAKER.bool().bool();
        String details = FAKER.lorem().paragraph();

        return new JSONObject()
                .put("title", title)
                .put("doneStatus", isDone)
                .put("description", details);
    }

    /**
     * Creates a random project object with realistic but arbitrary values.
     *
     * @return JSONObject representing a fake project.
     */
    public static JSONObject createRandomProject() {
        String title = FAKER.book().title();
        boolean completed = FAKER.bool().bool();
        boolean active = FAKER.bool().bool();
        String description = FAKER.lorem().paragraph();

        return new JSONObject()
                .put("title", title)
                .put("completed", completed)
                .put("active", active)
                .put("description", description);
    }
}
