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
        return "int";
    }

    @Override
    public void parse(String sData) {

    }
}
