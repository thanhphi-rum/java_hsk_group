package dao;

import connect.ConnectDB;
import entity.Phim;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhimDAO {

    // Chuyển 1 dòng ResultSet thành Phim
    private Phim taoPhim(ResultSet rs) throws SQLException {
        String maPhim = rs.getString("MaPhim");
        String tenPhim = rs.getString("TenPhim");
        String theLoai = rs.getString("TheLoai");
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

    // Thêm Phim
    public boolean themPhim(Phim phim) {
        String sql = "INSERT INTO Phim (MaPhim, TenPhim, TheLoai, ThoiLuong, TrangThai, MoTa, DaoDien, DienVien, GioiHanTuoi, NgayKhoiChieu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phim.getMaPhim());
            ps.setString(2, phim.getTenPhim());
            ps.setString(3, phim.getTheLoai());
            ps.setInt(4, phim.getThoiLuong());
            ps.setString(5, phim.getTrangThai());
            ps.setString(6, phim.getMoTa());
            ps.setString(7, phim.getDaoDien());
            ps.setString(8, phim.getDienVien());
            ps.setString(9, phim.getGioiHanTuoi());
            ps.setDate(10, phim.getNgayKhoiChieu() == null ? null : java.sql.Date.valueOf(phim.getNgayKhoiChieu()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Sửa Phim
    public boolean suaPhim(Phim phim) {
        String sql = "UPDATE Phim SET TenPhim = ?, TheLoai = ?, ThoiLuong = ?, TrangThai = ?, MoTa = ?, DaoDien = ?, DienVien = ?, GioiHanTuoi = ?, NgayKhoiChieu = ? WHERE MaPhim = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phim.getTenPhim());
            ps.setString(2, phim.getTheLoai());
            ps.setInt(3, phim.getThoiLuong());
            ps.setString(4, phim.getTrangThai());
            ps.setString(5, phim.getMoTa());
            ps.setString(6, phim.getDaoDien());
            ps.setString(7, phim.getDienVien());
            ps.setString(8, phim.getGioiHanTuoi());
            ps.setDate(9, phim.getNgayKhoiChieu() == null ? null : java.sql.Date.valueOf(phim.getNgayKhoiChieu()));
            ps.setString(10, phim.getMaPhim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa Phim
    public boolean xoaPhim(String maPhim) {
        String sql = "DELETE FROM Phim WHERE MaPhim = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhim);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy tất cả Phim
    public List<Phim> layTatCaPhim() {
        List<Phim> danhSachPhim = new ArrayList<>();
        String sql = "SELECT * FROM Phim";
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
        String sql = "SELECT * FROM Phim WHERE MaPhim = ?";
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

    // Tìm kiếm Phim theo từ khóa
    public List<Phim> timKiemPhim(String keyword) {
        List<Phim> danhSachPhim = new ArrayList<>();
        String sql = "SELECT * FROM Phim WHERE MaPhim LIKE ? OR TenPhim LIKE ? OR TheLoai LIKE ?";
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
