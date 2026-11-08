package dao;

import connect.ConnectDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Chỉ thống kê từ HoaDon: đơn giữ chỗ chưa thu tiền không được tính vào doanh thu. */
public class ThongKeDAO {
    public record DongThongKe(String period, BigDecimal revenue) {}

    public List<DongThongKe> doanhThu(String period) throws SQLException {
        // Biểu thức được chọn từ danh sách cố định, không nối chuỗi nhập tự do vào SQL.
        String group =
                switch (period) {
                    case "Ngày" -> "CAST(NgayLap AS DATE)";
                    case "Tuần" ->
                            "DATEADD(day, -(DATEDIFF(day, 0, NgayLap) % 7), CAST(NgayLap AS DATE))";
                    case "Tháng" -> "CONVERT(char(7),NgayLap,120)";
                    case "Năm" -> "YEAR(NgayLap)";
                    default -> throw new IllegalArgumentException("Khoảng thời gian không hợp lệ.");
                };
        String sql =
                "SELECT "
                        + group
                        + ", SUM(TongTien) FROM HoaDon GROUP BY "
                        + group
                        + " ORDER BY "
                        + group;
        List<DongThongKe> result = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
                Statement statement = conn.createStatement();
                ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next())
                result.add(new DongThongKe(rows.getString(1), rows.getBigDecimal(2)));
        }
        return result;
    }
}
