package models;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    ServerSocket ss;
    Socket s;
    DatabaseConnection database;
    BufferedReader in;
    PrintWriter out;

    public Server() {
        database = new DatabaseConnection();
        database.createUser("eu", "ppp", "maill");
    }

    class Connection {
        String IP;
        BufferedReader in;
        PrintWriter out;

    }

    @Override
    public void run() {

    }

    public void start() {

    }
}
