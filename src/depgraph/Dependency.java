package depgraph;

import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;

/** Dependency declaration before conversion to fixed coordinates. */
public record Dependency(String group, String artifact, String version,
                         String scope, String type, String optional) {
    private static final Set<String> SCOPES = Set.of("", "compile", "runtime");

    /** Reads a declaration and expands its property references. */
    public static Dependency read(Element element, Map<String, String> properties) {
        return new Dependency(value(element, "groupId", properties),
                value(element, "artifactId", properties), value(element, "version", properties),
                value(element, "scope", properties), value(element, "type", properties),
                value(element, "optional", properties));
    }

    /** Resolves one XML field through the effective property map. */
    private static String value(Element element, String name, Map<String, String> properties) {
        return PomModel.expand(Xml.text(element, name), properties);
    }

    /** Returns the management key, including Maven dependency type. */
    public String key() {
        return group + ":" + artifact + ":" + (type.isEmpty() ? "jar" : type);
    }

    /** Identifies imported dependency-management BOMs. */
    public boolean isImport() {
        return scope.equals("import") && type.equals("pom");
    }

    /** Selects nonoptional compile/runtime dependencies for the graph. */
    public boolean included() {
        return SCOPES.contains(scope) && !optional.equals("true");
    }

    /** Supplies omitted fields from dependencyManagement. */
    public Dependency managed(Map<String, Dependency> management) {
        Dependency base = management.get(key());
        if (base == null) {
            return this;
        }
        return new Dependency(group, artifact, fallback(version, base.version),
                fallback(scope, base.scope), type, fallback(optional, base.optional));
    }

    /** Chooses the explicit value before its managed default. */
    private static String fallback(String value, String defaultValue) {
        return value.isEmpty() ? defaultValue : value;
    }

    /** Requires a resolved, fixed Maven version. */
    public Coordinate coordinate() {
        return Coordinate.parse(group + ":" + artifact + ":" + version);
    }
}
