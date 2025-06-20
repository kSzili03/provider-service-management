package DBProject.DBConnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/dbproject";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Connection object
    private static Connection connection = null;

    // Returns a connection to the database
    public static Connection getConnection() throws SQLException {
        // If connection is null or closed, create a new one
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        // Return the active connection
        return connection;
    }
}