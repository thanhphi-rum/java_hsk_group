package dao;

import connect.ConnectDB;

import entity.ChiTietHoaDon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Đọc dữ liệu đã lưu; không dùng nội dung tạm trên màn chọn ghế để in hóa đơn. */
public class ChiTietHoaDonDAO {
    public ChiTietHoaDon layTheoMa(String id) throws SQLException {
        String headerSql =
                """
                SELECT h.*, n.TenNhanVien, k.TenKhachHang FROM HoaDon h
                JOIN NhanVien n ON n.MaNhanVien=h.MaNhanVien
                LEFT JOIN KhachHang k ON k.MaKhachHang=h.MaKhachHang WHERE h.MaHoaDon=?
                """;
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement statement = conn.prepareStatement(headerSql)) {
            statement.setString(1, id);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) throw new SQLException("Hóa đơn chưa được lưu hoặc đã bị xóa.");
                return new ChiTietHoaDon(
                        id,
                        row.getTimestamp("NgayLap").toLocalDateTime(),
                        row.getString("TenNhanVien"),
                        row.getString("TenKhachHang"),
                        row.getString("PhuongThucThanhToan"),
                        row.getBigDecimal("TongTien"),
                        layVe(conn, id));
            }
        }
    }

    private List<ChiTietHoaDon.DongVe> layVe(Connection conn, String id) throws SQLException {
        String sql =
                """
SELECT p.TenPhim, pc.TenPhong, s.GioBatDau, g.ViTri, v.MaGhe, v.GiaTien
FROM Ve v JOIN SuatChieu s ON s.MaSuatChieu=v.MaSuatChieu
JOIN Phim p ON p.MaPhim=s.MaPhim JOIN PhongChieu pc ON pc.MaPhongChieu=s.MaPhongChieu
JOIN Ghe g ON g.MaGhe=v.MaGhe WHERE v.MaHoaDon=? ORDER BY g.ViTri
""";
        List<ChiTietHoaDon.DongVe> result = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet row = statement.executeQuery()) {
                while (row.next()) {
                    String seat = row.getString("ViTri");
                    result.add(
                            new ChiTietHoaDon.DongVe(
                                    row.getString("TenPhim"),
                                    row.getString("TenPhong"),
                                    row.getTimestamp("GioBatDau").toLocalDateTime(),
                                    seat == null ? row.getString("MaGhe") : seat,
                                    row.getBigDecimal("GiaTien")));
                }
            }
        }
        return List.copyOf(result);
    }
}
