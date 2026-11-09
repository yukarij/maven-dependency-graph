package depgraph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GraphTests {
    private static final int CHAIN_LENGTH = 20000;

    private GraphTests() { }

    public static void run() throws Exception {
        TestRepository repository = new TestRepository(Path.of("examples/repository.csv"));
        Graph diamond = Graph.traverse("A", repository);
        assert new ArrayList<>(diamond.edges().keySet()).equals(List.of("A", "B", "D", "C"));
        assert diamond.edges().get("B").equals(List.of("D"));
        assert diamond.edges().get("C").equals(List.of("D"));
        assert Graph.traverse("F", repository).edges().equals(Map.of("F", List.of("F")));
        assert Graph.traverse("G", repository).edges().keySet().equals(java.util.Set.of("G", "H"));
        assert Graph.traverse("I", repository).edges().equals(Map.of("I", List.of()));
        TestSupport.fails(() -> Graph.traverse("Z", repository));
        testDeepChain();
    }

    private static void testDeepChain() throws Exception {
        Repository chain = name -> {
            int index = Integer.parseInt(name);
            return index < CHAIN_LENGTH ? List.of(Integer.toString(index + 1)) : List.of();
        };
        Graph graph = Graph.traverse("1", chain);
        assert graph.edges().size() == CHAIN_LENGTH;
    }
}
