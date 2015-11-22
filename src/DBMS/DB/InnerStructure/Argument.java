package DBMS.DB.InnerStructure;

import DBMS.DB.InnerStructure.Types.Type;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Argument {
    private String name;
    private Type type;

    public Argument(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int compareTo(Argument to) {
        String compareWith = this.getType().toString();
        String compareTo  = to.getType().toString();
        return compareWith.compareTo(compareTo);
    }
}
