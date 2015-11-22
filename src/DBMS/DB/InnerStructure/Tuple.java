package DBMS.DB.InnerStructure;

import DBMS.DB.InnerStructure.Keys.PrimaryKey;
import DBMS.DB.InnerStructure.Types.Type;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by dmitriy on 20-Nov-15.
 */
public class Tuple {
    private Table table;
    private Map<Argument, Type> values = new LinkedHashMap<>();

    public Tuple(Table table) {
        this.table = table;
    }

    public Map<Argument, Type> getValues() {
        return values;
    }

    public void addValue(Argument argument, Type value) {
        values.put(argument, value);
    }

    public Type getValue(Argument argument) {
        return values.get(argument);
    }

    public Table getTable() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple tuple = (Tuple) o;

        if (!table.equals(tuple.getTable())) return false;

        if (table.getPrimaryKey() == null) return !(values != null ? !values.equals(tuple.values) : tuple.values != null);
        else {
            PrimaryKey key = table.getPrimaryKey();
            for (Argument argument : key.getKeyArguments()) {
                if ()
            }
        }
    }

    @Override
    public int hashCode() {
        return values != null ? values.hashCode() : 0;
    }
}
