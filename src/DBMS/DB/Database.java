package DBMS.DB;

import java.io.File;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Database {
    private String name;
    private File db;
    private FileIO reader;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
