package dao;

import connect.ConnectDB;

import entity.GheBanVe;
import entity.LuaChonBanVe;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Các truy vấn đọc dữ liệu cho màn bán vé. Không hiển thị hộp thoại hoặc xử lý nút bấm. */
public class DanhMucBanVeDAO {
    public List<LuaChonBanVe> phimDangChieu() throws SQLException {
        return readChoices(
                "SELECT MaPhim, TenPhim FROM Phim WHERE TrangThai=N'Đang Chiếu' ORDER BY TenPhim");
    }

    public List<LuaChonBanVe> khachHang() throws SQLException {
        return readChoices("SELECT MaKhachHang, TenKhachHang FROM KhachHang ORDER BY TenKhachHang");
    }

    private List<LuaChonBanVe> readChoices(String sql) throws SQLException {
        List<LuaChonBanVe> result = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
                Statement statement = conn.createStatement();
                ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next())
                result.add(new LuaChonBanVe(rows.getString(1), rows.getString(2), BigDecimal.ZERO));
        }
        return result;
    }

    /** Chỉ đưa lên màn hình khuyến mãi còn hiệu lực và có tỷ lệ giảm hợp lệ. */
    public List<LuaChonBanVe> khuyenMai() throws SQLException {
        String sql =
                """
                SELECT MaKhuyenMai, MoTa, PhanTramGiam FROM KhuyenMai
                WHERE NgayBatDau <= CAST(GETDATE() AS DATE)
                  AND NgayKetThuc >= CAST(GETDATE() AS DATE)
                  AND PhanTramGiam BETWEEN 0 AND 100
                """;
        List<LuaChonBanVe> result = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
                Statement statement = conn.createStatement();
                ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next())
                result.add(
                        new LuaChonBanVe(
                                rows.getString(1),
                                rows.getString(2) + " (" + rows.getBigDecimal(3) + "%)",
                                rows.getBigDecimal(3)));
        }
        return result;
    }

    /** Không cho chọn suất đã bắt đầu hoặc đã ngừng mở bán. */
    public List<LuaChonBanVe> suatChieu(String movieId) throws SQLException {
        String sql =
                """
                SELECT s.MaSuatChieu, s.GioBatDau, p.TenPhong, s.GiaVe
                FROM SuatChieu s JOIN PhongChieu p ON p.MaPhongChieu=s.MaPhongChieu
                WHERE s.MaPhim=? AND s.GioBatDau>GETDATE() AND s.TrangThai=N'Đang Mở Bán'
                  AND p.TrangThai=N'Hoạt động'
                ORDER BY s.GioBatDau
                """;
        List<LuaChonBanVe> result = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, movieId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next())
                    result.add(
                            new LuaChonBanVe(
                                    rows.getString(1),
                                    rows.getTimestamp(2)
                                                    .toLocalDateTime()
                                                    .format(
                                                            java.time.format.DateTimeFormatter
                                                                    .ofPattern("dd/MM HH:mm"))
                                            + " - "
                                            + rows.getString(3),
                                    rows.getBigDecimal(4)));
            }
        }
        return result;
    }

    public List<GheBanVe> gheTheoSuat(String showId) throws SQLException {
        String sql =
                """
                SELECT g.MaGhe, g.ViTri, gs.TrangThai FROM Ghe_SuatChieu gs
                JOIN Ghe g ON g.MaGhe=gs.MaGhe WHERE gs.MaSuatChieu=? ORDER BY g.ViTri, g.MaGhe
                """;
        List<GheBanVe> result = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, showId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    String id = rows.getString(1);
                    String position = rows.getString(2);
                    result.add(
                            new GheBanVe(
                                    id,
                                    position == null || position.isBlank() ? id : position,
                                    rows.getString(3)));
                }
            }
        }
        return result;
    }
}
