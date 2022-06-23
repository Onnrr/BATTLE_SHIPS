package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private Thread t;
    ServerSocket ss;
    Socket s;
    DatabaseConnection database;
    ArrayList<Connection> connectedUsers;

    public Server() {
        database = new DatabaseConnection();
        connectedUsers = new ArrayList<Connection>();
        try {
            ss = new ServerSocket(9999);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AcceptConnections accept = new AcceptConnections();
        accept.start();

    }

    public void broadcast(String message) {
        for (Connection con : connectedUsers) {
            if (con != null) {
                con.sendMessage(message);
            }
        }
    }

    public void privateSend(int id, String message) {
        // TODO
        // connectedUsers.get(index).sendMessage(message);
    }

    private void execute(String message) {
        // TODO execute commands
    }

    class Connection implements Runnable {
        BufferedReader in;
        PrintWriter out;
        Socket socket;

        public Connection(Socket s) {
            socket = s;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                e.printStackTrace();
            }
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
                message = in.readLine();
                // while ((message = in.readLine()) != null) { // SUS CODE
                System.out.println(message);
                // execute(message);
                // }
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

    public class AcceptConnections implements Runnable {
        private Thread t;
        Socket s;

        @Override
        public void run() {
            while (true) {
                try {
                    s = ss.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Connection newConnection = new Connection(s);
                newConnection.start();
                connectedUsers.add(newConnection);
                System.out.println("New Connection");
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

}
