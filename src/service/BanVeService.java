package service;

import dao.BanVeDAO;
import dao.DanhMucBanVeDAO;
import dao.DatVeDAO;

import entity.GheBanVe;
import entity.LuaChonBanVe;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/** Điều phối luồng khách đặt chỗ và nhân viên thu tiền. Không chứa Swing hay câu SQL. */
public class BanVeService {
    private final DanhMucBanVeDAO catalog = new DanhMucBanVeDAO();
    private final DatVeDAO bookings = new DatVeDAO();
    private final BanVeDAO sales = new BanVeDAO();

    public List<LuaChonBanVe> phimDangChieu() throws SQLException {
        return catalog.phimDangChieu();
    }

    public List<LuaChonBanVe> suatChieu(String movieId) throws SQLException {
        return catalog.suatChieu(movieId);
    }

    /** Không tải danh sách thông tin khách hàng lên màn hình công khai. */
    public List<LuaChonBanVe> khachHang() throws SQLException {
        Session.requireStaff();
        return catalog.khachHang();
    }

    public List<LuaChonBanVe> khuyenMai() throws SQLException {
        Session.requireStaff();
        return catalog.khuyenMai();
    }

    /** Nhả giữ chỗ đã hết hạn rồi mới lấy trạng thái ghế mới nhất. */
    public List<GheBanVe> gheTheoSuat(String showId) throws SQLException {
        bookings.refresh(showId);
        return catalog.gheTheoSuat(showId);
    }

    /** Khách không cần đăng nhập. Kết quả chỉ là đơn giữ chỗ, chưa phát sinh doanh thu. */
    public entity.DatCho datCho(String showId, Collection<String> seats) throws SQLException {
        return bookings.datCho(showId, seats);
    }

    public entity.DatCho traDon(String id) throws SQLException {
        Session.requireStaff();
        return bookings.traCuuNhanVien(id);
    }

    /** Nhân viên đã xác nhận thu tiền; DAO kiểm tra lại ghế và lưu tất cả trong một transaction. */
    public String banTaiQuay(
            String showId,
            Collection<String> seats,
            String customer,
            String promotion,
            BigDecimal approvedTotal)
            throws SQLException {
        Session.requireStaff();
        return sales.luu(showId, seats, Session.maNhanVien, customer, promotion, approvedTotal);
    }

    public String thanhToanDon(String id) throws SQLException {
        Session.requireStaff();
        return bookings.thanhToan(id);
    }
}
