package dao;

import entity.ComboBapNuoc;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComboBapNuoc_DAO {
    private final Connection conn;

    public ComboBapNuoc_DAO(Connection conn) {
        this.conn = conn;
    }

    public List<ComboBapNuoc> layComboDangBan() throws SQLException {
        List<ComboBapNuoc> ds = new ArrayList<>();
        String sql = "SELECT * FROM ComboBapNuoc WHERE TrangThai = 'Đang bán'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new ComboBapNuoc(
                    rs.getString("MaCombo"),
                    rs.getString("TenCombo"),
                    rs.getString("MoTa"),
                    rs.getDouble("GiaBan"),
                    rs.getString("TrangThai")
                ));
            }
        }
        return ds;
    }

    public boolean themCombo(ComboBapNuoc cb) throws SQLException {
        String sql = "INSERT INTO ComboBapNuoc (MaCombo, TenCombo, MoTa, GiaBan, TrangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cb.getMaCombo());
            ps.setString(2, cb.getTenCombo());
            ps.setString(3, cb.getMoTa());
            ps.setDouble(4, cb.getGiaBan());
            ps.setString(5, cb.getTrangThai());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhatCombo(ComboBapNuoc cb) throws SQLException {
        String sql = "UPDATE ComboBapNuoc SET TenCombo = ?, MoTa = ?, GiaBan = ?, TrangThai = ? WHERE MaCombo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cb.getTenCombo());
            ps.setString(2, cb.getMoTa());
            ps.setDouble(3, cb.getGiaBan());
            ps.setString(4, cb.getTrangThai());
            ps.setString(5, cb.getMaCombo());
            return ps.executeUpdate() > 0;
        }
    }
}