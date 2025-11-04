package storyTest;

import io.cucumber.core.cli.Main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.platform.suite.api.*;
import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "storyTest")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-report.html")
public class TestRunner {
    public static void main(String[] args) throws Exception {
        final long seed = 12345L;                 // deterministic shuffle
        final Random rng = new Random(seed);

        // Collect all .feature files under src/test/resources/features
        List<String> features = Files.walk(Paths.get("src/test/resources/features"))
                .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".feature"))
                .map(p -> p.toAbsolutePath().toString())
                .collect(Collectors.toList());

        // Shuffle feature execution order
        Collections.shuffle(features, rng);
        System.out.println("Feature run order (seed=" + seed + "):");
        features.forEach(System.out::println);

        // Build cucumber CLI args and run
        List<String> cli = new ArrayList<>(Arrays.asList(
                "--glue", "storyTest",
                "--plugin", "pretty"
        ));
        cli.addAll(features);

        Main.main(cli.toArray(new String[0]));
    }
}
