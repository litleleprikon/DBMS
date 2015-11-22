package DBMS;

import DBMS.Parser.Operator;
import DBMS.Parser.QueryArgument;
import DBMS.Parser.SqlParser;
import DBMS.Parser.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class Main {


    public static void main(String[] args) throws IOException {

        SqlParser parser = new SqlParser();
        ArrayList<Operator> operators;

        operators=parser.parse("SELECT p.id, p.title, p.abstract, pt.name AS p_type FROM project.publications AS p" +
                " LEFT JOIN project.publication_type as pt ON p.type = pt.id" +
                " LEFT JOIN (VALUES (0, 1), (1, 2), (2, 3), (3, 4)) as x(id, ordering) ON p.id = x.id" +
                " WHERE p.id = ANY(%s)" +
                " ORDER BY x.ordering");


        for (Operator operator : operators) {
            System.out.println(operator.getType());
            if (operator.getValues()!=null) {
                for (Value value : operator.getValues()) {
                    System.out.println(Arrays.toString(value.getValues()));
                }
            }
            if (operator.getInnerQuery()!=null) {
                for (Operator inner : operator.getInnerQuery()) {
                    System.out.println(inner.getType());
                }
            }
            if (operator.getArguments()!=null) {
                for (QueryArgument argument : operator.getArguments()) {
                    if (argument.getAlias()!=null) System.out.println(argument.getData()+" AS "+argument.getAlias());
                    else if (argument.getCondition()!=null) System.out.println(argument.getData()+" "+argument.getCondition()+" "+argument.getCompare().getData());
                    else System.out.println(argument.getData());
                }
            }
        }
    }
}
