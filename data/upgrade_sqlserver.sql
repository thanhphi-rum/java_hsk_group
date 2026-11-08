-- SQL Server. Không DROP database/bảng và không xóa dữ liệu hiện có.
USE QuanLyBanVePhimMoi;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL
CREATE TABLE dbo.NhanVien(MaNhanVien varchar(10) PRIMARY KEY,TenNhanVien nvarchar(100) NOT NULL,NgaySinh date NOT NULL,GioiTinh nvarchar(10) NOT NULL,SoDienThoai varchar(15) NOT NULL,Email nvarchar(100),DiaChi nvarchar(200),ChucVu nvarchar(50));
IF OBJECT_ID(N'dbo.Phim',N'U') IS NULL
CREATE TABLE dbo.Phim(MaPhim varchar(10) PRIMARY KEY,TenPhim nvarchar(100) NOT NULL,TheLoai nvarchar(50) NOT NULL,ThoiLuong int NOT NULL CHECK(ThoiLuong>0),TrangThai nvarchar(30) NOT NULL,MoTa nvarchar(1000),DaoDien nvarchar(100),DienVien nvarchar(500),GioiHanTuoi nvarchar(20),NgayKhoiChieu date,PosterPath nvarchar(500));
IF OBJECT_ID(N'dbo.PhongChieu',N'U') IS NULL
CREATE TABLE dbo.PhongChieu(MaPhongChieu varchar(10) PRIMARY KEY,TenPhong nvarchar(50) NOT NULL,SucChua int NOT NULL DEFAULT 0);
IF OBJECT_ID(N'dbo.SuatChieu',N'U') IS NULL
CREATE TABLE dbo.SuatChieu(MaSuatChieu varchar(10) PRIMARY KEY,MaPhim varchar(10) NOT NULL REFERENCES dbo.Phim(MaPhim),MaPhongChieu varchar(10) NOT NULL REFERENCES dbo.PhongChieu(MaPhongChieu),GioBatDau datetime NOT NULL,GioKetThuc datetime NOT NULL,GiaVe decimal(12,2) NOT NULL DEFAULT 100000,TrangThai nvarchar(30) NOT NULL DEFAULT N'Đang Mở Bán',CHECK(GioKetThuc>GioBatDau));
IF OBJECT_ID(N'dbo.Ghe',N'U') IS NULL
CREATE TABLE dbo.Ghe(MaGhe varchar(10) PRIMARY KEY,MaPhongChieu varchar(10) NOT NULL REFERENCES dbo.PhongChieu(MaPhongChieu),ViTri nvarchar(20) NOT NULL,LoaiGhe nvarchar(20) NOT NULL DEFAULT N'Thường',UNIQUE(MaPhongChieu,ViTri));
IF OBJECT_ID(N'dbo.Ghe_SuatChieu',N'U') IS NULL
CREATE TABLE dbo.Ghe_SuatChieu(MaSuatChieu varchar(10) NOT NULL REFERENCES dbo.SuatChieu(MaSuatChieu),MaGhe varchar(10) NOT NULL REFERENCES dbo.Ghe(MaGhe),TrangThai nvarchar(20) NOT NULL DEFAULT N'Trống',PRIMARY KEY(MaSuatChieu,MaGhe));
IF OBJECT_ID(N'dbo.KhachHang',N'U') IS NULL
CREATE TABLE dbo.KhachHang(MaKhachHang varchar(10) PRIMARY KEY,TenKhachHang nvarchar(100) NOT NULL,SoDienThoai varchar(15) NOT NULL,Email nvarchar(100),NgaySinh date,GioiTinh nvarchar(10),DiemTichLuy int NOT NULL DEFAULT 0);
IF OBJECT_ID(N'dbo.KhuyenMai',N'U') IS NULL
CREATE TABLE dbo.KhuyenMai(MaKhuyenMai varchar(10) PRIMARY KEY,TenKhuyenMai nvarchar(100),MoTa nvarchar(200) NOT NULL,PhanTramGiam decimal(5,2) NOT NULL CHECK(PhanTramGiam BETWEEN 0 AND 100),NgayBatDau date,NgayKetThuc date);
IF OBJECT_ID(N'dbo.HoaDon',N'U') IS NULL
CREATE TABLE dbo.HoaDon(MaHoaDon varchar(10) PRIMARY KEY,NgayLap datetime NOT NULL,MaNhanVien varchar(10) NOT NULL REFERENCES dbo.NhanVien(MaNhanVien),MaKhachHang varchar(10) NULL REFERENCES dbo.KhachHang(MaKhachHang),TongTien decimal(12,2) NOT NULL CHECK(TongTien>=0),MaKhuyenMai varchar(10) NULL REFERENCES dbo.KhuyenMai(MaKhuyenMai),PhuongThucThanhToan nvarchar(50) NOT NULL DEFAULT N'Tiền mặt');
IF OBJECT_ID(N'dbo.Ve',N'U') IS NULL
CREATE TABLE dbo.Ve(MaVe varchar(10) PRIMARY KEY,MaSuatChieu varchar(10) NOT NULL REFERENCES dbo.SuatChieu(MaSuatChieu),MaGhe varchar(10) NOT NULL REFERENCES dbo.Ghe(MaGhe),MaHoaDon varchar(10) NOT NULL REFERENCES dbo.HoaDon(MaHoaDon),GiaTien decimal(12,2) NOT NULL,UNIQUE(MaSuatChieu,MaGhe));
IF OBJECT_ID(N'dbo.TaiKhoanUngDung',N'U') IS NULL
CREATE TABLE dbo.TaiKhoanUngDung(TenDangNhap varchar(50) PRIMARY KEY,MatKhauHash varchar(200) NOT NULL,MaNhanVien varchar(10) NOT NULL REFERENCES dbo.NhanVien(MaNhanVien),HoatDong bit NOT NULL DEFAULT 1);
COMMIT;
GO
-- Bổ sung cột cho các bản dự án đã có dữ liệu.
IF COL_LENGTH('dbo.Phim','PosterPath') IS NULL ALTER TABLE dbo.Phim ADD PosterPath nvarchar(500) NULL;
IF COL_LENGTH('dbo.Phim','MoTa') IS NULL ALTER TABLE dbo.Phim ADD MoTa nvarchar(1000) NULL;
IF COL_LENGTH('dbo.Phim','DaoDien') IS NULL ALTER TABLE dbo.Phim ADD DaoDien nvarchar(100) NULL;
IF COL_LENGTH('dbo.Phim','DienVien') IS NULL ALTER TABLE dbo.Phim ADD DienVien nvarchar(500) NULL;
IF COL_LENGTH('dbo.Phim','GioiHanTuoi') IS NULL ALTER TABLE dbo.Phim ADD GioiHanTuoi nvarchar(20) NULL;
IF COL_LENGTH('dbo.Phim','NgayKhoiChieu') IS NULL ALTER TABLE dbo.Phim ADD NgayKhoiChieu date NULL;
IF COL_LENGTH('dbo.NhanVien','ChucVu') IS NULL ALTER TABLE dbo.NhanVien ADD ChucVu nvarchar(50) NULL;
IF COL_LENGTH('dbo.SuatChieu','GiaVe') IS NULL ALTER TABLE dbo.SuatChieu ADD GiaVe decimal(12,2) NOT NULL DEFAULT 100000;
IF COL_LENGTH('dbo.SuatChieu','TrangThai') IS NULL ALTER TABLE dbo.SuatChieu ADD TrangThai nvarchar(30) NOT NULL DEFAULT N'Đang Mở Bán';
IF COL_LENGTH('dbo.Ghe','ViTri') IS NULL ALTER TABLE dbo.Ghe ADD ViTri nvarchar(20) NULL;
IF COL_LENGTH('dbo.Ghe','LoaiGhe') IS NULL ALTER TABLE dbo.Ghe ADD LoaiGhe nvarchar(20) NOT NULL DEFAULT N'Thường';
IF COL_LENGTH('dbo.KhachHang','NgaySinh') IS NULL ALTER TABLE dbo.KhachHang ADD NgaySinh date NULL;
IF COL_LENGTH('dbo.KhachHang','GioiTinh') IS NULL ALTER TABLE dbo.KhachHang ADD GioiTinh nvarchar(10) NULL;
IF COL_LENGTH('dbo.KhachHang','DiemTichLuy') IS NULL ALTER TABLE dbo.KhachHang ADD DiemTichLuy int NOT NULL DEFAULT 0;
IF COL_LENGTH('dbo.KhuyenMai','TenKhuyenMai') IS NULL ALTER TABLE dbo.KhuyenMai ADD TenKhuyenMai nvarchar(100) NULL;
IF COL_LENGTH('dbo.KhuyenMai','NgayBatDau') IS NULL ALTER TABLE dbo.KhuyenMai ADD NgayBatDau date NULL;
IF COL_LENGTH('dbo.KhuyenMai','NgayKetThuc') IS NULL ALTER TABLE dbo.KhuyenMai ADD NgayKetThuc date NULL;
IF COL_LENGTH('dbo.HoaDon','PhuongThucThanhToan') IS NULL ALTER TABLE dbo.HoaDon ADD PhuongThucThanhToan nvarchar(50) NOT NULL DEFAULT N'Tiền mặt';
IF COL_LENGTH('dbo.Ve','MaHoaDon') IS NULL ALTER TABLE dbo.Ve ADD MaHoaDon varchar(10) NULL;
GO
-- Chỉ nạp dữ liệu minh họa khi chưa có nhân viên/phòng/phim.
IF NOT EXISTS(SELECT 1 FROM NhanVien)
INSERT INTO NhanVien(MaNhanVien,TenNhanVien,NgaySinh,GioiTinh,SoDienThoai,ChucVu) VALUES('NV001',N'Nhân viên mẫu','2000-01-01',N'Nam','0900000000',N'Nhân viên');
IF NOT EXISTS(SELECT 1 FROM PhongChieu)
INSERT INTO PhongChieu(MaPhongChieu,TenPhong,SucChua) VALUES('PC01',N'Phòng 1',0),('PC02',N'Phòng 2',0);
IF NOT EXISTS(SELECT 1 FROM Phim)
INSERT INTO Phim(MaPhim,TenPhim,TheLoai,ThoiLuong,TrangThai,PosterPath) VALUES('PH001',N'Avengers: Endgame',N'Hành động',180,N'Đang Chiếu','img/avengers.jpg'),('PH002',N'Frozen II',N'Hoạt hình',103,N'Đang Chiếu','img/frozen2.jpg');
GO
