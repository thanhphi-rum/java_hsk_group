package dao;

import connect.ConnectDB;
import java.sql.*;
import java.util.*;

/** CRUD cho các danh mục cố định. Giá trị nhập luôn đi qua PreparedStatement. */
public class DanhMucDAO {
    public record Field(String name, String label, String type, boolean required) {}
    private final String table;
    private final List<Field> fields;

    public DanhMucDAO(String table) {
        this.table = table;
        String spec = switch (table) {
            case "KhachHang" -> "MaKhachHang:Mã khách hàng:s!,TenKhachHang:Tên khách hàng:s!,SoDienThoai:Số điện thoại:s!,Email:Email:s,DiemTichLuy:Điểm tích lũy:i!";
            case "NhanVien" -> "MaNhanVien:Mã nhân viên:s!,TenNhanVien:Tên nhân viên:s!,NgaySinh:Ngày sinh:d!,GioiTinh:Giới tính:s!,SoDienThoai:Số điện thoại:s!,Email:Email:s,DiaChi:Địa chỉ:s,ChucVu:Chức vụ:s!";
            case "LoaiPhim" -> "MaLoaiPhim:Mã loại phim:s!,TenLoaiPhim:Tên loại phim:s!,MoTa:Mô tả:s";
            case "KhuyenMai" -> "MaKhuyenMai:Mã khuyến mãi:s!,TenKhuyenMai:Tên khuyến mãi:s!,MoTa:Mô tả:s!,PhanTramGiam:Phần trăm giảm:n!,NgayBatDau:Ngày bắt đầu:d!,NgayKetThuc:Ngày kết thúc:d!";
            case "PhongChieu" -> "MaPhongChieu:Mã phòng:s!,TenPhong:Tên phòng:s!,LoaiPhongChieu:Loại phòng:s!,TrangThai:Trạng thái:s!,SucChua:Số ghế hiện có:i!";
            case "Phim" -> "MaPhim:Mã phim:s!,TenPhim:Tên phim:s!,MaLoaiPhim:Mã loại phim:s!,ThoiLuong:Thời lượng phút:i!,TrangThai:Trạng thái:s!,MoTa:Mô tả:s,DaoDien:Đạo diễn:s,DienVien:Diễn viên:s,GioiHanTuoi:Giới hạn tuổi:s,NgayKhoiChieu:Ngày khởi chiếu:d,PosterPath:Ảnh phim:s";
            default -> throw new IllegalArgumentException("Danh mục không được hỗ trợ");
        };
        fields = Arrays.stream(spec.split(",")).map(value -> {
            String[] parts = value.split(":");
            return new Field(parts[0], parts[1], parts[2].substring(0, 1), parts[2].endsWith("!"));
        }).toList();
    }

    public List<Field> fields() { return fields; }
    public String table() { return table; }

    public List<Object[]> list() throws SQLException {
        service.Session.requireStaff();
        List<Object[]> result = new ArrayList<>();
        String columns = String.join(",", fields.stream().map(Field::name).toList());
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT " + columns + " FROM " + table + " ORDER BY " + fields.get(0).name())) {
            while (r.next()) {
                Object[] row = new Object[fields.size()];
                for (int i = 0; i < row.length; i++) row[i] = r.getObject(i + 1);
                result.add(row);
            }
        }
        return result;
    }

    public void save(Object[] values, boolean edit) throws SQLException {
        service.Session.requireStaff();
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (table.equals("PhongChieu") && edit && !"Hoạt động".equals(values[3])) {
                    try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM SuatChieu WHERE MaPhongChieu=? AND GioKetThuc>GETDATE()")) {
                        p.setObject(1, values[0]);
                        try (ResultSet r = p.executeQuery()) {
                            if (r.next()) throw new SQLException("Phòng còn lịch chiếu chưa kết thúc; cần xử lý lịch trước khi bảo trì.");
                        }
                    }
                }
                if (table.equals("Phim") && edit) {
                    try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM SuatChieu WHERE MaPhim=?")) {
                        p.setObject(1, values[0]);
                        try (ResultSet r = p.executeQuery()) {
                            if (r.next()) {
                                try (PreparedStatement check = c.prepareStatement("SELECT ThoiLuong FROM Phim WHERE MaPhim=?")) {
                                    check.setObject(1, values[0]);
                                    try (ResultSet old = check.executeQuery()) {
                                        old.next();
                                        if (old.getInt(1) != ((Number) values[3]).intValue())
                                            throw new SQLException("Phim đã có lịch chiếu; không đổi thời lượng để tránh sai lịch.");
                                    }
                                }
                            }
                        }
                    }
                }
                String sql;
                if (edit) sql = "UPDATE " + table + " SET " + String.join(",", fields.subList(1, fields.size()).stream().map(f -> f.name() + "=?").toList()) + " WHERE " + fields.get(0).name() + "=?";
                else {
                    String names = String.join(",", fields.stream().map(Field::name).toList());
                    String marks = String.join(",", Collections.nCopies(values.length, "?"));
                    sql = "INSERT INTO " + table + "(" + names + (table.equals("Phim") ? ",TheLoai" : "") + ") VALUES(" + marks + (table.equals("Phim") ? ",(SELECT TenLoaiPhim FROM LoaiPhim WHERE MaLoaiPhim=?)" : "") + ")";
                }
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    int position = 1;
                    for (int i = edit ? 1 : 0; i < values.length; i++) p.setObject(position++, values[i]);
                    if (edit) p.setObject(position, values[0]);
                    else if (table.equals("Phim")) p.setObject(position, values[2]);
                    if (p.executeUpdate() != 1) throw new SQLException("Bản ghi đã thay đổi. Hãy làm mới danh sách.");
                }
                if (table.equals("Phim") || table.equals("LoaiPhim")) {
                    try (Statement s = c.createStatement()) {
                        s.executeUpdate("UPDATE p SET TheLoai=l.TenLoaiPhim FROM Phim p JOIN LoaiPhim l ON l.MaLoaiPhim=p.MaLoaiPhim");
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public void delete(Object id) throws SQLException {
        service.Session.requireStaff();
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM " + table + " WHERE " + fields.get(0).name() + "=?")) {
            p.setObject(1, id);
            if (p.executeUpdate() != 1) throw new SQLException("Bản ghi không còn tồn tại.");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547) throw new SQLException("Dữ liệu đang được sử dụng bởi bảng liên quan, không thể xóa.", ex);
            throw ex;
        }
    }
}
