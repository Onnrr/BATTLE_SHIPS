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
    final String NEW_MESSAGE = "message";
    final String LEAVE = "leave";
    final String READY = "ready";
    final String CAN_LEAVE = "LEAVE";
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
    final String SEND_MESSAGE = "MESSAGE";
    final String ONLINE_PLAYERS = "ONLINE_PLAYERS";
    final String OPPONENT_DISCONNECTED = "OPPONENT_DISCONNECTED";
    final String OPPONENT_READY = "OPPONENT_READY";
    final String PLAYER_GUESS = "guess";
    final String GUESS = "GUESS";
    final String PLAYER_HIT = "hit";
    final String PLAYER_MISS = "miss";
    final String HIT = "HIT";
    final String MISS = "MISS";
    final String TO_GAME = "to_game";
    final String DUMMY = "";

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
        int opponentID;
        boolean stop;
        /*
         * 0 online
         * 1 in game
         */

        public Connection(Socket s) {
            socket = s;
            userID = -1;
            opponentID = -1;
            name = "";
            status = 0;
            stop = false;
        }

        public void setOpponentID(int id) {
            opponentID = id;
        }

        public int getOpponentID() {
            return opponentID;
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

                while (!stop) {
                    message = in.readLine();
                    decode(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            stop = true;
        }

        public void decode(String command) {
            String[] result = command.split(" ");
            System.out.println(command);
            if (result[0].equals(CREATE)) {
                if (database.createUser(result[1], result[2], result[3])) {
                    out.println(SUCCESS);
                    System.out.println("New Account Created");
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
                        String onlinePlayers = ONLINE_PLAYERS;
                        for (int i = 0; i < connectedUsers.size(); i++) {
                            if (connectedUsers.get(i).getUserID() != id) {
                                connectedUsers.get(i).sendMessage(CONNECTED + " " + userID + " " + userName);

                                onlinePlayers += " " + connectedUsers.get(i).getUserID() + " "
                                        + connectedUsers.get(i).getName() + " "
                                        + connectedUsers.get(i).getStatus();
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
                    if (c.getUserID() == getOpponentID()) {
                        c.setStatus(0);
                        c.setOpponentID(-1);
                        c.sendMessage(OPPONENT_DISCONNECTED);
                        String onlinePlayers = ONLINE_PLAYERS;
                        for (Connection con : connectedUsers) {
                            if (c.getUserID() == con.getUserID()) {
                                continue;
                            }
                            onlinePlayers += " " + con.getUserID() + " "
                                    + con.getName() + " "
                                    + con.getStatus();
                        }
                        c.sendMessage(onlinePlayers);
                    }
                    c.sendMessage(DISCONNECTED + " " + getUserID() + " " + getName());
                }
                stop();
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
                        out.println(GAME_START + " " + c.getUserID() + " " + c.getName());
                        c.sendMessage(GAME_START + " " + getUserID() + " " + getName());
                        c.setOpponentID(getUserID());
                        setOpponentID(c.getUserID());
                        c.setStatus(1);
                        setStatus(1);
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
            } else if (result[0].equals(NEW_MESSAGE)) {
                String message = command.substring(8);
                for (Connection c : connectedUsers) {
                    if (getUserID() != c.getUserID()) {
                        c.sendMessage(SEND_MESSAGE + " " + getUserID() + " " + getName() + " " + message);
                    }
                }
            } else if (result[0].equals(LEAVE)) {
                setStatus(0);
                String onlinePlayers = "ONLINE_PLAYERS";
                out.println(CAN_LEAVE);
                for (Connection con : connectedUsers) {
                    if (getUserID() == con.getUserID()) {
                        continue;
                    }
                    onlinePlayers += " " + con.getUserID() + " " + con.getName() + " " + con.getStatus();
                }
                out.println(onlinePlayers);
                System.out.println("Sent message to " + getName());
                for (Connection c : connectedUsers) {
                    if (getOpponentID() == c.getUserID()) {
                        c.setStatus(0);
                        c.setOpponentID(-1);
                        c.sendMessage(OPPONENT_DISCONNECTED);
                        String online = "ONLINE_PLAYERS";
                        for (Connection con : connectedUsers) {
                            if (c.getUserID() == con.getUserID()) {
                                continue;
                            }
                            online += " " + con.getUserID() + " "
                                    + con.getName() + " "
                                    + con.getStatus();
                        }
                        c.sendMessage(online);
                    }
                }
                setOpponentID(-1);

            } else if (result[0].equals(READY)) {
                // Sending message to opponent
                for (Connection c : connectedUsers) {
                    if (c.getUserID() == opponentID) {
                        c.sendMessage(OPPONENT_READY);
                    }
                }
            } else if (result[0].equals(PLAYER_GUESS)) {
                for (Connection c : connectedUsers) {
                    if (c.getUserID() == opponentID) {
                        c.sendMessage(GUESS + " " + result[1] + " " + result[2]);
                    }
                }
            } else if (result[0].equals(PLAYER_HIT)) {
                for (Connection c : connectedUsers) {
                    if (c.getUserID() == opponentID) {
                        c.sendMessage(HIT);
                    }
                }
            } else if (result[0].equals(PLAYER_MISS)) {
                for (Connection c : connectedUsers) {
                    if (c.getUserID() == opponentID) {
                        c.sendMessage(MISS);
                    }
                }
            } else if (result[0].equals(TO_GAME)) {
                out.println(DUMMY);
                for (Connection c : connectedUsers) {
                    if (c.getUserID() == opponentID) {
                        c.sendMessage(DUMMY);
                    }
                }
            } else {
                System.out.println(command);
            }
        }
    }

}
