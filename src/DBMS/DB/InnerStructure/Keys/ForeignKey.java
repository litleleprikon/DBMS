package DBMS.DB.InnerStructure.Keys;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Table;

import java.util.LinkedList;

/**
 * Created by dmitriy on 20-Nov-15.
 */
public class ForeignKey extends Key{
    protected Table foreignTable;


    public ForeignKey(Table table, Argument argument, Table foreignTable) {
        super(table, argument);
        this.foreignTable = foreignTable;
    }

    public Table getForeignTable() {
        return foreignTable;
    }
}
