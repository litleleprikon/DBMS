package DBMS.DB;

import DBMS.DB.InnerStructure.Types.Int;
import DBMS.DB.InnerStructure.Types.Type;
import DBMS.DB.InnerStructure.Types.VarChar;

import java.io.IOException;
import java.util.*;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Metadata {
    FileIO inOut;
    Map<String, TreeSet<Integer>> tabIndPages;
    List<Integer> metaPages;

    public Metadata(FileIO inOut) {
        this.inOut = inOut;
        tabIndPages = new HashMap<>();
        metaPages = new LinkedList<>();
    }

    public void addPage(String to, int page) {
        if (!tabIndPages.containsKey(to)) tabIndPages.put(to, new TreeSet<>());

        tabIndPages.get(to).add(page);
    }

    public void removePage(String from, int page) {
        if (!tabIndPages.containsKey(from)) return;

        tabIndPages.get(from).remove(page);
    }

    /**
     * Parses metadata from given @param page starting from given position @param i
     * @param page - page represented by array of bytes
     * @param i - position on page
     */
    protected void parseMeta(byte[] page, int i, String curStr) {
        if (page[i] == 0) return;

        boolean inProgress = curStr != null;
        if (!inProgress) { //no parsing is started or previous segment was finished
            //Parse table name
            curStr = getWord(page, i, Constants.ARGUMENTS_SEGMENT_START);
            i += curStr.length();

            //Parse pages
            parsePages(page, i, curStr);

        } else {//parsing of table didn't finish on previous page
            parsePages(page, i, curStr);
        }
    }


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
     * Parses pages segment
     * @param page - page from which parsing is going
     * @param i - position on the page
     * @param curStr - current structure for which arguments are being parsed
     */
    private void parsePages(byte[] page, int i, String curStr) {
        boolean inProgress = true;
        while (true) {
            if (page[i + 1] == Constants.END_OF_ARGUMENTS_SEGMENT) {inProgress = false; break;} //parsing for current structure is finished
            if (page[i + 1] == 0 || i + 1 == page.length - 10) break; //parsing for current structure is not finished, need to move to the next page

            //parse page number
            i++;
            String sNumber = getWord(page, i, Constants.ARGUMENTS_SEPARATOR);
            int number = Integer.valueOf(sNumber);
            i += sNumber.length();

            addPage(curStr, number);
        }

        if (inProgress) { //continue parsing for current structure on the next page
            byte[] nextPage = new byte[10];
            System.arraycopy(page, page.length - 10, nextPage, 0, nextPage.length);
            try {
                int next = FileIO.parseInt(nextPage);
                metaPages.add(next);
                parseMeta(inOut.readPage(next), 0, curStr);
            } catch (IOException e) {
                e.printStackTrace(); //TODO exception handle
            }

        } else { //parse next structure
            if (page[i + 1] == 0 || i + 1 == page.length - 10) { //moving to next page if needed
                byte[] nextPage = new byte[10];
                System.arraycopy(page, page.length - 10, nextPage, 0, nextPage.length);
                try {
                    int next = FileIO.parseInt(nextPage);
                    metaPages.add(next);
                    page = inOut.readPage(next);
                } catch (IOException e) {
                    e.printStackTrace(); //TODO exception handle
                }
                i = 0;
            }
            parseMeta(page, i == 0 ? i : ++i, null);
        }
    }

    public void addMetaPage(int page) {
        metaPages.add(page);
    }

    public List<Integer> getMetaPages() {
        return metaPages;
    }

    /**
     * Transforms metadata to writable form
     * @return list of pages of metadata
     */
    protected LinkedList<byte[]> toWritingForm() {
        LinkedList<byte[]> metaInPages = new LinkedList<>();

        int nextPage = 1;

        //Firstly write catalog tables
        LinkedList<byte[]> tabPages = null;
        for (String table : inOut.getDatabase().catalog) {
            tabPages = tableToPages(table, nextPage);
            for (byte[] page : tabPages) metaInPages.add(page);
            nextPage += tabPages.size();
        }

        //then write the rest tables
        writing:
            for (String table : tabIndPages.keySet()) {
                for (String s : inOut.getDatabase().catalog) {
                    if (s.equals(table)) continue writing;
                }

                tabPages = tableToPages(table, nextPage);
                for (byte[] page : tabPages) metaInPages.add(page);
                nextPage += tabPages.size();
            }

        return metaInPages;
    }

    /**
     * Transforms byte representation of metadata into writable pages
     * @param table
     * @param nextPage
     * @return
     */
    private LinkedList<byte[]> tableToPages(String table, int nextPage) {
        if (!tabIndPages.containsKey(table)) return null; //TODO exception

        LinkedList<byte[]> pages = new LinkedList<>();
        Byte[] tab = tableToBytes(table);
        int size = inOut.getDatabase().getHeader().getPageSize() - 10;
        int fullPages = tab.length / size;
        int restBytes = tab.length % size;

        //write all full pages
        int cut = 0;
        for (int i = 0; i < fullPages; i++) {
            byte[] page = new byte[inOut.getDatabase().getHeader().getPageSize()];
            int from = i * size - cut;

            //search for bytes to cut
            cut = 0;
            for (int j = from + size - 1; ;j--) {
                if (tab[j] == Constants.ARGUMENTS_SEGMENT_START ||
                        tab[j] == Constants.ARGUMENTS_SEPARATOR ||
                        tab[j] == Constants.END_OF_ARGUMENTS_SEGMENT ||
                        tab[j] == Constants.END_OF_BLOCK) break;
                else cut++;
            }

            System.arraycopy(tab, from, page, 0, size - cut);

            if (nextPage < metaPages.size()) { //if there are reserved pages for meta write pointer to the next
                byte[] num = String.valueOf(metaPages.get(nextPage)).getBytes();
                nextPage++;
                System.arraycopy(num, 0, page, size, num.length);
            } else { //else if there are no reserved pages, add new page
                int next = inOut.getDatabase().getHeader().getPageCount();

                byte[] num = String.valueOf(next).getBytes();
                System.arraycopy(num, 0, page, size, num.length);

                addMetaPage(next);
                inOut.getDatabase().getHeader().incrementPageCount();
                nextPage++;
            }
            pages.add(page);
        }

        //write rest bytes
        restBytes += cut;

        if (restBytes > size) {
            int from = tab.length - restBytes;
            restBytes -= size;
            byte[] page = new byte[inOut.getDatabase().getHeader().getPageSize()];

            //search for bytes to cut
            cut = 0;
            for (int j = from + size - 1; ;j--) {
                if (tab[j] == Constants.ARGUMENTS_SEGMENT_START ||
                        tab[j] == Constants.ARGUMENTS_SEPARATOR ||
                        tab[j] == Constants.END_OF_ARGUMENTS_SEGMENT ||
                        tab[j] == Constants.END_OF_BLOCK) break;
                else cut++;
            }
            restBytes += cut;

            System.arraycopy(tab, from, page, 0, size - cut);

            if (nextPage < metaPages.size()) { //if there are reserved pages for meta write pointer to the next
                byte[] num = String.valueOf(metaPages.get(nextPage)).getBytes();
                nextPage++;
                System.arraycopy(num, 0, page, size, num.length);
            } else { //else if there are no reserved pages, add new page
                int next = inOut.getDatabase().getHeader().getPageCount();

                byte[] num = String.valueOf(next).getBytes();
                System.arraycopy(num, 0, page, size, num.length);

                addMetaPage(next);
                inOut.getDatabase().getHeader().incrementPageCount();
                nextPage++;
            }
            pages.add(page);
        }

        if (restBytes > 0) {
            byte[] page = new byte[restBytes];
            int from = tab.length - restBytes;

            System.arraycopy(tab, from, page, 0, page.length);

            if (nextPage < metaPages.size()) { //if there are reserved pages for meta write pointer to the next
                byte[] num = String.valueOf(metaPages.get(nextPage)).getBytes();
                nextPage++;
                System.arraycopy(num, 0, page, size, num.length);
            } else { //else if there are no reserved pages, add new page
                int next = inOut.getDatabase().getHeader().getPageCount();

                byte[] num = String.valueOf(next).getBytes();
                System.arraycopy(num, 0, page, size, num.length);

                addMetaPage(next);
                inOut.getDatabase().getHeader().incrementPageCount();
                nextPage++;
            }

            pages.add(page);
        }

        return pages;
    }

    /**
     * Transforms information about table pages into byte array
     * @param table
     * @return
     */
    private Byte[] tableToBytes(String table) {
        ArrayList<Byte> tab = new ArrayList<>();
        TreeSet<Integer> pages = tabIndPages.get(table);

        for (byte b : table.getBytes()) tab.add(b);
        tab.add(Constants.ARGUMENTS_SEGMENT_START);
        for (int page : pages) {
            for (byte b : String.valueOf(page).getBytes()) tab.add(b);
            tab.add(Constants.ARGUMENTS_SEPARATOR);
        }
        tab.add(Constants.END_OF_ARGUMENTS_SEGMENT);

        Byte[] byteTable = new Byte[tab.size()];
        byteTable = tab.toArray(byteTable);
        return byteTable;
    }
}
