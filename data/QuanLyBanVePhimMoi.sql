
-- 1. TẠO DATABASE
USE master;
GO
IF DB_ID(N'QuanLyBanVePhimMoi') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyBanVePhimMoi SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyBanVePhimMoi;
END
GO

CREATE DATABASE QuanLyBanVePhimMoi;
GO
USE QuanLyBanVePhimMoi;
GO

-- Chỉ tạo bảng khi đã chuyển đúng database.
IF DB_NAME() <> N'QuanLyBanVePhimMoi'
BEGIN
    RAISERROR(N'Chưa chuyển được tới database của dự án. Không tạo bảng.', 16, 1);
    SET NOEXEC ON;
END;
GO

-- Các tùy chọn SQL Server cần cho cột tính toán và chỉ mục có WHERE.
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- 2. TẠO BẢNG
-- PRIMARY KEY: khóa chính. FOREIGN KEY: khóa ngoại nối hai bảng.
-- CHECK: kiểm tra dữ liệu hợp lệ. DEFAULT: giá trị dùng khi không nhập.
-- NhanVien(maNhanVien, tenNhanVien, soDienThoai, email, gioiTinh, ngaySinh, diaChi, chucVu)
CREATE TABLE NhanVien (
    MaNhanVien  VARCHAR(20)   NOT NULL,
    TenNhanVien NVARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(15)   NOT NULL,
    Email       VARCHAR(100)  NOT NULL,
    GioiTinh    NVARCHAR(10)  NOT NULL,
    NgaySinh    DATE          NOT NULL,
    DiaChi      NVARCHAR(200) NOT NULL,
    ChucVu      NVARCHAR(50)  NOT NULL DEFAULT N'Nhân Viên',   -- 'Quản Lý' hoặc 'Nhân Viên'
    TrangThai   NVARCHAR(20)  NOT NULL DEFAULT N'Đang làm',
    CONSTRAINT PK_NhanVien PRIMARY KEY (MaNhanVien)
);

-- TaiKhoan(tenDangNhap, matKhau, trangThai, maNhanVien)   NhanVien 1 --- 0..1 TaiKhoan
CREATE TABLE TaiKhoan (
    TenDangNhap VARCHAR(50)  NOT NULL,
    MatKhau     VARCHAR(255) NOT NULL,   -- SHA-256 dạng hex (ứng dụng tự băm khi tạo/đổi mật khẩu)
    TrangThai   NVARCHAR(50) NOT NULL DEFAULT N'Hoạt động',   -- 'Hoạt động' hoặc 'Khóa'
    MaNhanVien  VARCHAR(20)  NOT NULL,
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (TenDangNhap),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT UQ_TaiKhoan_NhanVien UNIQUE (MaNhanVien)
);

-- KhachHang(maKhachHang, tenKhachHang, email, soDienThoai, diemTichLuy)
CREATE TABLE KhachHang (
    MaKhachHang  VARCHAR(20)   NOT NULL,
    TenKhachHang NVARCHAR(100) NOT NULL,
    Email        VARCHAR(100)  NOT NULL,
    SoDienThoai  VARCHAR(15)   NOT NULL,
    DiemTichLuy  INT           NOT NULL DEFAULT 0,
    CONSTRAINT PK_KhachHang PRIMARY KEY (MaKhachHang),
    CONSTRAINT CK_KhachHang_Diem CHECK (DiemTichLuy >= 0)
);

-- KhuyenMai(maKhuyenMai, tenKhuyenMai, phanTramGiam, ngayBatDau, ngayKetThuc, moTa)
CREATE TABLE KhuyenMai (
    MaKhuyenMai  VARCHAR(20)   NOT NULL,
    TenKhuyenMai NVARCHAR(100) NOT NULL,
    PhanTramGiam DECIMAL(5,2)  NOT NULL,
    NgayBatDau   DATE          NOT NULL,
    NgayKetThuc  DATE          NOT NULL,
    MoTa         NVARCHAR(200) NULL,
    TrangThai    NVARCHAR(20)  NOT NULL DEFAULT N'Hoạt động',
    HinhThuc     NVARCHAR(30)  NOT NULL DEFAULT N'Giảm hóa đơn',  -- Giảm giá vé | Giảm giá combo | Giảm hóa đơn | Tặng combo
    SoComboTang  INT           NOT NULL DEFAULT 0,                -- chỉ dùng cho hình thức 'Tặng combo'
    CONSTRAINT PK_KhuyenMai PRIMARY KEY (MaKhuyenMai),
    CONSTRAINT CK_KhuyenMai_HinhThuc CHECK (HinhThuc IN (N'Giảm giá vé', N'Giảm giá combo', N'Giảm hóa đơn', N'Tặng combo')),
    CONSTRAINT CK_KhuyenMai_PhanTram CHECK (PhanTramGiam >= 0 AND PhanTramGiam <= 100),
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (NgayKetThuc >= NgayBatDau),
    CONSTRAINT CK_KhuyenMai_ComboTang CHECK (SoComboTang >= 0)
);

-- ComboBapNuoc(maCombo, tenCombo, giaBan, moTa, trangThai)
CREATE TABLE ComboBapNuoc (
    MaCombo   VARCHAR(20)    NOT NULL,
    TenCombo  NVARCHAR(100)  NOT NULL,
    GiaBan    DECIMAL(18,2)  NOT NULL,
    MoTa      NVARCHAR(200)  NULL,
    TrangThai NVARCHAR(50)   NOT NULL DEFAULT N'Đang bán',   -- 'Đang bán' hoặc 'Ngừng bán'
    CONSTRAINT PK_ComboBapNuoc PRIMARY KEY (MaCombo),
    CONSTRAINT CK_Combo_GiaBan CHECK (GiaBan >= 0)
);

-- TheLoai(maTheLoai, tenTheLoai, moTa)
CREATE TABLE TheLoai (
    MaTheLoai  VARCHAR(20)   NOT NULL,
    TenTheLoai NVARCHAR(100) NOT NULL,
    MoTa       NVARCHAR(200) NULL,
    CONSTRAINT PK_TheLoai PRIMARY KEY (MaTheLoai),
    CONSTRAINT UQ_TheLoai_Ten UNIQUE (TenTheLoai)
);

-- Phim(maPhim, tenPhim, thoiLuong, trangThai, moTa, daoDien, dienVien, gioiHanTuoi, ngayKhoiChieu)
-- Trạng thái: 'Sắp Chiếu', 'Đang Chiếu', 'Ngừng Chiếu'.   Thể loại lưu ở bảng Phim_TheLoai (Phim *---* TheLoai)
CREATE TABLE Phim (
    MaPhim        VARCHAR(20)   NOT NULL,
    TenPhim       NVARCHAR(200) NOT NULL,
    ThoiLuong     INT           NOT NULL,
    TrangThai     NVARCHAR(50)  NOT NULL,
    MoTa          NVARCHAR(MAX) NULL,
    DaoDien       NVARCHAR(100) NULL,
    DienVien      NVARCHAR(500) NULL,
    GioiHanTuoi   NVARCHAR(20)  NULL,
    NgayKhoiChieu DATE          NULL,
    CONSTRAINT PK_Phim PRIMARY KEY (MaPhim),
    CONSTRAINT CK_Phim_ThoiLuong CHECK (ThoiLuong > 0),
    CONSTRAINT CK_Phim_TrangThai CHECK (TrangThai IN (N'Sắp Chiếu', N'Đang Chiếu', N'Ngừng Chiếu')),
    CONSTRAINT CK_Phim_GioiHanTuoi CHECK (GioiHanTuoi IN (N'P', N'K', N'T13', N'T16', N'T18'))
);

-- Phim_TheLoai(maPhim, maTheLoai): một phim có thể thuộc nhiều thể loại, một thể loại có nhiều phim.
-- Xóa phim thì tự xóa các dòng thể loại của phim đó.
CREATE TABLE Phim_TheLoai (
    MaPhim    VARCHAR(20) NOT NULL,
    MaTheLoai VARCHAR(20) NOT NULL,
    CONSTRAINT PK_Phim_TheLoai PRIMARY KEY (MaPhim, MaTheLoai),
    CONSTRAINT FK_PhimTheLoai_Phim FOREIGN KEY (MaPhim) REFERENCES Phim(MaPhim) ON DELETE CASCADE,
    CONSTRAINT FK_PhimTheLoai_TheLoai FOREIGN KEY (MaTheLoai) REFERENCES TheLoai(MaTheLoai)
);

-- PhongChieu(maPhongChieu, tenPhongChieu, loaiPhongChieu, trangThai, sucChua)
CREATE TABLE PhongChieu (
    MaPhongChieu   VARCHAR(20)   NOT NULL,
    TenPhongChieu  NVARCHAR(100) NOT NULL,
    LoaiPhongChieu NVARCHAR(50)  NOT NULL,
    TrangThai      NVARCHAR(50)  NOT NULL DEFAULT N'Hoạt động',
    SucChua        INT           NOT NULL,
    CONSTRAINT PK_PhongChieu PRIMARY KEY (MaPhongChieu),
    CONSTRAINT CK_PhongChieu_SucChua CHECK (SucChua > 0)
);

-- Ghe(maGhe, viTri, maPhongChieu, loaiGhe)   PhongChieu 1 <>--- 1..* Ghe
-- MaGhe duy nhất toàn hệ thống vì Ghe_SuatChieu join theo MaGhe
CREATE TABLE Ghe (
    MaGhe        VARCHAR(20)  NOT NULL,
    ViTri        NVARCHAR(10) NOT NULL,
    MaPhongChieu VARCHAR(20)  NOT NULL,
    LoaiGhe      NVARCHAR(50) NOT NULL DEFAULT N'Thường',
    CONSTRAINT PK_Ghe PRIMARY KEY (MaGhe),
    CONSTRAINT FK_Ghe_PhongChieu FOREIGN KEY (MaPhongChieu) REFERENCES PhongChieu(MaPhongChieu),
    CONSTRAINT UQ_Ghe_Phong_ViTri UNIQUE (MaPhongChieu, ViTri)
);

-- SuatChieu(maSuatChieu, maPhim, maPhongChieu, gioBatDau, gioKetThuc, giaVe, trangThai)
-- NgayChieu tự lấy phần ngày của GioBatDau, không cần nhập riêng.
CREATE TABLE SuatChieu (
    MaSuatChieu  VARCHAR(20)   NOT NULL,
    MaPhim       VARCHAR(20)   NOT NULL,
    MaPhongChieu VARCHAR(20)   NOT NULL,
    GioBatDau    DATETIME      NOT NULL,
    GioKetThuc   DATETIME      NOT NULL,
    GiaVe        DECIMAL(18,2) NOT NULL DEFAULT 100000,
    TrangThai    NVARCHAR(50)  NOT NULL DEFAULT N'Đang Mở Bán',
    NgayChieu    AS CAST(GioBatDau AS DATE) PERSISTED,
    CONSTRAINT PK_SuatChieu PRIMARY KEY (MaSuatChieu),
    CONSTRAINT FK_SuatChieu_Phim FOREIGN KEY (MaPhim) REFERENCES Phim(MaPhim),
    CONSTRAINT FK_SuatChieu_PhongChieu FOREIGN KEY (MaPhongChieu) REFERENCES PhongChieu(MaPhongChieu),
    CONSTRAINT CK_SuatChieu_Gio CHECK (GioKetThuc > GioBatDau),
    CONSTRAINT CK_SuatChieu_GiaVe CHECK (GiaVe >= 0)
);

-- GheSuatChieu(maGhe, maSuatChieu, trangThai)  -> bảng Ghe_SuatChieu
-- Trạng thái: 'Trống', 'Đã đặt', 'Đã bán'. SuatChieuDAO tự xóa bảng này trước khi xóa suất.
CREATE TABLE Ghe_SuatChieu (
    MaSuatChieu VARCHAR(20)  NOT NULL,
    MaGhe       VARCHAR(20)  NOT NULL,
    TrangThai   NVARCHAR(20) NOT NULL DEFAULT N'Trống',
    CONSTRAINT PK_Ghe_SuatChieu PRIMARY KEY (MaSuatChieu, MaGhe),
    CONSTRAINT FK_GSC_SuatChieu FOREIGN KEY (MaSuatChieu) REFERENCES SuatChieu(MaSuatChieu),
    CONSTRAINT FK_GSC_Ghe FOREIGN KEY (MaGhe) REFERENCES Ghe(MaGhe)
);

-- Ve(maVe, maGheSuatChieu, giaTien, trangThai)
-- maGheSuatChieu = khóa ghép (MaSuatChieu, MaGhe) -> Ghe_SuatChieu.
-- Mỗi ghế trong một suất chỉ có tối đa một vé chưa hủy.
-- Trạng thái vé: 'Đã đặt' (giữ chỗ qua phiếu đặt vé), 'Đã bán' (đã lập hóa đơn), 'Đã hủy'.
CREATE TABLE Ve (
    MaVe        VARCHAR(20)   NOT NULL,
    MaSuatChieu VARCHAR(20)   NOT NULL,
    MaGhe       VARCHAR(20)   NOT NULL,
    GiaTien     DECIMAL(18,2) NOT NULL,
    TrangThai   NVARCHAR(50)  NOT NULL DEFAULT N'Đã bán',
    CONSTRAINT PK_Ve PRIMARY KEY (MaVe),
    CONSTRAINT FK_Ve_GheSuatChieu FOREIGN KEY (MaSuatChieu, MaGhe) REFERENCES Ghe_SuatChieu(MaSuatChieu, MaGhe),
    CONSTRAINT CK_Ve_GiaTien CHECK (GiaTien >= 0)
);

-- PhieuDatVe(maPhieuDatVe, ngayDat, maKhachHang, trangThai)   KhachHang 0..1 --- 0..* PhieuDatVe
-- Trạng thái: 'Chờ xác nhận', 'Đã xác nhận', 'Đã hủy'.
CREATE TABLE PhieuDatVe (
    MaPhieuDatVe VARCHAR(20)  NOT NULL,
    NgayDat      DATE         NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    MaKhachHang  VARCHAR(20)  NULL,
    TrangThai    NVARCHAR(50) NOT NULL DEFAULT N'Chờ xác nhận',
    CONSTRAINT PK_PhieuDatVe PRIMARY KEY (MaPhieuDatVe),
    CONSTRAINT FK_PhieuDatVe_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang)
);

-- ChiTietPhieuDatVe(maPhieuDatVe, maVe, donGia)   PhieuDatVe 1 <>--- 1..* ChiTietPhieuDatVe
-- Mỗi vé nằm trong tối đa 1 phiếu đặt (PK ghép + UNIQUE MaVe).
CREATE TABLE ChiTietPhieuDatVe (
    MaPhieuDatVe VARCHAR(20)   NOT NULL,
    MaVe         VARCHAR(20)   NOT NULL,
    DonGia       DECIMAL(18,2) NOT NULL,
    CONSTRAINT PK_ChiTietPhieuDatVe PRIMARY KEY (MaPhieuDatVe, MaVe),
    CONSTRAINT FK_CTPDV_PhieuDatVe FOREIGN KEY (MaPhieuDatVe) REFERENCES PhieuDatVe(MaPhieuDatVe) ON DELETE CASCADE,
    CONSTRAINT FK_CTPDV_Ve FOREIGN KEY (MaVe) REFERENCES Ve(MaVe),
    CONSTRAINT UQ_CTPDV_Ve UNIQUE (MaVe),
    CONSTRAINT CK_CTPDV_DonGia CHECK (DonGia >= 0)
);

-- HoaDon(maHoaDon, ngayLap, maNhanVien, maKhachHang, maKhuyenMai, phuongThucThanhToan, maPhieuDatVe)
-- Không có cột TongTien: tổng tiền = tinhTongTien() tính từ ChiTietHoaDon (xem view vw_HoaDon_TongTien).
-- MaKhachHang (khách vãng lai), MaKhuyenMai, MaPhieuDatVe cho phép NULL.
-- MaHoaDon dài: BanVe_GUI tạo mã dạng "HD" + System.currentTimeMillis().
CREATE TABLE HoaDon (
    MaHoaDon            VARCHAR(30)  NOT NULL,
    NgayLap             DATETIME     NOT NULL DEFAULT GETDATE(),
    MaNhanVien          VARCHAR(20)  NOT NULL,
    MaKhachHang         VARCHAR(20)  NULL,
    MaKhuyenMai         VARCHAR(20)  NULL,
    PhuongThucThanhToan NVARCHAR(50) NOT NULL DEFAULT N'Tiền mặt',
    MaPhieuDatVe        VARCHAR(20)  NULL,
    TrangThai           NVARCHAR(30) NOT NULL DEFAULT N'Đã thanh toán',
    GhiChuHuy           NVARCHAR(300) NULL,
    -- Lưu khuyến mãi lúc thanh toán để hóa đơn cũ không đổi khi sửa khuyến mãi.
    PhanTramGiamApDung   DECIMAL(5,2) NOT NULL DEFAULT 0,
    HinhThucApDung       NVARCHAR(30) NOT NULL DEFAULT N'Giảm hóa đơn',
    SoComboTangApDung    INT NOT NULL DEFAULT 0,
    CONSTRAINT PK_HoaDon PRIMARY KEY (MaHoaDon),
    CONSTRAINT CK_HoaDon_PhanTram CHECK (PhanTramGiamApDung BETWEEN 0 AND 100),
    CONSTRAINT CK_HoaDon_ComboTang CHECK (SoComboTangApDung >= 0),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (MaKhuyenMai) REFERENCES KhuyenMai(MaKhuyenMai),
    CONSTRAINT FK_HoaDon_PhieuDatVe FOREIGN KEY (MaPhieuDatVe) REFERENCES PhieuDatVe(MaPhieuDatVe)
);

-- ChiTietHoaDon(maHoaDon, maVe, maCombo, soLuong, donGia)   HoaDon 1 <>--- 1..* ChiTietHoaDon
-- Mỗi dòng là 1 vé (MaVe, số lượng 1) HOẶC 1 combo bắp nước (MaCombo, số lượng n), không được cả hai.
-- MaChiTietHoaDon tự tăng mỗi khi thêm một dòng chi tiết.
CREATE TABLE ChiTietHoaDon (
    MaChiTietHoaDon INT IDENTITY(1,1) NOT NULL,
    MaHoaDon        VARCHAR(30)   NOT NULL,
    MaVe            VARCHAR(20)   NULL,
    MaCombo         VARCHAR(20)   NULL,
    SoLuong         INT           NOT NULL DEFAULT 1,
    DonGia          DECIMAL(18,2) NOT NULL,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (MaChiTietHoaDon),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_Ve FOREIGN KEY (MaVe) REFERENCES Ve(MaVe),
    CONSTRAINT FK_CTHD_Combo FOREIGN KEY (MaCombo) REFERENCES ComboBapNuoc(MaCombo),
    CONSTRAINT CK_CTHD_VeHoacCombo CHECK (
        (MaVe IS NOT NULL AND MaCombo IS NULL AND SoLuong = 1)
        OR (MaVe IS NULL AND MaCombo IS NOT NULL AND SoLuong > 0)),
    CONSTRAINT CK_CTHD_DonGia CHECK (DonGia >= 0)
);
GO

-- 3. TẠO CHỈ MỤC
-- Các chỉ mục thường giúp tìm kiếm nhanh hơn.
CREATE INDEX IX_SuatChieu_MaPhim ON SuatChieu(MaPhim);
CREATE INDEX IX_SuatChieu_Phong_Gio ON SuatChieu(MaPhongChieu, GioBatDau);
CREATE INDEX IX_HoaDon_KhachHang ON HoaDon(MaKhachHang);
CREATE INDEX IX_HoaDon_NgayLap ON HoaDon(NgayLap);
CREATE INDEX IX_CTHD_HoaDon ON ChiTietHoaDon(MaHoaDon);

-- UNIQUE INDEX: không cho phép trùng dữ liệu trong các dòng thỏa điều kiện WHERE.
-- Vé đã hủy được bỏ qua, vì ghế đó có thể bán lại cho người khác.
CREATE UNIQUE INDEX UX_Ve_Suat_Ghe
ON Ve(MaSuatChieu, MaGhe)
WHERE TrangThai <> N'Đã hủy';

-- Một phiếu đặt chỉ được chuyển thành một hóa đơn.
-- Hóa đơn bán trực tiếp có MaPhieuDatVe = NULL nên không bị kiểm tra trùng.
CREATE UNIQUE INDEX UX_HoaDon_PhieuDatVe
ON HoaDon(MaPhieuDatVe)
WHERE MaPhieuDatVe IS NOT NULL;

-- Một vé chỉ được ghi vào một dòng chi tiết hóa đơn.
CREATE UNIQUE INDEX UX_CTHD_Ve
ON ChiTietHoaDon(MaVe)
WHERE MaVe IS NOT NULL;

-- Mỗi combo chỉ có một dòng trong cùng hóa đơn; mua nhiều thì tăng SoLuong.
CREATE UNIQUE INDEX UX_CTHD_HoaDon_Combo
ON ChiTietHoaDon(MaHoaDon, MaCombo)
WHERE MaCombo IS NOT NULL;
GO

-- 4. THÊM DỮ LIỆU MẪU
-- Nhân viên
INSERT INTO NhanVien (MaNhanVien, TenNhanVien, SoDienThoai, Email, GioiTinh, NgaySinh, DiaChi, ChucVu) VALUES
('NV01', N'Nguyễn Văn An', '0901234567', 'an.nguyen@rapphim.com', N'Nam', '1995-05-20', N'12 Nguyễn Huệ Quận 1',   N'Quản Lý'),
('NV02', N'Trần Thị Bình', '0912345678', 'binh.tran@rapphim.com', N'Nữ',  '1998-09-12', N'45 Lê Lợi Quận 1',       N'Nhân Viên'),
('NV03', N'Lê Minh Châu',  '0923456789', 'chau.le@rapphim.com',   N'Nữ',  '2000-01-30', N'78 Hai Bà Trưng Quận 3', N'Nhân Viên');
-- Tài khoản mẫu: admin / admin123 (Quản Lý), nv02 / 123456 (Nhân Viên). Hãy đổi mật khẩu sau khi đăng nhập.
-- Mật khẩu được băm SHA-256 ngay trong SQL, khớp với cách ứng dụng Java kiểm tra đăng nhập.
INSERT INTO TaiKhoan (TenDangNhap, MatKhau, TrangThai, MaNhanVien) VALUES
('admin', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'admin123'), 2), N'Hoạt động', 'NV01'),
('nv02',  CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '123456'),   2), N'Hoạt động', 'NV02');
-- Khách hàng
INSERT INTO KhachHang (MaKhachHang, TenKhachHang, Email, SoDienThoai, DiemTichLuy) VALUES
('KH01', N'Phạm Quốc Dũng',  'dung.pham@gmail.com', '0934567890', 120),
('KH02', N'Võ Thị Em',       'em.vo@gmail.com',     '0945678901',  40),
('KH03', N'Đặng Hoàng Phúc', 'phuc.dang@gmail.com', '0956789012',   0);
-- Khuyến mãi
INSERT INTO KhuyenMai (MaKhuyenMai, TenKhuyenMai, HinhThuc, PhanTramGiam, SoComboTang, NgayBatDau, NgayKetThuc, MoTa) VALUES
('KM001', N'Happy Day – Thứ 4 vui vẻ', N'Giảm giá vé',    20, 0, '2026-01-01', '2026-12-31', N'Giảm 20% giá vé (khuyến mãi thứ 4 hằng tuần)'),
('KM002', N'Sinh nhật thành viên',     N'Tặng combo',      0, 1, '2026-01-01', '2026-12-31', N'Tặng 1 combo (combo rẻ nhất trong đơn) cho khách thành viên đúng dịp sinh nhật'),
('KM003', N'Movie Lover',              N'Giảm giá vé',    15, 0, '2026-01-01', '2026-12-31', N'Giảm 15% giá vé cho người mê phim'),
('KM004', N'Couple Day',               N'Giảm hóa đơn',   20, 0, '2026-01-01', '2026-12-31', N'Giảm 20% tổng hóa đơn cho cặp đôi'),
('KM005', N'Family Movie',             N'Giảm hóa đơn',   15, 0, '2026-01-01', '2026-12-31', N'Giảm 15% tổng hóa đơn cho gia đình'),
('KM006', N'Student Discount',         N'Giảm giá vé',    20, 0, '2026-01-01', '2026-12-31', N'Giảm 20% giá vé cho học sinh, sinh viên'),
('KM007', N'Super Combo',              N'Giảm giá combo', 10, 0, '2026-01-01', '2026-12-31', N'Giảm 10% giá combo bắp nước'),
('KM008', N'Weekend Deal',             N'Giảm giá vé',    10, 0, '2026-01-01', '2026-12-31', N'Giảm 10% giá vé cuối tuần'),
('KM009', N'Member Special',           N'Giảm hóa đơn',   10, 0, '2026-01-01', '2026-12-31', N'Giảm 10% tổng hóa đơn cho thành viên'),
('KM010', N'Mừng phim bom tấn',        N'Giảm giá combo', 25, 0, '2026-01-01', '2026-12-31', N'Giảm 25% giá combo khi xem phim bom tấn');
-- Combo bắp nước
INSERT INTO ComboBapNuoc (MaCombo, TenCombo, GiaBan, MoTa, TrangThai) VALUES
('CB01', N'Solo Combo',   69000,  N'1 bắp + 1 nước',               N'Đang bán'),
('CB02', N'Couple Combo', 99000,  N'1 bắp + 2 nước',               N'Đang bán'),
('CB03', N'Family Combo', 159000, N'2 bắp + 4 nước + 2 snack',     N'Đang bán');
-- Thể loại phim (20 thể loại)
INSERT INTO TheLoai (MaTheLoai, TenTheLoai, MoTa) VALUES
('TL001', N'Hành động', N'Phim hành động'),
('TL002', N'Phiêu lưu', N'Phim phiêu lưu'),
('TL003', N'Hoạt hình', N'Phim hoạt hình'),
('TL004', N'Kinh dị', N'Phim kinh dị'),
('TL005', N'Hài', N'Phim hài'),
('TL006', N'Tình cảm', N'Phim tình cảm'),
('TL007', N'Tâm lý', N'Phim tâm lý'),
('TL008', N'Khoa học viễn tưởng', N'Phim khoa học viễn tưởng'),
('TL009', N'Viễn tưởng', N'Phim viễn tưởng'),
('TL010', N'Chính kịch', N'Phim chính kịch'),
('TL011', N'Hình sự', N'Phim hình sự'),
('TL012', N'Trinh thám', N'Phim trinh thám'),
('TL013', N'Chiến tranh', N'Phim chiến tranh'),
('TL014', N'Gia đình', N'Phim gia đình'),
('TL015', N'Thần thoại', N'Phim thần thoại'),
('TL016', N'Tài liệu', N'Phim tài liệu'),
('TL017', N'Âm nhạc', N'Phim âm nhạc'),
('TL018', N'Thể thao', N'Phim thể thao'),
('TL019', N'Võ thuật', N'Phim võ thuật'),
('TL020', N'Lịch sử', N'Phim lịch sử');
-- Phim: giữ mã có sẵn để khớp phần gán ảnh trong Java.
INSERT INTO Phim (MaPhim, TenPhim, ThoiLuong, TrangThai, MoTa, DaoDien, DienVien, GioiHanTuoi, NgayKhoiChieu) VALUES
('1', N'Avengers Endgame', 181, N'Đang Chiếu', N'Sau cú búng tay của Thanos, các Avengers còn lại tập hợp để đảo ngược thảm họa và khôi phục vũ trụ.', N'Anthony Russo, Joe Russo', N'Robert Downey Jr., Chris Evans, Mark Ruffalo, Chris Hemsworth, Scarlett Johansson', N'T13', '2026-01-10'),
('2', N'Frozen 2',         103, N'Đang Chiếu', N'Elsa cùng Anna, Kristoff và Olaf lên đường tìm nguồn gốc sức mạnh băng giá và bí ẩn của khu rừng bị phù phép.', N'Chris Buck, Jennifer Lee', N'Idina Menzel, Kristen Bell, Jonathan Groff, Josh Gad', N'P',   '2026-02-01'),
('3', N'Joker',            122, N'Đang Chiếu', N'Câu chuyện về Arthur Fleck, một diễn viên hài thất bại bị xã hội ruồng bỏ và dần trở thành tên tội phạm khét tiếng Joker.', N'Todd Phillips', N'Joaquin Phoenix, Robert De Niro, Zazie Beetz', N'T18', '2026-02-15'),
('4', N'Spider Man',       148, N'Đang Chiếu', N'Peter Parker cân bằng cuộc sống học sinh với trách nhiệm người hùng khi danh tính bị phơi bày và các kẻ thù từ đa vũ trụ xuất hiện.', N'Jon Watts', N'Tom Holland, Zendaya, Benedict Cumberbatch', N'T13', '2026-03-01'),
('5', N'The Lion King',    118, N'Đang Chiếu', N'Chú sư tử con Simba phải trốn chạy sau cái chết của vua cha Mufasa, rồi trưởng thành và trở về giành lại ngai vàng.', N'Jon Favreau', N'Donald Glover, Beyoncé, Seth Rogen, Chiwetel Ejiofor', N'P',   '2026-03-20'),
('6', N'Doraemon',         108, N'Đang Chiếu', N'Chú mèo máy Doraemon cùng Nobita và các bạn bước vào chuyến phiêu lưu mới với những bảo bối thần kỳ.', N'Shinnosuke Yakuwa', N'Wasabi Mizuta, Megumi Ohara, Yumi Kakazu', N'P',   '2026-04-01'),
('7', N'Kamen Rider',       90, N'Sắp Chiếu',  N'Những chiến binh Kamen Rider bảo vệ nhân loại trước các thế lực xâm lược trong trận chiến mới.', N'Koichi Sakamoto', N'Ryo Yoshizawa, Rina Takeda', N'T13', '2026-12-01'),
('8', N'Ngày Con Còn Mẹ', 107, N'Đang Chiếu', N'Bộ phim gia đình xúc động về tình mẹ và những người con trưởng thành, nhắc ta trân trọng từng ngày còn được ở bên mẹ.', N'Vũ Ngọc Đãng', N'Hồng Đào, Phương Anh Đào, Huy Anh, Ma Ran Đô, Quỳnh Lương', N'T13', '2026-11-11'),
('9', N'Godzilla Trừ Không', 135, N'Đang Chiếu', N'Nước Nhật hoang tàn sau chiến tranh phải đối mặt với sự xuất hiện của quái vật Godzilla. Người dân tự đứng lên chống lại thảm họa khi không còn gì để mất.', N'Takashi Yamazaki', N'Ryunosuke Kamiki, Minami Hamabe', N'T13', '2026-11-06');
-- Thể loại của từng phim: quan hệ nhiều-nhiều, mỗi phim có thể có nhiều thể loại (giao diện cho tick chọn nhiều thể loại)
INSERT INTO Phim_TheLoai (MaPhim, MaTheLoai) VALUES
('1', 'TL001'), ('1', 'TL008'),
('2', 'TL003'), ('2', 'TL002'), ('2', 'TL017'),
('3', 'TL007'), ('3', 'TL011'),
('4', 'TL001'), ('4', 'TL008'),
('5', 'TL003'), ('5', 'TL014'),
('6', 'TL003'), ('6', 'TL002'), ('6', 'TL014'),
('7', 'TL001'),
('8', 'TL014'), ('8', 'TL010'),
('9', 'TL001'), ('9', 'TL008');
-- Phòng chiếu
INSERT INTO PhongChieu (MaPhongChieu, TenPhongChieu, LoaiPhongChieu, TrangThai, SucChua) VALUES
('P01', N'Phòng 1', N'2D', N'Hoạt động', 100),
('P02', N'Phòng 2', N'3D', N'Hoạt động', 100);
-- Mỗi phòng có 10 hàng A-J, mỗi hàng 10 ghế. Hàng G-J là ghế VIP.
-- Dùng vòng lặp WHILE để không phải viết 200 câu INSERT.
DECLARE @SoPhong INT = 1;
DECLARE @Hang INT;
DECLARE @Cot INT;
DECLARE @MaPhong VARCHAR(20);
DECLARE @ViTri NVARCHAR(10);
DECLARE @LoaiGhe NVARCHAR(50);

WHILE @SoPhong <= 2
BEGIN
    SET @MaPhong = 'P0' + CAST(@SoPhong AS VARCHAR(1));
    SET @Hang = 0;

    WHILE @Hang < 10
    BEGIN
        -- CHAR(65) là A, CHAR(66) là B, ... CHAR(74) là J.
        IF @Hang >= 6
            SET @LoaiGhe = N'VIP';
        ELSE
            SET @LoaiGhe = N'Thường';

        SET @Cot = 1;
        WHILE @Cot <= 10
        BEGIN
            -- RIGHT(..., 2) lấy hai ký tự cuối: 1 thành 01, 10 vẫn là 10.
            SET @ViTri = CHAR(65 + @Hang) + RIGHT('0' + CAST(@Cot AS VARCHAR(2)), 2);
            INSERT INTO Ghe (MaGhe, ViTri, MaPhongChieu, LoaiGhe)
            VALUES (@MaPhong + @ViTri, @ViTri, @MaPhong, @LoaiGhe);

            SET @Cot = @Cot + 1;
        END;
        SET @Hang = @Hang + 1;
    END;
    SET @SoPhong = @SoPhong + 1;
END;

-- Suất chiếu mẫu vào ngày sau ngày chạy script, không trùng giờ trong cùng phòng.
-- CAST(... AS DATE) bỏ phần giờ; biến DATETIME sẽ có giờ 00:00.
DECLARE @NgayMai DATETIME = CAST(DATEADD(DAY, 1, GETDATE()) AS DATE);

-- DATEADD cộng giờ hoặc phút. Giờ kết thúc = giờ bắt đầu + thời lượng phim.
INSERT INTO SuatChieu (MaSuatChieu, MaPhim, MaPhongChieu, GioBatDau, GioKetThuc, GiaVe, TrangThai)
VALUES
('SC01', '1', 'P01', DATEADD(HOUR, 9, @NgayMai), DATEADD(MINUTE, 181, DATEADD(HOUR, 9, @NgayMai)), 100000, N'Đang Mở Bán'),
('SC02', '2', 'P01', DATEADD(HOUR, 13, @NgayMai), DATEADD(MINUTE, 103, DATEADD(HOUR, 13, @NgayMai)), 100000, N'Đang Mở Bán'),
('SC03', '3', 'P01', DATEADD(HOUR, 16, @NgayMai), DATEADD(MINUTE, 122, DATEADD(HOUR, 16, @NgayMai)), 100000, N'Đang Mở Bán'),
('SC04', '4', 'P02', DATEADD(HOUR, 10, @NgayMai), DATEADD(MINUTE, 148, DATEADD(HOUR, 10, @NgayMai)), 120000, N'Đang Mở Bán'),
('SC05', '5', 'P02', DATEADD(HOUR, 14, @NgayMai), DATEADD(MINUTE, 118, DATEADD(HOUR, 14, @NgayMai)), 120000, N'Đang Mở Bán'),
('SC06', '6', 'P02', DATEADD(HOUR, 18, @NgayMai), DATEADD(MINUTE, 108, DATEADD(HOUR, 18, @NgayMai)), 120000, N'Đang Mở Bán');
-- Khởi tạo trạng thái ghế 'Trống' cho mọi suất chiếu mẫu (giống GheDAO.khoiTaoTrangThaiGheChoSuatChieu)
INSERT INTO Ghe_SuatChieu (MaSuatChieu, MaGhe, TrangThai)
SELECT sc.MaSuatChieu, g.MaGhe, N'Trống'
FROM SuatChieu sc
JOIN Ghe g ON g.MaPhongChieu = sc.MaPhongChieu;
-- Vé mẫu cho suất SC01: VE001, VE002 đã bán (hóa đơn HD001); VE003 đang giữ chỗ (phiếu đặt PD001)
INSERT INTO Ve (MaVe, MaSuatChieu, MaGhe, GiaTien, TrangThai) VALUES
('VE001', 'SC01', 'P01E05', 100000, N'Đã bán'),
('VE002', 'SC01', 'P01E06', 100000, N'Đã bán'),
('VE003', 'SC01', 'P01E07', 100000, N'Đã đặt');

UPDATE Ghe_SuatChieu
SET TrangThai = N'Đã bán'
WHERE MaSuatChieu = 'SC01' AND MaGhe IN ('P01E05', 'P01E06');

UPDATE Ghe_SuatChieu
SET TrangThai = N'Đã đặt'
WHERE MaSuatChieu = 'SC01' AND MaGhe = 'P01E07';
-- Phiếu đặt vé: KH02 giữ chỗ 1 vé
INSERT INTO PhieuDatVe (MaPhieuDatVe, NgayDat, MaKhachHang, TrangThai)
VALUES ('PD001', CAST(GETDATE() AS DATE), 'KH02', N'Chờ xác nhận');

INSERT INTO ChiTietPhieuDatVe (MaPhieuDatVe, MaVe, DonGia) VALUES ('PD001', 'VE003', 100000);
-- Hóa đơn mẫu: KH01 mua 2 vé + 1 Solo Combo, khuyến mãi KM009 (Member Special - giảm 10% hóa đơn)
--   (100000 + 100000 + 69000) x 0.9 = 242100
INSERT INTO HoaDon (
    MaHoaDon, NgayLap, MaNhanVien, MaKhachHang, MaKhuyenMai,
    PhuongThucThanhToan, PhanTramGiamApDung, HinhThucApDung, SoComboTangApDung
)
VALUES ('HD001', GETDATE(), 'NV02', 'KH01', 'KM009', N'Tiền mặt', 10, N'Giảm hóa đơn', 0);

INSERT INTO ChiTietHoaDon (MaHoaDon, MaVe, MaCombo, SoLuong, DonGia) VALUES
('HD001', 'VE001', NULL,   1, 100000),
('HD001', 'VE002', NULL,   1, 100000),
('HD001', NULL,    'CB01', 1, 69000);
GO

-- 5. TẠO CÁC VIEW TÍNH TIỀN
-- VIEW là câu SELECT được lưu lại để Java có thể truy vấn như một bảng.
-- Bước 1: Cộng tiền vé và combo của từng hóa đơn.
-- LEFT JOIN giữ lại cả hóa đơn chưa có chi tiết; ISNULL đổi NULL thành 0.
CREATE VIEW vw_HoaDon_TienHang AS
SELECT
    hd.MaHoaDon, hd.NgayLap, hd.MaNhanVien, hd.MaKhachHang,
    hd.MaKhuyenMai, hd.PhuongThucThanhToan, hd.MaPhieuDatVe, hd.TrangThai,
    hd.PhanTramGiamApDung, hd.HinhThucApDung, hd.SoComboTangApDung,
    ISNULL(SUM(CASE WHEN ct.MaVe IS NOT NULL
                    THEN ct.SoLuong * ct.DonGia ELSE 0 END), 0) AS TienVe,
    ISNULL(SUM(CASE WHEN ct.MaCombo IS NOT NULL
                    THEN ct.SoLuong * ct.DonGia ELSE 0 END), 0) AS TienCombo,
    ISNULL(MIN(CASE WHEN ct.MaCombo IS NOT NULL
                    THEN ct.DonGia END), 0) AS GiaComboReNhat,
    ISNULL(SUM(CASE WHEN ct.MaCombo IS NOT NULL
                    THEN ct.SoLuong ELSE 0 END), 0) AS SoCombo
FROM HoaDon hd
LEFT JOIN ChiTietHoaDon ct ON hd.MaHoaDon = ct.MaHoaDon
GROUP BY
    hd.MaHoaDon, hd.NgayLap, hd.MaNhanVien, hd.MaKhachHang,
    hd.MaKhuyenMai, hd.PhuongThucThanhToan, hd.MaPhieuDatVe, hd.TrangThai,
    hd.PhanTramGiamApDung, hd.HinhThucApDung, hd.SoComboTangApDung;
GO

-- Bước 2: Tính số tiền được giảm. CASE WHEN tương tự if/else trong Java.
-- Dùng khuyến mãi đã lưu trong HoaDon, không lấy lại từ bảng KhuyenMai.
CREATE VIEW vw_HoaDon_GiamGia AS
SELECT
    t.*,
    CASE
        WHEN HinhThucApDung IN (N'Giảm giá vé', N'Giảm hóa đơn')
            THEN TienVe * PhanTramGiamApDung / 100.0
        ELSE 0
    END AS GiamVe,
    CASE
        WHEN HinhThucApDung IN (N'Giảm giá combo', N'Giảm hóa đơn')
            THEN TienCombo * PhanTramGiamApDung / 100.0
        -- Tặng combo: lấy giá rẻ nhất nhân với số combo được tặng.
        -- Không tặng nhiều hơn số combo có trong hóa đơn.
        WHEN HinhThucApDung = N'Tặng combo' THEN
            GiaComboReNhat * CASE
                WHEN SoCombo < SoComboTangApDung THEN SoCombo
                ELSE SoComboTangApDung
            END
        ELSE 0
    END AS GiamCombo
FROM vw_HoaDon_TienHang t;
GO

-- Bước 3: Số tiền phải trả = tiền hàng - tiền giảm.
-- Giữ tên view và tên cột này vì phần Java đang sử dụng.
CREATE VIEW vw_HoaDon_TongTien AS
SELECT
    MaHoaDon, NgayLap, MaNhanVien, MaKhachHang,
    MaKhuyenMai, PhuongThucThanhToan, MaPhieuDatVe, TrangThai,
    CAST(TienVe + TienCombo AS DECIMAL(18,2)) AS TongTienGoc,
    CAST(TienVe + TienCombo - GiamVe - GiamCombo AS DECIMAL(18,2)) AS TongTien,
    CAST(TienVe - GiamVe AS DECIMAL(18,2)) AS DoanhThuVe,
    CAST(TienCombo - GiamCombo AS DECIMAL(18,2)) AS DoanhThuCombo
FROM vw_HoaDon_GiamGia;
GO

-- Tổng tiền của phiếu đặt vé = tổng đơn giá các vé được giữ chỗ.
CREATE VIEW vw_PhieuDatVe_TongTien AS
SELECT
    pd.MaPhieuDatVe, pd.NgayDat, pd.MaKhachHang, pd.TrangThai,
    ISNULL(SUM(ct.DonGia), 0) AS TongTienDat
FROM PhieuDatVe pd
LEFT JOIN ChiTietPhieuDatVe ct ON pd.MaPhieuDatVe = ct.MaPhieuDatVe
GROUP BY pd.MaPhieuDatVe, pd.NgayDat, pd.MaKhachHang, pd.TrangThai;
GO

-- 6. KIỂM TRA SAU KHI TẠO
-- HD001: 2 vé 100.000 + 1 combo 69.000, giảm 10% = 242.100 đồng.
SELECT MaHoaDon, TongTienGoc, TongTien, DoanhThuVe, DoanhThuCombo
FROM vw_HoaDon_TongTien;

SELECT MaPhieuDatVe, TongTienDat
FROM vw_PhieuDatVe_TongTien;

PRINT N'Đã tạo xong database và dữ liệu mẫu.';
GO

-- Đặt ở cuối để cửa sổ SSMS dùng được bình thường sau khi chạy toàn bộ file.
SET NOEXEC OFF;
GO
