# Nhóm chức năng nhân viên, tài khoản, hóa đơn và thanh toán

Dự án Java Swing dùng SQL Server. Trong `data/QuanLyBanVePhimMoi.sql` là file SQL duy nhất: script xóa database `QuanLyBanVePhimMoi` hiện tại (nếu có), tạo lại database, các bảng và dữ liệu mẫu. Chạy toàn bộ file một lần trong SQL Server Management Studio. **Mọi dữ liệu cũ trong database này sẽ bị xóa.** Cập nhật kết nối trong `src/connect/ConnectDB.java` cho máy chạy.

Mã Java dùng JDK 17 trở lên và các thư viện trong `lib/`. Từ thư mục gốc dự án, biên dịch và chạy như sau:

```powershell
$src = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -cp "lib/*" -d bin $src
java -cp "bin;lib/*" gui.MainFrame
```

Các nghiệp vụ đã cập nhật:

- Đăng nhập nhân viên; hồ sơ có trạng thái Đang làm/Nghỉ việc, tài khoản hỗ trợ đổi/đặt lại mật khẩu, khóa/mở khóa và quyền quản lý/nhân viên.
- Quản lý khuyến mãi với trạng thái hoạt động/ngừng, phần trăm giảm và thời hạn.
- Bán nhiều ghế, giá VIP cao hơn 20%, thêm combo, xác nhận phương thức thanh toán.
- Transaction kiểm tra ghế, vé, suất chiếu, khuyến mãi, giá combo và tổng hóa đơn; cộng điểm cùng giao dịch.
- Đổi phiếu đặt thành hóa đơn; chuyển trạng thái vé/ghế từ Đã đặt sang Đã bán.
- Hủy/hoàn tiền theo dạng soft-cancel để giữ lịch sử; hủy vé, giải phóng ghế, trừ lại điểm. Cần thực hiện hoàn tiền qua phương thức ban đầu bên ngoài ứng dụng trước khi ghi nhận.
- Tra cứu hóa đơn theo mã, ngày lập, mã/tên/số điện thoại khách hàng; xem chi tiết vé/ghế/suất và combo, in lại hóa đơn.
- Thống kê doanh thu, số hóa đơn và số vé theo ngày/tuần/tháng/năm; phân tích theo phim, suất chiếu và nhân viên.
- Ngăn xóa nhân viên đã lập hóa đơn và ngăn sửa/xóa suất chiếu đã phát sinh vé.

`ChiTietPhieuDatVe` theo schema hiện tại chỉ chứa vé; combo được thêm tại quầy và lưu riêng thành dòng `ChiTietHoaDon`. Điểm thưởng là 1 điểm cho mỗi 10.000 đồng thanh toán.
