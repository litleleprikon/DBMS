package DBMS.Parser;

import DBMS.DB.InnerStructure.Argument;

import java.util.ArrayList;
import java.util.LinkedList;

public class Operator {
    private String type;
    private LinkedList<QueryArgument> arguments = new LinkedList<>();
    private ArrayList<Operator> innerQuery;

    private ArrayList<Value> values;

    private Func func;

    public Func getFunc() {
        return func;
    }

    public void setFunc(Func func) {
        this.func = func;
    }

    private int priority;

    public void setArguments(LinkedList<QueryArgument> arguments) {
        this.arguments = arguments;
    }

    public ArrayList<Value> getValues() {
        return values;
    }

    public void setValues(ArrayList<Value> values) {
        this.values = values;
    }

    public ArrayList<Operator> getInnerQuery() {
        return innerQuery;
    }

    public Operator(String type, int priority) {
        this.type = type.toUpperCase();
        this.priority = priority;
    }

    public Operator(String type) {
        this.type=type.toUpperCase();
    }

    public Operator() {
    }

    public void addArgument(QueryArgument argument) {
        arguments.addLast(argument);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LinkedList<QueryArgument> getArguments() {
        return arguments;
    }

    public void setQueryArguments(LinkedList<QueryArgument> arguments) {
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
