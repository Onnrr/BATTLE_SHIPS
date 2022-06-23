package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    Connection connection;

    public DatabaseConnection() {
        // try {
        // Class.forName("com.mysql.jdbc.Driver");
        // } catch (ClassNotFoundException e1) {
        // e1.printStackTrace();
        // }
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/BattleShips", "root", "onur2001");
            System.out.println("Connected to database");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database connection failed");
        }
    }
}
