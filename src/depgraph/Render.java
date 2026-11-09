package depgraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Render {
    private static final int LAST_OFFSET = 1;

    private record Frame(String name, String prefix, boolean last, boolean exit) { }

    private Render() { }

    public static String plantUml(Graph graph) {
        StringBuilder result = new StringBuilder("@startuml\nleft to right direction\n");
        Map<String, String> aliases = new LinkedHashMap<>();
        for (String name : graph.edges().keySet()) {
            String alias = "node" + aliases.size();
            aliases.put(name, alias);
            result.append("rectangle \"").append(name).append("\" as ").append(alias).append('\n');
        }
        graph.edges().forEach((parent, children) -> children.forEach(child ->
                result.append(aliases.get(parent)).append(" --> ").append(aliases.get(child)).append('\n')));
        return result.append("@enduml\n").toString();
    }

    public static String ascii(Graph graph, String root) {
        StringBuilder result = new StringBuilder(root + "\n");
        Set<String> active = new HashSet<>();
        Set<String> expanded = new HashSet<>();
        Deque<Frame> stack = new ArrayDeque<>();
        active.add(root);
        expanded.add(root);
        pushChildren(stack, graph, root, "");
        while (!stack.isEmpty()) {
            Frame frame = stack.pop();
            if (frame.exit) {
                active.remove(frame.name);
            } else {
                appendNode(result, frame, active, expanded);
                if (!active.contains(frame.name) && expanded.add(frame.name)) {
                    active.add(frame.name);
                    stack.push(new Frame(frame.name, "", false, true));
                    String prefix = frame.prefix + (frame.last ? "    " : "|   ");
                    pushChildren(stack, graph, frame.name, prefix);
                }
            }
        }
        return result.toString();
    }

    private static void appendNode(StringBuilder result, Frame frame,
                                   Set<String> active, Set<String> expanded) {
        result.append(frame.prefix).append(frame.last ? "\\-- " : "+-- ").append(frame.name);
        if (active.contains(frame.name)) {
            result.append(" [cycle]");
        } else if (expanded.contains(frame.name)) {
            result.append(" [shared]");
        }
        result.append('\n');
    }

    private static void pushChildren(Deque<Frame> stack, Graph graph, String name, String prefix) {
        List<String> children = graph.edges().get(name);
        List<Frame> frames = new ArrayList<>();
        for (String child : children) {
            boolean last = frames.size() == children.size() - LAST_OFFSET;
            frames.add(new Frame(child, prefix, last, false));
        }
        java.util.Collections.reverse(frames);
        frames.forEach(stack::push);
    }
}
