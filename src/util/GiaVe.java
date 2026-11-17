package util;

import java.util.Locale;

/** Quy tắc tính giá vé theo loại ghế. Màn hình bán vé và DAO thanh toán cùng dùng lớp này để không lệch giá. */
public final class GiaVe {
    /** Ghế VIP đắt hơn ghế thường 20%. */
    public static final double HE_SO_VIP = 1.2;

    private GiaVe() { }

    public static boolean laVip(String loaiGhe) {
        return loaiGhe != null && loaiGhe.toLowerCase(Locale.ROOT).contains("vip");
    }

    public static double tinh(double giaCoBan, String loaiGhe) {
        double gia = laVip(loaiGhe) ? giaCoBan * HE_SO_VIP : giaCoBan;
        return lamTron(gia);
    }

    /** Làm tròn tiền đến 2 chữ số thập phân (khớp DECIMAL(18,2) trong SQL), tránh sai số của double. */
    public static double lamTron(double soTien) {
        return Math.round(soTien * 100) / 100.0;
    }
}
