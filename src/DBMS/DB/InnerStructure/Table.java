package DBMS.DB.InnerStructure;

import java.util.*;

/**
 * Created by dmitriy on 20-Nov-15.
 * Class for tables
 */
public class Table implements Iterator<Tuple>{
    private String name;
    private Map<String, Argument> arguments;
    private LinkedList<Tuple> tuples;
    private LinkedList<Argument> primaryKeys;
    private LinkedList<Argument> foreignKeys;
    private LinkedList<Index> indexes;
    private int counter;

    public Table(String name) {
        this.name = name;
        arguments = new LinkedHashMap<>();
        tuples = new LinkedList<>();
        foreignKeys = new LinkedList<>();
        primaryKeys = new LinkedList<>();
        counter = 0;
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

    public LinkedList<Tuple> getTuples() {
        return tuples;
    }

    @Override
    public boolean hasNext() {
        return counter < tuples.size()-1;
    }

    @Override
    public Tuple next() {
        return tuples.get(counter++);
    }
}
