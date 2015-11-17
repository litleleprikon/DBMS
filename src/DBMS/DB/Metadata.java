package DBMS.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Metadata {
    Map<String, TreeSet<Argument>> tableStructure;
    Map<String, IndexType> indexType;
    Map<String, TreeSet<Integer>> tabIndPages;

    enum IndexType {
        Tree, Hash
    }

    public Metadata() {
        tableStructure = new HashMap<>();
        tabIndPages = new HashMap<>();
        indexType = new HashMap<>();
    }

    public void addArgument(String table, Argument argument) {
        if (!tableStructure.containsKey(table)) tableStructure.put(table, new TreeSet<>());

        tableStructure.get(table).add(argument);
    }

    public void addPage(String to, int page) {
        if (!tabIndPages.containsKey(to)) tableStructure.put(to, new TreeSet<>());

        tabIndPages.get(to).add(page);
    }

    public void addIndexType(String index, IndexType type) {
        indexType.put(index, type);
    }

    public void removePage(String from, int page) {
        if (!tabIndPages.containsKey(from)) return;

        tabIndPages.get(from).remove(page);
    }
}
