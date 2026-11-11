package entity;

public class ComboBapNuoc {
	private String maCombo;
	private String tenCombo;
	private double giaBan;
	private String moTa;
	private String trangThai;
	
	public ComboBapNuoc(String maCombo, String tenCombo, double giaBan, String moTa, String trangThai) {
		this.maCombo = maCombo;
		this.tenCombo = tenCombo;
		this.giaBan = giaBan;
		this.moTa = moTa;
		this.trangThai = trangThai;
	}

	public ComboBapNuoc() {
		super();
	}

	public String getMaCombo() {
		return maCombo;
	}

	public void setMaCombo(String maCombo) {
		this.maCombo = maCombo;
	}

	public String getTenCombo() {
		return tenCombo;
	}

	public void setTenCombo(String tenCombo) {
		this.tenCombo = tenCombo;
	}

	public double getGiaBan() {
		return giaBan;
	}

	public void setGiaBan(double giaBan) {
		this.giaBan = giaBan;
	}

	public String getMoTa() {
		return moTa;
	}

	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	@Override
	public String toString() {
		return "ComboBapNuoc [maCombo=" + maCombo + ", tenCombo=" + tenCombo + ", giaBan=" + giaBan + ", moTa=" + moTa
				+ ", trangThai=" + trangThai + "]";
	}
	
	
}
