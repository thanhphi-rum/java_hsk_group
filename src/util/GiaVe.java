package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** Quy tắc tính giá vé theo loại ghế. Màn hình bán vé và DAO thanh toán cùng dùng lớp này để không lệch giá. */
public final class GiaVe {
    /** Ghế VIP đắt hơn ghế thường 20%. */
    public static final BigDecimal HE_SO_VIP = new BigDecimal("1.20");

    private GiaVe() { }

    public static boolean laVip(String loaiGhe) {
        return loaiGhe != null && loaiGhe.toLowerCase(Locale.ROOT).contains("vip");
    }

    public static BigDecimal tinh(BigDecimal giaCoBan, String loaiGhe) {
        BigDecimal gia = laVip(loaiGhe) ? giaCoBan.multiply(HE_SO_VIP) : giaCoBan;
        return gia.setScale(2, RoundingMode.HALF_UP);
    }
}
