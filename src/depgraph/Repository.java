package depgraph;

import java.util.List;

/** Supplies direct dependencies without invoking a package manager. */
public interface Repository {
    /** Returns direct dependencies or raises an explicit repository error. */
    List<String> dependencies(String name) throws Exception;
}
