package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static final String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyBanVePhimMoi";
    private static final String username = "sa";
    private static final String password = "sapassword";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load SQL Server JDBC Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Establish the connection
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Kết nối tới SQL Server thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy driver JDBC");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối tới cơ sở dữ liệu");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đóng kết nối thành công");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Test the connection
        Connection connection = getConnection();
        closeConnection(connection);
    }
}