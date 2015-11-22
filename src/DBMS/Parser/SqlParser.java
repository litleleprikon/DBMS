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

    private boolean condition(QueryArgument a, QueryArgument b, int condition) {
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
        return query.toUpperCase().equals("AS");
    }

    private boolean isValues(String query) {
        return query.toUpperCase().equals(keywords[17]);
    }


    public ArrayList<Operator> parse(String query) {
        ArrayList<Operator> parsed = new ArrayList<>();
        int priority = 0;
        String[] splittedQuery = query.split(" ");
        Operator operator = new Operator();

        for (int i = 0; i < splittedQuery.length + 1; i++) {
            try {
                if (isCommand(splittedQuery[i])) {
                    if (i != 0) parsed.add(operator);
                    operator = new Operator(splittedQuery[i]);

                    if (isValues(splittedQuery[i])) {
                        operator.setValues(parseValuesQuery(splittedQuery,i));
                        i++;
                        while (!isCommand(splittedQuery[i])) i++;
                    }
                    if (isCommand(splittedQuery[i + 1])) {
                        operator.setType(operator.getType() + " " + splittedQuery[i + 1]);
                        i++;
                    }
                    if (splittedQuery[i + 1].startsWith("(") || splittedQuery[i + 1].equals("("))
                    {
                        operator.setInnerQuery(parseInnerQuery(splittedQuery, i+1));
                    }
                    else {
                        operator.setQueryArguments(parseQueryArguments(splittedQuery, i));
                        i++;
                        while (!isCommand(splittedQuery[i])) i++;
                        i--;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                parsed.add(operator);
            }
        }


        return parsed;
    }


//    public void parseOperator(String operator, LinkedList<String> queryArguments) {
//        System.out.println(operator);
//        for (String queryArgument : queryArguments) {
//            System.out.println(queryArgument);
//        }
//    }

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

                queryArguments.removeLast();
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

    public ArrayList<Operator> parseInnerQuery(String[] query, int i) {
        String innerQuery = "";
        String[] temp = new String[query.length];

        System.arraycopy(query,i,temp,i,query.length-i);

        temp[i] = temp[i].substring(1);
        while (!temp[i].endsWith(")")) {
            innerQuery += temp[i]+" ";
            i++;
        }
        innerQuery += temp[i].substring(0, temp[i].length()-1);
        return parse(innerQuery);
    }

    public ArrayList<Value> parseValuesQuery(String[] query, int i) {
        ArrayList<Value> values = new ArrayList<>();
        i++;

        String valueString = "";

        while (i<query.length&&!isCommand(query[i])) {
            valueString+=query[i];
            i++;
        }

        String[] valuesArray;

        valueString = valueString.replaceAll("[)][,]", ")\0");
        valuesArray = valueString.split("\0");

        for (String value : valuesArray) {
            value=value.substring(1,value.length()-1);
            String[] vals;
            vals=value.split(", ");
            values.add(new Value(vals));
        }

        return values;
    }

//    public void parseQueryArguments(LinkedList<String> QueryArguments, String s) {
//        s = s.replaceAll("([,]|[']|[\"])", " ");
//        s = s.trim();
//        QueryArguments.addLast(s);
//    }


//    public void parseValuesQuery(String values) {
//        String[] query;
//        query = values.split(" ", 2);
//        System.out.println(query[0]);
//        parseValues(query[1]);
//    }

//    public void parseValues(String valueString) {
//        String[] values;
//        valueString = valueString.replaceAll("[)][,] ", ")\0");
//        values = valueString.split("\0");
//        for (String value : values) {
//            System.out.println(value);
//        }
//    }
}
