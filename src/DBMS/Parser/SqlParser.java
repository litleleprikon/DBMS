package DBMS.Parser;

import java.util.Arrays;
import java.util.LinkedList;

public class SqlParser {

    private final String[] keywords = {
            "SELECT", "FROM",
            "WHERE", "GROUP BY",
            "LIMIT", "OFFSET",
            "LEFT JOIN", "ANY",
            "CREATE TABLE", "ALTER TABLE",
            "INSERT", "UPDATE", "DELETE"
    };

    /**
     * Query parsing into Operator + Parameters
     *
     * @param query - parsed query
     */
    public void parse(String query) {
        String[] splittedQuery = query.split(" ");
        LinkedList<String> arguments = new LinkedList<>();
        String operator = "";

        for (String s : splittedQuery) {
            if ((Arrays.asList(keywords).contains(s.toUpperCase()))) {
                parseOperator(operator, arguments);
                arguments.clear();
                operator = s.toUpperCase();
            } else {
                parseArguments(arguments, s);
            }
        }

        parseOperator(operator,arguments);


    }

    public void parseOperator(String operator, LinkedList<String> arguments) {
        System.out.println(operator);
        for (String argument : arguments) {
            System.out.println(argument);
        }
    }

    public void parseArguments(LinkedList<String> arguments, String s) {
        s=s.replaceAll("([,]|[']|[\"])"," ");
        s=s.trim();
        arguments.addLast(s);
    }
}
