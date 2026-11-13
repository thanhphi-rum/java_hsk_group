package entity;

public class Ve {
    private String maVe;
    private GheSuatChieu maGheSuatChieu;
    private double giaTien;
    private String trangThai;
	public Ve(String maVe, GheSuatChieu maGheSuatChieu, double giaTien, String trangThai) {
		this.maVe = maVe;
		this.maGheSuatChieu = maGheSuatChieu;
		this.giaTien = giaTien;
		this.trangThai = trangThai;
	}
	public Ve() {
		super();
	}
	public String getMaVe() {
		return maVe;
	}
	public void setMaVe(String maVe) {
		this.maVe = maVe;
	}
	public GheSuatChieu getMaGheSuatChieu() {
		return maGheSuatChieu;
	}
	public void setMaGheSuatChieu(GheSuatChieu maGheSuatChieu) {
		this.maGheSuatChieu = maGheSuatChieu;
	}
	public double getGiaTien() {
		return giaTien;
	}
	public void setGiaTien(double giaTien) {
		this.giaTien = giaTien;
	}
	public String getTrangThai() {
		return trangThai;
	}
	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
    
	
}