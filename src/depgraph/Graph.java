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
}
