# Kịch bản test thủ công và demo – Quản lý bán vé rạp phim

## 0. Chuẩn bị trước giờ demo (làm tối nay)
1. Chạy `data/QuanLyBanVePhimMoi.sql` (DB mới) **hoặc** `data/migration_merged.sql` (DB đang dùng) để phim có đủ mô tả, đạo diễn, diễn viên.
2. **Suất chiếu mẫu được tạo cho "ngày mai tính từ lúc chạy SQL".** Nếu chạy SQL từ hôm qua trở về trước thì suất đã qua giờ và không bán được. Vào *Quản lý Suất Chiếu* thêm suất mới cho ngày demo (giờ bắt đầu phải lớn hơn giờ hiện tại).
3. Quy tắc bán: suất không còn "Đang Mở Bán" hoặc đã bắt đầu quá **15 phút** thì không bán/thanh toán được.
4. Khuyến mãi **KM03 đã hết hạn** (31/01/2026) nên không xuất hiện trong danh sách khuyến mãi khi bán vé. KM01 (10%) và KM02 (20%) còn hạn đến 31/12/2026.
5. Khi in hóa đơn, chọn máy in **Microsoft Print to PDF** nếu không có máy in thật. File hóa đơn tự lưu vào thư mục `hoadon/` trong thư mục dự án.

Tài khoản: **admin / admin123** (Quản Lý, thấy mọi mục) và **nv02 / 123456** (Nhân Viên, không thấy Quản lý Phim, Suất chiếu, Nhân viên, Tài khoản, Khuyến mãi, Thống kê).

Bảng giá để tính tay: ghế thường = giá suất; **ghế VIP (hàng G–J) = giá suất × 1,2**. Combo: CB01 59.000 · CB02 99.000 · CB03 159.000. Khuyến mãi tính trên **tổng gồm cả combo**. Điểm tích lũy = tổng tiền sau giảm ÷ 10.000 (lấy phần nguyên). Mã ghế dạng `P01A01` (phòng, hàng, cột).

## 1. Nghiệp vụ Phiếu đặt vé
**Phiếu đặt là giữ chỗ trước, chưa thu tiền.** Khách đặt ghế, rạp giữ ghế cho khách, khách đến quầy mới trả tiền.

| Bước | Ở đâu | Điều xảy ra |
|---|---|---|
| 1. Tạo phiếu | *Phim → Đặt vé → chọn suất, ghế → "Tạo phiếu đặt"* | Tạo `PhieuDatVe` (trạng thái **Chờ xác nhận**) và `ChiTietPhieuDatVe`. Vé và ghế chuyển sang **Đã đặt** nên người khác không chọn được. Chưa có hóa đơn. Combo và khuyến mãi **không** lưu vào phiếu. |
| 2a. Thanh toán | *Hóa Đơn → "Thanh toán phiếu đặt"* | Thu tiền, sinh **hóa đơn** (`HoaDon.maPhieuDatVe` trỏ về phiếu). Vé và ghế thành **Đã bán**, phiếu thành **Đã thanh toán**. |
| 2b. Hủy | *Phiếu đặt → chọn phiếu → "Hủy phiếu"* | Phiếu thành **Đã hủy**, vé **Đã hủy**, ghế trở về **Trống**. |

Mua trực tiếp tại quầy thì **không cần phiếu** (`maPhieuDatVe` để trống). Trong sơ đồ class: `PhieuDatVe` 1 — 1..\* `ChiTietPhieuDatVe`, `ChiTietPhieuDatVe` 0..1 — 1 `Ve`, và `HoaDon` 0..1 — 0..1 `PhieuDatVe`.

## 2. Thứ tự demo gợi ý (khoảng 15 phút)
1. Đăng nhập sai mật khẩu thì báo lỗi; sau đó đăng nhập **admin**.
2. *Phim*: tìm theo tên, lọc theo thể loại, **Xem chi tiết** (có đạo diễn, diễn viên, mô tả).
3. *Quản lý Phim*: Thêm → Sửa → Xóa (mục 3.1).
4. *Phim → Đặt vé*: bán vé tại quầy có combo và khuyến mãi (mục 3.5), thu tiền, in hóa đơn, mở thư mục `hoadon/`.
5. Tạo **phiếu đặt**, xem ở *Phiếu đặt*, rồi thanh toán ở *Hóa Đơn* (mục 3.6).
6. *Hóa Đơn*: Chi tiết, In lại, **Ghi nhận hoàn tiền** (ghế trả về trống).
7. *Quản lý Khách Hàng / Nhân Viên / Khuyến Mãi / Suất Chiếu / Tài Khoản* (mục 3.2 đến 3.4, 3.7).
8. *Thống Kê*: chọn khoảng ngày, xem theo Thời gian, Phim, Suất chiếu, Nhân viên.
9. Đăng xuất, đăng nhập **nv02**, cho thầy thấy không có các mục quản trị.

## 3. Dữ liệu test chi tiết

### 3.1 Quản lý Phim → Thêm
**Dữ liệu hợp lệ (dùng khi demo):**

| Trường | Giá trị |
|---|---|
| Mã phim | `10` |
| Tên phim | `Mai` |
| Thể loại | `Tâm Lý` |
| Thời lượng | `131` |
| Giới hạn tuổi | `T18` |
| Ngày khởi chiếu | `14/02/2026` |
| Đạo diễn | `Trấn Thành` |
| Diễn viên | `Phương Anh Đào, Tuấn Trần` |
| Mô tả | `Câu chuyện về một người phụ nữ tìm lại hạnh phúc.` |
| Poster | chọn 1 file .jpg hoặc .png bất kỳ (hoặc bỏ trống) |
| Trạng thái | `Đang Chiếu` |

Kết quả mong đợi: báo "Thêm phim mới thành công", phim xuất hiện ở bảng. Vào *Phim → Xem chi tiết* thấy đủ thông tin; nếu có poster thì ảnh hiện ở bảng.

**Dữ liệu không hợp lệ (mỗi dòng phải báo lỗi và không lưu):**

| Nhập | Lỗi mong đợi |
|---|---|
| Để trống mã, tên, thể loại hoặc thời lượng | "Vui lòng nhập đầy đủ thông tin" |
| Mã `1` (đã có) | "Mã phim đã tồn tại" |
| Tên `Phim @#$` | Tên chỉ chứa chữ, số, dấu cách, dấu phẩy, dấu chấm |
| Thể loại `Hành động 2` hoặc `Hành động, Tâm lý` | Thể loại chỉ chứa chữ cái |
| Thời lượng `5`, `1000`, `abc`, `-90` | Phải là số nguyên có 2 hoặc 3 chữ số |
| Ngày `32/13/2026`, `2026-02-14` | Phải đúng dạng dd/MM/yyyy |
| Poster là file `.txt` hoặc `.pdf` | Ảnh phải là .jpg, .jpeg hoặc .png |

**Sửa:** chọn phim `10`, đổi thời lượng `131` thành `135` và đổi trạng thái, rồi lưu. Kiểm tra bảng và màn chi tiết: đạo diễn, diễn viên, mô tả **vẫn còn**, không bị xóa trống.
**Xóa:** xóa phim `10` (chưa có suất) thì thành công. Thử xóa phim `1` (đã có suất chiếu) thì báo không xóa được.
**Tìm kiếm:** gõ `avengers`, gõ `hành động`, gõ `99` (không có) thì bảng rỗng.

### 3.2 Quản lý Khách Hàng
- Hợp lệ: Mã `KH04` · Tên `Nguyễn Thị Hoa` · SĐT `0967890123` · Email `hoa.nguyen@gmail.com` · Điểm `0`.
- Không hợp lệ: Mã `KH01` (trùng) · Tên `Hoa123` · SĐT `123456789`, `0123`, `09abc` · Email `hoa@`, `hoa.gmail.com` · Điểm `abc`.
- SĐT phải đúng 10 số và bắt đầu bằng 0.

### 3.3 Quản lý Nhân Viên
- Hợp lệ: Mã `NV04` · Tên `Hoàng Gia Bảo` · SĐT `0978901234` · Email `bao.hoang@rapphim.com` · Địa chỉ `10 Pasteur Quận 1` · Chức vụ `Nhân Viên`.
- Không hợp lệ: Mã `NV01` (trùng) · Tên `Bao 99` · Địa chỉ có ký tự `@#` · SĐT và email sai như mục 3.2.
- Nút **Đang làm / Nghỉ việc**: đổi trạng thái thì nhãn trong bảng đổi màu.

### 3.4 Quản lý Khuyến Mãi
- Hợp lệ: Mã `KM04` · Tên `Cuối tuần vui` · Giảm `15` · Từ `01/11/2026` · Đến `31/12/2026`.
- Không hợp lệ: Giảm `0` hoặc `101` · Ngày kết thúc **trước** ngày bắt đầu · Để trống mã hoặc tên.
- Xóa khuyến mãi đã dùng trong hóa đơn thì báo không xóa được; chuyển sang "Ngừng hoạt động" thay thế.

### 3.5 Bán vé tại quầy (Phim → chọn phim "Đang Chiếu" → Đặt vé)
Kịch bản mẫu: phim `Avengers Endgame`, suất `SC01` (giá 100.000), ghế `P01A01` (thường) và `P01G01` (VIP), combo `CB02` ×1, khuyến mãi `KM01`, khách `KH01`, phương thức Tiền mặt.

Tính tay: 100.000 + 120.000 + 99.000 = **319.000**, giảm 10% còn **287.100**, điểm cộng = **28**. Khách đưa 300.000 thì tiền thừa **12.900**.

Kiểm tra sau khi bán:
- Ghế `P01A01`, `P01G01` chuyển sang "Đã bán" trên sơ đồ ghế.
- *Hóa Đơn* có dòng mới.
- *Quản lý Khách Hàng*: điểm KH01 tăng 28.
- Thư mục `hoadon/` có file `<mã hóa đơn>.pdf`.

Thử các lỗi:
- Bấm thanh toán khi chưa chọn suất hoặc ghế thì có cảnh báo.
- Tiền mặt nhập thiếu: hiện "Còn thiếu…" và nút "Đã thu tiền" bị khóa.
- Ghế đã bán thì không chọn được.
- KM03 (hết hạn) không có trong danh sách khuyến mãi.
- Chọn Chuyển khoản thì hiện mã VietQR.

### 3.6 Phiếu đặt
1. Chọn suất `SC02`, ghế `P01B01` và `P01B02`, bấm **Tạo phiếu đặt** và xác nhận. Kết quả: có mã phiếu, hai ghế chuyển "Đã đặt".
2. Vào *Phiếu đặt*: thấy phiếu ở trạng thái *Chờ xác nhận*; bấm vào xem chi tiết vé và tổng tiền (200.000).
3. Nhánh A: *Hóa Đơn → Thanh toán phiếu đặt*, nhập mã phiếu, thu tiền. Phiếu thành *Đã thanh toán*, ghế thành *Đã bán*, hóa đơn có liên kết với phiếu.
4. Nhánh B: tạo phiếu thứ hai với ghế `P01B05`, vào *Phiếu đặt → Hủy phiếu*. Ghế `P01B05` trở lại **Trống**; phiếu đã hủy thì nút Hủy bị khóa.
5. Thử đặt ghế đã có người giữ thì báo "Ghế vừa được giữ hoặc bán".

### 3.7 Tài khoản và phân quyền
- Thêm tài khoản: chỉ nhân viên chưa có tài khoản mới chọn được. Tên đăng nhập 3–30 ký tự gồm chữ không dấu, số, `_` hoặc `.`; mật khẩu tối thiểu **6** ký tự; nhập lại phải khớp. Thử `ab` (quá ngắn), `abc 12` (có khoảng trắng), mật khẩu `123`.
- **Đặt Lại Mật Khẩu**, **Khóa / Mở Khóa**: khóa `nv02` rồi thử đăng nhập thì bị từ chối; mở khóa lại.
- Không thể tự khóa hoặc xóa tài khoản đang đăng nhập; không thể khóa hoặc xóa quản lý hoạt động cuối cùng.
- Đổi mật khẩu (thanh bên): sai mật khẩu cũ, mật khẩu mới dưới 6 ký tự, hoặc trùng mật khẩu cũ đều báo lỗi.

### 3.8 Hoàn tiền và in lại hóa đơn
- Chọn hóa đơn ở mục 3.5, bấm **Ghi nhận hoàn tiền** và nhập lý do. Trạng thái thành "Đã hoàn tiền", ghế `P01A01`, `P01G01` về **Trống**, điểm KH01 bị trừ lại 28. Hoàn lần hai thì báo "đã hủy/hoàn tiền trước đó".
- **In lại** tạo lại file PDF cùng mã trong `hoadon/` (ghi đè).

### 3.9 Thống kê
Chọn khoảng ngày chứa hóa đơn vừa tạo: số hóa đơn, số vé, doanh thu phải khớp với tính tay. Đổi "Theo" sang Phim, Suất chiếu, Nhân viên. Hóa đơn đã hoàn tiền **không** được tính vào doanh thu (kiểm tra số sau khi hoàn tiền).

## 4. Lỗi hay gặp khi demo
- Không đăng nhập được: SQL Server chưa chạy hoặc sai `db.url`; kiểm tra bằng `sqlcmd`.
- Không có suất để bán: xem mục 0.2.
- Poster phim mới không hiện: chọn ảnh bằng nút "Chọn ảnh…" ở form Thêm/Sửa (tự lưu thành `img/poster_<mã>.jpg`).
- Màn hình nhỏ (1366×768): menu bên trái tự thu gọn, vẫn dùng bình thường.
