package DBMS.DB.InnerStructure.Types;

public class VarChar extends Type<String>{
    public VarChar() {
    }

    public VarChar(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }


    @Override
    public void parse(String sData) {
        data = sData;
    }

    @Override
    public String getType() {
        return "varchar";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
