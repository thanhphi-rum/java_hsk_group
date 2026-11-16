package entity;

import java.time.LocalDate;

public class KhuyenMai {
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private String moTa;
    private double phanTramGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String trangThai = "Hoạt động";
    private String hinhThuc = HT_HOA_DON;
    private int soComboTang = 0;

    public static final String HT_VE = "Giảm giá vé", HT_COMBO = "Giảm giá combo", HT_HOA_DON = "Giảm hóa đơn", HT_TANG_COMBO = "Tặng combo";
    public static final String[] DS_HINH_THUC = {HT_VE, HT_COMBO, HT_HOA_DON, HT_TANG_COMBO};
    
	public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, String moTa, double phanTramGiam, LocalDate ngayBatDau,
			LocalDate ngayKetThuc) {
		this.maKhuyenMai = maKhuyenMai;
		this.tenKhuyenMai = tenKhuyenMai;
		this.moTa = moTa;
		this.phanTramGiam = phanTramGiam;
		this.ngayBatDau = ngayBatDau;
		this.ngayKetThuc = ngayKetThuc;
	}

	public KhuyenMai(String maKhuyenMai) {
		this.maKhuyenMai = maKhuyenMai;
	}

	public KhuyenMai() {
		super();
	}

	public String getMaKhuyenMai() {
		return maKhuyenMai;
	}

	public void setMaKhuyenMai(String maKhuyenMai) {
		this.maKhuyenMai = maKhuyenMai;
	}

	public String getTenKhuyenMai() {
		return tenKhuyenMai;
	}

	public void setTenKhuyenMai(String tenKhuyenMai) {
		this.tenKhuyenMai = tenKhuyenMai;
	}

	public String getMoTa() {
		return moTa;
	}

	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}

	public double getPhanTramGiam() {
		return phanTramGiam;
	}

	public void setPhanTramGiam(double phanTramGiam) {
		this.phanTramGiam = phanTramGiam;
	}

	public LocalDate getNgayBatDau() {
		return ngayBatDau;
	}

	public void setNgayBatDau(LocalDate ngayBatDau) {
		this.ngayBatDau = ngayBatDau;
	}

	public LocalDate getNgayKetThuc() {
		return ngayKetThuc;
	}

	public void setNgayKetThuc(LocalDate ngayKetThuc) {
		this.ngayKetThuc = ngayKetThuc;
	}

    

    public String getTrangThai(){return trangThai;}
    public void setTrangThai(String trangThai){this.trangThai=trangThai;}

    /** Khuyến mãi có hiệu lực vào ngày mua, kể cả hai ngày bắt đầu/kết thúc. */
    public boolean kiemTraHieuLuc(LocalDate ngayMua) {
        return ngayMua != null && ngayBatDau != null && ngayKetThuc != null
                && "Hoạt động".equalsIgnoreCase(trangThai)
                && !ngayMua.isBefore(ngayBatDau) && !ngayMua.isAfter(ngayKetThuc);
    }

    public String getHinhThuc(){return hinhThuc;}
    public void setHinhThuc(String hinhThuc){this.hinhThuc=hinhThuc==null||hinhThuc.isBlank()?HT_HOA_DON:hinhThuc;}
    public int getSoComboTang(){return soComboTang;}
    public void setSoComboTang(int soComboTang){this.soComboTang=soComboTang;}
    public boolean laTangCombo(){return HT_TANG_COMBO.equals(hinhThuc);}

    /** Mức ưu đãi dạng chữ: "20%" hoặc "1 combo". */
    public String mucUuDai(){
        if(laTangCombo())return soComboTang+" combo";
        return new java.math.BigDecimal(String.valueOf(phanTramGiam)).stripTrailingZeros().toPlainString()+"%";
    }

    /**
     * Tổng tiền sau giảm theo hình thức khuyến mãi (cùng công thức với view vw_HoaDon_TongTien trong SQL).
     * @param tienVe tổng tiền vé, @param tienCombo tổng tiền combo,
     * @param giaComboReNhat đơn giá combo thấp nhất trong đơn (null nếu không có combo), @param soCombo tổng số combo trong đơn
     */
    public java.math.BigDecimal tongSauGiam(java.math.BigDecimal tienVe,java.math.BigDecimal tienCombo,java.math.BigDecimal giaComboReNhat,int soCombo){
        java.math.BigDecimal goc=tienVe.add(tienCombo);
        java.math.BigDecimal pct=new java.math.BigDecimal(String.valueOf(phanTramGiam)).movePointLeft(2);
        java.math.BigDecimal giam;
        if(HT_VE.equals(hinhThuc))giam=tienVe.multiply(pct);
        else if(HT_COMBO.equals(hinhThuc))giam=tienCombo.multiply(pct);
        else if(HT_TANG_COMBO.equals(hinhThuc))giam=giaComboReNhat==null?java.math.BigDecimal.ZERO:giaComboReNhat.multiply(java.math.BigDecimal.valueOf(Math.min(soComboTang,soCombo)));
        else giam=goc.multiply(pct);
        return goc.subtract(giam).setScale(2,java.math.RoundingMode.HALF_UP);
    }
}
