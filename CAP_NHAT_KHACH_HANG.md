# Bản cập nhật khách hàng và thanh toán

Trạng thái: đã sửa mã nguồn và biên dịch. Chưa áp dụng các script SQL mới và chưa chép bản này sang Downloads do hệ thống xét duyệt tự động không thực hiện được (hết hạn mức). Không coi bản trong Downloads là bản mới này.

## Luồng sử dụng
- Chạy gui.Main_GUI (hoặc run.cmd): khách vào danh sách phim không cần đăng ký/đăng nhập.
- Khách chọn suất và tối đa 10 ghế, nhận mã đặt chỗ. Giữ tối đa 10 phút và không quá giờ chiếu; ghi lại mã rồi đến quầy thanh toán. Chưa tạo hóa đơn hoặc ghi nhận doanh thu.
- Nút Đăng nhập nhân viên riêng. Nhân viên vào Nhận đặt vé, tra mã và nhập tiền khách đưa; tiền thiếu/hủy không được lưu. Chỉ xác nhận thu tiền thành công mới tạo vé, hóa đơn và ghi doanh thu.
- Bán trực tiếp tại quầy cũng bắt buộc bước nhập tiền khách đưa, tính tiền trả lại và xác nhận thu tiền.
- Trang chủ chỉ giữ tiêu đề đen, nút và ảnh nền rạp phim sẵn có.

## Áp dụng database
Chạy lần lượt trong SQL Server Management Studio, trên database QuanLyBanVePhimMoi:
1. data/guest_booking.sql (thêm bảng đặt chỗ, không xóa bảng cũ).
2. data/repair_seat_status.sql (sửa trạng thái Trống bị lỗi dấu, không mở lại ghế đã bán hoặc đang giữ).

Mã cũ tạo ghế cho suất mới dùng literal 'Trống' thiếu tiền tố N, có thể làm mất dấu trên SQL Server. Đã sửa thành N'Trống'. Chưa đọc lại trạng thái thực tế của dãy A trên máy để xác nhận đây là nguyên nhân duy nhất.

Không sao chép đè db.properties của bạn khi đưa mã sang thư mục khác. Chạy build.cmd để biên dịch, sau đó run.cmd. Nếu dùng Eclipse: F5, Project → Clean, chạy gui.Main_GUI. Tài khoản nhân viên đã tạo trước đó vẫn dùng được.

## Cần kiểm thử sau khi chạy SQL
Khách đặt ghế không tạo hóa đơn; hai khách không giữ cùng ghế; đơn quá hạn được nhả ghế khi tải lại/bán/đặt chỗ; nhân viên nhập thiếu tiền hoặc hủy không lưu; thu đủ tiền chỉ tạo một hóa đơn; bấm thanh toán lại đơn cũ phải bị chặn. Bản cập nhật này chưa được chạy kiểm thử tích hợp với database do hạn mức xét duyệt.
