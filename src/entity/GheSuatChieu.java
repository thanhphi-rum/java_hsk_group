package entity;

import java.time.LocalDateTime;

public class GheSuatChieu {
    private Ghe ghe;
    private SuatChieu suatChieu;
    private String trangThai;
	public GheSuatChieu(Ghe ghe, SuatChieu suatChieu, String trangThai) {
		this.ghe = ghe;
		this.suatChieu = suatChieu;
		this.trangThai = trangThai;
	}
	public GheSuatChieu() {
		super();
	}
	public Ghe getGhe() {
		return ghe;
	}
	public void setGhe(Ghe ghe) {
		this.ghe = ghe;
	}
	public SuatChieu getSuatChieu() {
		return suatChieu;
	}
	public void setSuatChieu(SuatChieu suatChieu) {
		this.suatChieu = suatChieu;
	}
	public String getTrangThai() {
		return trangThai;
	}
	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
    
    
}
