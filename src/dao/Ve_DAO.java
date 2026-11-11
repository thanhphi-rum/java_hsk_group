package dao;

import entity.Ve;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Ve_DAO {
    private final Connection conn;

    public Ve_DAO(Connection conn) {
        this.conn = conn;
    }

    // Kiểm tra ghế đã bị giữ hoặc bán trong suất chiếu chưa
    public boolean kiemTraGheDaDatTrongSuat(String maSuatChieu, String maGhe) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve WHERE MaSuatChieu = ? AND MaGhe = ? AND TrangThai IN ('Đã giữ', 'Đã bán')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSuatChieu);
            ps.setString(2, maGhe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean themVe(Ve ve) throws SQLException {
        String sql = "INSERT INTO Ve (MaVe, MaSuatChieu, MaGhe, GiaVe, TrangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ve.getMaVe());
            ps.setString(2, ve.getMaSuatChieu());
            ps.setString(3, ve.getMaGhe());
            ps.setDouble(4, ve.getGiaVe());
            ps.setString(5, ve.getTrangThai());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Ve> layVeTheoPhieuDat(String maPhieuDatVe) throws SQLException {
        List<Ve> ds = new ArrayList<>();
        String sql = "SELECT v.* FROM Ve v JOIN ChiTietPhieuDatVe ct ON v.MaVe = ct.MaVe WHERE ct.MaPhieuDatVe = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieuDatVe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Ve(
                        rs.getString("MaVe"),
                        rs.getString("MaSuatChieu"),
                        rs.getString("MaGhe"),
                        rs.getDouble("GiaVe"),
                        rs.getString("TrangThai")
                    ));
                }
            }
        }
        return ds;
    }
}