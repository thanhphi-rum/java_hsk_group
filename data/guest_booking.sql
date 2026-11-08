USE QuanLyBanVePhimMoi;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID('dbo.DatVe','U') IS NULL
CREATE TABLE dbo.DatVe (
 MaDatVe varchar(16) PRIMARY KEY,
 MaSuatChieu varchar(10) NOT NULL REFERENCES dbo.SuatChieu(MaSuatChieu),
 NgayDat datetime NOT NULL DEFAULT GETDATE(),
 HetHan datetime NOT NULL,
 GiaVe decimal(12,2) NOT NULL CHECK(GiaVe>=0),
 TrangThai varchar(20) NOT NULL DEFAULT 'CHO_THANH_TOAN',
 MaHoaDon varchar(10) NULL REFERENCES dbo.HoaDon(MaHoaDon)
);
IF OBJECT_ID('dbo.ChiTietDatVe','U') IS NULL
CREATE TABLE dbo.ChiTietDatVe (
 MaDatVe varchar(16) NOT NULL REFERENCES dbo.DatVe(MaDatVe),
 MaGhe varchar(10) NOT NULL REFERENCES dbo.Ghe(MaGhe),
 PRIMARY KEY(MaDatVe,MaGhe)
);
COMMIT;
GO
