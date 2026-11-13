package entity;

import java.time.LocalDateTime;

public class PhieuDatVe {
	private String maPhieuDatVe;
	private LocalDateTime ngayDat;
	private KhachHang maKhachHang;
	private String trangThai;
	
	public PhieuDatVe(String maPhieuDatVe, LocalDateTime ngayDat, KhachHang maKhachHang, String trangThai) {
		this.maPhieuDatVe = maPhieuDatVe;
		this.ngayDat = ngayDat;
		this.maKhachHang = maKhachHang;
		this.trangThai = trangThai;
	}

	public PhieuDatVe() {
		super();
	}

	public String getMaPhieuDatVe() {
		return maPhieuDatVe;
	}

	public void setMaPhieuDatVe(String maPhieuDatVe) {
		this.maPhieuDatVe = maPhieuDatVe;
	}

	public LocalDateTime getNgayDat() {
		return ngayDat;
	}

	public void setNgayDat(LocalDateTime ngayDat) {
		this.ngayDat = ngayDat;
	}

	public KhachHang getMaKhachHang() {
		return maKhachHang;
	}

	public void setMaKhachHang(KhachHang maKhachHang) {
		this.maKhachHang = maKhachHang;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	@Override
	public String toString() {
		return "PhieuDatVe [maPhieuDatVe=" + maPhieuDatVe + ", ngayDat=" + ngayDat + ", trangThai=" + trangThai + "]";
	}
	
	
	
}
