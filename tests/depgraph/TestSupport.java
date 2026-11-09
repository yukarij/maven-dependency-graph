package depgraph;

public final class TestSupport {

    private TestSupport() { }

    @FunctionalInterface
    public interface Operation {

        void run() throws Exception;
    }

    public static void fails(Operation operation) throws Exception {
        try {
            operation.run();
        } catch (IllegalArgumentException exception) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    public static void error(Operation operation, String message) throws Exception {
        try {
            operation.run();
        } catch (Exception exception) {
            assert exception.getMessage().contains(message) : exception;
            return;
        }
        throw new AssertionError("Expected error containing: " + message);
    }
}
