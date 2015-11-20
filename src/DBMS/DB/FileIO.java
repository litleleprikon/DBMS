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
public class FileIO {
    private File db;
    protected Header header;
    protected Metadata metadata;

    /**
     * CONSTANTS
     * Special symbols for dividing data
     */
    private final static byte ARGUMENTS_SEGMENT_START = 0x01;
    private final static byte ARGUMENT_TYPE_SEPARATOR = 0x02;
    private final static byte ARGUMENTS_SEPARATOR = 0x03;
    private final static byte END_OF_ARGUMENTS_SEGMENT = 0x04;
    private final static byte END_OF_BLOCK = 0x17;
    private final static byte TABLE_STRUCTURE_SEGMENT = 0x11;
    private final static byte TABLE_PAGES_SEGMENT = 0x12;
    private final static byte INDEX_PAGES_SEGMENT = 0x13;

    public FileIO(File db) {
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
            byte[] page = readPage(header.getMetaPage());

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

    /**
     * Parses metadata from given @param page starting from given position @param i
     * @param page - page represented by array of bytes
     * @param i - position on page
     */
    private void parseMeta(byte[] page, int i) {
        if (page[i] == 0 || i == page.length - 10) {//there isn't anything left on this page, move to the next
            byte[] nextPage = new byte[10];
            System.arraycopy(page, page.length - 10, nextPage, 0, nextPage.length);
            try {
                parseMeta(readPage(parseInt(nextPage)), 0);
            } catch (IOException e) {
                e.printStackTrace(); //TODO exception handle
            }
        } else {
            byte curSeg = page[i];

            if (curSeg < TABLE_STRUCTURE_SEGMENT || curSeg > INDEX_PAGES_SEGMENT) return; //TODO exception

            switch (curSeg) {
                case TABLE_STRUCTURE_SEGMENT:
                    parseTableStructure(page, ++i, null);
                    break;

                case TABLE_PAGES_SEGMENT:
                    parseTablePages(page, ++i, null, false);
                    break;

                case INDEX_PAGES_SEGMENT:
                    parseIndexPages(page, ++i, null, false);
                    break;
            }
        }
    }

    /**
     * Parses Table Structure block represented like (whitespaces are for readability, there won't be any actually):
     * 17 Table 1 argument 2 type 3 argument 2 type 3 ... argument 2 type 3 4 Table 1 argument 2 type 3 ... argument 2 type 3 4 23
     * 17 - start of block @code TABLE_STRUCTURE_SEGMENT, 23 - end of block @code END_OF_BLOCK
     * 1 - start of arguments section @code ARGUMENT_SEGMENT_START
     * 2 - argument-type separator @code ARGUMENT_TYPE_SEPARATOR
     * 3 - arguments separator @code ARGUMENTS_SEPARATOR
     * @param page - page from which structure is being parsed
     * @param i - position on the page
     * @param curStr - current structure (read table) being parsed
     */
    private void parseTableStructure(byte[] page, int i, String curStr) {
        if (page[i] == END_OF_BLOCK) parseMeta(page, ++i);

        boolean inProgress = curStr != null;
        //base case, no parsing is started or previous segment was finished
        if (!inProgress) {
            //Parse table name
            curStr = getWord(page, i, ARGUMENTS_SEGMENT_START);
            i += curStr.length();

            //Parse arguments
            parseArguments(page, i, curStr);

        } else {//parsing of table didn't finish on previous page
            parseArguments(page, i, curStr);
        }
    }

    /**
     * Parses arguments segment of Table Structure block
     * @param page - page from which parsing is going
     * @param i - position on the page
     * @param curStr - current structure (read table) for which arguments are being parsed
     */
    private void parseArguments(byte[] page, int i, String curStr) {
        boolean inProgress = true;
        while (true) {
            if (page[i + 1] == END_OF_ARGUMENTS_SEGMENT) {inProgress = false; break;} //parsing for current table is finished
            if (page[i + 1] == 0 || i + 1 == page.length - 10) break; //parsing for current table is not finished, need to move to the next page

            //parse argument name
            i++;
            String name = getWord(page, i, ARGUMENT_TYPE_SEPARATOR);
            i += name.length();

            //parse argument type
            i++;
            String sType = getWord(page, i, ARGUMENTS_SEPARATOR);
            Type type = chooseType(sType);
            i += sType.length();

            //add information in meta
            metadata.addArgument(curStr, new Argument(name, type));
        }

        if (inProgress) { //continue parsing for current table on the next page
            byte[] nextPage = new byte[10];
            System.arraycopy(page, page.length - 10, nextPage, 0, nextPage.length);
            try {
                parseTableStructure(readPage(parseInt(nextPage)), 0, curStr);
            } catch (IOException e) {
                e.printStackTrace(); //TODO exception handle
            }

        } else { //parse next table or segment
            if (page[i + 1] == 0 || i + 1 == page.length - 10) { //moving to next page if needed
                byte[] nextPage = new byte[10];
                System.arraycopy(page, page.length - 10, nextPage, 0, nextPage.length);
                try {
                    page = readPage(parseInt(nextPage));
                } catch (IOException e) {
                    e.printStackTrace(); //TODO exception handle
                }
                i = 0;
            }
            parseTableStructure(page, i == 0 ? i : ++i, null);
        }
    }

    /**
     * Chooses type for argument according to its string representation
     * @param type - string representation of type
     * @return object of chosen type
     */
    private Type chooseType(String type) {
        if ("int".equals(type)) return new Int();
        else if ("varchar".equals(type)) return new FixedVarChar();
        else if (type.contains("(")) return new VarChar(Integer.valueOf(type.substring(type.indexOf('(') + 1, type.indexOf(')'))));

        return null;
    }

    /**
     * Parses next word from @code page starting from @code i until @code div
     * @param page - page from which word is parsed
     * @param i - current position on the page
     * @param div - divider of words
     * @return String representation of parsed word
     */
    private String getWord(byte[] page, int i, byte div) {
        StringBuilder sb = new StringBuilder();
        while (page[i] != div) sb.append((char) page[i++]);
        return sb.toString();
    }

    private void parseTablePages(byte[] page, int i, String curStr, boolean inProgress) {

    }

    private void parseIndexPages(byte[] page, int i, String curStr, boolean inProgress) {

    }
}
