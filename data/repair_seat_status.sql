-- Sửa trạng thái bị mất dấu do literal SQL cũ thiếu tiền tố N.
-- Không mở lại ghế đã bán. Chạy sau guest_booking.sql.
USE QuanLyBanVePhimMoi;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
UPDATE gs SET TrangThai=N'Trống'
FROM Ghe_SuatChieu gs
WHERE gs.TrangThai IN (N'Tr?ng',N'Trong',N'Trông')
AND NOT EXISTS(SELECT 1 FROM Ve v WHERE v.MaSuatChieu=gs.MaSuatChieu AND v.MaGhe=gs.MaGhe)
AND NOT EXISTS(SELECT 1 FROM DatVe d JOIN ChiTietDatVe ct ON ct.MaDatVe=d.MaDatVe
 WHERE d.MaSuatChieu=gs.MaSuatChieu AND ct.MaGhe=gs.MaGhe AND d.TrangThai='CHO_THANH_TOAN' AND d.HetHan>GETDATE());
COMMIT;
GO
