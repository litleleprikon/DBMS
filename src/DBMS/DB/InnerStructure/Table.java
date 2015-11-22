package DBMS.DB.InnerStructure;

import DBMS.DB.InnerStructure.Keys.ForeignKey;
import DBMS.DB.InnerStructure.Keys.Key;
import DBMS.DB.InnerStructure.Keys.PrimaryKey;

import java.util.*;

/**
 * Created by dmitriy on 20-Nov-15.
 */
public class Table {
    private String name;
    private Map<String, Argument> arguments;
    private Set<Tuple> tuples;
    private PrimaryKey primaryKey;
    private LinkedList<ForeignKey> foreignKeys;
    private LinkedList<Index> indexes;

    public Table(String name) {
        this.name = name;
        arguments = new LinkedHashMap<>();
        tuples = new HashSet<>();
        foreignKeys = new LinkedList<>();
    }

    public void addArgument(Argument argument) {
        arguments.put(argument.getName(), argument);
    }

    public void addPrimaryKey(PrimaryKey key) {
        primaryKey = key;
    }

    public void addForeignKey(ForeignKey key) {
        foreignKeys.add(key);
    }

    public String getName() {
        return name;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public LinkedList<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public LinkedList<Index> getIndexes() {
        return indexes;
    }

    public Map<String, Argument> getArguments() {
        return arguments;
    }

    public Set<Tuple> getTuples() {
        return tuples;
    }
}
