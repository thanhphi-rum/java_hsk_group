package entity;

public class GheSuatChieu {
    private String maSuatChieu;
    private String maGhe;
    private String trangThai;

    public GheSuatChieu() {
    }

    public GheSuatChieu(String maSuatChieu, String maGhe, String trangThai) {
        this.maSuatChieu = maSuatChieu;
        this.maGhe = maGhe;
        this.trangThai = trangThai;
    }

    public String getMaSuatChieu() {
        return maSuatChieu;
    }

    public void setMaSuatChieu(String maSuatChieu) {
        this.maSuatChieu = maSuatChieu;
    }

    public String getMaGhe() {
        return maGhe;
    }

    public void setMaGhe(String maGhe) {
        this.maGhe = maGhe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}