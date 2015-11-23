package DBMS.DB;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Indexes.BPTreeIndex;
import DBMS.DB.InnerStructure.Keys.ForeignKey;
import DBMS.DB.InnerStructure.Keys.Key;
import DBMS.DB.InnerStructure.Keys.PrimaryKey;
import DBMS.DB.InnerStructure.Table;
import DBMS.DB.InnerStructure.Tuple;
import DBMS.DB.InnerStructure.Types.Int;
import DBMS.DB.InnerStructure.Types.Type;
import DBMS.DB.InnerStructure.Types.VarChar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

    private Database(String name) {
        this.name = name;
        inOut = new FileIO(this);
        catalogTables = new HashMap<>();
        tables = new HashMap<>();

        Table tabArg = new Table(catalog[0]);
        tabArg.addArgument(new Argument("Table", new VarChar()));
        tabArg.addArgument(new Argument("Argument", new VarChar()));
        catalogTables.put(tabArg.getName(), tabArg);

        Table tabKey = new Table(catalog[1]);
        tabKey.addArgument(new Argument("Table", new VarChar()));
        tabKey.addArgument(new Argument("Key", new VarChar()));
        catalogTables.put(tabKey.getName(), tabKey);

        Table tabInd = new Table(catalog[2]);
        tabInd.addArgument(new Argument("Table", new VarChar()));
        tabInd.addArgument(new Argument("Index", new VarChar()));
        catalogTables.put(tabInd.getName(), tabInd);
    }

    public static Database create(String name, int pageSize) {
        Database database = new Database(name);
        database.header = new Header(pageSize, 1, 0);
        database.metadata = new Metadata(database.inOut);
        database.metadata.addMetaPage(0);


        database.metadata.addPage(catalog[0], database.getHeader().getPageCount());
        database.header.incrementPageCount();

        database.metadata.addPage(catalog[1], database.getHeader().getPageCount());
        database.header.incrementPageCount();

        database.metadata.addPage(catalog[2], database.getHeader().getPageCount());
        database.header.incrementPageCount();

        return database;
    }


    public static Database create(String name) {
        int defaultSize = 4096;
        return create(name, defaultSize);
    }

    public static Database load(String name) {
        Database database = new Database(name);

        database.getInOut().readMetadata();

        //read tables structure from catalog tables
        for (String s : catalog) database.scan(s);

        //load tables arguments
        Table catTab = database.catalogTables.get(catalog[0]);
        for (Tuple tuple : catTab.getTuples()) {
            String tabName = (String) tuple.getValue("Table").getData();
            String arg = ((String) tuple.getValue("Argument").getData());
            String[] arr = arg.split(String.valueOf((char) Constants.ARGUMENT_TYPE_SEPARATOR));

            if (!database.tables.containsKey(tabName)) database.tables.put(tabName, new Table(tabName));

            Table table = database.tables.get(tabName);
            Argument argument = new Argument(arr[0], arr[1].equals(Type.varchar) ? new VarChar() : new Int());
            table.addArgument(argument);
        }

        //load tables keys
        catTab = database.catalogTables.get(catalog[1]);
        for (Tuple tuple : catTab.getTuples()) {
            String tabName = (String) tuple.getValue("Table").getData();
            Table table = database.tables.get(tabName);

            String[] arguments = ((String) tuple.getValue("Key").getData()).split(String.valueOf((char) Constants.ARGUMENT_TYPE_SEPARATOR));

            String foreignTable = arguments[0];
            String argument = arguments[1];

           if ("none".equals(foreignTable)) {
               PrimaryKey key = new PrimaryKey(table.getArgument(argument));
               table.setPrimaryKey(key);
           }  else {
               ForeignKey key = new ForeignKey(table.getArgument(argument), database.tables.get(foreignTable));
               table.addForeignKey(key);
           }

        }

        //load tables indexes
        catTab = database.catalogTables.get(catalog[2]);
        for (Tuple tuple : catTab.getTuples()) {
            String tabName = (String) tuple.getValue("Table").getData();
            Table table = database.tables.get(tabName);

            String[] arguments = ((String) tuple.getValue("Index").getData()).split(String.valueOf((char) Constants.ARGUMENT_TYPE_SEPARATOR));

            String indName = arguments[0];
            String indArg = arguments[1];
            String argType = table.getArgument(indArg).getType().getType();

            if (Type.integer.equals(argType)) {
                BPTreeIndex<Integer, Integer> index = new BPTreeIndex<>(indName, table.getArgument(indArg));
                table.addIndex(index);
            } else {
                BPTreeIndex<String, Integer> index = new BPTreeIndex<>(indName, table.getArgument(indArg));
                table.addIndex(index);
            }
        }

        // load indexes
        for (String sTable : database.tables.keySet()) {
            Table table = database.tables.get(sTable);
            for (String index : table.getIndexes().keySet()) {
                database.scanIndex(index, sTable);
            }
        }

        return database;
    }

    public void createTable(String name, Argument[] arguments, PrimaryKey primaryKey) {
        if (tables.containsKey(name)) return; //TODO exception
        Table table = new Table(name);
        table.setPrimaryKey(primaryKey);
        tables.put(name, table);
        metadata.addPage(name, header.getPageCount());
        header.incrementPageCount();

        if (primaryKey != null) insert(catalog[1], new Type[]{new VarChar(name), new VarChar("none" + (char)Constants.ARGUMENT_TYPE_SEPARATOR + primaryKey.getArgument().getName())});

        Table tabArg = catalogTables.get(catalog[0]);
        for (Argument argument : arguments) {
            table.addArgument(argument);

            insert(tabArg.getName(), new Type[]{new VarChar(name), new VarChar(argument.getName() + (char) Constants.ARGUMENT_TYPE_SEPARATOR + argument.getType().getType())});
        }
    }

    public void insert(String sTable, Type[] values) {
        Table table = tables.get(sTable);
        for (String s : catalog) {
            if (s.equals(sTable)) {
                table = catalogTables.get(sTable);
                break;
            }
        }

        Tuple tuple = new Tuple(table);

        int ind = 0;
        for (String s : table.getArguments().keySet()) {
            tuple.addValue(table.getArgument(s), values[ind++]);
        }

        table.addTuple(tuple);

        //write new tuple
        int pageNum = metadata.getPage(sTable);
        try {
            byte[] page = inOut.readPage(pageNum);
            int next;
            while ((next = FileIO.getNextPage(page)) != -1) {
                pageNum = next;
                page = inOut.readPage(next);
            }

            int nullIndex;
            for (nullIndex = 0; nullIndex < page.length; nullIndex++) {
                if (page[nullIndex] == 0) break;
            }

            byte[] byteTuple = tuple.toWritingForm();

            if (byteTuple.length + nullIndex < page.length - FileIO.pointerSize) {//tuple fits to page
                System.arraycopy(byteTuple, 0, page, nullIndex, byteTuple.length);
                inOut.writePage(pageNum, page);

            } else {//allocate new page
                next = header.getPageCount();
                header.incrementPageCount();
                byte[] nextInBytes = String.valueOf(next).getBytes();
                //write pointer to new page
                System.arraycopy(nextInBytes, 0, page, page.length - FileIO.pointerSize, nextInBytes.length);

                //write new page
                page = new byte[header.getPageSize()];
                System.arraycopy(byteTuple, 0, page, 0, byteTuple.length);

                inOut.writePage(next, page);
            }

            inOut.writeMetadata();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void scanIndex(String index, String table) {
        if (!metadata.getPages().containsKey(index)) return;

        int startPage = metadata.getPage(index);
        byte[] page = null;
        try {
            page = inOut.readPage(startPage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        scanIndex(index, table, page, 0);
    }

    private void scanIndex(String sIndex, String sTable, byte[] page, int i) {
        if (page[i] == 0) {
            int num = FileIO.getNextPage(page);
            if (num == -1) return; //TODO exception
            else {
                try {
                    page = inOut.readPage(num);
                    i = 0;
                    scanIndex(sIndex, sTable, page, i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        BPTreeIndex index = tables.get(sTable).getIndex(sIndex);
        String key = FileIO.getWord(page, i, Constants.ARGUMENTS_SEPARATOR);
        i += key.length();

        i++;
        String value = FileIO.getWord(page, i, Constants.ARGUMENTS_SEPARATOR);
        i += key.length();

        i++;

        if (index.getArguments().getType().getType().equals(Type.integer)) {
            index.put(Integer.valueOf(key), Integer.valueOf(value));
        } else index.put(key, Integer.valueOf(value));

        if (page[i] == 0 || i == page.length - FileIO.pointerSize) { //move to the next page
            int num = FileIO.getNextPage(page);
            if (num != -1) {
                try {
                    page = inOut.readPage(num);
                    i = 0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        scanIndex(sIndex, sTable, page, i);
    }

    private void scan(String table) {
        if (!metadata.getPages().containsKey(table)) return;

        int startPage = metadata.getPage(table);
        byte[] page = null;
        try {
            page = inOut.readPage(startPage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isCatalog = false;
        for (String s : catalog) {
            if (s.equals(table)) {
                isCatalog = true;
                break;
            }
        }

        scan(table, page, 0, isCatalog);
    }

    private void scan(String sTable, byte[] page, int i, boolean isCatalog) {
        if (page[i] == 0) {
            int num = FileIO.getNextPage(page);
            if (num == -1) return; //TODO exception
            else {
                try {
                    page = inOut.readPage(num);
                    i = 0;
                    scan(sTable, page, i, isCatalog);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Table table = isCatalog ? catalogTables.get(sTable) : tables.get(sTable);
        Tuple tuple = new Tuple(table);

        for (String s : table.getArguments().keySet()) {
            String value = FileIO.getWord(page, i, Constants.ARGUMENTS_SEPARATOR);
            i += value.length();

            Type val = table.getArgument(s).getType().getType().equals(Type.varchar) ? new VarChar() : new Int();
            val.parse(value);
            tuple.addValue(table.getArgument(s), val);

            i++;
        }
        i++;

        table.addTuple(tuple);

        //check page
        if (page[i] == 0 || i == page.length - FileIO.pointerSize) { //move to the next page
            int num = FileIO.getNextPage(page);
            if (num != -1) {
                try {
                    page = inOut.readPage(num);
                    i = 0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        scan(sTable, page, i, isCatalog);
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
