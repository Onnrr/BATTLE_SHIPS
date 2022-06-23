package models;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AcceptConnections implements Runnable {
    private Thread t;
    ServerSocket ss;
    Socket s;
    ArrayList<Connection> connections;

    public AcceptConnections(ServerSocket serverSocket, ArrayList<Connection> connections) {
        ss = serverSocket;
        this.connections = connections;
    }

    @Override
    public void run() {
        while (true) {
            try {
                s = ss.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Connection newConnection = new Connection(s);
            connections.add(newConnection);
        }

    }

    public void start() {
        System.out.println("Starting Accept Connections");
        if (t == null) {
            t = new Thread(this, "Accept");
            t.start();
        }
    }

}
