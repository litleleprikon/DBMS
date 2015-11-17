package DBMS.DB.Types;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class FixedVarChar extends VarChar {
    private static int defaultSize = 32; //Size in bytes

    public FixedVarChar() {
        super(defaultSize);
    }
}
