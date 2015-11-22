package DBMS.DB.InnerStructure.Types;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Int extends Type<Integer> {
    public Int() {
    }

    public Int(Integer data){
        this.data = data;
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }

    @Override
    public void parse(String sData) {
        data = Integer.parseInt(sData);
    }

    @Override
    public String getType() {
        return "int";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
