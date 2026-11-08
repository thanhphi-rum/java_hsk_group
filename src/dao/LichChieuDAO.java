package dao;

import connect.ConnectDB;

import entity.SuatChieu;

import java.sql.*;

/** Ghi lịch chiếu và khởi tạo ghế trong cùng transaction. */
public class LichChieuDAO {
    /** Danh sách phòng để chọn khi lập lịch. */
    public java.util.List<String> maPhongChieu() throws SQLException {
        java.util.List<String> result = new java.util.ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rows =
                        stmt.executeQuery(
                                "SELECT MaPhongChieu FROM PhongChieu WHERE TrangThai=N'Hoạt động' ORDER BY MaPhongChieu")) {
            while (rows.next()) result.add(rows.getString(1));
        }
        return result;
    }

    public void luu(SuatChieu show, boolean editing) throws SQLException {
        try (Connection conn = ConnectDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (editing) DatVeDAO.lockShow(conn, show.getMaSuatChieu());
                // Khóa phòng để hai lượt thêm lịch cùng lúc không cùng vượt qua kiểm tra trùng giờ.
                try (PreparedStatement lock =
                        conn.prepareStatement(
                                "SELECT MaPhongChieu FROM PhongChieu WITH(UPDLOCK,HOLDLOCK) WHERE"
                                    + " MaPhongChieu=? AND TrangThai=N'Hoạt động'")) {
                    lock.setString(1, show.getPhongChieu().getMaPhongChieu());
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next()) throw new SQLException("Phòng chiếu không tồn tại.");
                    }
                }
                if (editing) kiemTraChuaBanVe(conn, show.getMaSuatChieu());
                kiemTraTrungGio(conn, show, editing);
                String sql =
                        editing
                                ? "UPDATE SuatChieu SET"
                                      + " MaPhim=?,MaPhongChieu=?,GioBatDau=?,GioKetThuc=?,GiaVe=?,TrangThai=?"
                                      + " WHERE MaSuatChieu=?"
                                : "INSERT INTO"
                                      + " SuatChieu(MaPhim,MaPhongChieu,GioBatDau,GioKetThuc,GiaVe,TrangThai,MaSuatChieu)"
                                      + " VALUES(?,?,?,?,?,?,?)";
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setString(1, show.getPhim().getMaPhim());
                    statement.setString(2, show.getPhongChieu().getMaPhongChieu());
                    statement.setTimestamp(3, Timestamp.valueOf(show.getGioBatDau()));
                    statement.setTimestamp(4, Timestamp.valueOf(show.getGioKetThuc()));
                    statement.setDouble(5, show.getGiaVe());
                    statement.setString(6, show.getTrangThai());
                    statement.setString(7, show.getMaSuatChieu());
                    if (statement.executeUpdate() != 1)
                        throw new SQLException("Không lưu được suất chiếu.");
                }
                if (!editing) {
                    // N'...' giữ đúng tiếng Việt; thiếu N có thể khiến ghế trống bị khóa trên GUI.
                    try (PreparedStatement statement =
                            conn.prepareStatement(
                                    "INSERT INTO Ghe_SuatChieu(MaSuatChieu,MaGhe,TrangThai) SELECT"
                                        + " ?,MaGhe,N'Trống' FROM Ghe WHERE MaPhongChieu=?")) {
                        statement.setString(1, show.getMaSuatChieu());
                        statement.setString(2, show.getPhongChieu().getMaPhongChieu());
                        statement.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private void kiemTraChuaBanVe(Connection conn, String id) throws SQLException {
        String sql =
                """
SELECT 1 WHERE EXISTS(SELECT 1 FROM Ve WHERE MaSuatChieu=?)
OR EXISTS(SELECT 1 FROM DatVe WHERE MaSuatChieu=? AND TrangThai='CHO_THANH_TOAN' AND HetHan>GETDATE())
""";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, id);
            try (ResultSet row = statement.executeQuery()) {
                if (row.next())
                    throw new SQLException(
                            "Suất đã bán vé hoặc đang giữ chỗ, không được đổi lịch.");
            }
        }
    }

    private void kiemTraTrungGio(Connection conn, SuatChieu show, boolean editing)
            throws SQLException {
        // Hai khoảng thời gian giao nhau khi bắt đầu cũ < kết thúc mới và kết thúc cũ > bắt đầu
        // mới.
        String sql =
                "SELECT 1 FROM SuatChieu WHERE MaPhongChieu=? AND GioBatDau<? AND GioKetThuc>?"
                        + (editing ? " AND MaSuatChieu<>?" : "");
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, show.getPhongChieu().getMaPhongChieu());
            statement.setTimestamp(2, Timestamp.valueOf(show.getGioKetThuc()));
            statement.setTimestamp(3, Timestamp.valueOf(show.getGioBatDau()));
            if (editing) statement.setString(4, show.getMaSuatChieu());
            try (ResultSet row = statement.executeQuery()) {
                if (row.next()) throw new SQLException("Phòng đã có suất chiếu trùng giờ.");
            }
        }
    }
}
