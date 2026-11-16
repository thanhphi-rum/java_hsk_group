package entity;

/** Một dòng hóa đơn: vé đơn lẻ (maVe) hoặc combo (maCombo) với số lượng. */
public class ChiTietHoaDon {
    private String maCTH;
    private HoaDon hoaDon;
    private Ve ve;
    private ComboBapNuoc comboBapNuoc;
    private int soLuong;
    private double donGia;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(HoaDon hoaDon, Ve ve, ComboBapNuoc comboBapNuoc, int soLuong, double donGia) {
        this.hoaDon = hoaDon;
        this.ve = ve;
        this.comboBapNuoc = comboBapNuoc;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public ChiTietHoaDon(String maCTH, HoaDon hoaDon, Ve ve, ComboBapNuoc comboBapNuoc, int soLuong, double donGia) {
        this(hoaDon, ve, comboBapNuoc, soLuong, donGia);
        this.maCTH = maCTH;
    }

    public String getMaCTH() {
        return maCTH;
    }

    public void setMaCTH(String maCTH) {
        this.maCTH = maCTH;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public Ve getVe() {
        return ve;
    }

    public void setVe(Ve ve) {
        this.ve = ve;
    }

    public ComboBapNuoc getComboBapNuoc() {
        return comboBapNuoc;
    }

    public void setComboBapNuoc(ComboBapNuoc comboBapNuoc) {
        this.comboBapNuoc = comboBapNuoc;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    /** Thành tiền của dòng = số lượng × đơn giá. */
    public double tinhThanhTien() {
        return soLuong * donGia;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon [maCTH=" + maCTH + ", maHoaDon=" + (hoaDon == null ? null : hoaDon.getMaHoaDon())
                + ", maVe=" + (ve == null ? null : ve.getMaVe())
                + ", maCombo=" + (comboBapNuoc == null ? null : comboBapNuoc.getMaCombo())
                + ", soLuong=" + soLuong + ", donGia=" + donGia + "]";
    }
}
