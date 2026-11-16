package dao;

import connect.ConnectDB;
import entity.ComboBapNuoc;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComboBapNuocDAO {
    public static final String DANG_BAN = "Đang bán";
    public static final String NGUNG_BAN = "Ngừng bán";
    private static final String CAC_COT = "MaCombo,TenCombo,GiaBan,MoTa,TrangThai";
    private static final BigDecimal GIA_TOI_DA = new BigDecimal("9999999999999999.99");

    public List<ComboBapNuoc> layComboDangBan() {
        List<ComboBapNuoc> danhSach = new ArrayList<>();
        String sql = "SELECT " + CAC_COT + " FROM ComboBapNuoc WHERE TrangThai=? AND GiaBan>=0 ORDER BY TenCombo,MaCombo";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, DANG_BAN);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) danhSach.add(docCombo(r));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    public List<ComboBapNuoc> layTatCaCombo() throws SQLException {
        List<ComboBapNuoc> danhSach = new ArrayList<>();
        String sql = "SELECT " + CAC_COT + " FROM ComboBapNuoc ORDER BY TenCombo,MaCombo";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {
            while (r.next()) danhSach.add(docCombo(r));
        }
        return danhSach;
    }

    public boolean themCombo(ComboBapNuoc combo) throws SQLException {
        kiemTraDuLieu(combo);
        String sql = "INSERT INTO ComboBapNuoc(" + CAC_COT + ") VALUES(?,?,?,?,?)";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, combo.getMaCombo().trim());
            p.setString(2, combo.getTenCombo().trim());
            p.setBigDecimal(3, combo.getGiaBan().setScale(2));
            p.setString(4, combo.getMoTa());
            p.setString(5, combo.getTrangThai());
            return p.executeUpdate() == 1;
        }
    }

    public boolean suaCombo(ComboBapNuoc combo) throws SQLException {
        kiemTraDuLieu(combo);
        String sql = "UPDATE ComboBapNuoc SET TenCombo=?,GiaBan=?,MoTa=?,TrangThai=? WHERE MaCombo=?";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, combo.getTenCombo().trim());
            p.setBigDecimal(2, combo.getGiaBan().setScale(2));
            p.setString(3, combo.getMoTa());
            p.setString(4, combo.getTrangThai());
            p.setString(5, combo.getMaCombo().trim());
            return p.executeUpdate() == 1;
        }
    }

    /** Giữ combo trong lịch sử hóa đơn khi ngừng bán. */
    public boolean capNhatTrangThai(String maCombo, String trangThai) throws SQLException {
        kiemTraChuoiBatBuoc(maCombo, 20, "Mã combo");
        kiemTraTrangThai(trangThai);
        String sql = "UPDATE ComboBapNuoc SET TrangThai=? WHERE MaCombo=?";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, trangThai);
            p.setString(2, maCombo.trim());
            return p.executeUpdate() == 1;
        }
    }

    public static void kiemTraDuLieu(ComboBapNuoc combo) {
        if (combo == null) throw new IllegalArgumentException("Thông tin combo không được để trống.");
        kiemTraChuoiBatBuoc(combo.getMaCombo(), 20, "Mã combo");
        kiemTraChuoiBatBuoc(combo.getTenCombo(), 100, "Tên combo");
        if (combo.getMoTa() != null && combo.getMoTa().length() > 200) {
            throw new IllegalArgumentException("Mô tả tối đa 200 ký tự.");
        }
        BigDecimal gia = combo.getGiaBan();
        if (gia == null || gia.signum() < 0) {
            throw new IllegalArgumentException("Giá bán phải là số lớn hơn hoặc bằng 0.");
        }
        if (gia.compareTo(GIA_TOI_DA) > 0) {
            throw new IllegalArgumentException("Giá bán vượt quá giới hạn 9.999.999.999.999.999,99.");
        }
        if (gia.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Giá bán chỉ được có tối đa 2 chữ số thập phân.");
        }
        kiemTraTrangThai(combo.getTrangThai());
    }

    private static void kiemTraChuoiBatBuoc(String giaTri, int doDaiToiDa, String nhan) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            throw new IllegalArgumentException(nhan + " không được để trống.");
        }
        if (giaTri.length() > doDaiToiDa) {
            throw new IllegalArgumentException(nhan + " tối đa " + doDaiToiDa + " ký tự.");
        }
    }

    private static void kiemTraTrangThai(String trangThai) {
        if (!DANG_BAN.equals(trangThai) && !NGUNG_BAN.equals(trangThai)) {
            throw new IllegalArgumentException("Trạng thái phải là Đang bán hoặc Ngừng bán.");
        }
    }

    private static ComboBapNuoc docCombo(ResultSet r) throws SQLException {
        return new ComboBapNuoc(r.getString("MaCombo"), r.getString("TenCombo"), r.getBigDecimal("GiaBan"),
                r.getString("MoTa"), r.getString("TrangThai"));
    }
}
