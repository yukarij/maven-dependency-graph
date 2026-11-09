package depgraph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ConfigTests {
    private static final Path DIRECTORY = Path.of("build", "config-tests");
    private static final String VALID = "package,A\nrepository,repo.csv\ntest_mode,true\nascii_tree,true\n";

    private ConfigTests() { }

    public static void run() throws Exception {
        Files.createDirectories(DIRECTORY);
        Files.writeString(DIRECTORY.resolve("repo.csv"), "package,dependencies\nA,\n");
        List<String> invalid = List.of(
                VALID.replace("package,A", "package,lower"),
                VALID.replace("package,A", "package,"),
                VALID.replace("repo.csv", "missing.csv"),
                VALID.replace("test_mode,true", "test_mode,yes"),
                VALID.replace("ascii_tree,true", "ascii_tree,1"),
                VALID.replace("ascii_tree,true\n", ""),
                VALID + "other,value\n", VALID + "package,B\n",
                VALID.replace("repository,repo.csv", "repository,a,b"),
                VALID.replace("repository,repo.csv", "repository,\"broken"),
                VALID.replace("test_mode,true", "test_mode,false"),
                "package,g:a:1\nrepository,ftp://example.org\ntest_mode,false\nascii_tree,false\n",
                "package,g:a:1\nrepository,relative/path\ntest_mode,false\nascii_tree,false\n");
        for (String content : invalid) {
            Path file = DIRECTORY.resolve("invalid.csv");
            Files.writeString(file, content);
            TestSupport.fails(() -> Config.load(file));
        }
        testQuotedPath();
        testBadRepositories();
        TestSupport.fails(() -> Main.execute(new String[] {"examples/test.csv", "9"}));
        TestSupport.fails(() -> Main.execute(new String[] {}));
    }

    private static void testQuotedPath() throws Exception {
        Files.writeString(DIRECTORY.resolve("repo,one.csv"), "package,dependencies\nA,\n");
        Path file = DIRECTORY.resolve("quoted.csv");
        Files.writeString(file, VALID.replace("repo.csv", "\"repo,one.csv\""));
        assert Config.load(file).repository().endsWith("repo,one.csv");
    }

    private static void testBadRepositories() throws Exception {
        List<String> invalid = List.of("", "wrong,header\nA,\n", "package,dependencies\na,\n",
                "package,dependencies\nA,B\n", "package,dependencies\nA,\nA,\n",
                "package,dependencies\nA,lower\n");
        for (String content : invalid) {
            Path file = DIRECTORY.resolve("bad-repository.csv");
            Files.writeString(file, content);
            TestSupport.fails(() -> new TestRepository(file));
        }
    }
}
