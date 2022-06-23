package models;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    ServerSocket ss;
    Socket s;
    DatabaseConnection database;

    public Server() {
        database = new DatabaseConnection();
    }
}
