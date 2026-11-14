package entity;

public class PhongChieu {
    private String maPhongChieu;
    private String tenPhongChieu;
    private String loaiPhongChieu;
    private String trangThai;
    private int sucChua;
    
	public PhongChieu(String maPhongChieu, String tenPhongChieu, String loaiPhongChieu, String trangThai, int sucChua) {
		this.maPhongChieu = maPhongChieu;
		this.tenPhongChieu = tenPhongChieu;
		this.loaiPhongChieu = loaiPhongChieu;
		this.trangThai = trangThai;
		this.sucChua = sucChua;
	}

	public PhongChieu(String maPhongChieu) {
		this.maPhongChieu = maPhongChieu;
	}

	public PhongChieu() {
		super();
	}

	public String getMaPhongChieu() {
		return maPhongChieu;
	}

	public void setMaPhongChieu(String maPhongChieu) {
		this.maPhongChieu = maPhongChieu;
	}

	public String getTenPhongChieu() {
		return tenPhongChieu;
	}

	public void setTenPhongChieu(String tenPhongChieu) {
		this.tenPhongChieu = tenPhongChieu;
	}

	public String getLoaiPhongChieu() {
		return loaiPhongChieu;
	}

	public void setLoaiPhongChieu(String loaiPhongChieu) {
		this.loaiPhongChieu = loaiPhongChieu;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	public int getSucChua() {
		return sucChua;
	}

	public void setSucChua(int sucChua) {
		this.sucChua = sucChua;
	}
    
	
    
}