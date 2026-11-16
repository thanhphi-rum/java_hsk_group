package entity;

import java.time.LocalDateTime;

public class HoaDon {
    private String maHoaDon;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private KhuyenMai khuyenMai;
    private LocalDateTime ngayLap;
    private double tongTien;
    private String phuongThucThanhToan;
    private PhieuDatVe phieuDatVe;
    private String trangThai = "Đã thanh toán";
    
	public HoaDon(String maHoaDon, NhanVien nhanVien, KhachHang khachHang, KhuyenMai khuyenMai, LocalDateTime ngayLap,
			double tongTien, String phongThucThanhToan) {
		this.maHoaDon = maHoaDon;
		this.nhanVien = nhanVien;
		this.khachHang = khachHang;
		this.khuyenMai = khuyenMai;
		this.ngayLap = ngayLap;
		this.tongTien = tongTien;
		this.phuongThucThanhToan = phongThucThanhToan;
	}

	public HoaDon(String maHoaDon, NhanVien nhanVien, KhachHang khachHang, KhuyenMai khuyenMai, LocalDateTime ngayLap,
			double tongTien, String phongThucThanhToan, PhieuDatVe phieuDatVe) {
		this(maHoaDon, nhanVien, khachHang, khuyenMai, ngayLap, tongTien, phongThucThanhToan);
		this.phieuDatVe = phieuDatVe;
	}

	public HoaDon(String maHoaDon) {
		this.maHoaDon = maHoaDon;
	}

	public HoaDon() {
		super();
	}

	public String getMaHoaDon() {
		return maHoaDon;
	}

	public void setMaHoaDon(String maHoaDon) {
		this.maHoaDon = maHoaDon;
	}

	public NhanVien getNhanVien() {
		return nhanVien;
	}

	public void setNhanVien(NhanVien nhanVien) {
		this.nhanVien = nhanVien;
	}

	public KhachHang getKhachHang() {
		return khachHang;
	}

	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}

	public KhuyenMai getKhuyenMai() {
		return khuyenMai;
	}

	public void setKhuyenMai(KhuyenMai khuyenMai) {
		this.khuyenMai = khuyenMai;
	}

	public LocalDateTime getNgayLap() {
		return ngayLap;
	}

	public void setNgayLap(LocalDateTime ngayLap) {
		this.ngayLap = ngayLap;
	}

	public double getTongTien() {
		return tongTien;
	}

	public void setTongTien(double tongTien) {
		this.tongTien = tongTien;
	}

	public PhieuDatVe getPhieuDatVe() {
		return phieuDatVe;
	}

	public void setPhieuDatVe(PhieuDatVe phieuDatVe) {
		this.phieuDatVe = phieuDatVe;
	}

	public String getTrangThai(){return trangThai;}

    public void setTrangThai(String trangThai){this.trangThai=trangThai;}

    public String getPhongThucThanhToan() {
		return phuongThucThanhToan;
	}

	public void setPhongThucThanhToan(String phongThucThanhToan) {
		this.phuongThucThanhToan = phongThucThanhToan;
	}

	/** Theo sơ đồ: không có tongTien (tổng tiền được tính từ ChiTietHoaDon). */
	public HoaDon(String maHoaDon, LocalDateTime ngayLap, NhanVien nhanVien, KhachHang khachHang, KhuyenMai khuyenMai,
			String phuongThucThanhToan, PhieuDatVe phieuDatVe) {
		this(maHoaDon, nhanVien, khachHang, khuyenMai, ngayLap, 0, phuongThucThanhToan, phieuDatVe);
	}

	@Override
	public String toString() {
		return "HoaDon [maHoaDon=" + maHoaDon + ", ngayLap=" + ngayLap + ", phuongThucThanhToan=" + phuongThucThanhToan
				+ ", trangThai=" + trangThai + "]";
	}
}
