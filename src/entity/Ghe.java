package entity;

public class Ghe {
    private String maGhe;
    private PhongChieu phongChieu;
    private String viTri;
    private String loaiGhe;
    
	public Ghe(String maGhe, PhongChieu phongChieu, String viTri, String loaiGhe) {
		this.maGhe = maGhe;
		this.phongChieu = phongChieu;
		this.viTri = viTri;
		this.loaiGhe = loaiGhe;
	}

	public Ghe(String maGhe) {
		this.maGhe = maGhe;
	}

	public Ghe() {
		super();
	}

	public String getMaGhe() {
		return maGhe;
	}

	public void setMaGhe(String maGhe) {
		this.maGhe = maGhe;
	}

	public PhongChieu getPhongChieu() {
		return phongChieu;
	}

	public void setPhongChieu(PhongChieu phongChieu) {
		this.phongChieu = phongChieu;
	}

	public String getViTri() {
		return viTri;
	}

	public void setViTri(String viTri) {
		this.viTri = viTri;
	}

	public String getLoaiGhe() {
		return loaiGhe;
	}

	public void setLoaiGhe(String loaiGhe) {
		this.loaiGhe = loaiGhe;
	}

    
}
