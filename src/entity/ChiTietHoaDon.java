package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Dữ liệu hóa đơn đã thanh toán dùng để xuất/in lại PDF. */
public record ChiTietHoaDon(
        String id,
        LocalDateTime created,
        String employee,
        String customer,
        String paymentMethod,
        BigDecimal total,
        List<DongVe> tickets) {
    public record DongVe(
            String movie, String room, LocalDateTime starts, String seat, BigDecimal price) {}
}
