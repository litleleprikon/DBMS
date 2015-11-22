package DBMS.communication;

import DBMS.DB.InnerStructure.Table;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Connection extends Thread {
    private MySocket socket;
    private HashMap<Integer, Cursor> cursors;
    private JSONParser parser = new JSONParser();
    private int lastId;
    private int lastCursor;

    private static final String TYPE_FETCH = "FETCH";
    private static final String TYPE_QUERY = "QUERY";
    private static final String TYPE_CREATE_CURSOR = "CREATE_CURSOR";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAIL = "FAIL";

    public Connection() {
        socket = new MySocket();
        cursors = new HashMap<>();
        lastId = 0;
    }

    public void handleError(String message) {
        System.err.println(message);
        JSONObject response = new JSONObject();
        response.put("status", STATUS_FAIL);
        response.put("error", message);
        socket.sendMessage(response.toJSONString());
    }

    private void successResponse(JSONObject response) {
        response.put("status", STATUS_SUCCESS);
        socket.sendMessage(response.toJSONString());
    }

    public String waitQuery() {
        String message;
        JSONObject parsedMessage;
        while (true) {
            try {
                message = socket.waitMessage();
            } catch (IOException e) {
                System.out.println("IOException");
                continue;
            }
            if(message == null) {
                break;
            }
            try {
                parsedMessage = (JSONObject) parser.parse(message);
            } catch (ParseException e) {
                handleError("Incoming message parse error");
                continue;
            }
            String queryType = (String)parsedMessage.get("qtype");
            switch (queryType) {
                case TYPE_CREATE_CURSOR:
                    createCursor(parsedMessage);
                    break;
                case TYPE_FETCH:
                    fetch(parsedMessage);
                    break;
                case TYPE_QUERY:
                    lastCursor = Integer.parseInt(parsedMessage.get("id").toString());
                    return parsedMessage.get("query").toString();
            }
        }
        return null;
    }

    public void setDataToCursor(Table data) {
        cursors.get(lastCursor).setLastResults(data);
    }

    private void fetch(JSONObject data) {
        int id = (int)data.get("id");
        int num = (int)data.get("num");
        fetch(id, num);
    }

    public void createCursor(JSONObject obj) {
        Cursor cursor = new Cursor(lastId, this);
        cursors.put(lastId, cursor);
        cursorCreatedResponse(lastId);
        System.out.println("Created cursor. ID: " + lastId);
        lastId++;
    }

    private void cursorCreatedResponse(int id) {
        JSONObject response = new JSONObject();
        response.put("cur_id", id);
        successResponse(response);
    }

    private void fetch(int id, int num) {
        try {
            JSONObject data = cursors.get(id).fetch(num);
            successResponse(data);
        } catch (Cursor.NothingFetchException e) {
            handleError("Nothing to fetch");
            return;
        }
    }

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        JSONObject parsedMessage = null;
        try {
            parsedMessage = (JSONObject) parser.parse("{\"hello\": \"world\"}");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void close() {
        socket.close();
    }
}