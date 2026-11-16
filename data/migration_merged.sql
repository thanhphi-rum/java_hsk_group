:ON ERROR EXIT
-- Nang cap ca ban v6 va ban java_hsk_group, khong reset database.
USE QuanLyBanVePhimMoi;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO
IF COL_LENGTH('KhuyenMai','HinhThuc') IS NULL
    ALTER TABLE KhuyenMai ADD HinhThuc NVARCHAR(30) NOT NULL CONSTRAINT DF_KM_HinhThuc_Merged DEFAULT N'Giảm hóa đơn';
IF COL_LENGTH('KhuyenMai','SoComboTang') IS NULL
    ALTER TABLE KhuyenMai ADD SoComboTang INT NOT NULL CONSTRAINT DF_KM_Combo_Merged DEFAULT 0;
IF COL_LENGTH('HoaDon','TrangThai') IS NULL
    ALTER TABLE HoaDon ADD TrangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_HD_TrangThai_Merged DEFAULT N'Đã thanh toán';
IF COL_LENGTH('HoaDon','GhiChuHuy') IS NULL
    ALTER TABLE HoaDon ADD GhiChuHuy NVARCHAR(300) NULL;
GO
IF COL_LENGTH('HoaDon','PhanTramGiamApDung') IS NULL
BEGIN
    ALTER TABLE HoaDon ADD PhanTramGiamApDung DECIMAL(5,2) NOT NULL CONSTRAINT DF_HD_PhanTram_Merged DEFAULT 0;
    EXEC(N'UPDATE h SET PhanTramGiamApDung=ISNULL(k.PhanTramGiam,0) FROM HoaDon h LEFT JOIN KhuyenMai k ON k.MaKhuyenMai=h.MaKhuyenMai');
END
GO
IF COL_LENGTH('HoaDon','HinhThucApDung') IS NULL
BEGIN
    ALTER TABLE HoaDon ADD HinhThucApDung NVARCHAR(30) NOT NULL CONSTRAINT DF_HD_HinhThuc_Merged DEFAULT N'Giảm hóa đơn';
    EXEC(N'UPDATE h SET HinhThucApDung=ISNULL(k.HinhThuc,N''Giảm hóa đơn'') FROM HoaDon h LEFT JOIN KhuyenMai k ON k.MaKhuyenMai=h.MaKhuyenMai');
END
GO
IF COL_LENGTH('HoaDon','SoComboTangApDung') IS NULL
BEGIN
    ALTER TABLE HoaDon ADD SoComboTangApDung INT NOT NULL CONSTRAINT DF_HD_Combo_Merged DEFAULT 0;
    EXEC(N'UPDATE h SET SoComboTangApDung=ISNULL(k.SoComboTang,0) FROM HoaDon h LEFT JOIN KhuyenMai k ON k.MaKhuyenMai=h.MaKhuyenMai');
END
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name='CK_HD_Combo_Merged')
    ALTER TABLE HoaDon ADD CONSTRAINT CK_HD_Combo_Merged CHECK (SoComboTangApDung>=0);
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name='CK_HD_Pct_Merged')
    ALTER TABLE HoaDon ADD CONSTRAINT CK_HD_Pct_Merged CHECK (PhanTramGiamApDung BETWEEN 0 AND 100);
GO
CREATE OR ALTER VIEW vw_HoaDon_TongTien AS
WITH Tong AS (
    SELECT h.MaHoaDon,h.NgayLap,h.MaNhanVien,h.MaKhachHang,h.MaKhuyenMai,h.PhuongThucThanhToan,
           h.MaPhieuDatVe,h.TrangThai,h.PhanTramGiamApDung,h.HinhThucApDung,h.SoComboTangApDung,
           ISNULL(SUM(CASE WHEN ct.MaVe IS NOT NULL THEN ct.SoLuong*ct.DonGia ELSE 0 END),0) TienVe,
           ISNULL(SUM(CASE WHEN ct.MaCombo IS NOT NULL THEN ct.SoLuong*ct.DonGia ELSE 0 END),0) TienCombo,
           ISNULL(MIN(CASE WHEN ct.MaCombo IS NOT NULL THEN ct.DonGia END),0) GiaComboMin,
           ISNULL(SUM(CASE WHEN ct.MaCombo IS NOT NULL THEN ct.SoLuong ELSE 0 END),0) SoCombo
    FROM HoaDon h LEFT JOIN ChiTietHoaDon ct ON ct.MaHoaDon=h.MaHoaDon
    GROUP BY h.MaHoaDon,h.NgayLap,h.MaNhanVien,h.MaKhachHang,h.MaKhuyenMai,h.PhuongThucThanhToan,
             h.MaPhieuDatVe,h.TrangThai,h.PhanTramGiamApDung,h.HinhThucApDung,h.SoComboTangApDung
), Giam AS (
    SELECT *, CASE WHEN HinhThucApDung IN (N'Giảm giá vé',N'Giảm hóa đơn')
                   THEN TienVe*PhanTramGiamApDung/100.0 ELSE 0 END GiamVe,
              CASE WHEN HinhThucApDung IN (N'Giảm giá combo',N'Giảm hóa đơn') THEN TienCombo*PhanTramGiamApDung/100.0
                   WHEN HinhThucApDung=N'Tặng combo' THEN GiaComboMin*CASE WHEN SoCombo<SoComboTangApDung THEN SoCombo ELSE SoComboTangApDung END
                   ELSE 0 END GiamCombo
    FROM Tong
)
SELECT MaHoaDon,NgayLap,MaNhanVien,MaKhachHang,MaKhuyenMai,PhuongThucThanhToan,MaPhieuDatVe,TrangThai,
       CAST(TienVe+TienCombo AS DECIMAL(18,2)) TongTienGoc,
       CAST(TienVe+TienCombo-GiamVe-GiamCombo AS DECIMAL(18,2)) TongTien,
       CAST(TienVe-GiamVe AS DECIMAL(18,2)) DoanhThuVe,
       CAST(TienCombo-GiamCombo AS DECIMAL(18,2)) DoanhThuCombo
FROM Giam;
GO

/* ============================================================
   NÂNG CẤP CSDL QuanLyBanVePhimMoi (chạy trên CSDL đang dùng, KHÔNG xóa dữ liệu)
   - Tạo bảng TheLoai (20 thể loại TL001-TL020) và bảng Phim_TheLoai (Phim - TheLoai)
   - Chuyển thể loại cũ của từng phim (cột Phim.MaLoaiPhim, bảng LoaiPhim) sang Phim_TheLoai
   - Xóa cột Phim.MaLoaiPhim và bảng LoaiPhim
   Chạy lại nhiều lần vẫn an toàn.
   ============================================================ */
USE QuanLyBanVePhimMoi;
GO

-- 1. Bảng TheLoai
IF OBJECT_ID('TheLoai', 'U') IS NULL
    CREATE TABLE TheLoai (
        MaTheLoai  VARCHAR(20)   NOT NULL,
        TenTheLoai NVARCHAR(100) NOT NULL,
        MoTa       NVARCHAR(200) NULL,
        CONSTRAINT PK_TheLoai PRIMARY KEY (MaTheLoai),
        CONSTRAINT UQ_TheLoai_Ten UNIQUE (TenTheLoai)
    );
GO

-- 2. 20 thể loại (bỏ qua thể loại đã có)
INSERT INTO TheLoai (MaTheLoai, TenTheLoai, MoTa)
SELECT v.Ma, v.Ten, v.MoTa
FROM (VALUES
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
    ('TL020', N'Lịch sử', N'Phim lịch sử')
) AS v (Ma, Ten, MoTa)
WHERE NOT EXISTS (SELECT 1 FROM TheLoai t WHERE t.MaTheLoai = v.Ma OR t.TenTheLoai = v.Ten);
GO

-- 3. Bảng Phim_TheLoai (một phim có thể có nhiều thể loại)
IF OBJECT_ID('Phim_TheLoai', 'U') IS NULL
    CREATE TABLE Phim_TheLoai (
        MaPhim    VARCHAR(20) NOT NULL,
        MaTheLoai VARCHAR(20) NOT NULL,
        CONSTRAINT PK_Phim_TheLoai PRIMARY KEY (MaPhim, MaTheLoai),
        CONSTRAINT FK_PhimTheLoai_Phim FOREIGN KEY (MaPhim) REFERENCES Phim(MaPhim) ON DELETE CASCADE,
        CONSTRAINT FK_PhimTheLoai_TheLoai FOREIGN KEY (MaTheLoai) REFERENCES TheLoai(MaTheLoai)
    );
GO

-- 4. Chuyển dữ liệu từ LoaiPhim cũ (chỉ chạy khi Phim còn cột MaLoaiPhim)
IF COL_LENGTH('Phim', 'MaLoaiPhim') IS NOT NULL AND OBJECT_ID('LoaiPhim', 'U') IS NOT NULL
BEGIN
    -- 4a. Loại phim cũ chưa trùng tên với 20 thể loại trên -> thêm thành thể loại mới (TL021, TL022, ...)
    EXEC(N'
        DECLARE @n INT = (SELECT COUNT(*) FROM TheLoai);
        INSERT INTO TheLoai (MaTheLoai, TenTheLoai, MoTa)
        SELECT ''TL'' + RIGHT(''000'' + CAST(@n + ROW_NUMBER() OVER (ORDER BY l.MaLoaiPhim) AS VARCHAR(10)), 3),
               l.TenLoaiPhim, l.MoTa
        FROM LoaiPhim l
        WHERE NOT EXISTS (SELECT 1 FROM TheLoai t WHERE t.TenTheLoai = l.TenLoaiPhim COLLATE Vietnamese_CI_AS);
    ');
    -- 4b. Gán thể loại cho từng phim theo loại phim cũ
    EXEC(N'
        INSERT INTO Phim_TheLoai (MaPhim, MaTheLoai)
        SELECT p.MaPhim, t.MaTheLoai
        FROM Phim p
        JOIN LoaiPhim l ON l.MaLoaiPhim = p.MaLoaiPhim
        JOIN TheLoai t ON t.TenTheLoai = l.TenLoaiPhim COLLATE Vietnamese_CI_AS
        WHERE NOT EXISTS (SELECT 1 FROM Phim_TheLoai x WHERE x.MaPhim = p.MaPhim AND x.MaTheLoai = t.MaTheLoai);
    ');
END
GO

-- 5. Bỏ khóa ngoại + cột Phim.MaLoaiPhim, xóa bảng LoaiPhim
IF COL_LENGTH('Phim', 'MaLoaiPhim') IS NOT NULL
BEGIN
    DECLARE @fk SYSNAME;
    SELECT @fk = fk.name
    FROM sys.foreign_keys fk
    JOIN sys.foreign_key_columns fc ON fc.constraint_object_id = fk.object_id
    WHERE fk.parent_object_id = OBJECT_ID('Phim')
      AND COL_NAME(fc.parent_object_id, fc.parent_column_id) = 'MaLoaiPhim';
    IF @fk IS NOT NULL EXEC('ALTER TABLE Phim DROP CONSTRAINT ' + @fk);
    ALTER TABLE Phim DROP COLUMN MaLoaiPhim;
END
GO
IF OBJECT_ID('LoaiPhim', 'U') IS NOT NULL DROP TABLE LoaiPhim;
GO

-- Kiểm tra nhanh
SELECT p.MaPhim, p.TenPhim, t.MaTheLoai, t.TenTheLoai
FROM Phim p
LEFT JOIN Phim_TheLoai pt ON pt.MaPhim = p.MaPhim
LEFT JOIN TheLoai t ON t.MaTheLoai = pt.MaTheLoai
ORDER BY p.MaPhim;
GO
