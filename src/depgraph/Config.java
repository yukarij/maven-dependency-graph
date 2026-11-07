package depgraph;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Validated parameters read exclusively from a CSV configuration. */
public record Config(String packageName, String repository,
                     boolean testMode, boolean asciiTree) {
    private static final int COLUMN_COUNT = 2;
    private static final Set<String> KEYS = Set.of(
            "package", "repository", "test_mode", "ascii_tree");

    /** Loads and validates every required setting. */
    public static Config load(Path file) throws IOException {
        Map<String, String> values = readValues(file);
        if (!values.keySet().equals(KEYS)) {
            throw new IllegalArgumentException("CSV must contain exactly: " + KEYS);
        }
        boolean testMode = flag(values, "test_mode");
        boolean asciiTree = flag(values, "ascii_tree");
        String name = values.get("package");
        String repository = values.get("repository");
        validatePackage(name, testMode);
        repository = validateRepository(repository, testMode, file);
        return new Config(name, repository, testMode, asciiTree);
    }

    /** Reads unique, nonempty key-value records. */
    private static Map<String, String> readValues(Path file) throws IOException {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank()) {
                continue;
            }
            List<String> fields = Csv.parse(line);
            if (fields.size() != COLUMN_COUNT || fields.get(1).isBlank()) {
                throw new IllegalArgumentException("CSV requires nonempty key,value");
            }
            if (values.putIfAbsent(fields.get(0), fields.get(1)) != null) {
                throw new IllegalArgumentException("Duplicate key: " + fields.get(0));
            }
        }
        return values;
    }

    /** Accepts only explicit true and false values. */
    private static boolean flag(Map<String, String> values, String key) {
        String value = values.get(key);
        if (!Set.of("true", "false").contains(value)) {
            throw new IllegalArgumentException(key + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }

    /** Checks uppercase fixture names or fixed Maven coordinates. */
    private static void validatePackage(String name, boolean testMode) {
        String pattern = testMode ? "[A-Z]+" : Coordinate.PATTERN;
        if (!name.matches(pattern)) {
            throw new IllegalArgumentException("Invalid package: " + name);
        }
    }

    /** Resolves fixture paths relative to the CSV and checks HTTP URLs. */
    private static String validateRepository(String value, boolean test, Path file) {
        if (test) {
            Path parent = file.toAbsolutePath().getParent();
            Path repository = parent.resolve(value).normalize();
            if (!Files.isRegularFile(repository) || !Files.isReadable(repository)) {
                throw new IllegalArgumentException("Unreadable repository: " + repository);
            }
            return repository.toString();
        }
        URI uri = URI.create(value);
        if (!Set.of("https", "http").contains(uri.getScheme())
                || uri.getHost() == null || uri.getQuery() != null
                || uri.getFragment() != null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Repository must be an HTTP(S) base URL");
        }
        return value.replaceAll("/+$", "");
    }
}
