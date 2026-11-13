package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.KhachHang;
import entity.PhieuDatVe;
import util.SinhMa;

public class PhieuDatVe_DAO {
	private static final String SELECT_PHIEU = "select p.maPhieuDatVe, p.ngayDat, p.trangThai, "
			+ "k.maKhachHang, k.tenKhachHang, k.email, k.soDienThoai, k.diemTichLuy "
			+ "from PhieuDatVe p join KhachHang k on p.maKhachHang = k.maKhachHang ";

	private PhieuDatVe taoPhieu(ResultSet rs) throws SQLException {
		KhachHang kh = new KhachHang(rs.getString("maKhachHang"), rs.getString("tenKhachHang"),
				rs.getString("email"), rs.getString("soDienThoai"), rs.getInt("diemTichLuy"));
		return new PhieuDatVe_E(rs.getString("maPhieuDatVe"), rs.getTimestamp("ngayDat").toLocalDateTime(), kh,
				rs.getString("trangThai"));
	}

	public String taoMaPhieuMoi() {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		try (Statement statement = con.createStatement();
				ResultSet rs = statement.executeQuery("Select max(maPhieuDatVe) from PhieuDatVe")) {
			if (rs.next()) {
				return SinhMa.maTiepTheo(rs.getString(1), "PD", 6);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "PD000001";
	}

	public ArrayList<PhieuDatVe> getAllPhieu() {
		ArrayList<PhieuDatVe> ds = new ArrayList<PhieuDatVe>();
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		try (Statement statement = con.createStatement();
				ResultSet rs = statement.executeQuery(SELECT_PHIEU + "order by p.ngayDat desc")) {
			while (rs.next()) {
				ds.add(taoPhieu(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ds;
	}

	public PhieuDatVe getPhieuTheoMa(String maPhieuDatVe) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		try (PreparedStatement stmt = con.prepareStatement(SELECT_PHIEU + "where p.maPhieuDatVe = ?")) {
			stmt.setString(1, maPhieuDatVe);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return taoPhieu(rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Tìm theo mã phiếu, tên khách hoặc số điện thoại
	public ArrayList<PhieuDatVe> timPhieu(String tuKhoa) {
		ArrayList<PhieuDatVe> ds = new ArrayList<PhieuDatVe>();
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = SELECT_PHIEU
				+ "where p.maPhieuDatVe like ? or k.tenKhachHang like ? or k.soDienThoai like ? order by p.ngayDat desc";
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			String mau = "%" + tuKhoa + "%";
			stmt.setString(1, mau);
			stmt.setString(2, mau);
			stmt.setString(3, mau);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ds.add(taoPhieu(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ds;
	}

	public boolean themPhieu(PhieuDatVe phieu) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "insert into PhieuDatVe(maPhieuDatVe, ngayDat, maKhachHang, trangThai) values(?, ?, ?, ?)";
		int n = 0;
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, phieu.getMaPhieuDatVe());
			stmt.setTimestamp(2, Timestamp.valueOf(phieu.getNgayDat()));
			stmt.setString(3, phieu.getMaKhachHang().getMaKhachHang());
			stmt.setString(4, phieu.getTrangThai());
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n > 0;
	}

	public boolean capNhatTrangThai(String maPhieuDatVe, String trangThai) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		int n = 0;
		try (PreparedStatement stmt = con
				.prepareStatement("update PhieuDatVe set trangThai=? where maPhieuDatVe=?")) {
			stmt.setString(1, trangThai);
			stmt.setString(2, maPhieuDatVe);
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n > 0;
	}

	// Phiếu đã được lập hóa đơn chưa? (bảng HoaDon do thành viên 3 phụ trách)
	public boolean coHoaDon(String maPhieuDatVe) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		try (PreparedStatement stmt = con.prepareStatement("select count(*) from HoaDon where maPhieuDatVe=?")) {
			stmt.setString(1, maPhieuDatVe);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}