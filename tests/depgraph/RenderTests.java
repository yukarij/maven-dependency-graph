package depgraph;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class RenderTests {

    private RenderTests() { }

    public static void run() throws Exception {
        TestRepository repository = new TestRepository(Path.of("examples/repository.csv"));
        Graph graph = Graph.traverse("A", repository);
        String expected = "A\n+-- B\n|   \\-- D\n\\-- C\n    \\-- D [shared]\n";
        assert Render.ascii(graph, "A").equals(expected);
        assert Render.ascii(Graph.traverse("G", repository), "G")
                .equals("G\n\\-- H\n    \\-- G [cycle]\n");
        assert Render.ascii(Graph.traverse("F", repository), "F")
                .equals("F\n\\-- F [cycle]\n");
        assert Render.ascii(Graph.traverse("I", repository), "I").equals("I\n");
        String diagram = Render.plantUml(new Graph(Map.of("I", List.of())));
        assert diagram.equals("@startuml\nleft to right direction\nrectangle \"I\" as node0\n@enduml\n");
        assert Render.plantUml(graph).contains("node0 --> node1\n");
    }
}
