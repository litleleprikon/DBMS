package DBMS.DB.InnerStructure.Types;

/**
 * Created by dmitriy on 11/17/2015.
 */
public abstract class Type<T> {
    protected T data;

    public abstract void parse(String sData);

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
