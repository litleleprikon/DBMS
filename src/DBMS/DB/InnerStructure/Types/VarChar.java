package DBMS.DB.InnerStructure.Types;

public class VarChar extends Type<String>{
    public VarChar() {
    }

    public VarChar(String data) {
        this.data = data;
    }

    public String toString() {
        return "varchar";
    }


    @Override
    public void parse(String sData) {

    }
}
