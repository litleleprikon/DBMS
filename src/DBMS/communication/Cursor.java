package DBMS.communication;

import DBMS.DB.InnerStructure.Table;
import DBMS.DB.InnerStructure.Tuple;
import DBMS.DB.InnerStructure.Types.Type;
import javafx.scene.control.Tab;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class Cursor {

    public static class NothingFetchException extends Exception {
    }

    private int id;
    private Table lastResults;
    private Connection connection;

    public Cursor(int id, Connection connection) {
        this.id = id;
        this.connection = connection;
    }

    public JSONObject fetch(int num) throws NothingFetchException{
        if(lastResults == null) {
            throw new NothingFetchException();
        }
        JSONObject result = new JSONObject();
        JSONArray data = new JSONArray();

        for(int counter = 0; counter < num-1 && lastResults.hasNext(); counter++) {
            data.add(convertTuple(lastResults.next()));
        }
        if(!lastResults.hasNext()) {
            lastResults = null;
        }
        result.put("fields", getFields());
        result.put("row_count", lastResults.size());
        result.put("data", data);
        return result;
    }

    public JSONArray getFields() {
        return lastResults.getArguments().keySet().stream().collect(Collectors.toCollection(JSONArray::new));
    }

    public void setLastResults(Table results) {
        lastResults = results;
    }

    private JSONArray convertTuple(Tuple row) {
        JSONArray result = new JSONArray();
        for(Type value : row.getValues().values()) {
            result.add(value.getData());
        }
        return result;
    }
}
