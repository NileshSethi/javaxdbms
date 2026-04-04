package gamersync.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gamersync";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv().getOrDefault("435162ns", "435162ns");

    private static Connection connection = null;

    // Singleton pattern - only one connection at a time
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                throw new SQLException(
                    "Unable to connect to MySQL at " + URL + " with user '" + USER
                        + "'. Update gamersync/db/DBConnection.java and ensure MySQL is running.",
                    e
                );
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}
