package DBMS.DB.InnerStructure;

import DBMS.DB.InnerStructure.Indexes.Index;
import DBMS.DB.InnerStructure.Keys.ForeignKey;
import DBMS.DB.InnerStructure.Keys.PrimaryKey;

import java.util.*;

/**
 * Created by dmitriy on 20-Nov-15.
 * Class for tables
 */
public class Table implements Iterator<Tuple>{
    private String name;
    private Map<String, Argument> arguments;
    private ArrayList<Tuple> tuples;
    private PrimaryKey primaryKey;
    private LinkedList<ForeignKey> foreignKeys;
    private Map<String, Index> indexes;
    private int counter;

    public Table(String name) {
        this.name = name;
        arguments = new LinkedHashMap<>();
        tuples = new ArrayList<>();
        foreignKeys = new LinkedList<>();
        indexes = new HashMap<>();
        counter = 0;
    }

    public void addArgument(Argument argument) {
        arguments.put(argument.getName(), argument);
    }

    public void addTuple(Tuple tuple) {
        tuples.add(tuple);
    }

    public String getName() {
        return name;
    }

    public Map<String, Argument> getArguments() {
        return arguments;
    }

    public ArrayList<Tuple> getTuples() {
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

    public int size() {
        return tuples.size();
    }
}
