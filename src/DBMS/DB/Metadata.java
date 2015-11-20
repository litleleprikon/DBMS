package DBMS.DB;

import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Types.FixedVarChar;
import DBMS.DB.InnerStructure.Types.Int;
import DBMS.DB.InnerStructure.Types.Type;
import DBMS.DB.InnerStructure.Types.VarChar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Metadata {
    FileIO inOut;
    Map<String, TreeSet<Integer>> tabIndPages;
    private boolean wasChanged = false;

    public Metadata(FileIO inOut) {
        this.inOut = inOut;
        tabIndPages = new HashMap<>();
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
        if (page[i] == SeparatingConstants.END_OF_BLOCK) return;

        boolean inProgress = curStr != null;
        if (!inProgress) { //no parsing is started or previous segment was finished
            //Parse table name
            curStr = getWord(page, i, SeparatingConstants.ARGUMENTS_SEGMENT_START);
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
            if (page[i + 1] == SeparatingConstants.END_OF_ARGUMENTS_SEGMENT) {inProgress = false; break;} //parsing for current structure is finished
            if (page[i + 1] == 0 || i + 1 == page.length - 10) break; //parsing for current structure is not finished, need to move to the next page

            //parse page number
            i++;
            String sNumber = getWord(page, i, SeparatingConstants.ARGUMENTS_SEPARATOR);
            int number = Integer.valueOf(sNumber);
            i += sNumber.length();

            addPage(curStr, number);
        }

        if (inProgress) { //continue parsing for current structure on the next page
            byte[] nextPage = new byte[10];
            System.arraycopy(page, page.length - 10, nextPage, 0, nextPage.length);
            try {
                parseMeta(inOut.readPage(inOut.parseInt(nextPage)), 0, curStr);
            } catch (IOException e) {
                e.printStackTrace(); //TODO exception handle
            }

        } else { //parse next structure
            if (page[i + 1] == 0 || i + 1 == page.length - 10) { //moving to next page if needed
                byte[] nextPage = new byte[10];
                System.arraycopy(page, page.length - 10, nextPage, 0, nextPage.length);
                try {
                    page = inOut.readPage(inOut.parseInt(nextPage));
                } catch (IOException e) {
                    e.printStackTrace(); //TODO exception handle
                }
                i = 0;
            }
            parseMeta(page, i == 0 ? i : ++i, null);
        }
    }
}
