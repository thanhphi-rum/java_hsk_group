package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SuatChieu {
    private String maSuatChieu;
    private Phim phim;
    private PhongChieu phongChieu;
    private LocalDate ngayChieu;
    private LocalDateTime gioBatDau;
    private LocalDateTime gioKetThuc;
    private double giaVe;
    private String trangThai;
    
	public SuatChieu(String maSuatChieu, Phim phim, PhongChieu phongChieu, LocalDate ngayChieu, LocalDateTime gioBatDau,
			LocalDateTime gioKetThuc, double giaVe, String trangThai) {
		this.maSuatChieu = maSuatChieu;
		this.phim = phim;
		this.phongChieu = phongChieu;
		this.ngayChieu = ngayChieu;
		this.gioBatDau = gioBatDau;
		this.gioKetThuc = gioKetThuc;
		this.giaVe = giaVe;
		this.trangThai = trangThai;
	}

	public SuatChieu(String maSuatChieu) {
		this.maSuatChieu = maSuatChieu;
	}

	public SuatChieu() {
		super();
	}

	public String getMaSuatChieu() {
		return maSuatChieu;
	}

	public void setMaSuatChieu(String maSuatChieu) {
		this.maSuatChieu = maSuatChieu;
	}

	public Phim getPhim() {
		return phim;
	}

	public void setPhim(Phim phim) {
		this.phim = phim;
	}

	public PhongChieu getPhongChieu() {
		return phongChieu;
	}

	public void setPhongChieu(PhongChieu phongChieu) {
		this.phongChieu = phongChieu;
	}

	public LocalDate getNgayChieu() {
		return ngayChieu;
	}

	public void setNgayChieu(LocalDate ngayChieu) {
		this.ngayChieu = ngayChieu;
	}

	public LocalDateTime getGioBatDau() {
		return gioBatDau;
	}

	public void setGioBatDau(LocalDateTime gioBatDau) {
		this.gioBatDau = gioBatDau;
	}

	public LocalDateTime getGioKetThuc() {
		return gioKetThuc;
	}

	public void setGioKetThuc(LocalDateTime gioKetThuc) {
		this.gioKetThuc = gioKetThuc;
	}

	public double getGiaVe() {
		return giaVe;
	}

	public void setGiaVe(double giaVe) {
		this.giaVe = giaVe;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
}
