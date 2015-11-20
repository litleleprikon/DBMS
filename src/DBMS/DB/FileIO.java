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
     * Special symbols for data separating
     */
    private final static byte ARGUMENTS_SEGMENT_START = 1;
    private final static byte ARGUMENT_TYPE_SEPARATOR = 2;
    private final static byte ARGUMENTS_SEPARATOR = 3;
    private final static byte END_OF_ARGUMENTS_SEGMENT = 4;
    private final static byte END_OF_BLOCK = 23;
    private final static byte TABLE_STRUCTURE_SEGMENT = 17;
    private final static byte TABLE_PAGES_SEGMENT = 18;
    private final static byte INDEX_PAGES_SEGMENT = 19;
    private final static byte TREE = 6;
    private final static byte HASH = 21;

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

        if (header.getMetaPage() == -1) return; //TODO exception


        try {
            byte[] page = readPage(header.getMetaPage());

            metadata = new Metadata();
            parseMeta(page, 0);

        } catch (IOException e) {
            e.printStackTrace();
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
                int next = parseInt(nextPage);
                if (next == -1) return; //if there is no next page finish parsing
                else parseMeta(readPage(next), 0); //else continue parsing
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
                    parseTablePages(page, ++i, null);
                    break;

                case INDEX_PAGES_SEGMENT:
                    parseIndexPages(page, ++i, null);
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

    /**
     * Parses Table Pages block represented like (whitespaces are for readability, there won't be any actually):
     * 18 Table 1 page 3 page 3 ... page 3 4 Table 1 page 3 ... page 3 4 23
     * 18 - start of block @code TABLE_PAGES_SEGMENT, 23 - end of block @code END_OF_BLOCK
     * 1 - start of pages section @code ARGUMENT_SEGMENT_START
     * 3 - pages separator @code ARGUMENTS_SEPARATOR
     * @param page - page from which structure is being parsed
     * @param i - position on the page
     * @param curStr - current structure (read table) being parsed
     */
    private void parseTablePages(byte[] page, int i, String curStr) {
        if (page[i] == END_OF_BLOCK) parseMeta(page, ++i);

        boolean inProgress = curStr != null;
        if (!inProgress) { //no parsing is started or previous segment was finished
            //Parse table name
            curStr = getWord(page, i, ARGUMENTS_SEGMENT_START);
            i += curStr.length();

            //Parse pages
            parsePages(page, i, curStr, true);

        } else {//parsing of table didn't finish on previous page
            parsePages(page, i, curStr, true);
        }
    }

    /**
     * Parses Index Pages block represented like (whitespaces are for readability, there won't be any actually):
     * 19 Index [21]||[6] 1 page 3 page 3 ... page 3 4 Index [21]||[6] 1 page 3 ... page 3 4 23
     * 19 - start of block @code TABLE_PAGES_SEGMENT, 23 - end of block @code END_OF_BLOCK
     * 6 - index is tree marker @code TREE
     * 21 - index is hash marker @code HASH
     * 1 - start of pages section @code ARGUMENT_SEGMENT_START
     * 3 - pages separator @code ARGUMENTS_SEPARATOR
     * @param page - page from which structure is being parsed
     * @param i - position on the page
     * @param curStr - current structure (read table) being parsed
     */
    private void parseIndexPages(byte[] page, int i, String curStr) {
        if (page[i] == END_OF_BLOCK) parseMeta(page, ++i);

        boolean inProgress = curStr != null;
        //base case, no parsing is started or previous segment was finished
        if (!inProgress) {
            //Parse index name
            curStr = getWord(page, i, ARGUMENTS_SEGMENT_START);
            i += curStr.length();

            //Parse index type
            if (page[i] == TREE) metadata.addIndexType(curStr, Metadata.IndexType.Tree);
            else if (page[i] == HASH) metadata.addIndexType(curStr, Metadata.IndexType.Hash);
            i++;

            //Parse pages
            parsePages(page, i, curStr, false);

        } else {//parsing of table didn't finish on previous page
            parsePages(page, i, curStr, false);
        }
    }

    /**
     * Parses pages segment of Table Pages block or Index Pages block
     * @param page - page from which parsing is going
     * @param i - position on the page
     * @param curStr - current structure (read table) for which arguments are being parsed
     * @param isTable - true if current block is Table Pages, false if current block is Index Pages
     */
    private void parsePages(byte[] page, int i, String curStr, boolean isTable) {
        boolean inProgress = true;
        while (true) {
            if (page[i + 1] == END_OF_ARGUMENTS_SEGMENT) {inProgress = false; break;} //parsing for current structure is finished
            if (page[i + 1] == 0 || i + 1 == page.length - 10) break; //parsing for current structure is not finished, need to move to the next page

            //parse page number
            i++;
            String sNumber = getWord(page, i, ARGUMENTS_SEPARATOR);
            int number = Integer.valueOf(sNumber);
            i += sNumber.length();

            metadata.addPage(curStr, number);
        }

        if (inProgress) { //continue parsing for current structure on the next page
            byte[] nextPage = new byte[10];
            System.arraycopy(page, page.length - 10, nextPage, 0, nextPage.length);
            try {
                parseTablePages(readPage(parseInt(nextPage)), 0, curStr);
            } catch (IOException e) {
                e.printStackTrace(); //TODO exception handle
            }

        } else { //parse next structure or segment
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

            if (isTable) parseTablePages(page, i == 0 ? i : ++i, null);
            else parseIndexPages(page, i == 0 ? i : ++i, null);
        }
    }
}
