package depgraph;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HttpTests {
    private static final int RANDOM_PORT = 0;
    private static final int HTTP_OK = 200;
    private static final int HTTP_NOT_FOUND = 404;
    private static final String OPEN = "<project xmlns='http://maven.apache.org/POM/4.0.0'>";
    private static final String CLOSE = "</project>";

    private HttpTests() { }

    public static void run() throws Exception {
        Map<String, String> files = fixtures();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", RANDOM_PORT), RANDOM_PORT);
        server.createContext("/", exchange -> {
            String content = files.get(exchange.getRequestURI().getPath());
            byte[] bytes = (content == null ? "Not found" : content).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(content == null ? HTTP_NOT_FOUND : HTTP_OK, bytes.length);
            try (var body = exchange.getResponseBody()) {
                body.write(bytes);
            }
        });
        server.start();
        try {
            MavenRepository repository = new MavenRepository("http://127.0.0.1:" + server.getAddress().getPort());
            assert repository.dependencies("g:child:1").equals(List.of("g:base:1", "g:leaf:2", "g:other:3"));
            TestSupport.error(() -> repository.dependencies("g:absent:1"), "HTTP 404");
            TestSupport.error(() -> repository.dependencies("g:broken:1"), "XML");
            TestSupport.error(() -> repository.dependencies("g:cycle:1"), "Cyclic POM");
            TestSupport.error(() -> repository.dependencies("g:xxe:1"), "DOCTYPE");
        } finally {
            server.stop(RANDOM_PORT);
        }
    }

    private static Map<String, String> fixtures() {
        Map<String, String> files = new HashMap<>();
        String parent = "<parent><groupId>g</groupId><artifactId>parent</artifactId><version>1</version></parent>";
        files.put(path("parent"), OPEN + "<properties><other.version>3</other.version></properties>"
                + "<dependencies>" + dependency("base", "1") + "</dependencies>" + CLOSE);
        String bom = dependency("bom", "1").replace("</dependency>",
                "<scope>import</scope><type>pom</type></dependency>");
        files.put(path("child"), OPEN + parent + "<dependencyManagement><dependencies>" + bom
                + "</dependencies></dependencyManagement><dependencies>" + dependency("leaf", "")
                + dependency("other", "${other.version}") + "</dependencies>" + CLOSE);
        files.put(path("bom"), OPEN + "<dependencyManagement><dependencies>" + dependency("leaf", "2")
                + "</dependencies></dependencyManagement>" + CLOSE);
        files.put(path("broken"), "<project>invalid XML");
        files.put(path("cycle"), OPEN + parent.replace("parent</", "cycle</") + CLOSE);
        files.put(path("xxe"), "<!DOCTYPE project SYSTEM 'file:///absent'>" + OPEN + CLOSE);
        return files;
    }

    private static String dependency(String name, String version) {
        String versionXml = version.isEmpty() ? "" : "<version>" + version + "</version>";
        return "<dependency><groupId>g</groupId><artifactId>" + name + "</artifactId>"
                + versionXml + "</dependency>";
    }

    private static String path(String name) {
        return "/g/" + name + "/1/" + name + "-1.pom";
    }
}
