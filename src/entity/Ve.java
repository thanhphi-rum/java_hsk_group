package entity;

import java.math.BigDecimal;

public class Ve {
    private String maVe;
    private GheSuatChieu gheSuatChieu;
    private BigDecimal giaTien;
    private String trangThai;

    public Ve() {
    }

    public Ve(String maVe, GheSuatChieu gheSuatChieu,
              BigDecimal giaTien, String trangThai) {
        this.maVe = maVe;
        this.gheSuatChieu = gheSuatChieu;
        this.giaTien = giaTien;
        this.trangThai = trangThai;
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

    public BigDecimal getGiaTien() {
        return giaTien;
    }

    public void setGiaTien(BigDecimal giaTien) {
        this.giaTien = giaTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
