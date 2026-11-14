package dao;

import connect.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {
    public static final String HOAT_DONG = "Hoạt động";
    public static final String KHOA = "Khóa";

    private static Connection ketNoi() throws SQLException {
        Connection conn = ConnectDB.getConnection();
        if (conn == null) {
            throw new SQLException("Không kết nối được cơ sở dữ liệu");
        }
        return conn;
    }

    // ================= Đăng nhập =================

    /**
     * Kiểm tra đăng nhập.
     * Mật khẩu trong CSDL có thể là SHA-256 (chuẩn mới) hoặc văn bản thô (dữ liệu mẫu cũ);
     * nếu là văn bản thô và đăng nhập đúng thì tự động băm lại để lưu an toàn.
     *
     * @return tài khoản (kèm nhân viên) nếu đúng; null nếu sai tên hoặc mật khẩu.
     *         Việc bị khóa hay không do nơi gọi kiểm tra bằng TaiKhoan.isHoatDong().
     */
    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT tk.TenDangNhap, tk.MatKhau, tk.TrangThai, "
                + "nv.MaNhanVien, nv.TenNhanVien, nv.ChucVu, nv.TrangThai AS TrangThaiNhanVien "
                + "FROM TaiKhoan tk JOIN NhanVien nv ON tk.MaNhanVien = nv.MaNhanVien "
                + "WHERE tk.TenDangNhap = ?";
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String luu = rs.getString("MatKhau").trim();
                boolean cu = !laChuoiBam(luu);
                boolean dung = cu ? luu.equals(matKhau) : bam(matKhau).equalsIgnoreCase(luu);
                if (!dung) return null;
                if (!"Đang làm".equalsIgnoreCase(rs.getString("TrangThaiNhanVien"))) return null;

                String ten = rs.getString("TenDangNhap");
                if (cu) {
                    capNhatMatKhau(conn, ten, bam(matKhau)); // nâng cấp lên mật khẩu đã băm
                }
                NhanVien nv = new NhanVien(rs.getString("MaNhanVien"));
                nv.setTenNhanVien(rs.getString("TenNhanVien"));
                nv.setChucVu(rs.getString("ChucVu"));
                return new TaiKhoan(ten, null, rs.getString("TrangThai"), nv);
            }
        }
    }

    // ================= Quản lý tài khoản =================

    public List<TaiKhoan> layTatCa() throws SQLException {
        String sql = "SELECT tk.TenDangNhap, tk.TrangThai, nv.MaNhanVien, nv.TenNhanVien, nv.ChucVu "
                + "FROM TaiKhoan tk JOIN NhanVien nv ON tk.MaNhanVien = nv.MaNhanVien "
                + "ORDER BY tk.TenDangNhap";
        List<TaiKhoan> ds = new ArrayList<>();
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien(rs.getString("MaNhanVien"));
                nv.setTenNhanVien(rs.getString("TenNhanVien"));
                nv.setChucVu(rs.getString("ChucVu"));
                ds.add(new TaiKhoan(rs.getString("TenDangNhap"), null, rs.getString("TrangThai"), nv));
            }
        }
        return ds;
    }

    /** Nhân viên chưa có tài khoản (mỗi nhân viên chỉ có 1 tài khoản). */
    public List<NhanVien> layNhanVienChuaCoTaiKhoan() throws SQLException {
        String sql = "SELECT MaNhanVien, TenNhanVien, ChucVu FROM NhanVien "
                + "WHERE MaNhanVien NOT IN (SELECT MaNhanVien FROM TaiKhoan) ORDER BY MaNhanVien";
        List<NhanVien> ds = new ArrayList<>();
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien(rs.getString("MaNhanVien"));
                nv.setTenNhanVien(rs.getString("TenNhanVien"));
                nv.setChucVu(rs.getString("ChucVu"));
                ds.add(nv);
            }
        }
        return ds;
    }

    public boolean tonTai(String tenDangNhap) throws SQLException {
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM TaiKhoan WHERE TenDangNhap = ?")) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Tạo tài khoản mới (mật khẩu được băm trước khi lưu). */
    public boolean them(String tenDangNhap, String matKhau, String maNhanVien) throws SQLException {
        String sql = "INSERT INTO TaiKhoan (TenDangNhap, MatKhau, TrangThai, MaNhanVien) VALUES (?, ?, ?, ?)";
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, bam(matKhau));
            ps.setString(3, HOAT_DONG);
            ps.setString(4, maNhanVien);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean doiMatKhau(String tenDangNhap, String matKhauMoi) throws SQLException {
        try (Connection conn = ketNoi()) {
            return capNhatMatKhau(conn, tenDangNhap, bam(matKhauMoi));
        }
    }

    public boolean datTrangThai(String tenDangNhap, String trangThai) throws SQLException {
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement("UPDATE TaiKhoan SET TrangThai = ? WHERE TenDangNhap = ?")) {
            ps.setString(1, trangThai);
            ps.setString(2, tenDangNhap);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String tenDangNhap) throws SQLException {
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM TaiKhoan WHERE TenDangNhap = ?")) {
            ps.setString(1, tenDangNhap);
            return ps.executeUpdate() > 0;
        }
    }

    /** Số tài khoản quản lý đang hoạt động (để không khóa/xóa người quản lý cuối cùng). */
    public int demQuanLyHoatDong() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TaiKhoan tk JOIN NhanVien nv ON tk.MaNhanVien = nv.MaNhanVien "
                + "WHERE tk.TrangThai = ? AND nv.ChucVu LIKE N'%quản lý%'";
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, HOAT_DONG);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ================= Tiện ích =================

    private boolean capNhatMatKhau(Connection conn, String tenDangNhap, String matKhauDaBam) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE TaiKhoan SET MatKhau = ? WHERE TenDangNhap = ?")) {
            ps.setString(1, matKhauDaBam);
            ps.setString(2, tenDangNhap);
            return ps.executeUpdate() > 0;
        }
    }

    private static boolean laChuoiBam(String s) {
        return s.length() == 64 && s.matches("[0-9a-fA-F]{64}");
    }

    /** Băm mật khẩu bằng SHA-256, trả về chuỗi hex. */
    public static String bam(String s) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
