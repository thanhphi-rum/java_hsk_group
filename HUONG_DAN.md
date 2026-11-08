# Lưu ý cho bản cập nhật mới

Đọc CAP_NHAT_KHACH_HANG.md trước khi chạy: cần áp dụng hai script SQL mới. Bản này chưa được cập nhật vào Downloads do lỗi hạn mức xét duyệt. Khách mở gui.Main_GUI, không cần tài khoản; chỉ nhân viên đăng nhập. Các kết quả kiểm thử database bên dưới là của bản trước; bản khách đặt chỗ mới chỉ được biên dịch và kiểm thử offline.

# Quản lý bán vé phim — bản giao diện cơ bản

## Chạy trên máy hiện tại

- Mở `run.cmd`, hoặc import dự án vào Eclipse rồi chạy `gui.Main_GUI`.
- Lần đầu chọn **Thiết lập tài khoản lần đầu**, chọn nhân viên, nhập tên đăng nhập và mật khẩu riêng (ít nhất 8 ký tự), rồi đăng nhập.
- Database hiện có 1 nhân viên mẫu, 2 phòng và 2 phim mẫu. Chưa tạo tài khoản đăng nhập ứng dụng thay cho bạn.
- SQL Server trên máy đã được cấu hình chỉ nghe TCP tại `127.0.0.1:1433`. Tài khoản SQL riêng chỉ được đọc/ghi database bài tập. Tài khoản `sa` vẫn bị khóa.
- `db.properties` là cấu hình riêng trên máy, có mật khẩu SQL: giữ cục bộ, không đưa lên GitHub. Nó khác với tài khoản đăng nhập giao diện.

## Eclipse

1. File → Import → General → Existing Projects into Workspace.
2. Chọn thư mục dự án `java_hsk_group-master` trong Downloads.
3. Dùng JDK 17 trở lên. Nếu báo thiếu JavaSE-17: Project → Properties → Java Build Path → Libraries → JRE System Library → chọn JDK đã cài.
4. Chọn `QuanLyBanVePhim.launch` → Run As → QuanLyBanVePhim; hoặc chạy `src/gui/Main_GUI.java` dưới dạng Java Application.
5. Working directory phải là thư mục dự án, nơi có `db.properties`, `img`, `fonts` và `lib`.
6. Nếu dự án đã mở trước khi thay mã, nhấn F5 và Project → Clean.

## Các chức năng đã sửa

- Một cửa sổ chính, menu trái chuyển giữa các JPanel; bảng không sửa trực tiếp bằng cách gõ vào ô.
- Đăng nhập/đăng xuất; mật khẩu ứng dụng được băm PBKDF2 có salt, không lưu nguyên văn. Đây là bản đăng nhập cơ bản, chưa chia quyền quản lý/nhân viên.
- Thêm/sửa phim có ô đường dẫn và nút **Chọn ảnh**. Ảnh được sao chép vào `img/posters`; bỏ trống được. Hỗ trợ JPG/PNG tối đa 10 MB.
- **Phòng / dãy ghế**: chọn phòng, nhập A–Z, chọn 1–30 ghế rồi thêm. Ví dụ A + 10 tạo A01…A10. Chặn dãy trùng trong cùng phòng, tự cập nhật sức chứa và các suất chưa bắt đầu.
- Bán vé hiển thị hàng ghế; đổi suất sẽ bỏ lựa chọn cũ. Nhân viên lấy từ phiên đăng nhập. Giá lấy từ suất chiếu; khuyến mãi chỉ áp dụng trong thời hạn.
- Thanh toán lưu hóa đơn/vé/ghế trong cùng một transaction, kiểm tra ghế còn trống để chống bán trùng.
- Chỉ xuất PDF từ hóa đơn đã lưu: ngay sau thanh toán hoặc vào **Hóa đơn → chọn dòng → Xuất PDF**. PDF dùng font tiếng Việt, bảng ghế, đơn giá sau giảm và tổng tiền; có hộp thoại chọn chỗ lưu.
- Truy vấn thống kê ngày/tuần/tháng/năm đã sửa theo SQL Server; phần xuất báo cáo thống kê cũ không thuộc phạm vi sửa hóa đơn PDF.

## Tự thử nhanh

1. Đăng nhập bằng tài khoản vừa tạo.
2. Vào **Phòng / dãy ghế**, thêm dãy A với 10 ghế cho Phòng 1.
3. Vào **Suất chiếu**, tạo một suất trong tương lai cho Phòng 1; mã tối đa 10 ký tự. Giờ kết thúc tự tính theo thời lượng phim.
4. Vào **Bán vé**, chọn phim → suất → ghế, bấm **Thanh toán / Lưu hóa đơn** và xác nhận đã nhận tiền.
5. Xuất PDF, kiểm tra tiếng Việt, mã ghế và tổng tiền; vào Hóa đơn để xuất lại.
6. Thử chọn ghế rồi đổi suất: lựa chọn cũ phải được xóa. Ghế đã bán không được chọn lại.

## Database và giới hạn

- Dùng `data/upgrade_sqlserver.sql` cho SQL Server. Trên máy hiện tại script đã được chạy; không cần chạy lại khi chỉ mở ứng dụng.
- Máy khác cần tạo database `QuanLyBanVePhimMoi`, chạy script, rồi tạo `db.properties` từ file mẫu với tài khoản SQL của máy đó.
- Script không tự chuyển đổi lịch sử hóa đơn từ các schema cũ không tương thích. Cần sao lưu và kiểm tra dữ liệu trước nếu dùng database khác đã có giao dịch.
- Chức năng xóa hóa đơn cũ vẫn là xóa vé và trả ghế; chưa có quy trình hoàn tiền/lưu lịch sử hủy. Tạo thêm tài khoản và phân quyền nhiều vai trò chưa nằm trong bản sửa cơ bản này.

## Đã kiểm tra

Biên dịch Java 17; đăng nhập đúng/sai; lưu/sửa poster; thêm dãy ghế và chặn trùng; tính tiền theo suất và khuyến mãi; hóa đơn nhiều vé; đổi suất bỏ chọn ghế; rollback khi một ghế đã bán; hai giao dịch cùng mua một ghế chỉ một giao dịch thành công; không bán suất quá khứ; xuất PDF tiếng Việt và kiểm tra trang đã render; các màn hình dùng chung một JFrame.

Dữ liệu kiểm thử đã được dọn. Chưa kiểm thử thao tác trong Eclipse; có thể chạy bằng `run.cmd` hoặc cấu hình Eclipse ở trên.

Driver JDBC mới lấy từ nguồn chính thức của Microsoft: https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
