package DBMS.DB;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Database {
    private String name;
    private FileIO inOut;
    private Map<String, Table> catalogTables;
    private Map<String, Table> tables;
    private Header header;
    private Metadata metadata;

    public final static String[] catalog = {"TableArgument", "TableKey", "TableIndex"};

    private Database(String name, int pageSize) {
        this.name = name;
        inOut = new FileIO(this);
        catalogTables = new HashMap<>();
        tables = new HashMap<>();
    }

    public static Database create(String name, int pageSize) {
        Database database = new Database(name, pageSize);
        database.header = new Header(pageSize, 0, -1);
        database.metadata = new Metadata(database.inOut);

        Table tabArg = new Table(catalog[0]);
        tabArg.addArgument(new Argument("Table", new FixedVarChar()));
        tabArg.addArgument(new Argument("Argument", new FixedVarChar()));
        database.catalogTables.put(tabArg.getName(), tabArg);

        Table tabKey = new Table(catalog[1]);
        tabKey.addArgument(new Argument("Table", new FixedVarChar()));
        tabKey.addArgument(new Argument("Key", new FixedVarChar()));
        database.catalogTables.put(tabKey.getName(), tabKey);

        Table tabInd = new Table(catalog[2]);
        tabInd.addArgument(new Argument("Table", new FixedVarChar()));
        tabInd.addArgument(new Argument("Index", new FixedVarChar()));
        database.catalogTables.put(tabInd.getName(), tabInd);

        return database;
    }

    public static Database create(String name) {
        int defaultSize = 4096;
        return create(name, defaultSize);
    }

    public void createTable(String name) {
        if (tables.containsKey(name)) return; //TODO exception
        tables.put(name, new Table(name));
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

    public FileIO getInOut() {
        return inOut;
    }
}
