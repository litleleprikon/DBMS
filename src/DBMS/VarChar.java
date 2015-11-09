package DBMS;

public class VarChar {
    private int size;
    private char[] data;

    public char[] getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data.toCharArray();
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
