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
    final String DISCONNECT = "disconnect";
    final String INVITE = "invite";
    final String DECLINE_GAME = "decline_game";
    final String ACCEPT_GAME = "accept_game";
    final String DELETE_ACCOUNT = "delete";
    final String SUCCESS = "SUCCESS";
    final String FAIL = "FAIL";
    final String INFO = "INFO";
    final String CONNECTED = "CONNECTED";
    final String DISCONNECTED = "DISCONNECTED";
    final String INVITATION = "INVITATION";
    final String DECLINED = "DECLINED";
    final String ACCEPTED = "ACCEPTED";
    final String RANK = "RANK";
    final String GAME_START = "GAME_START";
    final String GAME_FAIL = "GAME_FAIL";

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
                    // TODO clear fields account created warning
                    connectedUsers.remove(this);
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
                        String onlinePlayers = "ONLINE_PLAYERS ";
                        for (int i = 0; i < connectedUsers.size(); i++) {
                            if (connectedUsers.get(i).getUserID() != id) {
                                connectedUsers.get(i).sendMessage(CONNECTED + " " + userID + " " + userName);

                                onlinePlayers += connectedUsers.get(i).getUserID() + " "
                                        + connectedUsers.get(i).getName() + " "
                                        + connectedUsers.get(i).getStatus() + " ";
                            }
                        }
                        out.println(onlinePlayers);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    set = database.getRank();
                    String rank = RANK + " ";
                    try {
                        while (set.next()) {
                            rank += set.getString("userName") + " " + set.getInt("userScore") + " ";
                        }
                        out.println(rank);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    out.println(FAIL);
                    connectedUsers.remove(this);
                    System.out.println("Login failed");
                }
            } else if (result[0].equals(DISCONNECT)) {
                connectedUsers.remove(this);
                for (Connection c : connectedUsers) {
                    c.sendMessage(DISCONNECTED + " " + getName());
                }
            } else if (result[0].equals(INVITE)) {
                int inviteID = Integer.parseInt(result[1]);
                for (Connection c : connectedUsers) {
                    if (c.getUserID() == inviteID) {
                        c.sendMessage(INVITATION + " " + getUserID() + " " + getName());
                    }
                }
            } else if (result[0].equals(DECLINE_GAME)) {
                int inviteID = Integer.parseInt(result[2]);
                for (Connection c : connectedUsers) {
                    if (c.getUserID() == inviteID) {
                        c.sendMessage(DECLINED + " " + result[1]);
                    }
                }
            } else if (result[0].equals(ACCEPT_GAME)) {
                for (Connection c : connectedUsers) {
                    if (c.getUserID() == Integer.parseInt(result[2]) && c.getStatus() == 0) {
                        out.println(GAME_START + " " + result[2]);
                        c.sendMessage(GAME_START + " " + getUserID());
                        return;
                    }
                }
                out.println(GAME_FAIL + " " + result[2]);
            } else if (result[0].equals(DELETE_ACCOUNT)) {
                if (database.deleteAccount(getUserID())) {
                    connectedUsers.remove(this);
                    System.out.println("Deleted");
                    for (Connection c : connectedUsers) {
                        c.sendMessage(DISCONNECTED + " " + getName());
                    }
                }
            } else {
                System.out.println(command);
            }
        }
    }

}
