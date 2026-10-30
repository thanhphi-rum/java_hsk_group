
-- =====================================================
-- DATABASE QUAN LY BAN VE PHIM
-- Tat ca ma chinh va ma ngoai dung VARCHAR
-- MySQL 8.0+
-- =====================================================

CREATE DATABASE QuanLyBanVePhimMoi

USE QuanLyBanVePhimMoi;

-- =====================================================
-- 1. NHAN VIEN
-- =====================================================
CREATE TABLE NhanVien (
    MaNhanVien VARCHAR(10) PRIMARY KEY,
    TenNhanVien VARCHAR(100) NOT NULL,
    NgaySinh DATE NOT NULL,
    GioiTinh VARCHAR(10) NOT NULL,
    SoDienThoai VARCHAR(15) NOT NULL,
    Email VARCHAR(100),
    DiaChi VARCHAR(200)
);

-- =====================================================
-- 2. PHIM
-- =====================================================
CREATE TABLE Phim (
    MaPhim VARCHAR(10) PRIMARY KEY,
    TenPhim VARCHAR(100) NOT NULL,
    TheLoai VARCHAR(50) NOT NULL,
    ThoiLuong INT NOT NULL,
    TrangThai VARCHAR(20) NOT NULL
);

-- =====================================================
-- 3. PHONG CHIEU
-- =====================================================
CREATE TABLE PhongChieu (
    MaPhongChieu VARCHAR(10) PRIMARY KEY,
    TenPhong VARCHAR(50) NOT NULL,
    SucChua INT NOT NULL
);

-- =====================================================
-- 4. SUAT CHIEU
-- =====================================================
CREATE TABLE SuatChieu (
    MaSuatChieu VARCHAR(10) PRIMARY KEY,
    MaPhim VARCHAR(10) NOT NULL,
    MaPhongChieu VARCHAR(10) NOT NULL,
    GioBatDau DATETIME NOT NULL,
    GioKetThuc DATETIME NOT NULL,

    FOREIGN KEY (MaPhim)
        REFERENCES Phim(MaPhim),

    FOREIGN KEY (MaPhongChieu)
        REFERENCES PhongChieu(MaPhongChieu)
);

-- =====================================================
-- 5. GHE
-- =====================================================
CREATE TABLE Ghe (
    MaGhe VARCHAR(10) PRIMARY KEY,
    MaPhongChieu VARCHAR(10) NOT NULL,

    FOREIGN KEY (MaPhongChieu)
        REFERENCES PhongChieu(MaPhongChieu)
);

-- =====================================================
-- 6. GHE SUAT CHIEU
-- =====================================================
CREATE TABLE Ghe_SuatChieu (
    MaSuatChieu VARCHAR(10) NOT NULL,
    MaGhe VARCHAR(10) NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'Trống',

    PRIMARY KEY (MaSuatChieu, MaGhe),

    FOREIGN KEY (MaSuatChieu)
        REFERENCES SuatChieu(MaSuatChieu),

    FOREIGN KEY (MaGhe)
        REFERENCES Ghe(MaGhe)
);

-- =====================================================
-- 7. KHACH HANG
-- =====================================================
CREATE TABLE KhachHang (
    MaKhachHang VARCHAR(10) PRIMARY KEY,
    TenKhachHang VARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(15) NOT NULL,
    Email VARCHAR(100) NOT NULL
);

-- =====================================================
-- 8. KHUYEN MAI
-- =====================================================
CREATE TABLE KhuyenMai (
    MaKhuyenMai VARCHAR(10) PRIMARY KEY,
    MoTa VARCHAR(200) NOT NULL,
    PhanTramGiam DECIMAL(5,2) NOT NULL
);

-- =====================================================
-- 9. HOA DON
-- MaHoaDon khong dung AUTO_INCREMENT vi la VARCHAR
-- =====================================================
CREATE TABLE HoaDon (
    MaHoaDon VARCHAR(10) PRIMARY KEY,
    MaSuatChieu VARCHAR(10) NOT NULL,
    MaGhe VARCHAR(10) NOT NULL,
    NgayLap DATETIME NOT NULL,
    MaNhanVien VARCHAR(10) NOT NULL,
    MaKhachHang VARCHAR(10) NOT NULL,
    TongTien DECIMAL(10,2) NOT NULL,
    MaKhuyenMai VARCHAR(10),

    FOREIGN KEY (MaNhanVien)
        REFERENCES NhanVien(MaNhanVien),

    FOREIGN KEY (MaGhe)
        REFERENCES Ghe(MaGhe),

    FOREIGN KEY (MaKhachHang)
        REFERENCES KhachHang(MaKhachHang),

    FOREIGN KEY (MaKhuyenMai)
        REFERENCES KhuyenMai(MaKhuyenMai),

    FOREIGN KEY (MaSuatChieu)
        REFERENCES SuatChieu(MaSuatChieu)
);

-- =====================================================
-- 10. VE
-- =====================================================
CREATE TABLE Ve (
    MaVe VARCHAR(10) PRIMARY KEY,
    MaSuatChieu VARCHAR(10) NOT NULL,
    MaGhe VARCHAR(10) NOT NULL,
    GiaTien DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (MaSuatChieu)
        REFERENCES SuatChieu(MaSuatChieu)
        ON DELETE CASCADE,

    FOREIGN KEY (MaGhe)
        REFERENCES Ghe(MaGhe)
        ON DELETE CASCADE
);

-- =====================================================
-- DU LIEU MAU
-- =====================================================

-- NhanVien
INSERT INTO NhanVien VALUES
('NV001', 'Nguyễn Văn A', '1985-02-15', 'Nam',
 '0912345678', 'a@gmail.com', 'HCM'),
('NV002', 'Trần Thị B', '1990-07-22', 'Nữ',
 '0987654321', 'b@gmail.com', 'HCM');

-- Phim
INSERT INTO Phim VALUES
('PH001', 'Avengers: Endgame', 'Hành động', 180, 'Đang chiếu'),
('PH002', 'Frozen II', 'Hoạt hình', 103, 'Đang chiếu');

-- PhongChieu
INSERT INTO PhongChieu VALUES
('PC01', 'Phòng 1', 50),
('PC02', 'Phòng 2', 50),
('PC03', 'Phòng 3', 50);

-- Ghe: G001-G050 thuoc PC01
--      G051-G100 thuoc PC02
--      G101-G150 thuoc PC03
INSERT INTO Ghe (MaGhe, MaPhongChieu)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 150
)
SELECT
    CONCAT('G', LPAD(n, 3, '0')),
    CASE
        WHEN n <= 50 THEN 'PC01'
        WHEN n <= 100 THEN 'PC02'
        ELSE 'PC03'
    END
FROM seq;

-- KhachHang
INSERT INTO KhachHang VALUES
('KH001', 'Phạm Minh D', '0901234567', 'd@gmail.com');

-- KhuyenMai
INSERT INTO KhuyenMai VALUES
('KM001', 'Không giảm', 0),
('KM002', 'Giảm 10%', 10);

-- SuatChieu
-- Ngay chieu duoc giu theo du lieu mau ban gui
INSERT INTO SuatChieu VALUES
('SC001', 'PH001', 'PC01',
 '2025-12-01 19:00:00', '2025-12-01 22:00:00'),
('SC002', 'PH002', 'PC02',
 '2025-12-01 20:00:00', '2025-12-01 21:45:00');

-- Ghe_SuatChieu
-- SC001 su dung G001-G050
-- SC002 su dung G051-G100
INSERT INTO Ghe_SuatChieu (MaSuatChieu, MaGhe, TrangThai)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    CASE
        WHEN n <= 50 THEN 'SC001'
        ELSE 'SC002'
    END,
    CONCAT('G', LPAD(n, 3, '0')),
    'Trống'
FROM seq;

-- Kiem tra bang va du lieu
SHOW TABLES;

SELECT * FROM NhanVien;
SELECT * FROM Phim;
SELECT * FROM PhongChieu;
SELECT * FROM SuatChieu;
SELECT * FROM Ghe;
SELECT * FROM Ghe_SuatChieu;
SELECT * FROM KhachHang;
SELECT * FROM KhuyenMai;
SELECT * FROM HoaDon;
SELECT * FROM Ve;
