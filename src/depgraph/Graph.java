package depgraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A directed graph: an edge points from a package to its dependency. */
public record Graph(Map<String, List<String>> edges) {
    /** Builds the reachable subgraph using DFS with an explicit stack. */
    public static Graph traverse(String root, Repository repository) throws Exception {
        Map<String, List<String>> edges = new LinkedHashMap<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (edges.containsKey(current)) {
                continue;
            }
            List<String> children = repository.dependencies(current);
            edges.put(current, List.copyOf(children));
            List<String> reversed = new ArrayList<>(children);
            java.util.Collections.reverse(reversed);
            for (String child : reversed) {
                if (!edges.containsKey(child)) {
                    stack.push(child);
                }
            }
        }
        return new Graph(edges);
    }

    /** Prints a deterministic adjacency list for the third development stage. */
    public String adjacency() {
        StringBuilder result = new StringBuilder();
        edges.forEach((name, children) -> result.append(name).append(" -> ")
                .append(String.join(" ", children)).append('\n'));
        return result.toString();
    }

    /** Reverses each edge while preserving vertices without incoming edges. */
    public Graph reversed() {
        Map<String, List<String>> reversed = new LinkedHashMap<>();
        edges.keySet().forEach(name -> reversed.put(name, new ArrayList<>()));
        edges.forEach((name, children) -> children.forEach(child ->
                reversed.computeIfAbsent(child, key -> new ArrayList<>()).add(name)));
        return new Graph(reversed);
    }

    /** Finds direct and transitive dependents with the same iterative DFS. */
    public List<String> dependents(String target) throws Exception {
        Graph reversed = reversed();
        if (!reversed.edges.containsKey(target)) {
            throw new IllegalArgumentException("Unknown package: " + target);
        }
        Graph reachable = traverse(target, name -> reversed.edges.get(name));
        return reachable.edges.keySet().stream().filter(name -> !name.equals(target)).toList();
    }
}
