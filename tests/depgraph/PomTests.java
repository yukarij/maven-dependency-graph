package depgraph;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Checks direct dependency parsing independently of remote repositories. */
public final class PomTests {
    /** Prevents utility construction. */
    private PomTests() { }

    /** Verifies namespaces, properties, scopes, management and XML isolation. */
    public static void run() throws Exception {
        String xml = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <properties><dep.version>2.0</dep.version></properties>
                  <dependencyManagement><dependencies><dependency>
                    <groupId>g</groupId><artifactId>b</artifactId>
                    <version>${dep.version}</version>
                  </dependency></dependencies></dependencyManagement>
                  <dependencies>
                    <dependency><groupId>g</groupId><artifactId>b</artifactId></dependency>
                    <dependency><groupId>g</groupId><artifactId>t</artifactId>
                      <version>1</version><scope>test</scope></dependency>
                    <dependency><groupId>g</groupId><artifactId>o</artifactId>
                      <version>1</version><optional>true</optional></dependency>
                  </dependencies>
                  <build><plugins><plugin><dependencies><dependency>
                    <groupId>ignored</groupId><artifactId>plugin</artifactId><version>1</version>
                  </dependency></dependencies></plugin></plugins></build>
                </project>
                """;
        PomModel model = PomModel.read(Xml.parse(xml.getBytes(StandardCharsets.UTF_8)),
                Coordinate.parse("g:a:1"), null);
        assert model.direct().equals(List.of("g:b:2.0"));
        assert PomModel.expand("${a}", Map.of("a", "${b}", "b", "value")).equals("value");
        TestSupport.fails(() -> PomModel.expand("${a}", Map.of("a", "${a}")));
        TestSupport.fails(() -> PomModel.expand("${missing}", Map.of()));
    }
}
