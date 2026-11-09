package depgraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public record PomModel(Map<String, String> properties,
                       Map<String, Dependency> management, List<Dependency> dependencies) {
    private static final Pattern PROPERTY = Pattern.compile("\\$\\{([^}]+)}");
    private static final int EXPANSION_LIMIT = 100;

    public static PomModel read(Element root, Coordinate coordinate, PomModel parent) {
        Map<String, String> properties = new LinkedHashMap<>();
        Map<String, Dependency> management = new LinkedHashMap<>();
        Map<String, Dependency> dependencies = new LinkedHashMap<>();
        if (parent != null) {
            properties.putAll(parent.properties);
            management.putAll(parent.management);
            parent.dependencies.forEach(item -> dependencies.put(item.key(), item));
        }
        addProperties(root, properties);
        properties.put("project.groupId", coordinate.group());
        properties.put("project.artifactId", coordinate.artifact());
        properties.put("project.version", coordinate.version());
        properties.put("pom.groupId", coordinate.group());
        properties.put("pom.artifactId", coordinate.artifact());
        properties.put("pom.version", coordinate.version());
        Element parentElement = Xml.child(root, "parent");
        for (String field : List.of("groupId", "artifactId", "version")) {
            properties.put("project.parent." + field, Xml.text(parentElement, field));
        }
        Element managed = Xml.child(Xml.child(root, "dependencyManagement"), "dependencies");
        readDependencies(managed, properties).forEach(item -> management.put(item.key(), item));
        readDependencies(Xml.child(root, "dependencies"), properties)
                .forEach(item -> dependencies.put(item.key(), item));
        return new PomModel(properties, management, new ArrayList<>(dependencies.values()));
    }

    private static void addProperties(Element root, Map<String, String> properties) {
        Element element = Xml.child(root, "properties");
        if (element == null) {
            return;
        }
        for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element property) {
                properties.put(property.getLocalName(), property.getTextContent().trim());
            }
        }
    }

    private static List<Dependency> readDependencies(Element element, Map<String, String> properties) {
        return Xml.children(element, "dependency").stream()
                .map(item -> Dependency.read(item, properties)).toList();
    }

    public static String expand(String value, Map<String, String> properties) {
        String result = value;
        for (int attempt = 0; attempt < EXPANSION_LIMIT; attempt++) {
            Matcher matcher = PROPERTY.matcher(result);
            if (!matcher.find()) {
                return result;
            }
            String replacement = properties.get(matcher.group(1));
            if (replacement == null) {
                throw new IllegalArgumentException("Unresolved POM property: " + matcher.group(1));
            }
            result = result.substring(0, matcher.start()) + replacement + result.substring(matcher.end());
        }
        throw new IllegalArgumentException("Cyclic or excessively nested POM property: " + value);
    }

    public List<Coordinate> imports() {
        return management.values().stream().filter(Dependency::isImport)
                .map(Dependency::coordinate).toList();
    }

    public PomModel withImports(Map<Coordinate, PomModel> models) {
        Map<String, Dependency> combined = new LinkedHashMap<>();
        for (Coordinate imported : imports()) {
            models.get(imported).management.forEach(combined::putIfAbsent);
        }
        management.values().stream().filter(item -> !item.isImport())
                .forEach(item -> combined.put(item.key(), item));
        return new PomModel(properties, combined, dependencies);
    }

    public List<String> direct() {
        return dependencies.stream().map(item -> item.managed(management))
                .filter(Dependency::included).map(Dependency::coordinate)
                .map(Coordinate::toString).distinct().toList();
    }
}
