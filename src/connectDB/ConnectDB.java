package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Kết nối SQL Server; thông tin kết nối lấy từ biến môi trường. */
public class ConnectDB {
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
                String url = System.getenv("RAPPHIM_DB_URL");
                String user = System.getenv("RAPPHIM_DB_USER");
                String password = System.getenv("RAPPHIM_DB_PASSWORD");
                if (url == null || url.trim().isEmpty()
                        || user == null || password == null) {
                    throw new IllegalStateException(
                            "Hãy thiết lập RAPPHIM_DB_URL, RAPPHIM_DB_USER và "
                                    + "RAPPHIM_DB_PASSWORD trước khi mở ứng dụng.");
                }
                connection = DriverManager.getConnection(url, user, password);
            }
            return connection;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không kết nối được SQL Server.", ex);
        }
    }

    public static synchronized void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                throw new IllegalStateException("Không đóng được kết nối database.", ex);
            } finally {
                connection = null;
            }
        }
    }
}
