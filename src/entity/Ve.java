package entity;

/** Vé: gắn với một ghế trong một suất chiếu cụ thể (GheSuatChieu). Vé thuộc hóa đơn nào do ChiTietHoaDon quản lý. */
public class Ve {
    private String maVe;
    private GheSuatChieu gheSuatChieu;
    private double giaTien;
    private String trangThai;

	public Ve(String maVe, GheSuatChieu gheSuatChieu, double giaTien, String trangThai) {
		this.maVe = maVe;
		this.gheSuatChieu = gheSuatChieu;
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

	public GheSuatChieu getGheSuatChieu() {
		return gheSuatChieu;
	}

	public void setGheSuatChieu(GheSuatChieu gheSuatChieu) {
		this.gheSuatChieu = gheSuatChieu;
	}

	/** Tiện ích: suất chiếu của vé (lấy qua GheSuatChieu). */
	public SuatChieu getSuatChieu() {
		return gheSuatChieu == null ? null : gheSuatChieu.getSuatChieu();
	}

	/** Tiện ích: ghế của vé (lấy qua GheSuatChieu). */
	public Ghe getGhe() {
		return gheSuatChieu == null ? null : gheSuatChieu.getGhe();
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

    @Override
    public String toString() {
        return "Ve [maVe=" + maVe + ", giaTien=" + giaTien + ", trangThai=" + trangThai + "]";
    }
}
