package depgraph;

import java.nio.file.Path;

/** Command-line entry point for variant 26. */
public final class Main {
    private static final int MAX_ARGUMENTS = 2;
    private static final int ERROR_EXIT = 1;

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

    /** Loads the CSV and prints the stage-one key-value representation. */
    public static void execute(String[] arguments) throws Exception {
        if (arguments.length == 0 || arguments.length > MAX_ARGUMENTS) {
            throw new IllegalArgumentException("Usage: run CONFIG.csv [stage]");
        }
        Config config = Config.load(Path.of(arguments[0]));
        printConfig(config);
    }

    /** Prints each of the four user-configurable parameters. */
    private static void printConfig(Config config) {
        System.out.println("package=" + config.packageName());
        System.out.println("repository=" + config.repository());
        System.out.println("test_mode=" + config.testMode());
        System.out.println("ascii_tree=" + config.asciiTree());
    }
}
