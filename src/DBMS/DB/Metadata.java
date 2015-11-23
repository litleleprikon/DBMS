package DBMS.DB;

import java.io.IOException;
import java.util.*;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Metadata {
    private FileIO inOut;
    private Map<String, Integer> pages;
    private ArrayList<Integer> metaPages;

    public Metadata(FileIO inOut) {
        this.inOut = inOut;
        pages = new HashMap<>();
        metaPages = new ArrayList<>();
    }

    public void addPage(String to, int page) {
        pages.putIfAbsent(to, page);
    }

    public void changePage(String to, int page) {
        pages.replace(to, page);
    }

    public void removePage(String from, int page) {
        pages.remove(from, page);
    }

    /**
     * Parses metadata from given @param page starting from given position @param i
     * @param page - page represented by array of bytes
     * @param i - position on page
     */
    protected void parseMeta(byte[] page, int i) {
        if (page[i] == Constants.END_OF_BLOCK || page[i] == 0) {
            int nextPage = FileIO.getNextPage(page);

            if (nextPage != -1)
                addMetaPage(nextPage);
            return;
        }

        //Parse table/index name
        String curStr = inOut.getWord(page, i, Constants.ARGUMENTS_SEGMENT_START);
        i += curStr.length();

        //parse page number
        i++;
        String sNumber = inOut.getWord(page, i, Constants.END_OF_ARGUMENTS_SEGMENT);
        int number = Integer.valueOf(sNumber);
        i += sNumber.length();

        addPage(curStr, number);

        //check page
        i++;
        if (page[i] == 0 || i == page.length - FileIO.pointerSize) {
            int nextPage = FileIO.getNextPage(page);

            if (nextPage != -1) try {
                page = inOut.readPage(nextPage);
                addMetaPage(nextPage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            i = 0;
        }

        parseMeta(page, i);
    }



    public void addMetaPage(int page) {
        metaPages.add(page);
    }

    public ArrayList<Integer> getMetaPages() {
        return metaPages;
    }

    public Map<String, Integer> getPages() {
        return pages;
    }

    public int getPage(String table) {
        return pages.get(table);
    }

    /**
     * Transforms metadata to writable form
     * @return list of pages of metadata
     */
    protected ArrayList<byte[]> toWritingForm() {
        ArrayList<byte[]> pages = new ArrayList<>();
        byte[] tab = metaToBytes();

        int size = inOut.getDatabase().getHeader().getPageSize() - FileIO.pointerSize;
        int fullPages = tab.length / size;
        int restBytes = tab.length % size;

        int nextPage = 1;

        //get full pages
        int cut = 0;
        for (int i = 0; i < fullPages; i++) {
            byte[] page = new byte[inOut.getDatabase().getHeader().getPageSize()];
            int from = i * size - cut;

            //search for bytes to cut
            cut = 0;
            for (int j = from + size - 1; ;j--) {
                if (tab[j] == Constants.ARGUMENTS_SEGMENT_START ||
                        tab[j] == Constants.END_OF_ARGUMENTS_SEGMENT ||
                        tab[j] == Constants.END_OF_BLOCK) break;
                else cut++;
            }

            System.arraycopy(tab, from, page, 0, size - cut);


            if (nextPage < metaPages.size()) { //if there are reserved pages for meta write pointer to the next
                byte[] num = String.valueOf(metaPages.get(nextPage)).getBytes();
                System.arraycopy(num, 0, page, size, num.length);

            } else { //else if there are no reserved pages, add new page
                int next = inOut.getDatabase().getHeader().getPageCount();

                byte[] num = String.valueOf(next).getBytes();
                System.arraycopy(num, 0, page, size, num.length);

                addMetaPage(next);
                inOut.getDatabase().getHeader().incrementPageCount();
            }
            nextPage++;
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
                        tab[j] == Constants.END_OF_ARGUMENTS_SEGMENT ||
                        tab[j] == Constants.END_OF_BLOCK) break;
                else cut++;
            }
            restBytes += cut;

            System.arraycopy(tab, from, page, 0, size - cut);

            if (nextPage < metaPages.size()) { //if there are reserved pages for meta write pointer to the next
                byte[] num = String.valueOf(metaPages.get(nextPage)).getBytes();

                System.arraycopy(num, 0, page, size, num.length);

            } else { //else if there are no reserved pages, add new page
                int next = inOut.getDatabase().getHeader().getPageCount();

                byte[] num = String.valueOf(next).getBytes();
                System.arraycopy(num, 0, page, size, num.length);

                addMetaPage(next);
                inOut.getDatabase().getHeader().incrementPageCount();
            }
            nextPage++;
            pages.add(page);
        }

        if (restBytes > 0) {
            byte[] page = new byte[inOut.getDatabase().getHeader().getPageSize()];
            int from = tab.length - restBytes;

            System.arraycopy(tab, from, page, 0, restBytes);

            if (nextPage < metaPages.size()) { //if there are reserved pages for meta write pointer to the next
                byte[] num = String.valueOf(metaPages.get(nextPage)).getBytes();
                System.arraycopy(num, 0, page, size, num.length);
            } else { //else if there are no reserved pages, add new page
                int next = inOut.getDatabase().getHeader().getPageCount();

                byte[] num = String.valueOf(next).getBytes();
                System.arraycopy(num, 0, page, size, num.length);

                addMetaPage(next);
                inOut.getDatabase().getHeader().incrementPageCount();
            }

            pages.add(page);
        }

        return pages;
    }

    /**
     * Transforms all meta into byte array
     * @return
     */
    private byte[] metaToBytes() {
        ArrayList<Byte> tab = new ArrayList<>();

        for (String str : pages.keySet()) {
            for (byte b : str.getBytes()) tab.add(b);

            tab.add(Constants.ARGUMENTS_SEGMENT_START);

            for (byte b : String.valueOf(pages.get(str)).getBytes()) tab.add(b);

            tab.add(Constants.END_OF_ARGUMENTS_SEGMENT);
        }
        tab.add(Constants.END_OF_BLOCK);

        byte[] byteTable = new byte[tab.size()];
        for (int i = 0; i < byteTable.length; i++) {
            byteTable[i] = tab.get(i);
        }
        return byteTable;
    }
}
