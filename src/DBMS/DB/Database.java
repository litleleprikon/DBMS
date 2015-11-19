package DBMS.DB;

import DBMS.DB.Types.FixedVarChar;
import DBMS.DB.Types.Int;
import DBMS.DB.Types.Type;
import DBMS.DB.Types.VarChar;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Database {
    private String name;
    private File db;
    private FileReader reader;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
