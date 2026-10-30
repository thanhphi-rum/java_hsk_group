package entity;

public class Ve {
    private String maVe;
    private SuatChieu suatChieu;
    private Ghe ghe;
    private HoaDon hoaDon;
    private double giaTien;
    
	public Ve(String maVe, SuatChieu suatChieu, Ghe ghe, HoaDon hoaDon, double giaTien) {
		this.maVe = maVe;
		this.suatChieu = suatChieu;
		this.ghe = ghe;
		this.hoaDon = hoaDon;
		this.giaTien = giaTien;
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

	public SuatChieu getSuatChieu() {
		return suatChieu;
	}

	public void setSuatChieu(SuatChieu suatChieu) {
		this.suatChieu = suatChieu;
	}

	public Ghe getGhe() {
		return ghe;
	}

	public void setGhe(Ghe ghe) {
		this.ghe = ghe;
	}

	public HoaDon getHoaDon() {
		return hoaDon;
	}

	public void setHoaDon(HoaDon hoaDon) {
		this.hoaDon = hoaDon;
	}

	public double getGiaTien() {
		return giaTien;
	}

	public void setGiaTien(double giaTien) {
		this.giaTien = giaTien;
	}
}