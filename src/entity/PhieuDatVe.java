package entity;

import java.time.LocalDateTime;

public class PhieuDatVe {
    private String maPhieuDatVe;
    private LocalDateTime ngayDat;
    private KhachHang khachHang;
    private String trangThai;

    public PhieuDatVe() {
    }

    public PhieuDatVe(String maPhieuDatVe, LocalDateTime ngayDat,
                      KhachHang khachHang, String trangThai) {
        this.maPhieuDatVe = maPhieuDatVe;
        this.ngayDat = ngayDat;
        this.khachHang = khachHang;
        this.trangThai = trangThai;
    }

    public String getMaPhieuDatVe() {
        return maPhieuDatVe;
    }

    public void setMaPhieuDatVe(String maPhieuDatVe) {
        this.maPhieuDatVe = maPhieuDatVe;
    }

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return maPhieuDatVe + " - " + trangThai;
    }
}
