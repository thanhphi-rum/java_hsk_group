package entity;

import java.time.LocalDate;

public class Phim {
    private String maPhim;
    private String tenPhim;
    private String theLoai;
    private int thoiLuong;
    private String trangThai;
    private String moTa;
    private String daoDien;
    private String dienVien;
    private String gioiHanTuoi;
    private LocalDate ngayKhoiChieu;
    
	public Phim(String maPhim, String tenPhim, String theLoai, int thoiLuong, String trangThai, String moTa,
			String daoDien, String dienVien, String gioiHanTuoi, LocalDate ngayKhoiChieu) {
		this.maPhim = maPhim;
		this.tenPhim = tenPhim;
		this.theLoai = theLoai;
		this.thoiLuong = thoiLuong;
		this.trangThai = trangThai;
		this.moTa = moTa;
		this.daoDien = daoDien;
		this.dienVien = dienVien;
		this.gioiHanTuoi = gioiHanTuoi;
		this.ngayKhoiChieu = ngayKhoiChieu;
	}
	
	public Phim(String maPhim) {
		this.maPhim = maPhim;
	}

	public Phim() {
		super();
	}

	public String getMaPhim() {
		return maPhim;
	}

	public void setMaPhim(String maPhim) {
		this.maPhim = maPhim;
	}

	public String getTenPhim() {
		return tenPhim;
	}

	public void setTenPhim(String tenPhim) {
		this.tenPhim = tenPhim;
	}

	public String getTheLoai() {
		return theLoai;
	}

	public void setTheLoai(String theLoai) {
		this.theLoai = theLoai;
	}

	public int getThoiLuong() {
		return thoiLuong;
	}

	public void setThoiLuong(int thoiLuong) {
		this.thoiLuong = thoiLuong;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	public String getMoTa() {
		return moTa;
	}

	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}

	public String getDaoDien() {
		return daoDien;
	}

	public void setDaoDien(String daoDien) {
		this.daoDien = daoDien;
	}

	public String getDienVien() {
		return dienVien;
	}

	public void setDienVien(String dienVien) {
		this.dienVien = dienVien;
	}

	public String getGioiHanTuoi() {
		return gioiHanTuoi;
	}

	public void setGioiHanTuoi(String gioiHanTuoi) {
		this.gioiHanTuoi = gioiHanTuoi;
	}

	public LocalDate getNgayKhoiChieu() {
		return ngayKhoiChieu;
	}

	public void setNgayKhoiChieu(LocalDate ngayKhoiChieu) {
		this.ngayKhoiChieu = ngayKhoiChieu;
	}
    
    

}
