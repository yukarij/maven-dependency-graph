package depgraph;

import java.nio.file.Path;

public final class Stages {

    private Stages() { }

    public static void run(Config config, String stage) throws Exception {
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
        if (stage.equals("4")) {
            Graph universe = repository instanceof TestRepository test
                    ? new Graph(test.all()) : Graph.traverse(config.packageName(), repository);
            universe.dependents(config.packageName()).forEach(System.out::println);
            return;
        }
        Graph graph = Graph.traverse(config.packageName(), repository);
        if (stage.equals("3")) {
            System.out.print(graph.adjacency());
        } else {
            System.out.print(Render.plantUml(graph));
            if (config.asciiTree()) {
                System.out.print(Render.ascii(graph, config.packageName()));
            }
        }
    }

    private static void printConfig(Config config) {
        System.out.println("package=" + config.packageName());
        System.out.println("repository=" + config.repository());
        System.out.println("test_mode=" + config.testMode());
        System.out.println("ascii_tree=" + config.asciiTree());
    }
}
