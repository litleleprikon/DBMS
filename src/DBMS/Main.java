package DBMS;

/**
 * Created by macbook on 08/11/15.
 */
public class Main {
    public static void main(String[] args) {
        VarChar chr = new VarChar(50);
        chr.setData("azaza");
        System.out.println(chr.toString());
        char temp = chr.getData()[38];
        System.out.println(temp);
    }
}
