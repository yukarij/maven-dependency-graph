package depgraph;

import java.nio.file.Path;
import java.util.List;

public final class AllTests {

    private AllTests() { }

    public static void main(String[] arguments) throws Exception {
        testConfiguration();
        ConfigTests.run();
        PomTests.run();
        HttpTests.run();
        GraphTests.run();
        ReverseTests.run();
        RenderTests.run();
        CliTests.run();
        System.out.println("PASS: 8 test groups (CSV, errors, POM, HTTP, DFS, reverse, rendering, CLI)");
    }

    private static void testConfiguration() throws Exception {
        Config config = Config.load(Path.of("examples/test.csv"));
        assert config.packageName().equals("A");
        assert config.testMode();
        assert config.asciiTree();
        assert Csv.parse("name,\"a,b\"").equals(List.of("name", "a,b"));
        assert Coordinate.parse("g.a:b:1.0").pomPath()
                .equals("g/a/b/1.0/b-1.0.pom");
    }
}
