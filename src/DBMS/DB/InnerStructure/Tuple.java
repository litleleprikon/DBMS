package DBMS.DB.InnerStructure;

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

    public Type getValue(String argument) {
        Argument arg = table.getArgument(argument);
        return values.get(arg);
    }

    public Table getTable() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple otherTuple = (Tuple) o;

        if (!table.equals(otherTuple.table)) return false;

        if (values.size() != otherTuple.values.size()) return false;

        for (Argument argument : values.keySet()) {
            Type otherValue = otherTuple.getValue(argument.getName());
            Type value = values.get(argument);

            if (!value.equals(otherValue)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return values != null ? values.hashCode() : 0;
    }
}
