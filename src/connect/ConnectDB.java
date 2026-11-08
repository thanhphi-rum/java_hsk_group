package connect;

import util.ProjectPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Mở kết nối JDBC. Cấu hình được đọc bởi DatabaseConfig, không phụ thuộc working directory. */
public class ConnectDB {
    public static Connection getConnection() throws SQLException {
        DatabaseConfig config = DatabaseConfig.load();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Thiếu JDBC driver SQL Server trong thư mục lib.", ex);
        }
        try {
            if (config.url().contains("integratedSecurity=true")) {
                Path nativeLibrary = ProjectPaths.resolve("mssql-jdbc_auth-13.6.0.x64.dll");
                if (!Files.isRegularFile(nativeLibrary))
                    throw new SQLException(
                            "Thiếu thư viện Windows Authentication: " + nativeLibrary);
                try {
                    System.load(nativeLibrary.toString());
                } catch (UnsatisfiedLinkError ex) {
                    throw new SQLException(
                            "Không tải được thư viện Windows Authentication x64.", ex);
                }
                return DriverManager.getConnection(config.url());
            }
            return DriverManager.getConnection(config.url(), config.user(), config.password());
        } catch (SQLException ex) {
            // Phân biệt lỗi mạng với lỗi tài khoản; không gộp mọi lỗi thành 'không kết nối được'.
            String reason;
            if (ex.getErrorCode() == 18456) reason = "SQL Server từ chối tài khoản hoặc mật khẩu.";
            else if (ex.getErrorCode() == 4060)
                reason = "Database không tồn tại hoặc tài khoản chưa có quyền truy cập.";
            else reason = "Không mở được kết nối SQL Server. Kiểm tra dịch vụ, TCP/IP và db.url.";
            throw new SQLException(
                    reason
                            + "\nCấu hình đang dùng: "
                            + config.source()
                            + "\nMã SQL: "
                            + ex.getErrorCode()
                            + " / SQLState: "
                            + ex.getSQLState(),
                    ex.getSQLState(),
                    ex.getErrorCode(),
                    ex);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
    }

    /** Chạy riêng lớp này để kiểm tra kết nối; không thay đổi dữ liệu. */
    public static void main(String[] args) throws SQLException {
        try (Connection conn = getConnection()) {
            System.out.println("SQL Server connected: " + conn.getCatalog());
            System.out.println("Configuration: " + DatabaseConfig.load().source());
        }
    }
}
