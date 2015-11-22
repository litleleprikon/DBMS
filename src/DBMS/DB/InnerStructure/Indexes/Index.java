package DBMS.DB.InnerStructure.Indexes;

import DBMS.DB.InnerStructure.Argument;

import java.util.LinkedList;

/**
 * Created by dmitriy on 20-Nov-15.
 */
public abstract class Index {
    protected String name;
    protected LinkedList<Argument> arguments;

}
