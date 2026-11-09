package dao;

import connect.ConnectDB;

import java.security.*;
import java.sql.*;
import java.util.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class TaiKhoanDAO {
    private static final int ITERATIONS = 210000;

    public static String hash(char[] password) throws GeneralSecurityException {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return ITERATIONS
                + ":"
                + Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verify(char[] password, String hash) throws GeneralSecurityException {
        try {
            String[] parts = hash.split(":");
            int iterations = Integer.parseInt(parts[0]);
            if (parts.length != 3 || iterations < 10000 || iterations > 1000000) return false;
            return MessageDigest.isEqual(
                    Base64.getDecoder().decode(parts[2]),
                    derive(password, Base64.getDecoder().decode(parts[1]), iterations));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String[] dangNhap(String username, char[] password)
            throws SQLException, GeneralSecurityException {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement p =
                        c.prepareStatement(
                                "SELECT t.MatKhauHash, n.MaNhanVien, n.TenNhanVien FROM"
                                    + " TaiKhoanUngDung t JOIN NhanVien n ON"
                                    + " n.MaNhanVien=t.MaNhanVien WHERE t.TenDangNhap=? AND"
                                    + " t.HoatDong=1")) {
            p.setString(1, username);
            try (ResultSet r = p.executeQuery()) {
                if (r.next() && verify(password, r.getString(1)))
                    return new String[] {r.getString(2), r.getString(3)};
                return null;
            }
        }
    }

<<<<<<< HEAD
    public boolean chuaCoTaiKhoan() throws SQLException {
        try (Connection c = ConnectDB.getConnection();
                Statement s = c.createStatement();
                ResultSet r = s.executeQuery("SELECT COUNT(*) FROM TaiKhoanUngDung")) {
            r.next();
            return r.getInt(1) == 0;
        }
    }

    /** Chỉ thiết lập lần đầu; khóa bảng tránh tạo hai tài khoản khởi tạo. */
    public void taoTaiKhoanDauTien(String username, char[] password, String employee)
            throws SQLException, GeneralSecurityException {
        String hashed = hash(password);
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (Statement s = c.createStatement();
                        ResultSet r =
                                s.executeQuery(
                                        "SELECT COUNT(*) FROM TaiKhoanUngDung WITH (TABLOCKX,"
                                            + " HOLDLOCK)")) {
                    r.next();
                    if (r.getInt(1) != 0) throw new SQLException("Tài khoản đã được thiết lập.");
                }
                try (PreparedStatement p =
                        c.prepareStatement(
                                "INSERT INTO"
                                    + " TaiKhoanUngDung(TenDangNhap,MatKhauHash,MaNhanVien,HoatDong)"
                                    + " VALUES(?,?,?,1)")) {
                    p.setString(1, username);
                    p.setString(2, hashed);
                    p.setString(3, employee);
                    p.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
=======
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
>>>>>>> 5bf5933 (update)
}
