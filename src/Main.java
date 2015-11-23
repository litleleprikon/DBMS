import DBMS.DB.Database;
import DBMS.DB.InnerStructure.Table;
import DBMS.communication.Connection;
import gudusoft.gsqlparser.TCustomSqlStatement;

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
            TCustomSqlStatement query = connection.waitQuery();
            if(query == null) {
                return;
            }
            System.out.println(query);
            Table data = db.executeQuery(query);
            connection.setDataToCursor(data);
        }
    }
}
