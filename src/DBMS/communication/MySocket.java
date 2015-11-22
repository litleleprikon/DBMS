package DBMS.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by litleleprikon on 16/11/15.
 * MySocket class for DBMS
 */
public class MySocket {
    private static final int SOCK_NUM = 5657;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public MySocket() {
        try {
            serverSocket = new ServerSocket(SOCK_NUM);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + SOCK_NUM);
            System.exit(1);
        }
        System.out.println("Waiting for communication.....");
        try {
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Connection is established");
        } catch (IOException e) {
            System.err.println("Accept failed.");
        }
    }


    public String waitMessage() throws IOException {
        return in.readLine();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() {
        try {
            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error while closing connection");
        }
    }
}