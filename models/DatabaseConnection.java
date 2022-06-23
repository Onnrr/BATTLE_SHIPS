package models;

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
        try {
            statement.executeUpdate(
                    "INSERT INTO users VALUES ( 0, '" + userName + "', '" + password + "', 0, NULL,'" + mail + "');");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database failed adding account");
            return false;
        }
    }

    private static int generateID() {
        // TODO
        return 1;
    }
}
