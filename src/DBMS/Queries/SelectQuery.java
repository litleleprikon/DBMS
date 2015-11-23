package DBMS.Queries;

import DBMS.DB.Database;
import DBMS.DB.InnerStructure.Table;
import gudusoft.gsqlparser.TCustomSqlStatement;

/**
 * Created by litleleprikon on 23/11/15.
 */
public class SelectQuery implements SQLQuery{
    private Database db;
    private TCustomSqlStatement statement;

    public SelectQuery(Database db, TCustomSqlStatement statement) {
        this.db = db;
        this.statement = statement;
    }

    public Table execute() {
        return null;
    }
}
