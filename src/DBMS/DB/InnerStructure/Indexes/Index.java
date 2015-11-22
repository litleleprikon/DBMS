package DBMS.DB.InnerStructure.Indexes;

import DBMS.DB.InnerStructure.Argument;

import java.util.AbstractMap;
import java.util.LinkedList;

/**
 * Created by litleleprikon on 22/11/15.
 * Abstract class for Indexes
 */
public abstract class Index<K extends Comparable<K>, V> extends AbstractMap {
    private String name;
    private LinkedList<Argument> arguments;

    public Index(String name, LinkedList<Argument> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
}
