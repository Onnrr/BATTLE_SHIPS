package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    //// COMMANDS ////
    final String CREATE = "create";
    final String LOGIN_CHECK = "login";
    final String SUCCESS = "SUCCESS";
    final String FAIL = "FAIL";
    final String INFO = "INFO";

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
        String name;
        int userID;
        int status;
        /*
         * 0 online
         * 1 in game
         */

        public Connection(Socket s) {
            socket = s;
            userID = -1;
            name = "";
            status = 0;
        }

        public void setUserID(int id) {
            userID = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getUserID() {
            return userID;
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
                String message;
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while ((message = in.readLine()) != null) {
                    decode(message);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void decode(String command) {
            String[] result = command.split(" ");

            if (result[0].equals(CREATE)) {
                if (database.createUser(result[1], result[2], result[3])) {
                    out.println(SUCCESS);
                    System.out.println("New Account Created");
                } else {
                    out.println(FAIL);
                }
            } else if (result[0].equals(LOGIN_CHECK)) {
                if (database.checkUser(result[1], result[2])) {
                    System.out.println("Successful Login");
                    ResultSet set = database.getUserInfo(result[1]);
                    String userInfo = "";

                    try {
                        set.next();
                        int id = set.getInt("userID");
                        String userName = set.getString("userName");
                        int score = set.getInt("userScore");
                        String mail = set.getString("userMail");

                        setUserID(id);
                        setName(userName);

                        userInfo += INFO + " " + id + " " + userName + " " + score + " " + mail;
                        out.println(userInfo);
                        broadcast("JOINED" + userName);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    out.println(FAIL);
                    System.out.println("Login failed");
                }
            }
        }
    }

}
