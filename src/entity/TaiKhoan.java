	package entity;

public class TaiKhoan {
	private String tenDangNhap;
	private String matKhau;
	private String trangThai;
	private NhanVien nhanVien;
	
	public TaiKhoan(String tenDangNhap, String matKhau, String trangThai, NhanVien nhanVien) {
		this.tenDangNhap = tenDangNhap;
		this.matKhau = matKhau;
		this.trangThai = trangThai;
		this.nhanVien = nhanVien;
	}

	public TaiKhoan() {
		super();
	}

	public String getTenDangNhap() {
		return tenDangNhap;
	}

	public void setTenDangNhap(String tenDangNhap) {
		this.tenDangNhap = tenDangNhap;
	}

	public String getMatKhau() {
		return matKhau;
	}

	public void setMatKhau(String matKhau) {
		this.matKhau = matKhau;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	public NhanVien getNhanVien() {
		return nhanVien;
	}

	public void setNhanVien(NhanVien nhanVien) {
		this.nhanVien = nhanVien;
	}

	/** Tài khoản còn hoạt động (không bị khóa). */
	public boolean isHoatDong() {
		return trangThai == null || trangThai.trim().equalsIgnoreCase("Hoạt động");
	}

	/** Người dùng có chức vụ quản lý thì được dùng các chức năng quản trị. */
	public boolean isQuanLy() {
		return nhanVien != null && nhanVien.getChucVu() != null
				&& nhanVien.getChucVu().toLowerCase().contains("quản lý");
	}
}
