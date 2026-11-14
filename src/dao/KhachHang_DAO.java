/**
 * @description: DAO cho bảng KhachHang
 * @author: phibu
 * @version: 1.0
 */

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.KhachHang;

public class KhachHang_DAO {
	private final Connection connection;

	public KhachHang_DAO() {
		this.connection = null;
	}

	public KhachHang_DAO(Connection connection) {
		this.connection = connection;
	}

	private Connection layKetNoi() {
		if (connection != null) {
			return connection;
		}
		ConnectDB.getInstance();
		return ConnectDB.getConnection();
	}

	// Dựng đối tượng KhachHang_E từ dòng hiện tại của ResultSet
	private KhachHang taoKhachHang(ResultSet rs) throws SQLException {
		String maKhachHang = rs.getString("maKhachHang");
		String tenKhachHang = rs.getString("tenKhachHang");
		String email = rs.getString("email");
		String soDienThoai = rs.getString("soDienThoai");
		int diemTichLuy = rs.getInt("diemTichLuy");
		return new KhachHang(maKhachHang, tenKhachHang, email, soDienThoai, diemTichLuy);
	}

	/** Tìm khách theo mã, tên hoặc số điện thoại trong luồng tiếp nhận tại quầy. */
	public ArrayList<KhachHang> timKhachHangChoTiepNhan(String tuKhoa) throws SQLException {
		ArrayList<KhachHang> danhSach = new ArrayList<KhachHang>();
		String sql = "SELECT maKhachHang, tenKhachHang, email, soDienThoai, diemTichLuy "
				+ "FROM KhachHang WHERE maKhachHang LIKE ? OR tenKhachHang LIKE ? "
				+ "OR soDienThoai LIKE ? ORDER BY tenKhachHang";
		String mau = "%" + tuKhoa.trim() + "%";
		try (PreparedStatement statement = layKetNoi().prepareStatement(sql)) {
			statement.setString(1, mau);
			statement.setString(2, mau);
			statement.setString(3, mau);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					danhSach.add(taoKhachHang(result));
				}
			}
		}
		return danhSach;
	}

	/** Đọc khách hàng theo mã để xác nhận phiếu không tham chiếu mã khách sai. */
	public KhachHang layKhachHangChoTiepNhan(String maKhachHang) throws SQLException {
		String sql = "SELECT maKhachHang, tenKhachHang, email, soDienThoai, diemTichLuy "
				+ "FROM KhachHang WHERE maKhachHang = ?";
		try (PreparedStatement statement = layKetNoi().prepareStatement(sql)) {
			statement.setString(1, maKhachHang);
			try (ResultSet result = statement.executeQuery()) {
				return result.next() ? taoKhachHang(result) : null;
			}
		}
	}

	public ArrayList<KhachHang> getAllKhachHang() {
		ArrayList<KhachHang> dsKhachHang = new ArrayList<KhachHang>();
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "Select * from KhachHang order by maKhachHang";
		try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
			while (rs.next()) {
				dsKhachHang.add(taoKhachHang(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dsKhachHang;
	}

	public KhachHang getKhachHangTheoMa(String maKhachHang) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "Select * from KhachHang where maKhachHang=?";
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, maKhachHang);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return taoKhachHang(rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Tìm theo mã, tên hoặc số điện thoại (gần đúng)
	public ArrayList<KhachHang> timKhachHang(String tuKhoa) {
		ArrayList<KhachHang> dsKhachHang = new ArrayList<KhachHang>();
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "Select * from KhachHang where maKhachHang like ? or tenKhachHang like ? or soDienThoai like ? order by maKhachHang";
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			String mau = "%" + tuKhoa + "%";
			stmt.setString(1, mau);
			stmt.setString(2, mau);
			stmt.setString(3, mau);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					dsKhachHang.add(taoKhachHang(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dsKhachHang;
	}

	// Sinh mã mới dạng KH001, KH002, ...
	public String taoMaKhachHangMoi() {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "Select max(maKhachHang) from KhachHang";
		try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
			if (rs.next() && rs.getString(1) != null) {
				int so = Integer.parseInt(rs.getString(1).substring(2));
				return String.format("KH%03d", so + 1);
			}
		} catch (SQLException | NumberFormatException e) {
			e.printStackTrace();
		}
		return "KH001";
	}

	// Kiểm tra số điện thoại đã có ở khách hàng khác chưa (maLoaiTru: mã được bỏ qua, có thể là null)
	public boolean tonTaiSoDienThoai(String soDienThoai, String maLoaiTru) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "Select count(*) from KhachHang where soDienThoai=? and maKhachHang<>?";
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, soDienThoai);
			stmt.setString(2, maLoaiTru == null ? "" : maLoaiTru);
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

	public boolean themKhachHang(KhachHang kh) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "insert into KhachHang(maKhachHang, tenKhachHang, email, soDienThoai, diemTichLuy) values(?, ?, ?, ?, ?)";
		int n = 0;
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, kh.getMaKhachHang());
			stmt.setString(2, kh.getTenKhachHang());
			stmt.setString(3, kh.getEmail());
			stmt.setString(4, kh.getSoDienThoai());
			stmt.setInt(5, kh.getDiemTichLuy());
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n > 0;
	}

	// Cập nhật thông tin liên hệ (không đổi điểm tích lũy, điểm chỉ đổi qua congDiemTichLuy)
	public boolean capNhatKhachHang(KhachHang kh) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "update KhachHang set tenKhachHang=?, email=?, soDienThoai=? where maKhachHang=?";
		int n = 0;
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, kh.getTenKhachHang());
			stmt.setString(2, kh.getEmail());
			stmt.setString(3, kh.getSoDienThoai());
			stmt.setString(4, kh.getMaKhachHang());
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n > 0;
	}

	// Cộng điểm tích lũy sau khi thanh toán (module hóa đơn có thể gọi hàm này)
	public boolean congDiemTichLuy(String maKhachHang, int diem) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getConnection();
		String sql = "update KhachHang set diemTichLuy = diemTichLuy + ? where maKhachHang=?";
		int n = 0;
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setInt(1, diem);
			stmt.setString(2, maKhachHang);
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n > 0;
	}
}
