package service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Quy tắc tính tiền độc lập với giao diện và database, có thể kiểm thử trực tiếp. */
public final class ThanhToanService {
    private ThanhToanService() {}

    /** Tổng tiền = đơn giá × số vé × (1 - phần trăm giảm); làm tròn đến đồng. */
    public static BigDecimal tinhTongTien(BigDecimal price, int count, BigDecimal discount) {
        if (price == null
                || discount == null
                || price.signum() < 0
                || count < 1
                || discount.signum() < 0
                || discount.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Giá vé, số lượng hoặc khuyến mãi không hợp lệ.");
        }
        return price.multiply(BigDecimal.valueOf(count))
                .multiply(BigDecimal.ONE.subtract(discount.movePointLeft(2)))
                .setScale(0, RoundingMode.HALF_UP);
    }

    /** Chỉ chấp nhận số nguyên VND không âm; thiếu tiền thì chưa được thanh toán. */
    public static BigDecimal tinhTienThoi(BigDecimal total, String input) {
        if (total == null || total.signum() < 0)
            throw new IllegalArgumentException("Tổng tiền không hợp lệ.");
        if (input == null || !input.trim().matches("[0-9]{1,12}")) {
            throw new IllegalArgumentException("Nhập tiền khách đưa bằng chữ số, ví dụ 100000.");
        }
        BigDecimal received = new BigDecimal(input.trim());
        if (received.compareTo(total) < 0)
            throw new IllegalArgumentException("Tiền khách đưa chưa đủ thanh toán.");
        return received.subtract(total);
    }
}
