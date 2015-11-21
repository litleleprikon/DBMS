package DBMS.DB;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Table;
import DBMS.DB.InnerStructure.Types.FixedVarChar;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Database {
    private String name;
    private FileIO inOut;
    private LinkedList<Table> catalogTables;
    private LinkedList<Table> tables;
    private Header header;
    private Metadata metadata;

    public final static String[] catalog = {"TableArgument", "TableKey", "TableIndex"};

    private Database(String name, int pageSize) {
        this.name = name;
        inOut = new FileIO(this);
        catalogTables = new LinkedList<>();
        tables = new LinkedList<>();
    }

    public static Database create(String name, int pageSize) {
        Database database = new Database(name, pageSize);

        Table tabArg = new Table(catalog[0]);
        tabArg.addArgument(new Argument("Table", new FixedVarChar()));
        tabArg.addArgument(new Argument("Argument", new FixedVarChar()));
        database.catalogTables.add(tabArg);

        Table tabKey = new Table(catalog[1]);
        tabKey.addArgument(new Argument("Table", new FixedVarChar()));
        tabKey.addArgument(new Argument("Key", new FixedVarChar()));
        database.catalogTables.add(tabKey);

        Table tabInd = new Table(catalog[2]);
        tabInd.addArgument(new Argument("Table", new FixedVarChar()));
        tabInd.addArgument(new Argument("Index", new FixedVarChar()));
        database.catalogTables.add(tabInd);

        return database;
    }

    public static Database create(String name) {
        int defaultSize = 4096;
        return create(name, defaultSize);
    }

    public String getName() {
        return name;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Header getHeader() {
        return header;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
