package dao;

import entity.Ve;
import entity.GheSuatChieu;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/** Truy vấn vé, suất chiếu và ghế còn trống. */
public class Ve_DAO {
    private final Connection connection;

    public Ve_DAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<String> laySuatChieuDangMoBan() throws SQLException {
        ArrayList<String> danhSach = new ArrayList<String>();
        String sql = "SELECT sc.maSuatChieu, p.tenPhim, sc.gioBatDau, sc.giaVe "
                + "FROM SuatChieu sc JOIN Phim p ON p.maPhim = sc.maPhim "
                + "WHERE sc.gioBatDau > GETDATE() AND sc.trangThai = N'Đang Mở Bán' "
                + "ORDER BY sc.gioBatDau";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                String gioChieu = result.getTimestamp("gioBatDau").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
                danhSach.add(result.getString("maSuatChieu") + " | "
                        + result.getString("tenPhim") + " | " + gioChieu + " | "
                        + result.getBigDecimal("giaVe").toPlainString() + " đồng/vé");
            }
        }
        return danhSach;
    }

    public BigDecimal layGiaVeTheoSuat(String maSuatChieu) throws SQLException {
        String sql = "SELECT giaVe FROM SuatChieu WHERE maSuatChieu = ? "
                + "AND gioBatDau > GETDATE() AND trangThai = N'Đang Mở Bán'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maSuatChieu);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getBigDecimal("giaVe");
                }
            }
        }
        throw new SQLException("Suất chiếu không tồn tại hoặc đã ngừng bán.");
    }

    /** Lấy ghế đang trống; giữ khóa đến khi transaction đặt vé kết thúc. */
    public ArrayList<String> layGheTrongSuat(String maSuatChieu) throws SQLException {
        ArrayList<String> danhSach = new ArrayList<String>();
        String sql = "SELECT gs.maGhe FROM GheSuatChieu gs WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE gs.maSuatChieu = ? AND gs.trangThai = N'Trống' "
                + "AND NOT EXISTS (SELECT 1 FROM Ve v WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE v.maSuatChieu = gs.maSuatChieu AND v.maGhe = gs.maGhe "
                + "AND v.trangThai IN (N'Đã giữ', N'Đã bán')) ORDER BY gs.maGhe";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maSuatChieu);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    danhSach.add(result.getString("maGhe"));
                }
            }
        }
        return danhSach;
    }

    /** Lấy toàn bộ ghế của suất để vẽ sơ đồ, gồm cả ghế đã được giữ/đặt. */
    public ArrayList<String> layTatCaGheTheoSuat(String maSuatChieu) throws SQLException {
        ArrayList<String> danhSach = new ArrayList<String>();
        String sql = "SELECT maGhe FROM GheSuatChieu WHERE maSuatChieu = ? ORDER BY maGhe";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maSuatChieu);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    danhSach.add(result.getString("maGhe"));
                }
            }
        }
        return danhSach;
    }
    public boolean giuGhe(String maSuatChieu, String maGhe) throws SQLException {
        String sql = "UPDATE GheSuatChieu SET trangThai = N'Đang giữ' "
                + "WHERE maSuatChieu = ? AND maGhe = ? AND trangThai = N'Trống'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maSuatChieu);
            statement.setString(2, maGhe);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean kiemTraGheDaDatTrongSuat(String maSuatChieu, String maGhe)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maSuatChieu = ? AND maGhe = ? "
                + "AND trangThai IN (N'Đã giữ', N'Đã bán')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maSuatChieu);
            statement.setString(2, maGhe);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }

    /** Lưu vé với giá đã đọc từ bảng SuatChieu, không nhận giá tự nhập trên GUI. */
    public boolean themVe(String maVe, String maSuatChieu, String maGhe,
                          BigDecimal giaTien, String trangThai) throws SQLException {
        String sql = "INSERT INTO Ve (maVe, maSuatChieu, maGhe, giaTien, trangThai) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maVe);
            statement.setString(2, maSuatChieu);
            statement.setString(3, maGhe);
            statement.setBigDecimal(4, giaTien);
            statement.setString(5, trangThai);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean huyVeTheoPhieu(String maPhieuDatVe) throws SQLException {
        String sql = "UPDATE Ve SET trangThai = N'Đã hủy' WHERE maVe IN "
                + "(SELECT maVe FROM ChiTietPhieuDatVe WHERE maPhieuDatVe = ?) "
                + "AND trangThai = N'Đã giữ'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuDatVe);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean traGheTheoPhieu(String maPhieuDatVe) throws SQLException {
        String sql = "UPDATE gs SET trangThai = N'Trống' FROM GheSuatChieu gs "
                + "JOIN Ve v ON v.maSuatChieu = gs.maSuatChieu AND v.maGhe = gs.maGhe "
                + "JOIN ChiTietPhieuDatVe ct ON ct.maVe = v.maVe "
                + "WHERE ct.maPhieuDatVe = ? AND gs.trangThai = N'Đang giữ'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuDatVe);
            return statement.executeUpdate() > 0;
        }
    }

    public ArrayList<Ve> layVeTheoPhieu(String maPhieuDatVe) throws SQLException {
        ArrayList<Ve> danhSach = new ArrayList<Ve>();
        String sql = "SELECT v.maVe, v.maSuatChieu, v.maGhe, v.giaTien, v.trangThai "
                + "FROM Ve v JOIN ChiTietPhieuDatVe ct ON ct.maVe = v.maVe "
                + "WHERE ct.maPhieuDatVe = ? ORDER BY v.maVe";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuDatVe);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Ve ve = new Ve();
                    ve.setMaVe(result.getString("maVe"));
                    ve.setGiaTien(result.getBigDecimal("giaTien"));
                    ve.setTrangThai(result.getString("trangThai"));
                    ve.setGheSuatChieu(new GheSuatChieu(
                            result.getString("maSuatChieu"),
                            result.getString("maGhe"),
                            result.getString("trangThai")));
                    danhSach.add(ve);
                }
            }
        }
        return danhSach;
    }
}
