package DBMS.DB.InnerStructure.Keys;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Table;

import java.util.LinkedList;

/**
 * Created by dmitriy on 20-Nov-15.
 */
public abstract class Key {
    protected Table table;
    protected LinkedList<Argument> keyArguments;

    public Key(Table table, LinkedList<Argument> keyArguments) {
        this.table = table;
        this.keyArguments = keyArguments;
    }

    public Table getTable() {
        return table;
    }

    public LinkedList<Argument> getKeyArguments() {
        return keyArguments;
    }
}
