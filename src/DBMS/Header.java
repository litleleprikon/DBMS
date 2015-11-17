package DBMS;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Header {
    private int pageSize;
    private int pageCount;
    private int metaPage;

    public Header(int pageSize, int pageCount, int metaPage) {
        this.pageSize = pageSize;
        this.pageCount = pageCount;
        this.metaPage = metaPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getMetaPage() {
        return metaPage;
    }
}
