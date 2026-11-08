package service;

import dao.LichChieuDAO;
import dao.PhimDAO;

import entity.Phim;
import entity.PhongChieu;
import entity.SuatChieu;

import java.sql.SQLException;
import java.time.LocalDateTime;

/** Quy tắc lập lịch: ngày tương lai, phim đang chiếu, giờ kết thúc tính từ thời lượng. */
public class SuatChieuService {
    public void luu(
            String id,
            String movieId,
            String roomId,
            LocalDateTime starts,
            double price,
            boolean editing)
            throws SQLException {
        Session.requireStaff();
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,10}"))
            throw new SQLException("Mã suất chiếu gồm 1–10 ký tự chữ, số, _ hoặc -.");
        if (starts == null || !starts.isAfter(LocalDateTime.now()))
            throw new SQLException("Giờ bắt đầu phải sau thời điểm hiện tại.");
        if (movieId == null || roomId == null)
            throw new SQLException("Vui lòng chọn phim và phòng chiếu.");
        if (!Double.isFinite(price) || price <= 0) throw new SQLException("Giá vé phải lớn hơn 0.");
        Phim movie = new PhimDAO().layPhimTheoMa(movieId);
        if (movie == null
                || !"Đang Chiếu".equalsIgnoreCase(movie.getTrangThai())
                || movie.getThoiLuong() <= 0)
            throw new SQLException("Phim không hợp lệ hoặc chưa mở chiếu.");
        SuatChieu show =
                new SuatChieu(
                        id,
                        movie,
                        new PhongChieu(roomId),
                        starts.toLocalDate(),
                        starts,
                        starts.plusMinutes(movie.getThoiLuong()),
                        price,
                        "Đang Mở Bán");
        new LichChieuDAO().luu(show, editing);
    }
}
