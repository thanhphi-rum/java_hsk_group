package dao;

import connect.ConnectDB;
import entity.*;
import util.GiaVe;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Quản lý vòng đời phiếu đặt vé; thanh toán nằm ở HoaDonDAO. */
public class PhieuDatVeDAO {
    public PhieuDatVe layPhieuTheoMa(String maPhieuDatVe) {
        String sql = "SELECT p.MaPhieuDatVe,p.NgayDat,p.MaKhachHang,p.TrangThai,k.TenKhachHang "
                + "FROM PhieuDatVe p LEFT JOIN KhachHang k ON k.MaKhachHang=p.MaKhachHang WHERE p.MaPhieuDatVe=?";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuDatVe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docPhieu(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PhieuDatVe> layDanhSach(String tuKhoa, String trangThai) throws SQLException {
        List<PhieuDatVe> ds = new ArrayList<>();
        String sql = "SELECT p.MaPhieuDatVe,p.NgayDat,p.MaKhachHang,p.TrangThai,k.TenKhachHang "
                + "FROM PhieuDatVe p LEFT JOIN KhachHang k ON k.MaKhachHang=p.MaKhachHang "
                + "WHERE (?='' OR p.MaPhieuDatVe LIKE ? OR ISNULL(k.TenKhachHang,N'') LIKE ? "
                + "OR ISNULL(k.SoDienThoai,'') LIKE ?) AND (? IS NULL OR p.TrangThai=?) "
                + "ORDER BY p.NgayDat DESC,p.MaPhieuDatVe DESC";
        String keyword = tuKhoa == null ? "" : tuKhoa.trim();
        String pattern = "%" + keyword + "%";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, trangThai);
            ps.setString(6, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(docPhieu(rs));
            }
        }
        return ds;
    }

    public List<ChiTietPhieuDatVe> layChiTiet(String maPhieuDatVe) throws SQLException {
        List<ChiTietPhieuDatVe> ds = new ArrayList<>();
        String sql = "SELECT d.MaVe,d.DonGia,v.MaSuatChieu,v.MaGhe,v.GiaTien,v.TrangThai FROM ChiTietPhieuDatVe d "
                + "JOIN Ve v ON v.MaVe=d.MaVe WHERE d.MaPhieuDatVe=? ORDER BY d.MaVe";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuDatVe);
            PhieuDatVe phieu = new PhieuDatVe(maPhieuDatVe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GheSuatChieu gsc = new GheSuatChieu(new Ghe(rs.getString("MaGhe")), new SuatChieu(rs.getString("MaSuatChieu")), null);
                    Ve ve = new Ve(rs.getString("MaVe"), gsc, rs.getDouble("GiaTien"), rs.getString("TrangThai"));
                    ds.add(new ChiTietPhieuDatVe(phieu, ve, rs.getDouble("DonGia")));
                }
            }
        }
        return ds;
    }

    public double taoPhieuDat(Connection conn, String maPhieuDatVe, String maKhachHang,
            String maSuatChieu, List<String> maGhe) throws SQLException {
        if (conn.getAutoCommit()) throw new SQLException("Tạo phiếu phải chạy trong transaction");
        if (maGhe == null || maGhe.isEmpty()) throw new SQLException("Chọn ít nhất một ghế");
        if (new HashSet<>(maGhe).size() != maGhe.size()) throw new SQLException("Danh sách ghế bị trùng");

        double giaCoBan;
        String trangThaiSuat;
        LocalDateTime gioBatDau;
        try (PreparedStatement ps = conn.prepareStatement("SELECT GiaVe,TrangThai,GioBatDau FROM SuatChieu WITH (UPDLOCK,HOLDLOCK) WHERE MaSuatChieu=?")) {
            ps.setString(1, maSuatChieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy suất chiếu");
                giaCoBan = rs.getDouble("GiaVe");
                trangThaiSuat = rs.getString("TrangThai");
                gioBatDau = rs.getTimestamp("GioBatDau").toLocalDateTime();
            }
        }
        String loiSuat = ThanhToanBanVeDAO.kiemTraSuat(trangThaiSuat, gioBatDau);
        if (loiSuat != null) throw new SQLException(loiSuat);

        Map<String, Double> giaTheoGhe = new LinkedHashMap<>();
        String sqlGhe = "SELECT gs.TrangThai,g.LoaiGhe FROM Ghe_SuatChieu gs WITH (UPDLOCK,HOLDLOCK) "
                + "JOIN Ghe g ON g.MaGhe=gs.MaGhe WHERE gs.MaSuatChieu=? AND gs.MaGhe=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlGhe)) {
            for (String ma : maGhe) {
                ps.setString(1, maSuatChieu);
                ps.setString(2, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Ghế không thuộc suất chiếu: " + ma);
                    if (!"Trống".equalsIgnoreCase(rs.getString("TrangThai")))
                        throw new SQLException("Ghế vừa được giữ hoặc bán: " + ma);
                    giaTheoGhe.put(ma, GiaVe.tinh(giaCoBan, rs.getString("LoaiGhe")));
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO PhieuDatVe(MaPhieuDatVe,MaKhachHang,NgayDat,TrangThai) VALUES(?,?,SYSDATETIME(),N'Chờ xác nhận')")) {
            ps.setString(1, maPhieuDatVe);
            ps.setString(2, maKhachHang == null || maKhachHang.isBlank() ? null : maKhachHang);
            ps.executeUpdate();
        }

        double tongTien = 0;
        String sqlVe = "INSERT INTO Ve(MaVe,MaSuatChieu,MaGhe,GiaTien,TrangThai) VALUES(?,?,?,?,N'Đã đặt')";
        String sqlChiTiet = "INSERT INTO ChiTietPhieuDatVe(MaPhieuDatVe,MaVe,DonGia) VALUES(?,?,?)";
        String sqlGiuGhe = "UPDATE Ghe_SuatChieu SET TrangThai=N'Đã đặt' WHERE MaSuatChieu=? AND MaGhe=? AND TrangThai=N'Trống'";
        try (PreparedStatement ve = conn.prepareStatement(sqlVe);
             PreparedStatement chiTiet = conn.prepareStatement(sqlChiTiet);
             PreparedStatement giuGhe = conn.prepareStatement(sqlGiuGhe)) {
            for (Map.Entry<String, Double> entry : giaTheoGhe.entrySet()) {
                String maVe = "VE" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
                ve.setString(1, maVe);
                ve.setString(2, maSuatChieu);
                ve.setString(3, entry.getKey());
                ve.setDouble(4, entry.getValue());
                if (ve.executeUpdate() != 1) throw new SQLException("Không tạo được vé cho ghế " + entry.getKey());

                chiTiet.setString(1, maPhieuDatVe);
                chiTiet.setString(2, maVe);
                chiTiet.setDouble(3, entry.getValue());
                if (chiTiet.executeUpdate() != 1) throw new SQLException("Không lưu được chi tiết phiếu đặt");

                giuGhe.setString(1, maSuatChieu);
                giuGhe.setString(2, entry.getKey());
                if (giuGhe.executeUpdate() != 1) throw new SQLException("Không giữ được ghế " + entry.getKey());
                tongTien += entry.getValue();
            }
        }
        return tongTien;
    }

    public void huyPhieuDat(Connection conn, String maPhieuDatVe) throws SQLException {
        if (conn.getAutoCommit()) throw new SQLException("Hủy phiếu phải chạy trong transaction");
        String trangThai;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TrangThai FROM PhieuDatVe WITH (UPDLOCK,HOLDLOCK) WHERE MaPhieuDatVe=?")) {
            ps.setString(1, maPhieuDatVe);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy phiếu đặt");
                trangThai = rs.getString("TrangThai");
            }
        }
        if (!"Chờ xác nhận".equals(trangThai) && !"Đã xác nhận".equals(trangThai))
            throw new SQLException("Chỉ hủy được phiếu đang chờ xác nhận");

        List<String[]> chiTiet = new ArrayList<>();
        String sql = "SELECT d.MaVe,v.MaSuatChieu,v.MaGhe,v.TrangThai AS TrangThaiVe,gs.TrangThai AS TrangThaiGhe "
                + "FROM ChiTietPhieuDatVe d JOIN Ve v WITH (UPDLOCK,HOLDLOCK) ON v.MaVe=d.MaVe "
                + "JOIN Ghe_SuatChieu gs WITH (UPDLOCK,HOLDLOCK) ON gs.MaSuatChieu=v.MaSuatChieu AND gs.MaGhe=v.MaGhe "
                + "WHERE d.MaPhieuDatVe=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieuDatVe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!"Đã đặt".equals(rs.getString("TrangThaiVe")) || !"Đã đặt".equals(rs.getString("TrangThaiGhe")))
                        throw new SQLException("Trạng thái vé hoặc ghế đã thay đổi; phiếu chưa được hủy");
                    chiTiet.add(new String[]{rs.getString("MaVe"), rs.getString("MaSuatChieu"), rs.getString("MaGhe")});
                }
            }
        }
        if (chiTiet.isEmpty()) throw new SQLException("Phiếu đặt không có vé");

        try (PreparedStatement ve = conn.prepareStatement("UPDATE Ve SET TrangThai=N'Đã hủy' WHERE MaVe=? AND TrangThai=N'Đã đặt'");
             PreparedStatement ghe = conn.prepareStatement("UPDATE Ghe_SuatChieu SET TrangThai=N'Trống' WHERE MaSuatChieu=? AND MaGhe=? AND TrangThai=N'Đã đặt'")) {
            for (String[] dong : chiTiet) {
                ve.setString(1, dong[0]);
                if (ve.executeUpdate() != 1) throw new SQLException("Không hủy được vé " + dong[0]);
                ghe.setString(1, dong[1]);
                ghe.setString(2, dong[2]);
                if (ghe.executeUpdate() != 1) throw new SQLException("Không trả được ghế " + dong[2]);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE PhieuDatVe SET TrangThai=N'Đã hủy' WHERE MaPhieuDatVe=? AND TrangThai IN (N'Chờ xác nhận',N'Đã xác nhận')")) {
            ps.setString(1, maPhieuDatVe);
            if (ps.executeUpdate() != 1) throw new SQLException("Trạng thái phiếu vừa thay đổi; hãy tải lại danh sách");
        }
    }

    private PhieuDatVe docPhieu(ResultSet rs) throws SQLException {
        Timestamp ngay = rs.getTimestamp("NgayDat");
        String maKhach = rs.getString("MaKhachHang");
        KhachHang khach = null;
        if (maKhach != null) {
            khach = new KhachHang(maKhach);
            khach.setTenKhachHang(rs.getString("TenKhachHang"));
        }
        return new PhieuDatVe(rs.getString("MaPhieuDatVe"),
                ngay == null ? null : ngay.toLocalDateTime(), khach, rs.getString("TrangThai"));
    }
}
