package entity;

/** Một dòng của phiếu đặt vé: một vé với đơn giá tại thời điểm đặt. */
public class ChiTietPhieuDatVe {
    private PhieuDatVe phieuDatVe;
    private Ve ve;
    private double donGia;

    public ChiTietPhieuDatVe() {
    }

    public ChiTietPhieuDatVe(PhieuDatVe phieuDatVe, Ve ve, double donGia) {
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

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    @Override
    public String toString() {
        return "ChiTietPhieuDatVe [maPhieuDatVe=" + (phieuDatVe == null ? null : phieuDatVe.getMaPhieuDatVe())
                + ", maVe=" + (ve == null ? null : ve.getMaVe()) + ", donGia=" + donGia + "]";
    }
}
