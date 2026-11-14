package entity;

import java.math.BigDecimal;

public class ChiTietPhieuDatVe {
    private PhieuDatVe phieuDatVe;
    private Ve ve;
    private BigDecimal donGia;

    public ChiTietPhieuDatVe() {
    }

    public ChiTietPhieuDatVe(PhieuDatVe phieuDatVe, Ve ve, BigDecimal donGia) {
        this.phieuDatVe = phieuDatVe;
        this.ve = ve;
        this.donGia = donGia;
    }

    public PhieuDatVe getPhieuDatVe() {
        return phieuDatVe;
    }

    public void setPhieuDatVe(PhieuDatVe phieuDatVe) {
        this.phieuDatVe = phieuDatVe;
    }

    public Ve getVe() {
        return ve;
    }

    public void setVe(Ve ve) {
        this.ve = ve;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    @Override
    public String toString() {
        String maPhieu = phieuDatVe == null ? "" : phieuDatVe.getMaPhieuDatVe();
        String maVe = ve == null ? "" : ve.getMaVe();
        return "Phiếu: " + maPhieu + ", vé: " + maVe + ", đơn giá: " + donGia;
    }
}
