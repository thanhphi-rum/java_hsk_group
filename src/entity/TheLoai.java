package entity;

/** Thể loại phim: TheLoai(maTheLoai, tenTheLoai, moTa). */
public class TheLoai {
    private String maTheLoai;
    private String tenTheLoai;
    private String moTa;

    public TheLoai(String maTheLoai, String tenTheLoai, String moTa) {
        this.maTheLoai = maTheLoai;
        this.tenTheLoai = tenTheLoai;
        this.moTa = moTa;
    }

    public TheLoai() {
        super();
    }

    public String getMaTheLoai() {
        return maTheLoai;
    }

    public void setMaTheLoai(String maTheLoai) {
        this.maTheLoai = maTheLoai;
    }

    public String getTenTheLoai() {
        return tenTheLoai;
    }

    public void setTenTheLoai(String tenTheLoai) {
        this.tenTheLoai = tenTheLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    /** JComboBox hiển thị tên thể loại. */
    @Override
    public String toString() {
        return tenTheLoai;
    }
}
