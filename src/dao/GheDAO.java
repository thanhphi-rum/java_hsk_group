package dao;

import connect.ConnectDB;
import entity.Ghe;
import entity.GheSuatChieu;
import entity.PhongChieu;
import entity.SuatChieu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GheDAO {

    // Lấy tất cả Ghế
    public List<Ghe> layTatCaGhe() {
        List<Ghe> danhSachGhe = new ArrayList<>();
        String sql = "SELECT * FROM Ghe";
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String maGhe = rs.getString("MaGhe");
                String maPhongChieu = rs.getString("MaPhongChieu");
                PhongChieu phongChieu = new PhongChieu(maPhongChieu);
                String viTri = rs.getString("ViTri");
                String loaiGhe = rs.getString("LoaiGhe");
                danhSachGhe.add(new Ghe(maGhe, phongChieu, viTri, loaiGhe));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachGhe;
    }

    // Lấy Ghế theo Phòng Chiếu
    public List<Ghe> layGheTheoMaPhongChieu(PhongChieu phongChieu) {
        List<Ghe> danhSachGhe = new ArrayList<>();
        String sql = "SELECT * FROM Ghe WHERE MaPhongChieu = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phongChieu.getMaPhongChieu());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maGhe = rs.getString("MaGhe");
                    String viTri = rs.getString("ViTri");
                    String loaiGhe = rs.getString("LoaiGhe");
                    danhSachGhe.add(new Ghe(maGhe, phongChieu, viTri, loaiGhe));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachGhe;
    }

    // Lấy Ghế theo Mã Suất Chiếu
    public List<GheSuatChieu> layGheTheoMaSuatChieu(String maSuatChieu) {
        List<GheSuatChieu> danhSach = new ArrayList<>();
        String sql = """
            SELECT g.MaGhe, g.MaPhongChieu, g.ViTri, g.LoaiGhe, gs.TrangThai
            FROM Ghe_SuatChieu gs
            JOIN Ghe g ON gs.MaGhe = g.MaGhe
            WHERE gs.MaSuatChieu = ?
        """;
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSuatChieu);
            try (ResultSet rs = ps.executeQuery()) {
                SuatChieu suatChieu = new SuatChieu(maSuatChieu);
                while (rs.next()) {
                    String maGhe = rs.getString("MaGhe");
                    String maPhongChieu = rs.getString("MaPhongChieu");
                    String viTri = rs.getString("ViTri");
                    String loaiGhe = rs.getString("LoaiGhe");
                    String trangThai = rs.getString("TrangThai");
                    Ghe ghe = new Ghe(maGhe, new PhongChieu(maPhongChieu), viTri, loaiGhe);
                    danhSach.add(new GheSuatChieu(ghe, suatChieu, trangThai));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    public void capNhatTrangThaiGhe(String maSuatChieu, String maGhe, String trangThai) {
        String sql = "UPDATE Ghe_SuatChieu SET TrangThai = ? WHERE MaSuatChieu = ? AND MaGhe = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maSuatChieu);
            ps.setString(3, maGhe);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // HoaDon không còn MaSuatChieu/MaGhe nên lấy ghế qua bảng Ve
    public boolean capNhatTrangThaiGheSauKhiXoaHoaDon(Connection conn, String maHoaDon) {
        String sql = """
            UPDATE Ghe_SuatChieu
            SET TrangThai = 'Trống'
            WHERE EXISTS (
                SELECT 1 FROM Ve v
                WHERE v.MaHoaDon = ?
                  AND v.MaSuatChieu = Ghe_SuatChieu.MaSuatChieu
                  AND v.MaGhe = Ghe_SuatChieu.MaGhe
            )
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void khoiTaoTrangThaiGheChoSuatChieu(String maSuatChieu, String maPhongChieu) {
        List<Ghe> gheList = layGheTheoMaPhongChieu(new PhongChieu(maPhongChieu));
        for (Ghe ghe : gheList) {
            String sql = "INSERT INTO Ghe_SuatChieu (MaSuatChieu, MaGhe, TrangThai) VALUES (?, ?, 'Trống')";
            try (Connection conn = ConnectDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maSuatChieu);
                ps.setString(2, ghe.getMaGhe());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean xoaGheTheoSuatChieu(Connection conn, String maSuatChieu) {
        String sql = "DELETE FROM Ghe_SuatChieu WHERE MaSuatChieu = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSuatChieu);
            int rows = ps.executeUpdate();
            return rows >= 0; // có thể 0 dòng nếu chưa có ghế cũng OK
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
