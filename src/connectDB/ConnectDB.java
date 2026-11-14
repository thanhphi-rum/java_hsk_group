package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
          + "databaseName=RapPhim_Member2_Test;"
          + "encrypt=true;trustServerCertificate=true";

    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";

    private static final ConnectDB instance = new ConnectDB();
    private static Connection connection;

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() {
        getConnection();
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Không kết nối được SQL Server.", e);
        }
    }

    public static synchronized void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không đóng được kết nối database.", e);
        }
    }
}