package DBMS.Parser;

import java.util.ArrayList;

public class Func {
    private ArrayList<String> arguments = new ArrayList<>();
    private String name;

    public Func() {
    }

    public ArrayList<String> getArguments() {
        return arguments;
    }

    public void addArgument(String argument) {
        arguments.add(argument);
    }

    public void setArguments(ArrayList<String> arguments) {
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
