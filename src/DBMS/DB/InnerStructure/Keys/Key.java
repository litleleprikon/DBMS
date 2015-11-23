package DBMS.DB.InnerStructure.Keys;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Table;

import java.util.LinkedList;

/**
 * Created by dmitriy on 20-Nov-15.
 */
public abstract class Key {
//    protected Table table;
    protected Argument argument;

    public Key(Argument argument) {
//        this.table = table;
        this.argument = argument;
    }

//    public Table getTable() {
//        return table;
//    }

    public Argument getArgument() {
        return argument;
    }
}
