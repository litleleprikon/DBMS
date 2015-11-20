package DBMS;

import DBMS.DB.Types.VarChar;
import DBMS.Parser.SqlParser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

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
        System.out.println();
    }
}
