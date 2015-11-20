package DBMS;

import DBMS.Parser.SqlParser;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
//        VarChar chr = new VarChar(50);
//        chr.setData("azaza");
//        System.out.println(chr.toString());
//        char temp = chr.getData()[38];
//        System.out.println(temp);

        SqlParser parser = new SqlParser();
        parser.parse("SELECT p.id, p.title, p.abstract, pt.name AS p_type from project.publication AS p " +
                "LEFT JOIN project.publication_type as pt ON p.type = pt.id " +
                "ORDER BY p.{0:s} " +
                "LIMIT %(limit)s " +
                "OFFSET %(offset)s");
        parser.parse("SELECT EXIST (SELECT * FROM publications)");

        parser.parse("SELECT p.id, p.title, p.abstract, pt.name AS p_type from project.publications AS p\n" +
                " LEFT JOIN project.publication_type as pt ON p.type = pt.id" +
                " \n" +
                " LEFT JOIN (VALUES (0, 1), (1, 2), (2, 3), (3, 4)) as x(id, ordering) ON p.id = x.id\n" +
                " \n" +
                " WHERE p.id = ANY(%s)\n" +
                " ORDER BY x.ordering");
        System.out.println();
    }
}
