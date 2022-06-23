package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    ServerSocket ss;
    private Thread t;
    Socket s;
    DatabaseConnection database;
    ExecutorService pool;
    ArrayList<Connection> connectedUsers;

    public Server() {
        database = new DatabaseConnection();
        connectedUsers = new ArrayList<Connection>();
    }

    public void start() {
        System.out.println("Starting Server");
        if (t == null) {
            t = new Thread(this, "Server");
            t.start();
        }
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (true) {
                s = ss.accept();
                Connection newConnection = new Connection(s);
                connectedUsers.add(newConnection);
                pool.execute(newConnection);
                System.out.println(connectedUsers.size());
                System.out.println("New Connection");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (Connection con : connectedUsers) {
            if (con != null) {
                con.sendMessage(message);
            }
        }
    }

    class Connection implements Runnable {
        BufferedReader in;
        PrintWriter out;
        Socket socket;

        public Connection(Socket s) {
            socket = s;
            System.out.println("Constructor");
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public String getMessage() {
            try {
                return in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("This runs");
                String message;
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                message = in.readLine();
                System.out.println(message);

                while ((message = in.readLine()) != null) { // SUS CODE
                    System.out.println(message);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void start() {
            System.out.println("Starting Listening messages");
            if (t == null) {
                t = new Thread(this, "Messages");
                t.start();
            }
        }
    }

}
