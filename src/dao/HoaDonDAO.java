package dao;

import connect.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.NhanVien;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    private HoaDon taoHoaDon(ResultSet rs) throws SQLException {
        String maHoaDon = rs.getString("MaHoaDon");
        NhanVien nhanVien = new NhanVien(rs.getString("MaNhanVien"));
        String maKH = rs.getString("MaKhachHang");
        KhachHang khachHang = (maKH == null) ? null : new KhachHang(maKH);
        String maKhuyenMai = rs.getString("MaKhuyenMai");
        KhuyenMai khuyenMai = (maKhuyenMai == null) ? null : new KhuyenMai(maKhuyenMai);
        Timestamp ts = rs.getTimestamp("NgayLap");
        LocalDateTime ngayLap = (ts == null) ? null : ts.toLocalDateTime();
        double tongTien = rs.getDouble("TongTien");
        String phuongThucThanhToan = rs.getString("PhuongThucThanhToan");
        return new HoaDon(maHoaDon, nhanVien, khachHang, khuyenMai, ngayLap, tongTien, phuongThucThanhToan);
    }

    // Thêm Hóa Đơn và trả về kết quả thành công hay không (Sử dụng Connection từ BanVe_GUI để quản lý Transaction)
    public boolean themHoaDon(Connection conn, HoaDon hoaDon) {
        String sql = "INSERT INTO HoaDon (MaHoaDon, MaNhanVien, MaKhachHang, MaKhuyenMai, NgayLap, TongTien, PhuongThucThanhToan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoaDon.getMaHoaDon());
            ps.setString(2, hoaDon.getNhanVien().getMaNhanVien());
            
            if (hoaDon.getKhachHang() != null && !hoaDon.getKhachHang().getMaKhachHang().isEmpty()) {
                ps.setString(3, hoaDon.getKhachHang().getMaKhachHang());
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
            }
            
            if (hoaDon.getKhuyenMai() != null && !hoaDon.getKhuyenMai().getMaKhuyenMai().isEmpty()) {
                ps.setString(4, hoaDon.getKhuyenMai().getMaKhuyenMai());
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
            }
            
            ps.setTimestamp(5, Timestamp.valueOf(hoaDon.getNgayLap()));
            ps.setDouble(6, hoaDon.getTongTien());
            ps.setString(7, hoaDon.getPhongThucThanhToan());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean xoaHoaDon(Connection conn, String maHoaDon) {
        String sql = "DELETE FROM HoaDon WHERE MaHoaDon = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<HoaDon> layTatCaHoaDon() {
        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon";
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                danhSachHoaDon.add(taoHoaDon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachHoaDon;
    }

    public List<HoaDon> layHoaDonTheoMaKhachHang(String maKhachHang) {
        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE MaKhachHang = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSachHoaDon.add(taoHoaDon(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachHoaDon;
    }

    public List<HoaDon> timKiemHoaDon(String keyword) {
        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE MaHoaDon LIKE ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSachHoaDon.add(taoHoaDon(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachHoaDon;
    }
}
