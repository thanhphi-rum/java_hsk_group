package entity;

public class ChiTietPhieuDatVe {
	private PhieuDatVe maPhieuDatVe;
	private Ve maVe;
	private double donGia;
	
	public ChiTietPhieuDatVe(PhieuDatVe maPhieuDatVe, Ve maVe, double donGia) {
		this.maPhieuDatVe = maPhieuDatVe;
		this.maVe = maVe;
		this.donGia = donGia;
	}

	public ChiTietPhieuDatVe() {
		super();
	}

	public PhieuDatVe getMaPhieuDatVe() {
		return maPhieuDatVe;
	}

	public void setMaPhieuDatVe(PhieuDatVe maPhieuDatVe) {
		this.maPhieuDatVe = maPhieuDatVe;
	}

	public Ve getMaVe() {
		return maVe;
	}

	public void setMaVe(Ve maVe) {
		this.maVe = maVe;
	}

	public double getDonGia() {
		return donGia;
	}

	public void setDonGia(double donGia) {
		this.donGia = donGia;
	}

	@Override
	public String toString() {
		return "ChiTietPhieuDatVe [donGia=" + donGia + "]";
	}
	
	
}
