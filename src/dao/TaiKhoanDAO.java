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
}
