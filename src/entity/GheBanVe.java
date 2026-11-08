package entity;

/** Trạng thái một ghế trong đúng một suất chiếu, không dùng chung trạng thái giữa các suất. */
public record GheBanVe(String id, String location, String status) {
    public boolean conTrong() {
        return "Trống".equals(status == null ? "" : status.trim());
    }

    public String dayGhe() {
        return location.replaceAll("[0-9]+$", "");
    }
}
