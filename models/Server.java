package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
    private Thread t;
    ServerSocket ss;
    Socket s;
    DatabaseConnection database;
    BufferedReader in;
    PrintWriter out;
    ArrayList<Connection> connectedUsers;

    public Server() {
        database = new DatabaseConnection();
        connectedUsers = new ArrayList<Connection>();
        try {
            ss = new ServerSocket(9999);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        AcceptConnections accept = new AcceptConnections(ss, connectedUsers);
        accept.start();

        start();
    }

    @Override
    public void run() {
        String message;

        try {
            while ((message = in.readLine()) != null) {
                execute(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        System.out.println("Starting Accept Connections");
        if (t == null) {
            t = new Thread(this, "Accept");
            t.start();
        }
    }

    public void broadcast(String message) {
        for (Connection con : connectedUsers) {
            if (con != null) {
                con.sendMessage(message);
            }
        }
    }

    public void privateSend(int index, String message) {
        connectedUsers.get(index).sendMessage(message);
    }

    private void execute(String message) {
        // TODO execute commands
    }

}
