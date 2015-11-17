package DBMS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class FileReader {
    private File db;

    public FileReader(String db) {
        this.db = new File(db).getAbsoluteFile();
    }

    public FileReader(File db) {
        this.db = db.getAbsoluteFile();
    }


    /**
     * Reads header(first 10 bytes) of Database file @code db
     */
    public void readHeader() {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(db);
            byte[] header = new byte[10];
            fis.read(header);

            //TODO header storing

        } catch (IOException e) {
            //TODO exception handle
        } finally {
            if (fis != null) try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Reads metadata from Database file @code db
     */
    public void readMeta() {
        
    }

}
