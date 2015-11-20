package DBMS.Parser;

import java.util.Arrays;
import java.util.LinkedList;

public class SqlParser {

    private final String[] keywords = {
            "SELECT", "FROM",
            "WHERE", "GROUP", "BY",
            "ORDER",
            "LIMIT", "OFFSET",
            "LEFT", "JOIN", "ANY",
            "CREATE", "TABLE", "ALTER",
            "INSERT", "UPDATE", "DELETE", "AS", "VALUES", "INTO", "ON"
    };

    private final String[] equation = {
            "=", "<", ">", "<=", ">="
    };

    /**
     * Query parsing into Operator + Parameters
     * ToDo: values and insert parsing
     * @param query - parsed query
     */
    public void parse(String query) {
        String[] splittedQuery = query.split(" ");
        LinkedList<String> arguments = new LinkedList<>();
        String operator = "";

        for (int i = 0; i < splittedQuery.length + 1; i++) {
            try {
                if (splittedQuery[i].startsWith("(") || splittedQuery[i].equals("(")) { //parsing inner query
                    splittedQuery[i] = splittedQuery[i].substring(1);
                    String innerQuery = "";

                    while (!splittedQuery[i].endsWith(")") && !splittedQuery[i].equals(")")) {
                        innerQuery += splittedQuery[i] + " ";
                        i++;
                    }

                    splittedQuery[i] = splittedQuery[i].substring(0, splittedQuery[i].length() - 1);
                    innerQuery += splittedQuery[i];
                    parse(innerQuery);

                } else { //parsing usual query
                    if ((Arrays.asList(keywords).contains(splittedQuery[i].toUpperCase()))) {//parsing command
                        if ((Arrays.asList(keywords).contains(splittedQuery[i + 1].toUpperCase()))) { //parsing two-word command with arguments
                            parseOperator(operator, arguments);
                            arguments.clear();

                            operator = splittedQuery[i].toUpperCase() + " " + splittedQuery[i + 1].toUpperCase();
                            i++;
                        } else { //parsing comand with arguments
                            parseOperator(operator, arguments);
                            arguments.clear();

                            operator = splittedQuery[i].toUpperCase();
                        }
                    } else if ((i < splittedQuery.length - 1)
                            &&
                            (Arrays.asList(equation).contains(splittedQuery[i + 1]))) { //parsing equation operator
                        parseArguments(arguments, splittedQuery[i] + " " + splittedQuery[i + 1] + " " + splittedQuery[i + 2]);

                        i += 2;
                    } else {
                        parseArguments(arguments, splittedQuery[i]);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                parseOperator(operator, arguments);
            }
        }
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
}
