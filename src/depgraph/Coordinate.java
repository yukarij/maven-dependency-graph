package depgraph;

/** Maven groupId:artifactId:version with a fixed version. */
public record Coordinate(String group, String artifact, String version) {
    public static final String PATTERN = "[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+";

    /** Validates and splits a coordinate. */
    public static Coordinate parse(String value) {
        if (!value.matches(PATTERN)) {
            throw new IllegalArgumentException("Expected groupId:artifactId:version: " + value);
        }
        String[] parts = value.split(":");
        return new Coordinate(parts[0], parts[1], parts[2]);
    }

    /** Returns the relative POM path defined by the Maven repository layout. */
    public String pomPath() {
        return group.replace('.', '/') + "/" + artifact + "/" + version
                + "/" + artifact + "-" + version + ".pom";
    }

    /** Returns canonical Maven coordinates. */
    @Override
    public String toString() {
        return group + ":" + artifact + ":" + version;
    }
}
