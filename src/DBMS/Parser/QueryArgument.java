package DBMS.Parser;

public class QueryArgument {

    private String data;
    private String alias;



    private String condition;

    private QueryArgument compare;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public QueryArgument getCompare() {
        return compare;
    }

    public void setCompare(QueryArgument compare) {
        this.compare = compare;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public QueryArgument(String data) {
        this.data = data;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int compareTo(QueryArgument to) {
        String compareWith = this.getData().toString();
        String compareTo  = to.getData().toString();
        return compareWith.compareTo(compareTo);
    }
}
