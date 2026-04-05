package gamersync.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = System.getenv().getOrDefault(
        "GAMERSYNC_DB_URL",
        "jdbc:mysql://localhost:3306/GamerSync?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );
    private static final String USER = System.getenv().getOrDefault("GAMERSYNC_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("GAMERSYNC_DB_PASSWORD", "435162ns");

    private static Connection connection = null;

    // Singleton pattern - only one connection at a time
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "MySQL JDBC driver not found. Add mysql-connector-j to classpath.",
                    e
                );
            }

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
