package depgraph;

import java.nio.file.Path;
import java.util.List;

/** Reverse traversal checks over the complete finite test repository. */
public final class ReverseTests {
    /** Prevents utility construction. */
    private ReverseTests() { }

    /** Covers external parents, transitive dependents, cycles and isolated nodes. */
    public static void run() throws Exception {
        Graph graph = new Graph(new TestRepository(Path.of("examples/repository.csv")).all());
        assert graph.dependents("A").equals(List.of("E"));
        assert graph.dependents("D").equals(List.of("B", "A", "E", "C"));
        assert graph.dependents("G").equals(List.of("H"));
        assert graph.dependents("F").isEmpty();
        assert graph.dependents("I").isEmpty();
        TestSupport.fails(() -> graph.dependents("Z"));
    }
}
