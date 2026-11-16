package dao;

import connect.ConnectDB;
import entity.TheLoai;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TheLoaiDAO {

    /** Toàn bộ thể loại (TL001, TL002, ...) theo mã, dùng cho combobox chọn thể loại phim. */
    public List<TheLoai> layTatCa() {
        List<TheLoai> ds = new ArrayList<>();
        String sql = "SELECT MaTheLoai, TenTheLoai, MoTa FROM TheLoai ORDER BY MaTheLoai";
        try (Connection conn = ConnectDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new TheLoai(rs.getString("MaTheLoai"), rs.getString("TenTheLoai"), rs.getString("MoTa")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}
