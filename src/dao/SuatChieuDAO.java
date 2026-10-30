package dao;

import connect.ConnectDB;
import entity.Phim;
import entity.PhongChieu;
import entity.SuatChieu;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SuatChieuDAO {

    // Chuyển 1 dòng ResultSet thành SuatChieu
    private SuatChieu taoSuatChieu(ResultSet rs) throws SQLException {
        String maSuatChieu = rs.getString("MaSuatChieu");
        Phim phim = new Phim(rs.getString("MaPhim"));
        PhongChieu phongChieu = new PhongChieu(rs.getString("MaPhongChieu"));
        LocalDate ngayChieu = rs.getDate("ngayChieu").toLocalDate();
        LocalDateTime gioBatDau = rs.getTimestamp("GioBatDau").toLocalDateTime();
        LocalDateTime gioKetThuc = rs.getTimestamp("GioKetThuc").toLocalDateTime();
        double giaVe = rs.getDouble("GiaVe");
        String trangThai = rs.getString("TrangThai");
        return new SuatChieu(maSuatChieu, phim, phongChieu, ngayChieu, gioBatDau, gioKetThuc, giaVe, trangThai);
    }

    // Thêm Suất Chiếu
    public boolean themSuatChieu(SuatChieu suatChieu) {
        String sql = "INSERT INTO SuatChieu (MaSuatChieu, MaPhim, MaPhongChieu, GioBatDau, GioKetThuc, GiaVe, TrangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, suatChieu.getMaSuatChieu());
            ps.setString(2, suatChieu.getPhim().getMaPhim());
            ps.setString(3, suatChieu.getPhongChieu().getMaPhongChieu());
            ps.setTimestamp(4, Timestamp.valueOf(suatChieu.getGioBatDau()));
            ps.setTimestamp(5, Timestamp.valueOf(suatChieu.getGioKetThuc()));
            ps.setDouble(6, suatChieu.getGiaVe());
            ps.setString(7, suatChieu.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Sửa Suất Chiếu
    public boolean suaSuatChieu(SuatChieu suatChieu) {
        String sql = "UPDATE SuatChieu SET MaPhim = ?, MaPhongChieu = ?, GioBatDau = ?, GioKetThuc = ?, GiaVe = ?, TrangThai = ? WHERE MaSuatChieu = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, suatChieu.getPhim().getMaPhim());
            ps.setString(2, suatChieu.getPhongChieu().getMaPhongChieu());
            ps.setTimestamp(3, Timestamp.valueOf(suatChieu.getGioBatDau()));
            ps.setTimestamp(4, Timestamp.valueOf(suatChieu.getGioKetThuc()));
            ps.setDouble(5, suatChieu.getGiaVe());
            ps.setString(6, suatChieu.getTrangThai());
            ps.setString(7, suatChieu.getMaSuatChieu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy tất cả Suất Chiếu
    public List<SuatChieu> layTatCaSuatChieu() {
        List<SuatChieu> danhSachSuatChieu = new ArrayList<>();
        String sql = "SELECT * FROM SuatChieu";
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                danhSachSuatChieu.add(taoSuatChieu(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachSuatChieu;
    }

    // Lấy Suất Chiếu theo Mã Suất Chiếu
    public SuatChieu laySuatChieuTheoMa(String maSuatChieu) {
        String sql = "SELECT * FROM SuatChieu WHERE MaSuatChieu = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSuatChieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return taoSuatChieu(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean xoaSuatChieu(String maSuatChieu) {
        try (Connection conn = ConnectDB.getConnection()) {
            conn.setAutoCommit(false);

            GheDAO gheDAO = new GheDAO();

            // Xóa Ghe_SuatChieu trước
            boolean xoaGhe = gheDAO.xoaGheTheoSuatChieu(conn, maSuatChieu);

            // Sau đó xóa suất chiếu
            String sql = "DELETE FROM SuatChieu WHERE MaSuatChieu = ?";
            boolean xoaSuat = false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maSuatChieu);
                xoaSuat = ps.executeUpdate() > 0;
            }

            if (xoaGhe && xoaSuat) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<SuatChieu> laySuatChieuTheoMaPhim(String maPhim) {
        List<SuatChieu> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM SuatChieu WHERE MaPhim = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(taoSuatChieu(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSach;
    }
}
