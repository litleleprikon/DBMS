package DBMS.DB.InnerStructure.Indexes;

import DBMS.DB.InnerStructure.Argument;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.LinkedList;

/**
 * Created by litleleprikon on 22/11/15.
 * Abstract class for Indexes
 */
public abstract class Index<K extends Comparable<K>, V> extends AbstractMap implements Serializable{
    private String name;
    private Argument arguments;

    public Index(String name, Argument arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public Argument getArguments() {
        return arguments;
    }
}
