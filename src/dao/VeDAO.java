package dao;

import connect.ConnectDB;
import entity.Ve;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VeDAO {
    // Thêm Vé (được gọi sau khi đã thêm Hóa Đơn thành công)
    public boolean themVe(Connection conn, Ve ve) {
        String sql = "INSERT INTO Ve (MaVe, MaSuatChieu, MaGhe, MaHoaDon, GiaTien) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ve.getMaVe());
            ps.setString(2, ve.getSuatChieu().getMaSuatChieu());
            ps.setString(3, ve.getGhe().getMaGhe());
            ps.setString(4, ve.getHoaDon().getMaHoaDon());
            ps.setDouble(5, ve.getGiaTien());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
