package DBMS;

import DBMS.Parser.SqlParser;
import DBMS.communication.Connection;
import DBMS.communication.Cursor;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("DBMS started");
        while (true) {
            Connection connection = new Connection();
            queriesLoop(connection);
            connection.close();
        }
    }

    private static void queriesLoop(Connection connection) {
        while(true) {
            String query = connection.waitQuery();
            SqlParser parser = new SqlParser();
            if(query == null) {
                return;
            }
            System.out.println(query);
            parser.parse(query);
        }
    }
}
