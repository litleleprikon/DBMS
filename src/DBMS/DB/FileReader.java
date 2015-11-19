package DBMS.DB;

import DBMS.DB.Types.FixedVarChar;
import DBMS.DB.Types.Int;
import DBMS.DB.Types.Type;
import DBMS.DB.Types.VarChar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by dmitriy on 11/19/2015.
 */
public class FileReader {
    private File db;
    protected Header header;
    protected Metadata metadata;

    public FileReader(File db) {
        this.db = db;
    }

    /**
     * Reads specified page of the file @code db
     * @param num - page number
     * @return array of bytes corresponding to specified page
     * @throws IOException
     */
    private byte[] readPage(int num) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(db, "rw");

        if (header == null) readHeader();

        raf.seek(num * header.getPageSize());
        byte[] page = new byte[header.getPageSize()];
        raf.read(page);

        return page;
    }

    /**
     * Parses array of bytes to integer
     * @param buffer - array of bytes to parse
     * @return parsed integer
     */
    private int parseInt(byte[] buffer) {
        String in = new String(buffer);
        return in.indexOf(0x00) == -1 ? Integer.valueOf(in) : Integer.valueOf(in.substring(0, in.indexOf(0x00)));
    }
    /**
     * Reads header(first 30 bytes) of Database file @code db
     */
    public void readHeader() {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(db, "rw");
            byte[] buffer = new byte[10];
            raf.read(buffer);
            int pageSize = parseInt(buffer);

            buffer = new byte[10];
            raf.read(buffer);
            int pageCount = parseInt(buffer);

            buffer = new byte[10];
            raf.read(buffer);
            int metaPointer = parseInt(buffer);

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
            metadata = new Metadata();
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
                        metadata.addArgument(table, new Argument(name, type));
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
}
