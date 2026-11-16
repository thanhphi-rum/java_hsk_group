package connect;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Kết nối SQL Server.
 *
 * Ý tưởng: mở kết nối tới SQL Server rất chậm, nên thay vì mở mới cho mỗi câu truy vấn,
 * ta giữ lại các kết nối đã dùng xong (gọi là "pool") để lần sau lấy ra dùng luôn.
 *
 * Cách dùng trong các lớp DAO / GUI KHÔNG đổi:
 *     try (Connection conn = ConnectDB.getConnection()) { ... }
 * Khi kết thúc khối try, conn.close() chỉ trả kết nối về pool chứ không đóng thật.
 */
public class ConnectDB {
    private static final String url = cauHinh("db.url", "CINEMA_DB_URL", "jdbc:sqlserver://localhost:1433;databaseName=QuanLyBanVePhimMoi;loginTimeout=8");
    private static final String username = cauHinh("db.user", "CINEMA_DB_USER", "sa");
    private static final String password = cauHinh("db.password", "CINEMA_DB_PASSWORD", "sapassword");

    /** Số kết nối rảnh tối đa được giữ lại. */
    private static final int TOI_DA = 5;

    /** Các kết nối thật đang rảnh, chờ được dùng lại. */
    private static final Deque<Connection> dangRanh = new ArrayDeque<>();

    /** Lấy kết nối từ pool hoặc mở mới; báo SQLException khi không kết nối được. */
    public static Connection getConnection() throws SQLException {
        try {
            Connection that = layKetNoiRanh();
            if (that == null) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                that = DriverManager.getConnection(url, username, password);
            }
            return boc(that);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy driver JDBC SQL Server", e);
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close(); // chỉ trả về pool
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối");
                e.printStackTrace();
            }
        }
    }

    /** Mở sẵn một kết nối lúc khởi động (chạy ở luồng nền) để lần dùng đầu tiên không phải chờ. */
    public static void khoiDong() {
        try {
            closeConnection(getConnection());
        } catch (SQLException e) {
            System.err.println("Không mở sẵn được kết nối: " + e.getMessage());
        }
    }

    /** Lấy ra một kết nối rảnh còn dùng được; kết nối đã hỏng thì bỏ đi. Không còn cái nào thì trả null. */
    private static Connection layKetNoiRanh() {
        while (true) {
            Connection c;
            synchronized (dangRanh) {
                c = dangRanh.pollFirst();
            }
            if (c == null) return null;
            try {
                if (!c.isClosed() && c.isValid(2)) return c;
            } catch (SQLException e) {
                // kết nối hỏng -> bỏ qua, thử cái khác
            }
            dongThat(c);
        }
    }

    /** Trả kết nối về pool (sau khi hủy giao dịch dang dở), hoặc đóng hẳn nếu pool đã đầy. */
    private static void traVePool(Connection that) {
        try {
            if (!that.getAutoCommit()) { // giao dịch chưa commit -> hủy, rồi bật lại chế độ tự commit
                that.rollback();
                that.setAutoCommit(true);
            }
        } catch (SQLException e) {
            dongThat(that);
            return;
        }
        synchronized (dangRanh) {
            if (dangRanh.size() < TOI_DA) {
                dangRanh.addFirst(that);
                return;
            }
        }
        dongThat(that);
    }

    /**
     * Bọc kết nối thật trong một "vỏ" (java.lang.reflect.Proxy): mọi lệnh đều chuyển cho kết nối thật,
     * riêng close() thì thay bằng traVePool(). Nhờ vậy code cũ dùng try-with-resources vẫn chạy đúng.
     */
    private static Connection boc(Connection that) {
        boolean[] daTra = {false}; // tránh trả về pool 2 lần nếu close() bị gọi lại
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) {
                        if (!daTra[0]) {
                            daTra[0] = true;
                            traVePool(that);
                        }
                        return null;
                    }
                    if (method.getName().equals("isClosed")) return daTra[0] || that.isClosed();
                    if (daTra[0]) throw new SQLException("Kết nối đã được đóng");
                    try {
                        return method.invoke(that, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause(); // ném lại đúng lỗi SQL gốc
                    }
                });
    }

    private static void dongThat(Connection c) {
        try {
            c.close();
        } catch (SQLException e) {
            // không quan trọng
        }
    }

    private static String cauHinh(String thuocTinh, String bienMoiTruong, String macDinh) {
        String giaTri = System.getProperty(thuocTinh);
        if (giaTri == null) giaTri = System.getenv(bienMoiTruong);
        return giaTri == null ? macDinh : giaTri;
    }

    public static void main(String[] args) throws SQLException {
        long t0 = System.nanoTime();
        Connection c1 = getConnection();
        long t1 = System.nanoTime();
        closeConnection(c1);
        Connection c2 = getConnection();
        long t2 = System.nanoTime();
        closeConnection(c2);
        System.out.println("Lần 1 (mở mới): " + (t1 - t0) / 1_000_000 + " ms");
        System.out.println("Lần 2 (dùng lại): " + (t2 - t1) / 1_000_000 + " ms");
    }
}
