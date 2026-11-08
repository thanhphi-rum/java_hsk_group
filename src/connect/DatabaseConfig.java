package connect;

import util.ProjectPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;

/** Chỉ đọc cấu hình; không mở kết nối và không in mật khẩu ra log. */
public record DatabaseConfig(String url, String user, String password, Path source) {
    public static DatabaseConfig load() throws SQLException {
        Path file = ProjectPaths.resolve("db.properties");
        if (!Files.isRegularFile(file)) {
            throw new SQLException(
                    "Thiếu cấu hình SQL tại: "
                            + file
                            + "\n"
                            + "Sao chép db.properties của bản đang chạy hoặc điền"
                            + " db.properties.example. Không dùng nguyên tài khoản mẫu"
                            + " YOUR_SQL_LOGIN.");
        }

        Properties values = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            values.load(input);
        } catch (IOException ex) {
            throw new SQLException("Không đọc được cấu hình: " + file, ex);
        }

        String url = values.getProperty("db.url", "").trim();
        String user = values.getProperty("db.user", "").trim();
        String password = System.getenv("CINEMA_DB_PASSWORD");
        if (password == null) password = values.getProperty("db.password", "");
        if (!url.startsWith("jdbc:sqlserver://")) {
            throw new SQLException("db.url phải là kết nối SQL Server trong: " + file);
        }
        if (!url.contains("integratedSecurity=true")
                && (user.isBlank() || user.equals("YOUR_SQL_LOGIN") || password.isBlank())) {
            throw new SQLException("Chưa điền tài khoản SQL thực tế trong: " + file);
        }
        return new DatabaseConfig(url, user, password, file);
    }

    /** Tránh vô tình lộ mật khẩu nếu đối tượng cấu hình được ghi log. */
    @Override
    public String toString() {
        return "DatabaseConfig[source=" + source + ", credentials=hidden]";
    }
}
