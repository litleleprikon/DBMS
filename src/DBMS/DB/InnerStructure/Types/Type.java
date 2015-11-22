package DBMS.DB.InnerStructure.Types;

/**
 * Created by dmitriy on 11/17/2015.
 */
public abstract class Type<T> {
    protected T data;

    public abstract void parse(String sData);

    public abstract String getType();

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Type<?> type = (Type<?>) o;

        return !(data != null ? !data.equals(type.data) : type.data != null);

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}
