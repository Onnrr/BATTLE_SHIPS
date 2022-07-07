package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    Connection connection;
    Statement statement;
    ResultSet resultSet;

    public DatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/BattleShips", "root", "onur2001");
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.out.println("Database connection failed");
        }
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            // TODO
            e.printStackTrace();
        }
    }

    public boolean createUser(String userName, String password, String mail) {
        ResultSet set;
        try {
            set = statement.executeQuery("SELECT userName FROM users where userName = '" + userName + "';");
            if (set.next()) {
                return false;
            }
            set = statement.executeQuery("SELECT userMail FROM users where userMail = '" + mail + "';");
            if (set.next()) {
                return false;
            }
            statement.executeUpdate(
                    "INSERT INTO users VALUES ( 0, '" + userName + "', '" + password + "', 0, NULL,'" + mail + "');");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database failed adding account");
            return false;
        }
    }

    public boolean checkUser(String userName, String password) {
        ResultSet set;
        try {
            set = statement.executeQuery("SELECT userPassword FROM users where userName = '" + userName + "';");
            if (!set.next()) {
                return false;
            }
            if (password.equals(set.getString("userPassword"))) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet getUserInfo(String userName) {
        ResultSet set;
        try {
            set = statement.executeQuery("SELECT * FROM users where userName = '" + userName + "';");
            return set;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteAccount(int id) {
        try {
            statement.executeUpdate("DELETE FROM users WHERE userID = " + id + ";");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet getRank() {
        ResultSet set;
        try {
            set = statement.executeQuery("SELECT * FROM users ORDER BY userScore DESC LIMIT 5;");
            return set;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setScore(int id, int score) {
        try {
            statement.executeUpdate("UPDATE users SET userScore = " + score + " WHERE userID = " + id + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean changePassword(int id, String exPassword, String newPassword) {
        ResultSet set;
        try {
            set = statement.executeQuery("SELECT userPassword FROM users where userID = " + id + ";");
            if (!set.next()) {
                System.out.println("a");
                return false;
            }
            if (!exPassword.equals(set.getString("userPassword"))) {
                System.out.println("b");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("c");
            e.printStackTrace();
        }
        try {
            statement
                    .executeUpdate("UPDATE users SET userPassword = '" + newPassword + "' WHERE userID = " + id + ";");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPassword(int id, String mail, int newPassword) {
        ResultSet set;
        try {
            set = statement.executeQuery("SELECT userMail FROM users where userID = " + id + ";");
            if (!set.next()) {
                System.out.println("a");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("c");
            e.printStackTrace();
        }
        try {
            statement
                    .executeUpdate("UPDATE users SET userPassword = '" + newPassword + "' WHERE userID = " + id + ";");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
