package depgraph;

import java.nio.file.Path;
import java.util.Set;

public final class Main {
    private static final int MAX_ARGUMENTS = 2;
    private static final int ERROR_EXIT = 1;
    private static final int NO_ARGUMENTS = 0;

    private Main() { }

    public static void main(String[] arguments) {
        try {
            execute(arguments);
        } catch (Exception exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(ERROR_EXIT);
        }
    }

    public static void execute(String[] arguments) throws Exception {
        if (arguments.length == NO_ARGUMENTS || arguments.length > MAX_ARGUMENTS) {
            throw new IllegalArgumentException("Usage: run CONFIG.csv [stage]");
        }
        Config config = Config.load(Path.of(arguments[0]));
        String stage = arguments.length == MAX_ARGUMENTS ? arguments[1] : "5";
        if (!Set.of("1", "2", "3", "4", "5").contains(stage)) {
            throw new IllegalArgumentException("Stage must be 1, 2, 3, 4 or 5");
        }
        Stages.run(config, stage);
    }
}
