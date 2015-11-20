package DBMS.DB.InnerStructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by dmitriy on 20-Nov-15.
 */
public class Table {
    private String name;
    private ArrayList<Argument> arguments;
    private Set<Tuple> tuples;
    private LinkedList<Argument> primaryKeys;
    private LinkedList<Argument> foreignKeys;
    private LinkedList<Index> indexes;

    public Table(String name) {
        this.name = name;
        arguments = new ArrayList<>();
        tuples = new HashSet<>();
        foreignKeys = new LinkedList<>();
        primaryKeys = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public Set<Tuple> getTuples() {
        return tuples;
    }
}
