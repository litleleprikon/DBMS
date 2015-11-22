package DBMS.DB;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Table;
import DBMS.DB.InnerStructure.Tuple;
import DBMS.DB.InnerStructure.Types.VarChar;

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
        database.header = new Header(pageSize, 1, 0);
        database.metadata = new Metadata(database.inOut);
        database.metadata.addMetaPage(0);

        Table tabArg = new Table(catalog[0]);
        tabArg.addArgument(new Argument("Table", new VarChar()));
        tabArg.addArgument(new Argument("Argument", new VarChar()));
        database.catalogTables.put(tabArg.getName(), tabArg);
        database.metadata.addPage(catalog[0], database.getHeader().getPageCount());
        database.header.incrementPageCount();

        Table tabKey = new Table(catalog[1]);
        tabKey.addArgument(new Argument("Table", new VarChar()));
        tabKey.addArgument(new Argument("Key", new VarChar()));
        database.catalogTables.put(tabKey.getName(), tabKey);
        database.metadata.addPage(catalog[1], database.getHeader().getPageCount());
        database.header.incrementPageCount();

        Table tabInd = new Table(catalog[2]);
        tabInd.addArgument(new Argument("Table", new VarChar()));
        tabInd.addArgument(new Argument("Index", new VarChar()));
        database.catalogTables.put(tabInd.getName(), tabInd);
        database.metadata.addPage(catalog[2], database.getHeader().getPageCount());
        database.header.incrementPageCount();

        return database;
    }

    public static Database create(String name) {
        int defaultSize = 4096;
        return create(name, defaultSize);
    }

    public void createTable(String name, Argument[] arguments) {
        if (tables.containsKey(name)) return; //TODO exception
        Table table = new Table(name);
        tables.put(name, table);
        metadata.addPage(name, header.getPageCount());
        header.incrementPageCount();

        Table tabArg = catalogTables.get(catalog[0]);
        for (Argument argument : arguments) {
            table.addArgument(argument);

            Tuple tabArgTuple = new Tuple(tabArg);
            tabArgTuple.addValue(tabArg.getArguments().get("Table"), new VarChar(name));
            tabArgTuple.addValue(tabArg.getArguments().get("Argument"), new VarChar(argument.toString()));
        }
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
