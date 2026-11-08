-- Bổ sung theo sơ đồ lớp; giữ bảng/cột cũ để tương thích. Không DROP dữ liệu.
SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;
IF OBJECT_ID('dbo.LoaiPhim','U') IS NULL
CREATE TABLE dbo.LoaiPhim(MaLoaiPhim varchar(10) PRIMARY KEY,TenLoaiPhim nvarchar(50) NOT NULL UNIQUE,MoTa nvarchar(500));
IF COL_LENGTH('dbo.Phim','MaLoaiPhim') IS NULL ALTER TABLE dbo.Phim ADD MaLoaiPhim varchar(10) NULL REFERENCES dbo.LoaiPhim(MaLoaiPhim);
IF COL_LENGTH('dbo.PhongChieu','LoaiPhongChieu') IS NULL ALTER TABLE dbo.PhongChieu ADD LoaiPhongChieu nvarchar(30) NOT NULL DEFAULT N'2D';
IF COL_LENGTH('dbo.PhongChieu','TrangThai') IS NULL ALTER TABLE dbo.PhongChieu ADD TrangThai nvarchar(30) NOT NULL DEFAULT N'Hoạt động';
IF OBJECT_ID('dbo.ChiTietHoaDon','U') IS NULL
CREATE TABLE dbo.ChiTietHoaDon(MaHoaDon varchar(10) NOT NULL REFERENCES dbo.HoaDon(MaHoaDon),MaVe varchar(10) NOT NULL UNIQUE REFERENCES dbo.Ve(MaVe),SoLuong int NOT NULL DEFAULT 1 CHECK(SoLuong=1),DonGia decimal(12,2) NOT NULL CHECK(DonGia>=0),ThanhTien AS (SoLuong*DonGia) PERSISTED,PRIMARY KEY(MaHoaDon,MaVe));
GO
INSERT INTO LoaiPhim(MaLoaiPhim,TenLoaiPhim)
SELECT 'LP'+RIGHT('00000000'+CAST(ROW_NUMBER() OVER(ORDER BY TheLoai)+(SELECT COUNT(*) FROM LoaiPhim) AS varchar(8)),8),TheLoai
FROM (SELECT DISTINCT TheLoai FROM Phim WHERE NOT EXISTS(SELECT 1 FROM LoaiPhim l WHERE l.TenLoaiPhim=Phim.TheLoai)) q;
UPDATE p SET MaLoaiPhim=l.MaLoaiPhim FROM Phim p JOIN LoaiPhim l ON l.TenLoaiPhim=p.TheLoai WHERE p.MaLoaiPhim IS NULL;
INSERT INTO ChiTietHoaDon(MaHoaDon,MaVe,SoLuong,DonGia) SELECT MaHoaDon,MaVe,1,GiaTien FROM Ve WHERE MaHoaDon IS NOT NULL AND NOT EXISTS(SELECT 1 FROM ChiTietHoaDon c WHERE c.MaVe=Ve.MaVe);
