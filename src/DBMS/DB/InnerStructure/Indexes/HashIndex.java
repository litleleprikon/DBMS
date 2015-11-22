package DBMS.DB.InnerStructure.Indexes;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by dmitriy on 22-Nov-15.
 */
public class HashIndex<K extends Comparable<K>, V> extends Index {
    public HashIndex(String name, LinkedList linkedList) {
        super(name, linkedList);
    }

    @Override
    public Set<Entry> entrySet() {
        return null;
    }
}
