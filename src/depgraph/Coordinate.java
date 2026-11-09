package depgraph;

public record Coordinate(String group, String artifact, String version) {
    public static final String PATTERN = "[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+";

    public static Coordinate parse(String value) {
        if (!value.matches(PATTERN)) {
            throw new IllegalArgumentException("Expected groupId:artifactId:version: " + value);
        }
        String[] parts = value.split(":");
        return new Coordinate(parts[0], parts[1], parts[2]);
    }

    public String pomPath() {
        return group.replace('.', '/') + "/" + artifact + "/" + version
                + "/" + artifact + "-" + version + ".pom";
    }

    @Override
    public String toString() {
        return group + ":" + artifact + ":" + version;
    }
}
