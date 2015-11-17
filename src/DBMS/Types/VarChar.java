package DBMS.Types;

public class VarChar extends Type{
    private char[] data;

    public char[] getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data.toCharArray();
        if (data.length() > size) {
            int dif = data.length() - size;
            this.data = data.substring(0, data.length() - dif).toCharArray();
        }
        if (data.length()<size) {
            this.data = new char[size];
            System.arraycopy(data.toCharArray(),0,this.data,0,data.length());
        }
    }

    public String toString() {
        return String.copyValueOf(data);
    }

    public VarChar(int size) {
        this.size = size;
    }
}
