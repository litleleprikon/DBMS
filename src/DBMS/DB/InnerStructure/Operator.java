package DBMS.DB.InnerStructure;

import java.util.ArrayList;
import java.util.LinkedList;

public class Operator {
    private String type;
    private LinkedList<Argument> arguments;
    private ArrayList<Operator> innerQuery;
    private int priority;

    public Operator(String type, int priority) {
        this.type = type;
        this.priority = priority;
    }

    public Operator(String type) {
        this.type=type;
    }

    public Operator() {
    }

    public void addArgument(Argument argument) {
        arguments.addLast(argument);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LinkedList<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(LinkedList<Argument> arguments) {
        this.arguments = arguments;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }

    public void setInnerQuery(ArrayList<Operator> innerQuery) {
        this.innerQuery = innerQuery;
    }


}
