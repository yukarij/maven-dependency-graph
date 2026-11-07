package depgraph;

import java.nio.file.Path;
import java.util.List;

/** Runs dependency-free automated tests with assertions enabled. */
public final class AllTests {
    /** Prevents utility construction. */
    private AllTests() { }

    /** Executes all available test groups. */
    public static void main(String[] arguments) throws Exception {
        testConfiguration();
        System.out.println("PASS: configuration");
    }

    /** Checks CSV quoting, relative paths and Maven coordinate layout. */
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
