package service;

import dao.ChiTietPhieuDatVe_DAO;
import dao.KhachHang_DAO;
import dao.PhieuDatVe_DAO;
import dao.Ve_DAO;
import entity.KhachHang;
import entity.PhieuDatVe;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Điều phối lập/hủy phiếu cùng vé và chi tiết trong một transaction. */
public class TaoPhieuDatVeService {
    private final Connection connection;
    private final KhachHang_DAO khachHangDAO;
    private final PhieuDatVe_DAO phieuDAO;
    private final Ve_DAO veDAO;
    private final ChiTietPhieuDatVe_DAO chiTietDAO;
    private BigDecimal giaVePhieuVuaTao = BigDecimal.ZERO;

    public TaoPhieuDatVeService(Connection connection) {
        this.connection = connection;
        this.khachHangDAO = new KhachHang_DAO(connection);
        this.phieuDAO = new PhieuDatVe_DAO(connection);
        this.veDAO = new Ve_DAO(connection);
        this.chiTietDAO = new ChiTietPhieuDatVe_DAO(connection);
    }

    public ArrayList<String> laySuatChieuDangMoBan() throws SQLException {
        return veDAO.laySuatChieuDangMoBan();
    }

    public ArrayList<String> layGheTrongSuat(String maSuatChieu) throws SQLException {
        return veDAO.layGheTrongSuat(maSuatChieu);
    }

    public ArrayList<String> layTatCaGheTheoSuat(String maSuatChieu) throws SQLException {
        return veDAO.layTatCaGheTheoSuat(maSuatChieu);
    }
    public BigDecimal layGiaVeTheoSuat(String maSuatChieu) throws SQLException {
        return veDAO.layGiaVeTheoSuat(maSuatChieu);
    }

    public String taoPhieu(String maKhachHang, String maSuatChieu,
                           List<String> danhSachMaGhe) throws SQLException {
        kiemTra(maKhachHang, maSuatChieu, danhSachMaGhe);

        String maPhieu = taoMa("PD");
        KhachHang khachHang = khachHangDAO.layKhachHangChoTiepNhan(maKhachHang);
        if (khachHang == null) {
            throw new SQLException("Không tìm thấy khách hàng " + maKhachHang + ". Hãy tìm khách trước khi tạo phiếu.");
        }
        PhieuDatVe phieu = new PhieuDatVe(
                maPhieu, LocalDateTime.now(), khachHang, "Chờ thanh toán");

        boolean autoCommitCu = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            // Giá và ghế được đọc lại bên trong transaction trước khi ghi.
            BigDecimal donGia = veDAO.layGiaVeTheoSuat(maSuatChieu);
            if (donGia == null || donGia.signum() <= 0) {
                throw new SQLException("Suất chiếu chưa có giá vé hợp lệ.");
            }
            ArrayList<String> gheTrong = veDAO.layGheTrongSuat(maSuatChieu);
            for (String maGhe : danhSachMaGhe) {
                if (!gheTrong.contains(maGhe)
                        || veDAO.kiemTraGheDaDatTrongSuat(maSuatChieu, maGhe)) {
                    throw new SQLException("Ghế " + maGhe + " không còn trống.");
                }
            }

            phieuDAO.themPhieuDatVe(phieu);
            for (String maGhe : danhSachMaGhe) {
                if (!veDAO.giuGhe(maSuatChieu, maGhe)) {
                    throw new SQLException("Ghế " + maGhe + " vừa được người khác chọn.");
                }
                String maVe = taoMa("V");
                veDAO.themVe(maVe, maSuatChieu, maGhe, donGia, "Đã giữ");
                chiTietDAO.themChiTiet(maPhieu, maVe, donGia);
            }
            connection.commit();
            giaVePhieuVuaTao = donGia;
            return maPhieu;
        } catch (SQLException | RuntimeException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommitCu);
        }
    }

    public BigDecimal getGiaVePhieuVuaTao() {
        return giaVePhieuVuaTao;
    }

    public void huyPhieu(String maPhieuDatVe) throws SQLException {
        if (maPhieuDatVe == null || maPhieuDatVe.trim().isEmpty()) {
            throw new IllegalArgumentException("Nhập mã phiếu cần hủy.");
        }

        boolean autoCommitCu = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            PhieuDatVe phieu = phieuDAO.layTheoMa(maPhieuDatVe);
            if (phieu == null) {
                throw new SQLException("Không tìm thấy phiếu đặt.");
            }
            if (!"Chờ thanh toán".equals(phieu.getTrangThai())) {
                throw new SQLException("Chỉ hủy được phiếu đang chờ thanh toán.");
            }
            if (!veDAO.huyVeTheoPhieu(maPhieuDatVe)) {
                throw new SQLException("Không tìm thấy vé đang giữ trong phiếu.");
            }
            if (!veDAO.traGheTheoPhieu(maPhieuDatVe)) {
                throw new SQLException("Không thể trả ghế của phiếu về trạng thái trống.");
            }
            if (!phieuDAO.huyPhieuDangCho(maPhieuDatVe)) {
                throw new SQLException("Phiếu đã được xử lý bởi giao dịch khác.");
            }
            connection.commit();
        } catch (SQLException | RuntimeException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommitCu);
        }
    }

    private void kiemTra(String maKhachHang, String maSuatChieu,
                         List<String> danhSachMaGhe) {
        if (rong(maKhachHang)) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng.");
        }
        if (rong(maSuatChieu)) {
            throw new IllegalArgumentException("Vui lòng chọn suất chiếu.");
        }
        if (danhSachMaGhe == null || danhSachMaGhe.isEmpty()) {
            throw new IllegalArgumentException("Phiếu đặt phải có ít nhất một ghế.");
        }

        Set<String> gheDaChon = new HashSet<String>();
        for (String maGhe : danhSachMaGhe) {
            if (rong(maGhe) || !gheDaChon.add(maGhe)) {
                throw new IllegalArgumentException("Mã ghế không hợp lệ hoặc bị trùng.");
            }
        }
    }

    private boolean rong(String giaTri) {
        return giaTri == null || giaTri.trim().isEmpty();
    }

    private String taoMa(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase();
    }
}
