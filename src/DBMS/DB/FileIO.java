package DBMS.DB;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Created by dmitriy on 11/19/2015.
 */
public class FileIO {
    private Database database;
    private File db;
    public final static int pointerSize = 10;

    public FileIO(Database database) {
        this.database = database;
        this.db = new File(database.getName()).getAbsoluteFile();
        if (!db.exists()) try {
            db.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static int getNextPage(byte[] page) {
        byte[] nextPage = new byte[pointerSize];
        System.arraycopy(page, page.length - pointerSize, nextPage, 0, nextPage.length);

        return parseInt(nextPage);
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
    public void readMetadata() {
        readHeader();

        if (database.getHeader().getMetaPage() == -1) return; //TODO exception


        try {
            byte[] page = readPage(database.getHeader().getMetaPage());

            Metadata metadata = new Metadata(this);
            metadata.addMetaPage(database.getHeader().getMetaPage());
            metadata.parseMeta(page, 0);
            database.setMetadata(metadata);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePage(int num, byte[] page) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(db, "rw");

        if (database.getHeader() == null) readHeader();

        long pos = num * database.getHeader().getPageSize() + 30; //first 30 bytes reserved for header
        raf.seek(pos);
        raf.write(page);
    }

    public void writeHeader() {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(db, "rw");
            byte[] buffer = new byte[10];
            byte[] in = String.valueOf(database.getHeader().getPageSize()).getBytes();
            System.arraycopy(in, 0, buffer, 0, in.length);
            raf.write(buffer);

            buffer = new byte[10];
            in = String.valueOf(database.getHeader().getPageCount()).getBytes();
            System.arraycopy(in, 0, buffer, 0, in.length);
            raf.write(buffer);

            buffer = new byte[10];
            in = String.valueOf(database.getHeader().getMetaPage()).getBytes();
            System.arraycopy(in, 0, buffer, 0, in.length);
            raf.write(buffer);

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

    public void writeMetadata() {
        writeHeader();
        Metadata metadata = database.getMetadata();

        ArrayList<byte[]> pages = metadata.toWritingForm();
        ArrayList<Integer> pageNumbers = metadata.getMetaPages();

        for (int i = 0; i < pages.size(); i++) {
            try {
                writePage(pageNumbers.get(i), pages.get(i));
            } catch (IOException e) {
                e.printStackTrace(); //TODO exception handle
            }
        }
    }

    public Database getDatabase() {
        return database;
    }
}
