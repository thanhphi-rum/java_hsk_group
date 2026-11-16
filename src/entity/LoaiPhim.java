package entity;

/** Loại (thể loại) phim: Hành động, Hoạt hình, ... */
public class LoaiPhim {
    private String maLoaiPhim;
    private String tenLoaiPhim;
    private String moTa;

    public LoaiPhim() {
    }

    public LoaiPhim(String maLoaiPhim) {
        this.maLoaiPhim = maLoaiPhim;
    }

    public LoaiPhim(String maLoaiPhim, String tenLoaiPhim, String moTa) {
        this.maLoaiPhim = maLoaiPhim;
        this.tenLoaiPhim = tenLoaiPhim;
        this.moTa = moTa;
    }

    /** Tạo loại phim chỉ mới biết tên (chưa có mã); PhimDAO sẽ tự tra/tạo mã khi lưu. */
    public static LoaiPhim tuTen(String tenLoaiPhim) {
        return new LoaiPhim(null, tenLoaiPhim, null);
    }

    public String getMaLoaiPhim() {
        return maLoaiPhim;
    }

    public void setMaLoaiPhim(String maLoaiPhim) {
        this.maLoaiPhim = maLoaiPhim;
    }

    public String getTenLoaiPhim() {
        return tenLoaiPhim;
    }

    public void setTenLoaiPhim(String tenLoaiPhim) {
        this.tenLoaiPhim = tenLoaiPhim;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return tenLoaiPhim == null ? "" : tenLoaiPhim;
    }
}
