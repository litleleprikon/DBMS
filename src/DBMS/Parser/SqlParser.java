package DBMS.Parser;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

public class SqlParser {

    private final String[] keywords = {
            "SELECT", "FROM", "WHERE", "GROUP", "BY",
            "ORDER", "LIMIT", "OFFSET", "LEFT", "JOIN",
            "ANY", "CREATE", "TABLE", "ALTER", "INSERT",
            "UPDATE", "DELETE", "AS", "VALUES", "INTO",
            "ON"
    };

    private final String[] equation = {
            "=", "<", ">", "<=", ">="
    };

    private boolean condition(Argument a, Argument b, int condition) {
        int result = a.compareTo(b);
        switch (condition) {
            case 0:
                return result == 0;
            case 1:
                return result < 0;
            case 2:
                return result > 0;
            case 3:
                return (result < 0 || result == 0);
            case 4:
                return (result > 0 || result == 0);
            default:
                return true;
        }
    }

    private boolean isCommand(String query) {
        return Arrays.asList(keywords).contains(query.toUpperCase());
    }

    private boolean isAs(String query) {
        return query.toUpperCase().equals(keywords[17]);
    }


    public ArrayList<Operator> parse(String query,boolean...inner) {
        ArrayList<Operator> parsed = new ArrayList<>();
        int priority = 0;
        String[] splittedQuery = query.split(" ");
        Operator operator = new Operator();

        for (int i = 0; i < splittedQuery.length+1; i++) {
            try {
                if (isCommand(splittedQuery[i])) {
                    parsed.add(operator);
                    operator = new Operator(splittedQuery[i]);
                    if (isCommand(splittedQuery[i+1])) {
                        operator.setType(operator.getType()+splittedQuery[i+1]);
                        i++;
                    }
                    
                }
            }
            catch (ArrayIndexOutOfBoundsException e) {
                // TODO: 22/11/15 Handle exception
            }
        }


        return parsed;
    }


    public void parseOperator(String operator, LinkedList<String> arguments) {
        System.out.println(operator);
        for (String argument : arguments) {
            System.out.println(argument);
        }
    }

    public void parseArguments(LinkedList<String> arguments, String s) {
        s = s.replaceAll("([,]|[']|[\"])", " ");
        s = s.trim();
        arguments.addLast(s);
    }

    public void parseValuesQuery(String values) {
        String[] query;
        query = values.split(" ", 2);
        System.out.println(query[0]);
        parseValues(query[1]);
    }

    public void parseValues(String valueString) {
        String[] values;
        valueString = valueString.replaceAll("[)][,] ", ")\0");
        values = valueString.split("\0");
        for (String value : values) {
            System.out.println(value);
        }
    }
}
