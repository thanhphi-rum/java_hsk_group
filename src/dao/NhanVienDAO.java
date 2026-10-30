package dao;

import connect.ConnectDB;
import entity.NhanVien;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    // Thêm Nhân Viên
    public boolean themNhanVien(NhanVien nhanVien) {
        String sql = "INSERT INTO NhanVien (MaNhanVien, TenNhanVien, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, ChucVu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVien.getMaNhanVien());
            ps.setString(2, nhanVien.getTenNhanVien());
            ps.setDate(3, java.sql.Date.valueOf(nhanVien.getNgaySinh()));
            ps.setString(4, nhanVien.getGioiTinh());
            ps.setString(5, nhanVien.getSoDienThoai());
            ps.setString(6, nhanVien.getEmail());
            ps.setString(7, nhanVien.getDiaChi());
            ps.setString(8, nhanVien.getChucVu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Sửa Nhân Viên
    public boolean suaNhanVien(NhanVien nhanVien) {
        String sql = "UPDATE NhanVien SET TenNhanVien = ?, NgaySinh = ?, GioiTinh = ?, SoDienThoai = ?, Email = ?, DiaChi = ?, ChucVu = ? WHERE MaNhanVien = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nhanVien.getTenNhanVien());
            ps.setDate(2, java.sql.Date.valueOf(nhanVien.getNgaySinh()));
            ps.setString(3, nhanVien.getGioiTinh());
            ps.setString(4, nhanVien.getSoDienThoai());
            ps.setString(5, nhanVien.getEmail());
            ps.setString(6, nhanVien.getDiaChi());
            ps.setString(7, nhanVien.getChucVu());
            ps.setString(8, nhanVien.getMaNhanVien());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa Nhân Viên
    public boolean xoaNhanVien(String maNhanVien) {
        String sql = "DELETE FROM NhanVien WHERE MaNhanVien = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy tất cả Nhân Viên
    public List<NhanVien> layTatCaNhanVien() {
        List<NhanVien> danhSachNhanVien = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String maNhanVien = rs.getString("MaNhanVien");
                String tenNhanVien = rs.getString("TenNhanVien");
                java.sql.Date ns = rs.getDate("NgaySinh");
                LocalDate ngaySinh = (ns == null) ? null : ns.toLocalDate();
                String gioiTinh = rs.getString("GioiTinh");
                String soDienThoai = rs.getString("SoDienThoai");
                String email = rs.getString("Email");
                String diaChi = rs.getString("DiaChi");
                String chucVu = rs.getString("ChucVu");
                danhSachNhanVien.add(new NhanVien(maNhanVien, tenNhanVien, ngaySinh, gioiTinh, soDienThoai, email, diaChi, chucVu));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachNhanVien;
    }

    // Lấy Nhân Viên theo Mã Nhân Viên
    public NhanVien layNhanVienTheoMa(String maNhanVien) {
        String sql = "SELECT * FROM NhanVien WHERE MaNhanVien = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tenNhanVien = rs.getString("TenNhanVien");
                    java.sql.Date ns = rs.getDate("NgaySinh");
                    LocalDate ngaySinh = (ns == null) ? null : ns.toLocalDate();
                    String gioiTinh = rs.getString("GioiTinh");
                    String soDienThoai = rs.getString("SoDienThoai");
                    String email = rs.getString("Email");
                    String diaChi = rs.getString("DiaChi");
                    String chucVu = rs.getString("ChucVu");
                    return new NhanVien(maNhanVien, tenNhanVien, ngaySinh, gioiTinh, soDienThoai, email, diaChi, chucVu);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
