package entity;

import java.math.BigDecimal;

public class ComboBapNuoc {
    private String maCombo,tenCombo,moTa,trangThai;
    private BigDecimal giaBan;
    public ComboBapNuoc(String maCombo,String tenCombo,BigDecimal giaBan,String moTa,String trangThai){this.maCombo=maCombo;this.tenCombo=tenCombo;this.giaBan=giaBan;this.moTa=moTa;this.trangThai=trangThai;}
    public String getMaCombo(){return maCombo;} public String getTenCombo(){return tenCombo;} public BigDecimal getGiaBan(){return giaBan;} public String getMoTa(){return moTa;} public String getTrangThai(){return trangThai;}
    @Override public String toString(){return tenCombo+" — "+String.format("%,.0f VNĐ",giaBan.doubleValue());}
}
