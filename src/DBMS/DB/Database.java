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
    private Header header;
    private Metadata metadata;

    /**
     * Reads header(first 10 bytes) of Database file @code db
     */
    public void readHeader() {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(db, "rw");
            int pageSize = raf.readInt();
            int pageCount = raf.readInt();
            int metaPointer = raf.readInt();

            header = new Header(pageSize, pageCount, metaPointer);

        } catch (IOException e) {
            //TODO exception handle
        } finally {
            if (raf != null) try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Reads metadata from Database file @code db
     */
    public void readMeta() {
        readHeader();

        if (header.getMetaPage() == 0) return; //TODO exception

        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(db, "rw");
            byte[] page = new byte[header.getPageSize()];

            /**
             * 17 - start of table structure segment
             * 18 - start of table pages segment
             * 19 - start of index segment
             */
            Metadata meta = new Metadata();
            int segment;
            raf.read(page);

            if (page[0] > 19) return; //TODO exception
            segment = page[0];

            switch (segment) {
                case 17:
                    StringBuilder sb = new StringBuilder();
                    int i = 1;
                    while (page[i] > 32) {
                        sb.append((char)page[i]);
                        i++;
                    }
                    String table = sb.toString();


                    while (i > 19) {
                        //parse name of argument
                        sb = new StringBuilder();
                        i++;
                        while (page[i] > 32) {
                            sb.append((char)page[i]);
                            i++;
                        }
                        String name = sb.toString();

                        //parse type of argument
                        sb = new StringBuilder();
                        i++;
                        while (page[i] > 32) {
                            sb.append((char)page[i]);
                            i++;
                        }
                        Type type = null;
                        String sType = sb.toString();

                        if (sType.equals("int")) type = new Int();
                        if (sType.equals("varchar")) type = new FixedVarChar();
                        if (sType.indexOf('(') != -1) {
                            int size = Integer.valueOf(sType.substring(sType.indexOf('('), sType.indexOf(')')));
                            type = new VarChar(size);
                        }
                        meta.addArgument(table, new Argument(name, type));
                    } //TODO read next table and next segments
            }


        } catch (IOException e) {
            //TODO exception handle
        } finally {
            if (raf != null) try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Header getHeader() {
        return header;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
