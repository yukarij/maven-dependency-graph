package depgraph;

import java.nio.file.Path;
import java.util.Set;

/** Command-line entry point for variant 26. */
public final class Main {
    private static final int MAX_ARGUMENTS = 2;
    private static final int ERROR_EXIT = 1;
    private static final int NO_ARGUMENTS = 0;

    /** Prevents utility construction. */
    private Main() { }

    /** Reports configuration and input errors without a stack trace. */
    public static void main(String[] arguments) {
        try {
            execute(arguments);
        } catch (Exception exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(ERROR_EXIT);
        }
    }

    /** Runs the selected development stage; settings always come from CSV. */
    public static void execute(String[] arguments) throws Exception {
        if (arguments.length == NO_ARGUMENTS || arguments.length > MAX_ARGUMENTS) {
            throw new IllegalArgumentException("Usage: run CONFIG.csv [stage]");
        }
        Config config = Config.load(Path.of(arguments[0]));
        String stage = arguments.length == MAX_ARGUMENTS ? arguments[1] : "3";
        if (!Set.of("1", "2", "3").contains(stage)) {
            throw new IllegalArgumentException("Stage must be 1, 2 or 3");
        }
        if (stage.equals("1")) {
            printConfig(config);
            return;
        }
        Repository repository = config.testMode() ? new TestRepository(Path.of(config.repository()))
                : new MavenRepository(config.repository());
        if (stage.equals("2")) {
            repository.dependencies(config.packageName()).forEach(System.out::println);
            return;
        }
        System.out.print(Graph.traverse(config.packageName(), repository).adjacency());
    }

    /** Prints each of the four user-configurable parameters. */
    private static void printConfig(Config config) {
        System.out.println("package=" + config.packageName());
        System.out.println("repository=" + config.repository());
        System.out.println("test_mode=" + config.testMode());
        System.out.println("ascii_tree=" + config.asciiTree());
    }
}
