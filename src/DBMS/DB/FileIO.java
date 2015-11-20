package DBMS.DB;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Types.FixedVarChar;
import DBMS.DB.InnerStructure.Types.Int;
import DBMS.DB.InnerStructure.Types.Type;
import DBMS.DB.InnerStructure.Types.VarChar;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by dmitriy on 11/19/2015.
 */
public class FileIO {
    private Database database;
    private File db;

    public FileIO(Database database) {
        this.database = database;
        this.db = new File(database.getName()).getAbsoluteFile();
    }

    /**
     * Reads specified page of the file @code db
     * @param num - page number
     * @return array of bytes corresponding to specified page
     * @throws IOException
     */
    public byte[] readPage(int num) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(db, "rw");

        if (database.getHeader() == null) readHeader();

        raf.seek(num * database.getHeader().getPageSize() + 30); //first 30 bytes reserved for header
        byte[] page = new byte[database.getHeader().getPageSize()];
        raf.read(page);

        return page;
    }

    /**
     * Parses array of bytes to integer
     * @param buffer - array of bytes to parse
     * @return parsed integer
     */
    public static int parseInt(byte[] buffer) {
        String in = new String(buffer);
        if (in.indexOf(0) == 0) return -1;
        else return in.indexOf(0) == -1 ? Integer.valueOf(in) : Integer.valueOf(in.substring(0, in.indexOf(0x00)));
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

            database.setHeader(new Header(pageSize, pageCount, metaPointer));

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

        if (database.getHeader().getMetaPage() == -1) return; //TODO exception


        try {
            byte[] page = readPage(database.getHeader().getMetaPage());

            Metadata metadata = new Metadata(this);
            metadata.parseMeta(page, 0, null);
            database.setMetadata(metadata);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
