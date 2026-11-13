package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.ChiTietPhieuDatVe_E;
import entity.Ghe_E;
import entity.GheSuatChieu_E;
import entity.PhieuDatVe_E;
import entity.SuatChieu_E;
import entity.Ve_E;

public class ChiTietPhieuDatVe_DAO {

	// Chi tiết của một phiếu, kèm thông tin vé và vị trí ghế
	public ArrayList<ChiTietPhieuDatVe_E> getChiTietTheoPhieu(String maPhieuDatVe) {
		ArrayList<ChiTietPhieuDatVe_E> ds = new ArrayList<ChiTietPhieuDatVe_E>();
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "select c.donGia, v.maVe, v.giaTien, v.trangThai, v.maGhe, v.maSuatChieu, g.viTri, g.loaiGhe "
				+ "from ChiTietPhieuDatVe c join Ve v on c.maVe = v.maVe join Ghe g on v.maGhe = g.maGhe "
				+ "where c.maPhieuDatVe = ? order by g.viTri";
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, maPhieuDatVe);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Ghe ghe = new Ghe(rs.getString("maGhe"), rs.getString("viTri"), rs.getString("loaiGhe"));
					GheSuatChieu gheSuat = new GheSuatChieu_E(ghe, new SuatChieu(rs.getString("maSuatChieu")),
							null);
					Ve ve = new Ve_E(rs.getString("maVe"), gheSuat, rs.getDouble("giaTien"),
							rs.getString("trangThai"));
					ds.add(new ChiTietPhieuDatVe_E(new PhieuDatVe(maPhieuDatVe), ve, rs.getDouble("donGia")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ds;
	}

	public boolean themChiTiet(ChiTietPhieuDatVe ct) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "insert into ChiTietPhieuDatVe(maPhieuDatVe, maVe, donGia) values(?, ?, ?)";
		int n = 0;
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, ct.getMaPhieuDatVe().getMaPhieuDatVe());
			stmt.setString(2, ct.getMaVe().getMaVe());
			stmt.setDouble(3, ct.getDonGia());
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n > 0;
	}

	public boolean xoaChiTietTheoPhieu(String maPhieuDatVe) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		int n = 0;
		try (PreparedStatement stmt = con.prepareStatement("delete from ChiTietPhieuDatVe where maPhieuDatVe=?")) {
			stmt.setString(1, maPhieuDatVe);
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n > 0;
	}
}