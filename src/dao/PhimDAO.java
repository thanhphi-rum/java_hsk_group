package dao;

import connect.ConnectDB;
import entity.Phim;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhimDAO {

    /**
     * Biểu thức SQL gộp tên các thể loại của phim (từ bảng Phim_TheLoai + TheLoai), cách nhau dấu phẩy.
     * Dùng trong câu SELECT có bảng Phim đặt bí danh là p; phim chưa có thể loại thì trả về NULL.
     */
    public static final String SQL_TEN_THE_LOAI =
            "STUFF((SELECT ', ' + t.TenTheLoai FROM Phim_TheLoai pt JOIN TheLoai t ON t.MaTheLoai = pt.MaTheLoai "
            + "WHERE pt.MaPhim = p.MaPhim ORDER BY t.MaTheLoai FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, '')";

    /** Phim kèm tên thể loại (cột TheLoai lấy từ bảng Phim_TheLoai). */
    private static final String SELECT_PHIM = "SELECT p.*, " + SQL_TEN_THE_LOAI + " AS TheLoai FROM Phim p";

    /** Trả về mã thể loại ứng với tên thể loại; null nếu tên đó không có trong bảng TheLoai. */
    private String maTheLoai(Connection conn, String tenTheLoai) throws SQLException {
        String ten = tenTheLoai == null ? "" : tenTheLoai.trim();
        try (PreparedStatement ps = conn.prepareStatement("SELECT MaTheLoai FROM TheLoai WHERE TenTheLoai = ?")) {
            ps.setString(1, ten);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Ghi thể loại của phim vào Phim_TheLoai: xóa thể loại cũ rồi thêm thể loại đã chọn. */
    private void ghiTheLoai(Connection conn, String maPhim, String tenTheLoai) throws SQLException {
        String ma = maTheLoai(conn, tenTheLoai);
        if (ma == null) {
            throw new SQLException("Thể loại \"" + tenTheLoai + "\" không có trong bảng TheLoai");
        }
        try (PreparedStatement xoa = conn.prepareStatement("DELETE FROM Phim_TheLoai WHERE MaPhim = ?")) {
            xoa.setString(1, maPhim);
            xoa.executeUpdate();
        }
        try (PreparedStatement them = conn.prepareStatement("INSERT INTO Phim_TheLoai (MaPhim, MaTheLoai) VALUES (?, ?)")) {
            them.setString(1, maPhim);
            them.setString(2, ma);
            them.executeUpdate();
        }
    }

    // Chuyển 1 dòng ResultSet thành Phim
    private Phim taoPhim(ResultSet rs) throws SQLException {
        String maPhim = rs.getString("MaPhim");
        String tenPhim = rs.getString("TenPhim");
        String theLoai = rs.getString("TheLoai");
        if (theLoai == null) theLoai = "";
        int thoiLuong = rs.getInt("ThoiLuong");
        String trangThai = rs.getString("TrangThai");
        String moTa = rs.getString("MoTa");
        String daoDien = rs.getString("DaoDien");
        String dienVien = rs.getString("DienVien");
        String gioiHanTuoi = rs.getString("GioiHanTuoi");
        java.sql.Date nkc = rs.getDate("NgayKhoiChieu");
        LocalDate ngayKhoiChieu = (nkc == null) ? null : nkc.toLocalDate();
        return new Phim(maPhim, tenPhim, theLoai, thoiLuong, trangThai, moTa, daoDien, dienVien, gioiHanTuoi, ngayKhoiChieu);
    }

    // Thêm Phim (thông tin phim + thể loại trong cùng một transaction)
    public boolean themPhim(Phim phim) {
        String sql = "INSERT INTO Phim (MaPhim, TenPhim, ThoiLuong, TrangThai, MoTa, DaoDien, DienVien, GioiHanTuoi, NgayKhoiChieu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn == null) return false;
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, phim.getMaPhim());
                ps.setString(2, phim.getTenPhim());
                ps.setInt(3, phim.getThoiLuong());
                ps.setString(4, phim.getTrangThai());
                ps.setString(5, phim.getMoTa());
                ps.setString(6, phim.getDaoDien());
                ps.setString(7, phim.getDienVien());
                ps.setString(8, phim.getGioiHanTuoi());
                ps.setDate(9, phim.getNgayKhoiChieu() == null ? null : java.sql.Date.valueOf(phim.getNgayKhoiChieu()));
                if (ps.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
                ghiTheLoai(conn, phim.getMaPhim(), phim.getTheLoai());
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Cập nhật các cột của bảng Phim; KHÔNG đổi thể loại (dùng khi sửa nhanh giới hạn tuổi, ngày, trạng thái ngay trên bảng). */
    public boolean suaPhim(Phim phim) {
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn == null) return false;
            return capNhatCot(conn, phim);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Cập nhật các cột của bảng Phim và đổi thể loại thành thể loại trong phim.getTheLoai(), cùng một transaction. */
    public boolean suaPhimVaTheLoai(Phim phim) {
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn == null) return false;
            conn.setAutoCommit(false);
            try {
                if (!capNhatCot(conn, phim)) {
                    conn.rollback();
                    return false;
                }
                ghiTheLoai(conn, phim.getMaPhim(), phim.getTheLoai());
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean capNhatCot(Connection conn, Phim phim) throws SQLException {
        String sql = "UPDATE Phim SET TenPhim = ?, ThoiLuong = ?, TrangThai = ?, MoTa = ?, DaoDien = ?, DienVien = ?, GioiHanTuoi = ?, NgayKhoiChieu = ? WHERE MaPhim = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phim.getTenPhim());
            ps.setInt(2, phim.getThoiLuong());
            ps.setString(3, phim.getTrangThai());
            ps.setString(4, phim.getMoTa());
            ps.setString(5, phim.getDaoDien());
            ps.setString(6, phim.getDienVien());
            ps.setString(7, phim.getGioiHanTuoi());
            ps.setDate(8, phim.getNgayKhoiChieu() == null ? null : java.sql.Date.valueOf(phim.getNgayKhoiChieu()));
            ps.setString(9, phim.getMaPhim());
            return ps.executeUpdate() > 0;
        }
    }

    // Xóa Phim (xóa thể loại của phim trước, cùng một transaction)
    public boolean xoaPhim(String maPhim) {
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn == null) return false;
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement xoaTL = conn.prepareStatement("DELETE FROM Phim_TheLoai WHERE MaPhim = ?")) {
                    xoaTL.setString(1, maPhim);
                    xoaTL.executeUpdate();
                }
                boolean xoaDuoc;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Phim WHERE MaPhim = ?")) {
                    ps.setString(1, maPhim);
                    xoaDuoc = ps.executeUpdate() > 0;
                }
                if (xoaDuoc) conn.commit(); else conn.rollback();
                return xoaDuoc;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy tất cả Phim
    public List<Phim> layTatCaPhim() {
        List<Phim> danhSachPhim = new ArrayList<>();
        String sql = SELECT_PHIM + " ORDER BY p.MaPhim";
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                danhSachPhim.add(taoPhim(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachPhim;
    }

    public Phim layPhimTheoMa(String maPhim) {
        String sql = SELECT_PHIM + " WHERE p.MaPhim = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return taoPhim(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Tìm kiếm Phim theo từ khóa (mã phim, tên phim hoặc thể loại)
    public List<Phim> timKiemPhim(String keyword) {
        List<Phim> danhSachPhim = new ArrayList<>();
        String sql = SELECT_PHIM + " WHERE p.MaPhim LIKE ? OR p.TenPhim LIKE ? OR EXISTS ("
                + "SELECT 1 FROM Phim_TheLoai pt JOIN TheLoai t ON t.MaTheLoai = pt.MaTheLoai "
                + "WHERE pt.MaPhim = p.MaPhim AND t.TenTheLoai LIKE ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern); // Tìm theo mã phim
            ps.setString(2, pattern); // Tìm theo tên phim
            ps.setString(3, pattern); // Tìm theo thể loại
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSachPhim.add(taoPhim(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachPhim;
    }
}
