package dao;

import connect.ConnectDB;

import java.math.*;
import java.sql.*;
import java.util.*;

/** Một giao dịch duy nhất cho hóa đơn, vé và trạng thái ghế. */
public class BanVeDAO {
    public static BigDecimal tinhTien(BigDecimal price, int count, BigDecimal discount) {
        return service.ThanhToanService.tinhTongTien(price, count, discount);
    }

    private static String id(String prefix) {
        return prefix
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 9)
                        .toUpperCase(Locale.ROOT);
    }

    public String luu(
            String show,
            Collection<String> seats,
            String employee,
            String customer,
            String promotion)
            throws SQLException {
        return luu(show, seats, employee, customer, promotion, null);
    }

    public String luu(
            String show,
            Collection<String> seats,
            String employee,
            String customer,
            String promotion,
            BigDecimal expectedTotal)
            throws SQLException {
        service.Session.requireStaff();
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                String invoice =
                        luuTrongGiaoDich(
                                c, show, seats, employee, customer, promotion, null, expectedTotal);
                c.commit();
                return invoice;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    String luuTrongGiaoDich(
            Connection c,
            String show,
            Collection<String> seats,
            String employee,
            String customer,
            String promotion,
            BigDecimal reservedPrice,
            BigDecimal expectedTotal)
            throws SQLException {
        service.Session.requireStaff();
        if (!Objects.equals(employee, service.Session.maNhanVien)
                || seats.isEmpty()
                || seats.size() != new HashSet<>(seats).size())
            throw new SQLException("Nhân viên hoặc ghế không hợp lệ.");
        DatVeDAO.lockShow(c, show);
        DatVeDAO.giaiPhongHetHan(c, show);
        // 1. Kiểm tra suất vẫn còn mở bán và đọc đơn giá tại thời điểm thanh toán.
        BigDecimal price;
        try (PreparedStatement p =
                c.prepareStatement(
                        "SELECT GiaVe FROM SuatChieu WITH (UPDLOCK,HOLDLOCK) WHERE MaSuatChieu=?"
                            + " AND GioBatDau>GETDATE() AND TrangThai=N'Đang Mở Bán'")) {
            p.setString(1, show);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Suất chiếu đã bắt đầu hoặc đã ngừng bán.");
                price = r.getBigDecimal(1);
            }
        }
        if (reservedPrice != null) price = reservedPrice;
        // 2. Kiểm tra khuyến mãi còn hiệu lực, không tin hoàn toàn giá từ giao diện.
        BigDecimal discount = BigDecimal.ZERO;
        if (promotion != null)
            try (PreparedStatement p =
                    c.prepareStatement(
                            "SELECT PhanTramGiam FROM KhuyenMai WHERE MaKhuyenMai=? AND"
                                + " NgayBatDau<=CAST(GETDATE() AS DATE) AND"
                                + " NgayKetThuc>=CAST(GETDATE() AS DATE)")) {
                p.setString(1, promotion);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next())
                        throw new SQLException("Khuyến mãi đã hết hạn hoặc chưa hiệu lực.");
                    discount = r.getBigDecimal(1);
                }
            }
        BigDecimal total = tinhTien(price, seats.size(), discount);
        if (expectedTotal != null && total.compareTo(expectedTotal) != 0)
            throw new SQLException(
                    "Giá vé hoặc khuyến mãi đã thay đổi. Vui lòng tải lại và xác nhận số tiền"
                        + " mới.");
        // 3. Tạo hóa đơn trong cùng giao dịch với vé và cập nhật ghế.
        String invoice = id("H");
        try (PreparedStatement p =
                c.prepareStatement(
                        "INSERT INTO"
                            + " HoaDon(MaHoaDon,MaNhanVien,MaKhachHang,MaKhuyenMai,NgayLap,TongTien,PhuongThucThanhToan)"
                            + " VALUES(?,?,?,?,GETDATE(),?,N'Tiền mặt')")) {
            p.setString(1, invoice);
            p.setString(2, employee);
            p.setString(3, customer);
            p.setString(4, promotion);
            p.setBigDecimal(5, total);
            p.executeUpdate();
        }
        int index = 0;
        BigDecimal each = total.divide(BigDecimal.valueOf(seats.size()), 0, RoundingMode.DOWN);
        // 4. Chỉ bán ghế còn trống. Bất kỳ ghế nào lỗi thì rollback toàn bộ hóa đơn.
        for (String seat : new TreeSet<>(seats)) {
            try (PreparedStatement p =
                    c.prepareStatement(
                            "UPDATE gs SET TrangThai=N'Đã đặt' FROM Ghe_SuatChieu gs JOIN Ghe g ON"
                                + " g.MaGhe=gs.MaGhe JOIN SuatChieu sc ON"
                                + " sc.MaSuatChieu=gs.MaSuatChieu AND"
                                + " sc.MaPhongChieu=g.MaPhongChieu WHERE gs.MaSuatChieu=? AND"
                                + " gs.MaGhe=? AND gs.TrangThai=N'Trống' AND NOT EXISTS(SELECT 1"
                                + " FROM Ve v WHERE v.MaSuatChieu=gs.MaSuatChieu AND"
                                + " v.MaGhe=gs.MaGhe)")) {
                p.setString(1, show);
                p.setString(2, seat);
                if (p.executeUpdate() != 1)
                    throw new SQLException(
                            "Ghế "
                                    + seat
                                    + " vừa được bán hoặc không còn khả dụng. Vui lòng chọn lại.");
            }
            BigDecimal amount =
                    ++index == seats.size()
                            ? total.subtract(each.multiply(BigDecimal.valueOf(seats.size() - 1)))
                            : each;
            try (PreparedStatement p =
                    c.prepareStatement(
                            "INSERT INTO Ve(MaVe,MaSuatChieu,MaGhe,MaHoaDon,GiaTien)"
                                + " VALUES(?,?,?,?,?)")) {
                String ticketId = id("V");
                p.setString(1, ticketId);
                p.setString(2, show);
                p.setString(3, seat);
                p.setString(4, invoice);
                p.setBigDecimal(5, amount);
                p.executeUpdate();
                try (PreparedStatement detail = c.prepareStatement("INSERT INTO ChiTietHoaDon(MaHoaDon,MaVe,SoLuong,DonGia) VALUES(?,?,1,?)")) {
                    detail.setString(1, invoice); detail.setString(2, ticketId);
                    detail.setBigDecimal(3, amount); detail.executeUpdate();
                }
            }
        }
        return invoice;
    }
}
