package dao;

import connect.ConnectDB;

import java.sql.*;
import java.util.*;

public class PhongGheDAO {
    public List<String[]> phong() throws SQLException {
        List<String[]> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
                Statement s = c.createStatement();
                ResultSet r =
                        s.executeQuery(
                                "SELECT MaPhongChieu,TenPhong,SucChua FROM PhongChieu ORDER BY"
                                    + " MaPhongChieu")) {
            while (r.next())
                list.add(new String[] {r.getString(1), r.getString(2), r.getString(3)});
        }
        return list;
    }

    public List<String[]> ghe(String room) throws SQLException {
        List<String[]> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement p =
                        c.prepareStatement(
                                "SELECT MaGhe,ViTri,LoaiGhe FROM Ghe WHERE MaPhongChieu=? ORDER BY"
                                    + " ViTri")) {
            p.setString(1, room);
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    list.add(new String[] {r.getString(1), r.getString(2), r.getString(3)});
            }
        }
        return list;
    }

    public void themDay(String room, String row, int count) throws SQLException {
        if (room == null || !row.matches("[A-Z]") || count < 1 || count > 30)
            throw new SQLException("Dãy ghế phải từ A–Z và có 1–30 ghế.");
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Khóa phòng để tránh hai người cùng thêm một dãy.
                try (PreparedStatement p =
                        c.prepareStatement(
                                "SELECT MaPhongChieu FROM PhongChieu WITH (UPDLOCK,HOLDLOCK) WHERE"
                                    + " MaPhongChieu=?")) {
                    p.setString(1, room);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new SQLException("Phòng không tồn tại.");
                    }
                }
                try (PreparedStatement p =
                        c.prepareStatement(
                                "SELECT COUNT(*) FROM Ghe WHERE MaPhongChieu=? AND ViTri LIKE ?")) {
                    p.setString(1, room);
                    p.setString(2, row + "%");
                    try (ResultSet r = p.executeQuery()) {
                        r.next();
                        if (r.getInt(1) > 0)
                            throw new SQLException("Dãy " + row + " đã tồn tại trong phòng.");
                    }
                }
                for (int i = 1; i <= count; i++) {
                    String seat = row + String.format("%02d", i);
                    String id = room + "_" + seat;
                    if (id.length() > 10)
                        throw new SQLException(
                                "Mã phòng quá dài; mã phòng tối đa 6 ký tự để tạo ghế.");
                    try (PreparedStatement p =
                            c.prepareStatement(
                                    "INSERT INTO Ghe(MaGhe,MaPhongChieu,ViTri,LoaiGhe)"
                                        + " VALUES(?,?,?,N'Thường')")) {
                        p.setString(1, id);
                        p.setString(2, room);
                        p.setString(3, seat);
                        p.executeUpdate();
                    }
                    try (PreparedStatement p =
                            c.prepareStatement(
                                    "INSERT INTO Ghe_SuatChieu(MaSuatChieu,MaGhe,TrangThai) SELECT"
                                        + " MaSuatChieu,?,N'Trống' FROM SuatChieu WHERE"
                                        + " MaPhongChieu=? AND GioBatDau>GETDATE()")) {
                        p.setString(1, id);
                        p.setString(2, room);
                        p.executeUpdate();
                    }
                }
                try (PreparedStatement p =
                        c.prepareStatement(
                                "UPDATE PhongChieu SET SucChua=(SELECT COUNT(*) FROM Ghe WHERE"
                                    + " MaPhongChieu=?) WHERE MaPhongChieu=?")) {
                    p.setString(1, room);
                    p.setString(2, room);
                    p.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
