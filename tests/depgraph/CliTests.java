package depgraph;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CliTests {

    private CliTests() { }

    public static void run() throws Exception {
        verifyIntermediateStages();
        verifyFinalOutput();
        verifyAsciiDisabled();
    }

    private static void verifyIntermediateStages() throws Exception {
        String first = capture("examples/test.csv", "1");
        assert first.contains("package=A") && first.contains("test_mode=true");
        assert capture("examples/test.csv", "2").replace("\r", "").equals("B\nC\n");
        assert capture("examples/test.csv", "3").startsWith("A -> B C\n");
        assert capture("examples/reverse.csv", "4").replace("\r", "").equals("B\nA\nE\nC\n");
    }

    private static void verifyFinalOutput() throws Exception {
        String finalOutput = capture("examples/test.csv");
        assert finalOutput.startsWith("@startuml\n");
        assert finalOutput.contains("[shared]");
        assert !finalOutput.contains("test_mode=");
    }

    private static void verifyAsciiDisabled() throws Exception {
        Path config = Path.of("build", "no-ascii.csv");
        String repository = Path.of("examples", "repository.csv").toAbsolutePath().toString();
        Files.writeString(config, "package,A\nrepository," + repository
                + "\ntest_mode,true\nascii_tree,false\n");
        String diagramOnly = capture(config.toString());
        assert diagramOnly.endsWith("@enduml\n");
        assert !diagramOnly.contains("[shared]");
    }

    private static String capture(String... arguments) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(stream);
            Main.execute(arguments);
        } finally {
            System.setOut(original);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
