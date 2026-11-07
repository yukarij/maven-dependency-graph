package depgraph;

/** Small assertion helpers; tests require no third-party framework. */
public final class TestSupport {
    /** Prevents utility construction. */
    private TestSupport() { }

    /** Checked operation expected to fail. */
    @FunctionalInterface
    public interface Operation {
        /** Runs the test operation. */
        void run() throws Exception;
    }

    /** Fails unless an input error is raised. */
    public static void fails(Operation operation) throws Exception {
        try {
            operation.run();
        } catch (IllegalArgumentException exception) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
