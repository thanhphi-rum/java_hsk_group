/* =====================================================================
   QUAN LY RAP PHIM  -  SQL Server (T-SQL)
   Thiet ke theo so do lop QL_RapPhim.
   Chay toan bo file 1 lan tren SQL Server Management Studio.
   Script nay se xoa database cu va tao lai schema + du lieu mau.
   ===================================================================== */

USE master;
GO

-- RESET: Xoa database cu va tao lai toan bo schema + du lieu mau.
IF DB_ID(N'QuanLyBanVePhimMoi') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyBanVePhimMoi SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyBanVePhimMoi;
END
GO

IF DB_ID(N'QuanLyBanVePhimMoi') IS NULL
    CREATE DATABASE QuanLyBanVePhimMoi;
GO

USE QuanLyBanVePhimMoi;
GO

/* =====================================================================
   1. TAO BANG
   ===================================================================== */

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
GO

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
GO

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
GO

-- KhuyenMai(maKhuyenMai, tenKhuyenMai, phanTramGiam, ngayBatDau, ngayKetThuc, moTa)
CREATE TABLE KhuyenMai (
    MaKhuyenMai  VARCHAR(20)   NOT NULL,
    TenKhuyenMai NVARCHAR(100) NOT NULL,
    PhanTramGiam DECIMAL(5,2)  NOT NULL,
    NgayBatDau   DATE          NOT NULL,
    NgayKetThuc  DATE          NOT NULL,
    MoTa         NVARCHAR(200) NULL,
    TrangThai    NVARCHAR(20)  NOT NULL DEFAULT N'Hoạt động',
    CONSTRAINT PK_KhuyenMai PRIMARY KEY (MaKhuyenMai),
    CONSTRAINT CK_KhuyenMai_PhanTram CHECK (PhanTramGiam >= 0 AND PhanTramGiam <= 100),
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (NgayKetThuc >= NgayBatDau)
);
GO

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
GO

-- LoaiPhim(maLoaiPhim, tenLoaiPhim, moTa)
CREATE TABLE LoaiPhim (
    MaLoaiPhim  VARCHAR(20)   NOT NULL,
    TenLoaiPhim NVARCHAR(100) NOT NULL,
    MoTa        NVARCHAR(200) NULL,
    CONSTRAINT PK_LoaiPhim PRIMARY KEY (MaLoaiPhim),
    CONSTRAINT UQ_LoaiPhim_Ten UNIQUE (TenLoaiPhim)
);
GO

-- Phim(maPhim, tenPhim, thoiLuong, trangThai, moTa, daoDien, dienVien, gioiHanTuoi, ngayKhoiChieu, maLoaiPhim)
-- Trạng thái: 'Sắp Chiếu', 'Đang Chiếu', 'Ngừng Chiếu'.   LoaiPhim 1 --- 0..* Phim
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
    MaLoaiPhim    VARCHAR(20)   NOT NULL,
    CONSTRAINT PK_Phim PRIMARY KEY (MaPhim),
    CONSTRAINT FK_Phim_LoaiPhim FOREIGN KEY (MaLoaiPhim) REFERENCES LoaiPhim(MaLoaiPhim),
    CONSTRAINT CK_Phim_ThoiLuong CHECK (ThoiLuong > 0)
);
GO

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
GO

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
GO

-- SuatChieu(maSuatChieu, maPhim, maPhongChieu, gioBatDau, gioKetThuc, giaVe, trangThai)
-- NgayChieu là cột tính toán (diagram có ngayChieu trong hàm khởi tạo), tự lấy từ GioBatDau.
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
GO
CREATE INDEX IX_SuatChieu_MaPhim ON SuatChieu(MaPhim);
CREATE INDEX IX_SuatChieu_Phong_Gio ON SuatChieu(MaPhongChieu, GioBatDau);
GO

-- GheSuatChieu(maGhe, maSuatChieu, trangThai)  -> bảng Ghe_SuatChieu
-- Trạng thái: 'Trống', 'Đã đặt'. Không có ON DELETE CASCADE: SuatChieuDAO tự xóa bảng này trước.
CREATE TABLE Ghe_SuatChieu (
    MaSuatChieu VARCHAR(20)  NOT NULL,
    MaGhe       VARCHAR(20)  NOT NULL,
    TrangThai   NVARCHAR(20) NOT NULL DEFAULT N'Trống',
    CONSTRAINT PK_Ghe_SuatChieu PRIMARY KEY (MaSuatChieu, MaGhe),
    CONSTRAINT FK_GSC_SuatChieu FOREIGN KEY (MaSuatChieu) REFERENCES SuatChieu(MaSuatChieu),
    CONSTRAINT FK_GSC_Ghe FOREIGN KEY (MaGhe) REFERENCES Ghe(MaGhe)
);
GO

-- Ve(maVe, maGheSuatChieu, giaTien, trangThai)
-- maGheSuatChieu = khóa ghép (MaSuatChieu, MaGhe) -> Ghe_SuatChieu.
-- UNIQUE (MaSuatChieu, MaGhe): mỗi ghế trong 1 suất chỉ có tối đa 1 vé (chặn bán trùng).
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
GO
CREATE UNIQUE INDEX UX_Ve_Suat_Ghe ON Ve(MaSuatChieu,MaGhe) WHERE TrangThai <> N'Đã hủy';
GO

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
GO

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
GO

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
    CONSTRAINT PK_HoaDon PRIMARY KEY (MaHoaDon),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (MaKhuyenMai) REFERENCES KhuyenMai(MaKhuyenMai),
    CONSTRAINT FK_HoaDon_PhieuDatVe FOREIGN KEY (MaPhieuDatVe) REFERENCES PhieuDatVe(MaPhieuDatVe)
);
GO
CREATE INDEX IX_HoaDon_KhachHang ON HoaDon(MaKhachHang);
CREATE INDEX IX_HoaDon_NgayLap ON HoaDon(NgayLap);
-- 1 phiếu đặt vé chỉ chuyển thành tối đa 1 hóa đơn
CREATE UNIQUE INDEX UX_HoaDon_PhieuDatVe ON HoaDon(MaPhieuDatVe) WHERE MaPhieuDatVe IS NOT NULL;
GO

-- ChiTietHoaDon(maHoaDon, maVe, maCombo, soLuong, donGia)   HoaDon 1 <>--- 1..* ChiTietHoaDon
-- Mỗi dòng là 1 vé (MaVe, số lượng 1) HOẶC 1 combo bắp nước (MaCombo, số lượng n), không được cả hai.
-- Diagram không có mã riêng cho dòng chi tiết nên dùng khóa thay thế MaChiTietHoaDon (tự tăng).
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
-- Mỗi vé chỉ nằm trong 1 dòng hóa đơn; mỗi combo chỉ xuất hiện 1 dòng trong cùng hóa đơn
CREATE UNIQUE INDEX UX_CTHD_Ve ON ChiTietHoaDon(MaVe) WHERE MaVe IS NOT NULL;
CREATE UNIQUE INDEX UX_CTHD_HoaDon_Combo ON ChiTietHoaDon(MaHoaDon, MaCombo) WHERE MaCombo IS NOT NULL;
CREATE INDEX IX_CTHD_HoaDon ON ChiTietHoaDon(MaHoaDon);
GO

/* =====================================================================
   2. VIEW TINH TONG TIEN (tuong ung tinhTongTien / tinhTongTienDat trong so do)
   ===================================================================== */

-- HoaDon.tinhTongTien(): tổng thành tiền các dòng chi tiết, trừ phần trăm khuyến mãi (nếu có)
CREATE VIEW vw_HoaDon_TongTien AS
SELECT h.MaHoaDon,
       h.NgayLap,
       h.MaNhanVien,
       h.MaKhachHang,
       h.MaKhuyenMai,
       h.PhuongThucThanhToan,
       h.MaPhieuDatVe,
       h.TrangThai,
       ISNULL(SUM(ct.SoLuong * ct.DonGia), 0)                                   AS TongTienGoc,
       CAST(ISNULL(SUM(ct.SoLuong * ct.DonGia), 0) * (1 - ISNULL(km.PhanTramGiam, 0) / 100.0)
            AS DECIMAL(18,2))                                                   AS TongTien
FROM HoaDon h
LEFT JOIN ChiTietHoaDon ct ON ct.MaHoaDon = h.MaHoaDon
LEFT JOIN KhuyenMai km     ON km.MaKhuyenMai = h.MaKhuyenMai
GROUP BY h.MaHoaDon, h.NgayLap, h.MaNhanVien, h.MaKhachHang, h.MaKhuyenMai,
         h.PhuongThucThanhToan, h.MaPhieuDatVe, h.TrangThai, km.PhanTramGiam;
GO

-- PhieuDatVe.tinhTongTienDat(): tổng đơn giá các vé trong phiếu
CREATE VIEW vw_PhieuDatVe_TongTien AS
SELECT p.MaPhieuDatVe,
       p.NgayDat,
       p.MaKhachHang,
       p.TrangThai,
       ISNULL(SUM(ct.DonGia), 0) AS TongTienDat
FROM PhieuDatVe p
LEFT JOIN ChiTietPhieuDatVe ct ON ct.MaPhieuDatVe = p.MaPhieuDatVe
GROUP BY p.MaPhieuDatVe, p.NgayDat, p.MaKhachHang, p.TrangThai;
GO

/* =====================================================================
   3. DU LIEU MAU
   ===================================================================== */

-- Nhân viên
INSERT INTO NhanVien (MaNhanVien, TenNhanVien, SoDienThoai, Email, GioiTinh, NgaySinh, DiaChi, ChucVu) VALUES
('NV01', N'Nguyễn Văn An', '0901234567', 'an.nguyen@rapphim.com', N'Nam', '1995-05-20', N'12 Nguyễn Huệ Quận 1',   N'Quản Lý'),
('NV02', N'Trần Thị Bình', '0912345678', 'binh.tran@rapphim.com', N'Nữ',  '1998-09-12', N'45 Lê Lợi Quận 1',       N'Nhân Viên'),
('NV03', N'Lê Minh Châu',  '0923456789', 'chau.le@rapphim.com',   N'Nữ',  '2000-01-30', N'78 Hai Bà Trưng Quận 3', N'Nhân Viên');
GO

-- Tài khoản mẫu: admin / admin123 (Quản Lý), nv02 / 123456 (Nhân Viên). Hãy đổi mật khẩu sau khi đăng nhập.
-- Mật khẩu được băm SHA-256 ngay trong SQL, khớp với cách ứng dụng Java kiểm tra đăng nhập.
INSERT INTO TaiKhoan (TenDangNhap, MatKhau, TrangThai, MaNhanVien) VALUES
('admin', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'admin123'), 2), N'Hoạt động', 'NV01'),
('nv02',  CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '123456'),   2), N'Hoạt động', 'NV02');
GO

-- Khách hàng
INSERT INTO KhachHang (MaKhachHang, TenKhachHang, Email, SoDienThoai, DiemTichLuy) VALUES
('KH01', N'Phạm Quốc Dũng',  'dung.pham@gmail.com', '0934567890', 120),
('KH02', N'Võ Thị Em',       'em.vo@gmail.com',     '0945678901',  40),
('KH03', N'Đặng Hoàng Phúc', 'phuc.dang@gmail.com', '0956789012',   0);
GO

-- Khuyến mãi
INSERT INTO KhuyenMai (MaKhuyenMai, TenKhuyenMai, PhanTramGiam, NgayBatDau, NgayKetThuc, MoTa) VALUES
('KM01', N'Giảm 10%',    10, '2026-01-01', '2026-12-31', N'Giảm 10% cho mọi hóa đơn'),
('KM02', N'Sinh viên',   20, '2026-01-01', '2026-12-31', N'Giảm 20% cho sinh viên'),
('KM03', N'Khai trương', 30, '2026-01-01', '2026-01-31', N'Giảm 30% tuần khai trương');
GO

-- Combo bắp nước
INSERT INTO ComboBapNuoc (MaCombo, TenCombo, GiaBan, MoTa, TrangThai) VALUES
('CB01', N'Combo 1 người', 59000,  N'1 bắp vừa + 1 nước ngọt',            N'Đang bán'),
('CB02', N'Combo 2 người', 99000,  N'1 bắp lớn + 2 nước ngọt',            N'Đang bán'),
('CB03', N'Combo gia đình', 159000, N'2 bắp lớn + 4 nước ngọt',           N'Đang bán');
GO

-- Loại phim
INSERT INTO LoaiPhim (MaLoaiPhim, TenLoaiPhim, MoTa) VALUES
('LP01', N'Hành Động', N'Phim hành động, phiêu lưu'),
('LP02', N'Hoạt Hình', N'Phim hoạt hình'),
('LP03', N'Tâm Lý',    N'Phim tâm lý, chính kịch');
GO

-- Phim: mã '1'..'7' để khớp phần gán ảnh theo MaPhim trong Phim_GUI
INSERT INTO Phim (MaPhim, TenPhim, ThoiLuong, TrangThai, MoTa, DaoDien, DienVien, GioiHanTuoi, NgayKhoiChieu, MaLoaiPhim) VALUES
('1', N'Avengers Endgame', 181, N'Đang Chiếu', N'', N'', N'', N'T13', '2026-01-10', 'LP01'),
('2', N'Frozen 2',         103, N'Đang Chiếu', N'', N'', N'', N'P',   '2026-02-01', 'LP02'),
('3', N'Joker',            122, N'Đang Chiếu', N'', N'', N'', N'T18', '2026-02-15', 'LP03'),
('4', N'Spider Man',       148, N'Đang Chiếu', N'', N'', N'', N'T13', '2026-03-01', 'LP01'),
('5', N'The Lion King',    118, N'Đang Chiếu', N'', N'', N'', N'P',   '2026-03-20', 'LP02'),
('6', N'Doraemon',         108, N'Đang Chiếu', N'', N'', N'', N'P',   '2026-04-01', 'LP02'),
('7', N'Kamen Rider',       90, N'Sắp Chiếu',  N'', N'', N'', N'T13', '2026-12-01', 'LP01');
GO

-- Phòng chiếu
INSERT INTO PhongChieu (MaPhongChieu, TenPhongChieu, LoaiPhongChieu, TrangThai, SucChua) VALUES
('P01', N'Phòng 1', N'2D', N'Hoạt động', 100),
('P02', N'Phòng 2', N'3D', N'Hoạt động', 100);
GO

-- Ghế: mỗi phòng 10 hàng (A-J) x 10 cột = 100 ghế. Mã ghế dạng P01A01. Hàng G-J là ghế VIP.
DECLARE @p INT = 1, @r INT, @c INT, @maPhong VARCHAR(20), @viTri NVARCHAR(10);
WHILE @p <= 2
BEGIN
    SET @maPhong = 'P0' + CAST(@p AS VARCHAR(1));
    SET @r = 0;
    WHILE @r < 10
    BEGIN
        SET @c = 1;
        WHILE @c <= 10
        BEGIN
            SET @viTri = CHAR(65 + @r) + RIGHT('0' + CAST(@c AS VARCHAR(2)), 2);
            INSERT INTO Ghe (MaGhe, ViTri, MaPhongChieu, LoaiGhe)
            VALUES (@maPhong + @viTri, @viTri, @maPhong, CASE WHEN @r >= 6 THEN N'VIP' ELSE N'Thường' END);
            SET @c = @c + 1;
        END
        SET @r = @r + 1;
    END
    SET @p = @p + 1;
END
GO

-- Suất chiếu mẫu: ngày mai, mỗi phòng không trùng giờ. GioKetThuc = GioBatDau + ThoiLuong (phút) của phim.
INSERT INTO SuatChieu (MaSuatChieu, MaPhim, MaPhongChieu, GioBatDau, GioKetThuc, GiaVe, TrangThai)
SELECT v.MaSuatChieu, v.MaPhim, v.MaPhongChieu,
       DATEADD(HOUR, v.Gio, CAST(CAST(DATEADD(DAY, 1, GETDATE()) AS DATE) AS DATETIME)),
       DATEADD(MINUTE, p.ThoiLuong,
               DATEADD(HOUR, v.Gio, CAST(CAST(DATEADD(DAY, 1, GETDATE()) AS DATE) AS DATETIME))),
       v.GiaVe, N'Đang Mở Bán'
FROM (VALUES
    ('SC01', '1', 'P01',  9, 100000),
    ('SC02', '2', 'P01', 13, 100000),
    ('SC03', '3', 'P01', 16, 100000),
    ('SC04', '4', 'P02', 10, 120000),
    ('SC05', '5', 'P02', 14, 120000),
    ('SC06', '6', 'P02', 18, 120000)
) AS v (MaSuatChieu, MaPhim, MaPhongChieu, Gio, GiaVe)
JOIN Phim p ON p.MaPhim = v.MaPhim;
GO

-- Khởi tạo trạng thái ghế 'Trống' cho mọi suất chiếu mẫu (giống GheDAO.khoiTaoTrangThaiGheChoSuatChieu)
INSERT INTO Ghe_SuatChieu (MaSuatChieu, MaGhe, TrangThai)
SELECT sc.MaSuatChieu, g.MaGhe, N'Trống'
FROM SuatChieu sc
JOIN Ghe g ON g.MaPhongChieu = sc.MaPhongChieu;
GO

-- Vé mẫu cho suất SC01: VE001, VE002 đã bán (hóa đơn HD001); VE003 đang giữ chỗ (phiếu đặt PD001)
INSERT INTO Ve (MaVe, MaSuatChieu, MaGhe, GiaTien, TrangThai) VALUES
('VE001', 'SC01', 'P01E05', 100000, N'Đã bán'),
('VE002', 'SC01', 'P01E06', 100000, N'Đã bán'),
('VE003', 'SC01', 'P01E07', 100000, N'Đã đặt');

UPDATE gs SET TrangThai=CASE WHEN v.TrangThai=N'Đã bán' THEN N'Đã bán' ELSE N'Đã đặt' END
FROM Ghe_SuatChieu gs JOIN Ve v ON v.MaSuatChieu=gs.MaSuatChieu AND v.MaGhe=gs.MaGhe
WHERE gs.MaSuatChieu='SC01' AND gs.MaGhe IN ('P01E05','P01E06','P01E07');
GO

-- Phiếu đặt vé: KH02 giữ chỗ 1 vé
INSERT INTO PhieuDatVe (MaPhieuDatVe, NgayDat, MaKhachHang, TrangThai)
VALUES ('PD001', CAST(GETDATE() AS DATE), 'KH02', N'Chờ xác nhận');

INSERT INTO ChiTietPhieuDatVe (MaPhieuDatVe, MaVe, DonGia) VALUES ('PD001', 'VE003', 100000);
GO

-- Hóa đơn mẫu: KH01 mua 2 vé + 1 combo, khuyến mãi KM01 (10%)
--   (100000 + 100000 + 59000) x 0.9 = 233100
INSERT INTO HoaDon (MaHoaDon, NgayLap, MaNhanVien, MaKhachHang, MaKhuyenMai, PhuongThucThanhToan, MaPhieuDatVe)
VALUES ('HD001', GETDATE(), 'NV02', 'KH01', 'KM01', N'Tiền mặt', NULL);

INSERT INTO ChiTietHoaDon (MaHoaDon, MaVe, MaCombo, SoLuong, DonGia) VALUES
('HD001', 'VE001', NULL,   1, 100000),
('HD001', 'VE002', NULL,   1, 100000),
('HD001', NULL,    'CB01', 1, 59000);
GO

-- Kiểm tra bảng, số dòng dữ liệu và tổng tiền hóa đơn mẫu
SELECT t.name AS Bang, SUM(p.rows) AS SoDong
FROM sys.tables t
JOIN sys.partitions p ON p.object_id = t.object_id AND p.index_id IN (0, 1)
GROUP BY t.name
ORDER BY t.name;

SELECT MaHoaDon, TongTienGoc, TongTien FROM vw_HoaDon_TongTien;
GO




