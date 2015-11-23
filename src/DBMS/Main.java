package DBMS;

import DBMS.DB.Database;
import DBMS.DB.InnerStructure.Table;
import DBMS.Parser.SqlParser;
import DBMS.communication.Connection;
import DBMS.communication.Cursor;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("DBMS started");
        Database db = Database.load("dmd_project");
        while (true) {
            Connection connection = new Connection();
            queriesLoop(connection, db);
            connection.close();
        }
    }

    private static void queriesLoop(Connection connection, Database db) {
        while(true) {
            String query = connection.waitQuery();
            SqlParser parser = new SqlParser(db);
            if(query == null) {
                return;
            }
            System.out.println(query);
            parser.parse(query);
            Table data = db.select(query);
            connection.setDataToCursor(data);
        }
    }
}
