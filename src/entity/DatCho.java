package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Thông tin đơn giữ chỗ; tách khỏi DAO để giao diện không phụ thuộc lớp truy vấn. */
public record DatCho(
        String id,
        String show,
        String movie,
        String room,
        LocalDateTime starts,
        LocalDateTime expires,
        String status,
        BigDecimal unitPrice,
        List<String> seats,
        String invoice) {
    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(seats.size()));
    }

    public String description() {
        return "Mã đặt vé: "
                + id
                + "\nPhim: "
                + movie
                + "\nPhòng: "
                + room
                + "\nSuất: "
                + starts.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + "\nGhế: "
                + String.join(", ", seats)
                + "\nTổng tiền: "
                + String.format("%,.0f đ", total())
                + "\nGiữ đến: "
                + expires.format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                + "\nTrạng thái: "
                + switch (status) {
                    case "CHO_THANH_TOAN" -> "Chờ thanh toán tại quầy";
                    case "DA_THANH_TOAN" -> "Đã thanh toán";
                    default -> "Đã hết hạn";
                };
    }
}
