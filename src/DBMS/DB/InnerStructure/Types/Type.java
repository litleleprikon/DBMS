package DBMS.DB.InnerStructure.Types;

public abstract class Type<T> {
    protected T data;

    public final static String varchar = "varchar";
    public final static String integer = "int";

    public abstract void parse(String sData);

    public T getData() {
        return data;
    }

    public abstract String getType();

    public void setData(T data) {
        this.data = data;
    }


}
