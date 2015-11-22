package DBMS.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class SqlParser {

    private final String[] keywords = {
            "SELECT", "FROM", "WHERE", "GROUP", "BY",
            "ORDER", "LIMIT", "OFFSET", "LEFT", "JOIN",
            "ANY", "CREATE", "TABLE", "ALTER", "INSERT",
            "UPDATE", "DELETE", "VALUES", "INTO", "ON"
    };

    private final String[] conditions = {
            "=", "<", ">", "<=", ">="
    };

    private boolean isCommand(String query) {
        return Arrays.asList(keywords).contains(query.toUpperCase());
    }

    private boolean isAs(String query) {
        return query.toUpperCase().equals("AS");
    }

    private boolean isValues(String query) {
        return query.toUpperCase().equals(keywords[17]);
    }


    public ArrayList<Operator> parse(String query) {

        ArrayList<Operator> parsed = new ArrayList<>();
        String[] splittedQuery = query.split(" ");
        Operator operator = new Operator();

        for (int i = 0; i < splittedQuery.length + 1; i++) {
            try {
                if (isCommand(splittedQuery[i])) {
                    if (i != 0) parsed.add(operator);
                    operator = new Operator(splittedQuery[i]);

                    if (isValues(splittedQuery[i])) {
                        operator.setValues(parseValuesQuery(splittedQuery, i));
                        i++;
                        while (!isCommand(splittedQuery[i])) {
                            i++;
                        }
                    }

                    if (isCommand(splittedQuery[i + 1])) {
                        operator.setType(operator.getType() + " " + splittedQuery[i + 1]);
                        i++;
                    }

                    if (splittedQuery[i + 1].startsWith("(") || splittedQuery[i + 1].equals("(")) {
                        operator.setInnerQuery(parseInnerQuery(splittedQuery, i + 1));
                    } else {
                        operator.setQueryArguments(parseQueryArguments(splittedQuery, i));
                        i++;
                        while (!isCommand(splittedQuery[i])) {
                            i++;
                        }
                        i--;
                    }
                }
                if (isAs(splittedQuery[i])) {
                    parseAs(operator,splittedQuery,i);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                parsed.add(operator);
            }
        }

        return parsed;
    }


    public LinkedList<QueryArgument> parseQueryArguments(String[] splittedQuery, int i) {
        LinkedList<QueryArgument> queryArguments = new LinkedList<>();
        i += 1;
        QueryArgument queryArgument;

        while (i < splittedQuery.length && !isCommand(splittedQuery[i])) {
            splittedQuery[i] = splittedQuery[i].replaceAll("([,]|[']|[\"])", " ").trim();

            if (Arrays.asList(conditions).contains(splittedQuery[i])) {

                queryArguments.removeLast();
                queryArgument = new QueryArgument(splittedQuery[i - 1]);
                queryArgument.setCompare(new QueryArgument(splittedQuery[i + 1]));
                queryArgument.setCondition(splittedQuery[i]);
                i++;

            } else if (isAs(splittedQuery[i])) {
                if (queryArguments.getLast()!=null) queryArguments.removeLast();
                queryArgument = new QueryArgument(splittedQuery[i - 1]);
                queryArgument.setAlias(splittedQuery[i + 1]);
                i++;

            } else {

                queryArgument = new QueryArgument(splittedQuery[i]);

            }

            queryArguments.addLast(queryArgument);
            i++;
        }
        return queryArguments;
    }

    public void parseAs (Operator operator, String[] query, int i) {
        if (query[i+1].contains("(")&&!query[i+1].startsWith("(")) {
            operator.setFunc(parseFunc(query,i+1));
        }
    }

    public ArrayList<Operator> parseInnerQuery(String[] query, int i) {
        String innerQuery = "";
        String[] temp = new String[query.length];

        System.arraycopy(query, i, temp, i, query.length - i);

        temp[i] = temp[i].substring(1);
        while (!temp[i].endsWith(")")) {
            innerQuery += temp[i] + " ";
            i++;
        }
        innerQuery += temp[i].substring(0, temp[i].length() - 1);
        return parse(innerQuery);
    }

    public ArrayList<Value> parseValuesQuery(String[] query, int i) {
        ArrayList<Value> values = new ArrayList<>();
        i++;

        String valueString = "";

        while (i < query.length && !isCommand(query[i])) {
            valueString += query[i];
            i++;
        }

        String[] valuesArray;

        valueString = valueString.replaceAll("[)][,]", ")\0");
        valuesArray = valueString.split("\0");

        for (String value : valuesArray) {
            value = value.substring(1, value.length() - 1);
            String[] vals;
            vals = value.split(", ");
            values.add(new Value(vals));
        }

        return values;
    }

    public Func parseFunc(String[] query, int i) {
        Func func = new Func();

        if (query[i].contains("(")&&!query[i].startsWith("(")) {
            func.setName(query[i].substring(0,query[i].indexOf("(")));
            func.addArgument(query[i].substring(query[i].indexOf("(")+1,query[i].length()-1));
            i++;
        }

        String arg;

        while (!query[i].endsWith(")")) {
            arg = query[i].replaceAll("([,]|[']|[\"])", " ").trim();
            func.addArgument(arg);
            i++;
        }

        arg = query[i].replaceAll("([,]|[']|[\"]|[)])", " ").trim();
        func.addArgument(arg);

        return func;
    }

}
