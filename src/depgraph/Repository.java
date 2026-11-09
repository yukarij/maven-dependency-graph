package depgraph;

import java.util.List;

@FunctionalInterface
public interface Repository {

    List<String> dependencies(String name) throws Exception;
}
