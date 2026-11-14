package dao;

import entity.KhachHang;
import entity.PhieuDatVe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/** DAO cho bảng PhieuDatVe. Connection được truyền vào để dùng chung transaction. */
public class PhieuDatVe_DAO {
    private Connection connection;

    public PhieuDatVe_DAO(Connection connection) {
        this.connection = connection;
    }

    public boolean themPhieuDatVe(PhieuDatVe phieu) throws SQLException {
        String sql = "INSERT INTO PhieuDatVe "
                + "(maPhieuDatVe, ngayDat, maKhachHang, trangThai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phieu.getMaPhieuDatVe());
            statement.setTimestamp(2, Timestamp.valueOf(phieu.getNgayDat()));
            statement.setString(3, phieu.getKhachHang().getMaKhachHang());
            statement.setString(4, phieu.getTrangThai());
            return statement.executeUpdate() == 1;
        }
    }

    public PhieuDatVe layTheoMa(String maPhieuDatVe) throws SQLException {
        String sql = "SELECT maPhieuDatVe, ngayDat, maKhachHang, trangThai "
                + "FROM PhieuDatVe WHERE maPhieuDatVe = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuDatVe);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return taoPhieu(result);
                }
            }
        }
        return null;
    }

    public ArrayList<PhieuDatVe> layTatCa() throws SQLException {
        ArrayList<PhieuDatVe> danhSach = new ArrayList<PhieuDatVe>();
        String sql = "SELECT maPhieuDatVe, ngayDat, maKhachHang, trangThai "
                + "FROM PhieuDatVe ORDER BY ngayDat DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                danhSach.add(taoPhieu(result));
            }
        }
        return danhSach;
    }

    public ArrayList<PhieuDatVe> timKiem(String tuKhoa) throws SQLException {
        ArrayList<PhieuDatVe> danhSach = new ArrayList<PhieuDatVe>();
        String sql = "SELECT maPhieuDatVe, ngayDat, maKhachHang, trangThai "
                + "FROM PhieuDatVe WHERE maPhieuDatVe LIKE ? OR maKhachHang LIKE ? "
                + "ORDER BY ngayDat DESC";
        String mau = "%" + tuKhoa.trim() + "%";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, mau);
            statement.setString(2, mau);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    danhSach.add(taoPhieu(result));
                }
            }
        }
        return danhSach;
    }

    public boolean capNhatTrangThai(String maPhieuDatVe, String trangThai) throws SQLException {
        String sql = "UPDATE PhieuDatVe SET trangThai = ? WHERE maPhieuDatVe = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trangThai);
            statement.setString(2, maPhieuDatVe);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean huyPhieuDangCho(String maPhieuDatVe) throws SQLException {
        String sql = "UPDATE PhieuDatVe SET trangThai = N'Đã hủy' "
                + "WHERE maPhieuDatVe = ? AND trangThai = N'Chờ thanh toán'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuDatVe);
            return statement.executeUpdate() == 1;
        }
    }

    private PhieuDatVe taoPhieu(ResultSet result) throws SQLException {
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang(result.getString("maKhachHang"));
        return new PhieuDatVe(
                result.getString("maPhieuDatVe"),
                result.getTimestamp("ngayDat").toLocalDateTime(),
                khachHang,
                result.getString("trangThai"));
    }
}
