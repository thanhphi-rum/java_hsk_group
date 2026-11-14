package entity;

import java.math.BigDecimal;

public class ComboBapNuoc {
    private String maCombo;
    private String tenCombo;
    private BigDecimal giaBan;
    private String moTa;
    private String trangThai;

    public ComboBapNuoc() {
    }

    public ComboBapNuoc(String maCombo, String tenCombo, BigDecimal giaBan,
                        String moTa, String trangThai) {
        this.maCombo = maCombo;
        this.tenCombo = tenCombo;
        this.giaBan = giaBan;
        this.moTa = moTa;
        this.trangThai = trangThai;
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

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
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
        return tenCombo + " - " + giaBan + " đồng";
    }
}
