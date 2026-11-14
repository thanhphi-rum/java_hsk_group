package dao;

import connect.ConnectDB;
import entity.KhachHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    // Thêm khách hàng mới
    public boolean themKhachHang(KhachHang khachHang) {
        String sql = "INSERT INTO KhachHang (MaKhachHang, TenKhachHang, SoDienThoai, Email, DiemTichLuy) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khachHang.getMaKhachHang());
            ps.setString(2, khachHang.getTenKhachHang());
            ps.setString(3, khachHang.getSoDienThoai());
            ps.setString(4, khachHang.getEmail());
            ps.setInt(5, khachHang.getDiemTichLuy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Sửa thông tin khách hàng
    public boolean suaKhachHang(KhachHang khachHang) {
        String sql = "UPDATE KhachHang SET TenKhachHang = ?, SoDienThoai = ?, Email = ?, DiemTichLuy = ? WHERE MaKhachHang = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khachHang.getTenKhachHang());
            ps.setString(2, khachHang.getSoDienThoai());
            ps.setString(3, khachHang.getEmail());
            ps.setInt(4, khachHang.getDiemTichLuy());
            ps.setString(5, khachHang.getMaKhachHang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa khách hàng
    public boolean xoaKhachHang(String maKhachHang) {
        String sql = "DELETE FROM KhachHang WHERE MaKhachHang = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy tất cả khách hàng
    public List<KhachHang> layTatCaKhachHang() {
        List<KhachHang> danhSachKhachHang = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang";
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String maKhachHang = rs.getString("MaKhachHang");
                String tenKhachHang = rs.getString("TenKhachHang");
                String soDienThoai = rs.getString("SoDienThoai");
                String email = rs.getString("Email");
                int diemTichLuy = rs.getInt("DiemTichLuy");
                danhSachKhachHang.add(new KhachHang(maKhachHang, tenKhachHang, soDienThoai, email, diemTichLuy));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachKhachHang;
    }

    // Lấy khách hàng theo mã
    public KhachHang layKhachHangTheoMa(String maKhachHang) {
        String sql = "SELECT * FROM KhachHang WHERE MaKhachHang = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tenKhachHang = rs.getString("TenKhachHang");
                    String soDienThoai = rs.getString("SoDienThoai");
                    String email = rs.getString("Email");
                    int diemTichLuy = rs.getInt("DiemTichLuy");
                    return new KhachHang(maKhachHang, tenKhachHang, soDienThoai, email, diemTichLuy);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
