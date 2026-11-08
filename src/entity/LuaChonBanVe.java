package entity;

import java.math.BigDecimal;

/** Dữ liệu dùng cho ô chọn phim/suất/khuyến mãi; không chứa thành phần Swing. */
public record LuaChonBanVe(String id, String label, BigDecimal amount) {
    @Override
    public String toString() {
        return label;
    }
}
