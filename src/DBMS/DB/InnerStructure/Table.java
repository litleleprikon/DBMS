package DBMS.DB.InnerStructure;

import java.util.*;

/**
 * Created by dmitriy on 20-Nov-15.
 */
public class Table {
    private String name;
    private Map<String, Argument> arguments;
    private Set<Tuple> tuples;
    private LinkedList<Argument> primaryKeys;
    private LinkedList<Argument> foreignKeys;
    private LinkedList<Index> indexes;

    public Table(String name) {
        this.name = name;
        arguments = new LinkedHashMap<>();
        tuples = new HashSet<>();
        foreignKeys = new LinkedList<>();
        primaryKeys = new LinkedList<>();
    }

    public void addArgument(Argument argument) {
        arguments.put(argument.getName(), argument);
    }

    public String getName() {
        return name;
    }

    public Map<String, Argument> getArguments() {
        return arguments;
    }

    public Set<Tuple> getTuples() {
        return tuples;
    }
}
