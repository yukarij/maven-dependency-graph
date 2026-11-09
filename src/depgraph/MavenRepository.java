package depgraph;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

public final class MavenRepository implements Repository {
    private static final int HTTP_OK = 200;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private final String baseUrl;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NORMAL).build();
    private final Map<Coordinate, PomModel> models = new HashMap<>();
    private final Map<Coordinate, Element> documents = new HashMap<>();

    public MavenRepository(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public java.util.List<String> dependencies(String name) throws Exception {
        Coordinate coordinate = Coordinate.parse(name);
        resolve(coordinate);
        return models.get(coordinate).direct();
    }

    private void resolve(Coordinate root) throws Exception {
        Deque<Coordinate> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Coordinate current = stack.peek();
            if (models.containsKey(current)) {
                stack.pop();
                continue;
            }
            Element document = document(current);
            Coordinate parent = parent(document);
            if (parent != null && schedule(parent, stack)) {
                continue;
            }
            PomModel model = PomModel.read(document, current, models.get(parent));
            boolean pending = false;
            for (Coordinate imported : model.imports()) {
                if (schedule(imported, stack)) {
                    pending = true;
                    break;
                }
            }
            if (!pending) {
                models.put(current, model.withImports(models));
                stack.pop();
            }
        }
    }

    private boolean schedule(Coordinate coordinate, Deque<Coordinate> stack) {
        if (models.containsKey(coordinate)) {
            return false;
        }
        if (stack.contains(coordinate)) {
            throw new IllegalArgumentException("Cyclic POM inheritance/import: " + coordinate);
        }
        stack.push(coordinate);
        return true;
    }

    private Coordinate parent(Element document) {
        Element element = Xml.child(document, "parent");
        if (element == null) {
            return null;
        }
        return Coordinate.parse(Xml.text(element, "groupId") + ":"
                + Xml.text(element, "artifactId") + ":" + Xml.text(element, "version"));
    }

    private Element document(Coordinate coordinate) throws Exception {
        if (!documents.containsKey(coordinate)) {
            URI uri = URI.create(baseUrl + "/" + coordinate.pomPath());
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(TIMEOUT).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != HTTP_OK) {
                throw new IOException("POM HTTP " + response.statusCode() + ": " + uri);
            }
            documents.put(coordinate, Xml.parse(response.body()));
        }
        return documents.get(coordinate);
    }
}
