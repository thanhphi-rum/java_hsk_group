package dao;

import connect.ConnectDB;

import entity.DatCho;

import service.Session;

import java.sql.*;
import java.util.*;

/** Đặt chỗ không cần tài khoản; chỉ tạo hóa đơn khi nhân viên nhận tiền. */
public class DatVeDAO {
    // Các thao tác cùng suất đều khóa SuatChieu trước để tuần tự hóa việc giữ/bán/nhả ghế.
    static void lockShow(Connection c, String show) throws SQLException {
        try (PreparedStatement p =
                c.prepareStatement(
                        "SELECT MaSuatChieu FROM SuatChieu WITH (UPDLOCK,HOLDLOCK) WHERE"
                            + " MaSuatChieu=?")) {
            p.setString(1, show);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Suất chiếu không tồn tại.");
            }
        }
    }

    /** Nhả ghế của đơn quá hạn; không thay đổi ghế đã bán hoặc đơn đã thanh toán. */
    static void giaiPhongHetHan(Connection c, String show) throws SQLException {
        try (PreparedStatement p =
                c.prepareStatement(
                        "UPDATE gs SET TrangThai=N'Trống' FROM Ghe_SuatChieu gs JOIN ChiTietDatVe"
                            + " ct ON ct.MaGhe=gs.MaGhe JOIN DatVe d ON d.MaDatVe=ct.MaDatVe AND"
                            + " d.MaSuatChieu=gs.MaSuatChieu WHERE d.MaSuatChieu=? AND"
                            + " d.TrangThai='CHO_THANH_TOAN' AND d.HetHan<=GETDATE() AND"
                            + " gs.TrangThai=N'Đang giữ'")) {
            p.setString(1, show);
            p.executeUpdate();
        }
        try (PreparedStatement p =
                c.prepareStatement(
                        "UPDATE DatVe SET TrangThai='HET_HAN' WHERE MaSuatChieu=? AND"
                            + " TrangThai='CHO_THANH_TOAN' AND HetHan<=GETDATE()")) {
            p.setString(1, show);
            p.executeUpdate();
        }
    }

    public void refresh(String show) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                lockShow(c, show);
                giaiPhongHetHan(c, show);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** Giữ từ 1 đến 10 ghế trong tối đa 10 phút. Nếu có một ghế bận, hủy toàn bộ lượt đặt. */
    public DatCho datCho(String show, Collection<String> seats) throws SQLException {
        if (seats.isEmpty() || seats.size() > 10 || seats.size() != new HashSet<>(seats).size())
            throw new SQLException("Mỗi lượt đặt từ 1 đến 10 ghế khác nhau.");
        String id =
                "D"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 15)
                                .toUpperCase(Locale.ROOT);
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                lockShow(c, show);
                giaiPhongHetHan(c, show);
                try (PreparedStatement p =
                        c.prepareStatement(
                                "INSERT INTO DatVe(MaDatVe,MaSuatChieu,HetHan,GiaVe) SELECT"
                                    + " ?,MaSuatChieu,CASE WHEN"
                                    + " GioBatDau<DATEADD(minute,10,GETDATE()) THEN GioBatDau ELSE"
                                    + " DATEADD(minute,10,GETDATE()) END,GiaVe FROM SuatChieu WHERE"
                                    + " MaSuatChieu=? AND GioBatDau>GETDATE() AND TrangThai=N'Đang"
                                    + " Mở Bán'")) {
                    p.setString(1, id);
                    p.setString(2, show);
                    if (p.executeUpdate() != 1)
                        throw new SQLException("Suất chiếu đã bắt đầu hoặc ngừng bán.");
                }
                for (String seat : new TreeSet<>(seats)) {
                    try (PreparedStatement p =
                            c.prepareStatement(
                                    "UPDATE gs SET TrangThai=N'Đang giữ' FROM Ghe_SuatChieu gs JOIN"
                                        + " Ghe g ON g.MaGhe=gs.MaGhe JOIN SuatChieu s ON"
                                        + " s.MaSuatChieu=gs.MaSuatChieu AND"
                                        + " s.MaPhongChieu=g.MaPhongChieu WHERE gs.MaSuatChieu=?"
                                        + " AND gs.MaGhe=? AND gs.TrangThai=N'Trống' AND NOT"
                                        + " EXISTS(SELECT 1 FROM Ve v WHERE"
                                        + " v.MaSuatChieu=gs.MaSuatChieu AND v.MaGhe=gs.MaGhe)")) {
                        p.setString(1, show);
                        p.setString(2, seat);
                        if (p.executeUpdate() != 1)
                            throw new SQLException(
                                    "Ghế "
                                            + seat
                                            + " vừa được người khác chọn. Vui lòng chọn lại.");
                    }
                    try (PreparedStatement p =
                            c.prepareStatement("INSERT INTO ChiTietDatVe VALUES(?,?)")) {
                        p.setString(1, id);
                        p.setString(2, seat);
                        p.executeUpdate();
                    }
                }
                DatCho result = read(c, id);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** Chỉ nhân viên được tra cứu đơn để thu tiền tại quầy. */
    public DatCho traCuuNhanVien(String id) throws SQLException {
        Session.requireStaff();
        try (Connection c = ConnectDB.getConnection()) {
            return read(c, id);
        }
    }

    private DatCho read(Connection c, String id) throws SQLException {
        try (PreparedStatement p =
                c.prepareStatement(
                        "SELECT d.*,s.GioBatDau,p.TenPhim,pc.TenPhong,CASE WHEN"
                            + " d.TrangThai='CHO_THANH_TOAN' AND d.HetHan<=GETDATE() THEN 'HET_HAN'"
                            + " ELSE d.TrangThai END AS TinhTrang FROM DatVe d JOIN SuatChieu s ON"
                            + " s.MaSuatChieu=d.MaSuatChieu JOIN Phim p ON p.MaPhim=s.MaPhim JOIN"
                            + " PhongChieu pc ON pc.MaPhongChieu=s.MaPhongChieu WHERE"
                            + " d.MaDatVe=?")) {
            p.setString(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Không tìm thấy mã đặt vé.");
                List<String> seats = new ArrayList<>();
                try (PreparedStatement q =
                        c.prepareStatement(
                                "SELECT g.ViTri FROM ChiTietDatVe ct JOIN Ghe g ON g.MaGhe=ct.MaGhe"
                                    + " WHERE ct.MaDatVe=? ORDER BY g.ViTri")) {
                    q.setString(1, id);
                    try (ResultSet a = q.executeQuery()) {
                        while (a.next()) seats.add(a.getString(1));
                    }
                }
                return new DatCho(
                        id,
                        r.getString("MaSuatChieu"),
                        r.getString("TenPhim"),
                        r.getString("TenPhong"),
                        r.getTimestamp("GioBatDau").toLocalDateTime(),
                        r.getTimestamp("HetHan").toLocalDateTime(),
                        r.getString("TinhTrang"),
                        r.getBigDecimal("GiaVe"),
                        List.copyOf(seats),
                        r.getString("MaHoaDon"));
            }
        }
    }

    /**
     * Kiểm tra hạn giữ chỗ, lập hóa đơn và vé, đánh dấu đơn đã thanh toán trong cùng transaction.
     */
    public String thanhToan(String id) throws SQLException {
        Session.requireStaff();
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                DatCho before = read(c, id);
                lockShow(c, before.show());
                giaiPhongHetHan(c, before.show());
                DatCho booking = read(c, id);
                if (!"CHO_THANH_TOAN".equals(booking.status()))
                    throw new SQLException("Đơn đã hết hạn hoặc đã thanh toán.");
                List<String> seats = new ArrayList<>();
                try (PreparedStatement p =
                        c.prepareStatement(
                                "SELECT MaGhe FROM ChiTietDatVe WHERE MaDatVe=? ORDER BY MaGhe")) {
                    p.setString(1, id);
                    try (ResultSet r = p.executeQuery()) {
                        while (r.next()) seats.add(r.getString(1));
                    }
                }
                for (String seat : seats)
                    try (PreparedStatement p =
                            c.prepareStatement(
                                    "UPDATE Ghe_SuatChieu SET TrangThai=N'Trống' WHERE"
                                        + " MaSuatChieu=? AND MaGhe=? AND TrangThai=N'Đang giữ'")) {
                        p.setString(1, booking.show());
                        p.setString(2, seat);
                        if (p.executeUpdate() != 1)
                            throw new SQLException("Trạng thái ghế không hợp lệ.");
                    }
                String invoice =
                        new BanVeDAO()
                                .luuTrongGiaoDich(
                                        c,
                                        booking.show(),
                                        seats,
                                        Session.maNhanVien,
                                        null,
                                        null,
                                        booking.unitPrice(),
                                        booking.total());
                try (PreparedStatement p =
                        c.prepareStatement(
                                "UPDATE DatVe SET TrangThai='DA_THANH_TOAN',MaHoaDon=? WHERE"
                                    + " MaDatVe=?")) {
                    p.setString(1, invoice);
                    p.setString(2, id);
                    p.executeUpdate();
                }
                c.commit();
                return invoice;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
