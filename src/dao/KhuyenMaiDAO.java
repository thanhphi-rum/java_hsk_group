package dao;

import connect.ConnectDB;
import entity.KhuyenMai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    // Thêm Khuyến Mãi
    public boolean themKhuyenMai(KhuyenMai khuyenMai) {
        String sql = "INSERT INTO KhuyenMai (MaKhuyenMai, TenKhuyenMai, MoTa, PhanTramGiam, NgayBatDau, NgayKetThuc) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khuyenMai.getMaKhuyenMai());
            ps.setString(2, khuyenMai.getTenKhuyenMai());
            ps.setString(3, khuyenMai.getMoTa());
            ps.setDouble(4, khuyenMai.getPhanTramGiam());
            ps.setDate(5, java.sql.Date.valueOf(khuyenMai.getNgayBatDau()));
            ps.setDate(6, java.sql.Date.valueOf(khuyenMai.getNgayKetThuc()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiểm tra Mã Khuyến Mãi đã tồn tại
    public boolean kiemTraMaKhuyenMai(String maKhuyenMai) {
        String sql = "SELECT COUNT(*) FROM KhuyenMai WHERE MaKhuyenMai = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy tất cả Khuyến Mãi
    public List<KhuyenMai> layTatCaKhuyenMai() {
        List<KhuyenMai> danhSachKhuyenMai = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maKhuyenMai = rs.getString("MaKhuyenMai");
                String tenKhuyenMai = rs.getString("TenKhuyenMai");
                String moTa = rs.getString("MoTa");
                double phanTramGiam = rs.getDouble("PhanTramGiam");
                java.sql.Date bd = rs.getDate("NgayBatDau");
                java.sql.Date kt = rs.getDate("NgayKetThuc");
                LocalDate ngayBatDau = (bd == null) ? null : bd.toLocalDate();
                LocalDate ngayKetThuc = (kt == null) ? null : kt.toLocalDate();
                danhSachKhuyenMai.add(new KhuyenMai(maKhuyenMai, tenKhuyenMai, moTa, phanTramGiam, ngayBatDau, ngayKetThuc));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachKhuyenMai;
    }
}
