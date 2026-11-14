package dao;

import entity.ChiTietPhieuDatVe;
import entity.PhieuDatVe;
import entity.Ve;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;

/** DAO cho các vé nằm trong một phiếu đặt. */
public class ChiTietPhieuDatVe_DAO {
    private Connection connection;

    public ChiTietPhieuDatVe_DAO(Connection connection) {
        this.connection = connection;
    }

    public boolean themChiTiet(String maPhieuDatVe, String maVe,
                               BigDecimal donGia) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuDatVe (maPhieuDatVe, maVe, donGia) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuDatVe);
            statement.setString(2, maVe);
            statement.setBigDecimal(3, donGia);
            return statement.executeUpdate() == 1;
        }
    }

    public ArrayList<ChiTietPhieuDatVe> layTheoPhieu(String maPhieuDatVe)
            throws SQLException {
        ArrayList<ChiTietPhieuDatVe> danhSach = new ArrayList<ChiTietPhieuDatVe>();
        String sql = "SELECT ct.maPhieuDatVe, ct.maVe, ct.donGia, v.giaTien, v.trangThai "
                + "FROM ChiTietPhieuDatVe ct JOIN Ve v ON v.maVe = ct.maVe "
                + "WHERE ct.maPhieuDatVe = ? ORDER BY ct.maVe";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuDatVe);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    PhieuDatVe phieu = new PhieuDatVe();
                    phieu.setMaPhieuDatVe(result.getString("maPhieuDatVe"));

                    Ve ve = new Ve();
                    ve.setMaVe(result.getString("maVe"));
                    ve.setGiaTien(result.getBigDecimal("giaTien"));
                    ve.setTrangThai(result.getString("trangThai"));

                    BigDecimal donGia = result.getBigDecimal("donGia");
                    danhSach.add(new ChiTietPhieuDatVe(phieu, ve, donGia));
                }
            }
        }
        return danhSach;
    }

    public boolean xoaTheoPhieu(String maPhieuDatVe) throws SQLException {
        String sql = "DELETE FROM ChiTietPhieuDatVe WHERE maPhieuDatVe = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuDatVe);
            return statement.executeUpdate() > 0;
        }
    }
}
