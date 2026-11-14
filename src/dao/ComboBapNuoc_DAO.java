package dao;

import entity.ComboBapNuoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** CRUD combo bằng PreparedStatement. */
public class ComboBapNuoc_DAO {
    private Connection connection;

    public ComboBapNuoc_DAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<ComboBapNuoc> layTatCa() throws SQLException {
        ArrayList<ComboBapNuoc> danhSach = new ArrayList<ComboBapNuoc>();
        String sql = "SELECT maCombo, tenCombo, giaBan, moTa, trangThai "
                + "FROM ComboBapNuoc ORDER BY tenCombo";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                danhSach.add(taoCombo(result));
            }
        }
        return danhSach;
    }

    public ArrayList<ComboBapNuoc> layComboDangBan() throws SQLException {
        ArrayList<ComboBapNuoc> danhSach = new ArrayList<ComboBapNuoc>();
        String sql = "SELECT maCombo, tenCombo, giaBan, moTa, trangThai "
                + "FROM ComboBapNuoc WHERE trangThai = ? ORDER BY tenCombo";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "Đang bán");
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    danhSach.add(taoCombo(result));
                }
            }
        }
        return danhSach;
    }

    public boolean themCombo(ComboBapNuoc combo) throws SQLException {
        String sql = "INSERT INTO ComboBapNuoc "
                + "(maCombo, tenCombo, giaBan, moTa, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, combo);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean capNhatCombo(ComboBapNuoc combo) throws SQLException {
        String sql = "UPDATE ComboBapNuoc SET tenCombo = ?, giaBan = ?, moTa = ?, "
                + "trangThai = ? WHERE maCombo = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, combo.getTenCombo());
            statement.setBigDecimal(2, combo.getGiaBan());
            statement.setString(3, combo.getMoTa());
            statement.setString(4, combo.getTrangThai());
            statement.setString(5, combo.getMaCombo());
            return statement.executeUpdate() == 1;
        }
    }

    private void ganThamSo(PreparedStatement statement, ComboBapNuoc combo)
            throws SQLException {
        statement.setString(1, combo.getMaCombo());
        statement.setString(2, combo.getTenCombo());
        statement.setBigDecimal(3, combo.getGiaBan());
        statement.setString(4, combo.getMoTa());
        statement.setString(5, combo.getTrangThai());
    }

    private ComboBapNuoc taoCombo(ResultSet result) throws SQLException {
        return new ComboBapNuoc(
                result.getString("maCombo"),
                result.getString("tenCombo"),
                result.getBigDecimal("giaBan"),
                result.getString("moTa"),
                result.getString("trangThai"));
    }
}
