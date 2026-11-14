package dao;

import connect.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.NhanVien;
import entity.SuatChieu;
import entity.Ghe;
import entity.Ve;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Atomic checkout: lock seats, validate live prices/promotions, write invoice lines and loyalty points. */
public class ThanhToanBanVeDAO {
    private final HoaDonDAO hoaDonDAO=new HoaDonDAO();
    private final VeDAO veDAO=new VeDAO();
    private final ChiTietHoaDonDAO chiTietDAO=new ChiTietHoaDonDAO();

    public BigDecimal thanhToan(Connection conn,String maHoaDon,String maNhanVien,String maKhachHang,
            String maKhuyenMai,String maSuatChieu,String phuongThuc,List<String> maGheDaChon,
            Map<String,Integer> comboSoLuong,BigDecimal tongDuKien)throws SQLException {
        if(maGheDaChon==null||maGheDaChon.isEmpty())throw new SQLException("Chưa chọn ghế");
        BigDecimal giaCoBan; String trangThaiSuat;
        try(PreparedStatement p=conn.prepareStatement("SELECT GiaVe,TrangThai FROM SuatChieu WITH (UPDLOCK,HOLDLOCK) WHERE MaSuatChieu=?")){
            p.setString(1,maSuatChieu);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Không tìm thấy suất chiếu");giaCoBan=r.getBigDecimal("GiaVe");trangThaiSuat=r.getString("TrangThai");}
        }
        if(!"Đang Mở Bán".equalsIgnoreCase(trangThaiSuat))throw new SQLException("Suất chiếu không còn mở bán");
        BigDecimal phanTram=BigDecimal.ZERO;
        if(maKhuyenMai!=null&&!maKhuyenMai.isBlank()){
            try(PreparedStatement p=conn.prepareStatement("SELECT PhanTramGiam FROM KhuyenMai WITH (UPDLOCK,HOLDLOCK) WHERE MaKhuyenMai=? AND TrangThai=N'Hoạt động' AND NgayBatDau<=CAST(GETDATE() AS date) AND NgayKetThuc>=CAST(GETDATE() AS date)")){
                p.setString(1,maKhuyenMai);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Khuyến mãi đã hết hạn, chưa bắt đầu hoặc đã ngừng hoạt động");phanTram=r.getBigDecimal(1);}
            }
        }
        List<Ve> veTao=new ArrayList<>();BigDecimal tongGoc=BigDecimal.ZERO;
        Set<String> uniqueSeats=new HashSet<>();
        for(String maGhe:maGheDaChon){
            if(!uniqueSeats.add(maGhe))throw new SQLException("Ghế bị lặp trong đơn");
            String loaiGhe;
            try(PreparedStatement p=conn.prepareStatement("SELECT g.LoaiGhe,gs.TrangThai FROM Ghe_SuatChieu gs WITH (UPDLOCK,HOLDLOCK) JOIN Ghe g ON g.MaGhe=gs.MaGhe WHERE gs.MaSuatChieu=? AND gs.MaGhe=?")){
                p.setString(1,maSuatChieu);p.setString(2,maGhe);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Ghế không thuộc suất chiếu: "+maGhe);if(!"Trống".equalsIgnoreCase(r.getString("TrangThai")))throw new SQLException("Ghế vừa được người khác giữ/bán: "+maGhe);loaiGhe=r.getString("LoaiGhe");}
            }
            BigDecimal gia=giaCoBan;
            if(loaiGhe!=null&&loaiGhe.toLowerCase(Locale.ROOT).contains("vip"))gia=gia.multiply(new BigDecimal("1.20"));
            gia=gia.setScale(2,RoundingMode.HALF_UP);tongGoc=tongGoc.add(gia);
            String maVe="V"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.ROOT);
            veTao.add(new Ve(maVe,new SuatChieu(maSuatChieu),new Ghe(maGhe),new HoaDon(maHoaDon),gia.doubleValue()));
        }
        Map<String,BigDecimal> comboGia=new LinkedHashMap<>();
        if(comboSoLuong!=null)for(Map.Entry<String,Integer> e:comboSoLuong.entrySet()){
            if(e.getValue()==null||e.getValue()<=0)throw new SQLException("Số lượng combo không hợp lệ");
            try(PreparedStatement p=conn.prepareStatement("SELECT GiaBan FROM ComboBapNuoc WITH (UPDLOCK,HOLDLOCK) WHERE MaCombo=? AND TrangThai=N'Đang bán'")){
                p.setString(1,e.getKey());try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Combo không còn bán: "+e.getKey());BigDecimal gia=r.getBigDecimal(1);comboGia.put(e.getKey(),gia);tongGoc=tongGoc.add(gia.multiply(BigDecimal.valueOf(e.getValue())));}
            }
        }
        BigDecimal tongSauGiam=tongGoc.multiply(BigDecimal.ONE.subtract(phanTram.movePointLeft(2))).setScale(2,RoundingMode.HALF_UP);
        if(tongDuKien!=null&&tongDuKien.setScale(2,RoundingMode.HALF_UP).compareTo(tongSauGiam)!=0)throw new SQLException("Tổng tiền vừa thay đổi. Hãy kiểm tra lại trước khi thanh toán.");
        HoaDon h=new HoaDon(maHoaDon,new NhanVien(maNhanVien),maKhachHang==null||maKhachHang.isBlank()?null:new KhachHang(maKhachHang),maKhuyenMai==null||maKhuyenMai.isBlank()?null:new KhuyenMai(maKhuyenMai),LocalDateTime.now(),tongSauGiam.doubleValue(),phuongThuc);
        hoaDonDAO.themHoaDon(conn,h);
        for(Ve ve:veTao){
            if(!veDAO.themVe(conn,ve))throw new SQLException("Không tạo được vé");
            chiTietDAO.themVe(conn,maHoaDon,ve.getMaVe(),ve.getGiaTien());
            try(PreparedStatement p=conn.prepareStatement("UPDATE Ghe_SuatChieu SET TrangThai=N'Đã bán' WHERE MaSuatChieu=? AND MaGhe=? AND TrangThai=N'Trống'")){p.setString(1,maSuatChieu);p.setString(2,ve.getGhe().getMaGhe());if(p.executeUpdate()!=1)throw new SQLException("Ghế vừa được giao dịch khác cập nhật: "+ve.getGhe().getMaGhe());}
        }
        if(comboSoLuong!=null)for(Map.Entry<String,Integer> e:comboSoLuong.entrySet())chiTietDAO.themCombo(conn,maHoaDon,e.getKey(),e.getValue(),comboGia.get(e.getKey()).doubleValue());
        try(PreparedStatement p=conn.prepareStatement("SELECT TongTien FROM vw_HoaDon_TongTien WHERE MaHoaDon=?")){p.setString(1,maHoaDon);try(ResultSet r=p.executeQuery()){if(!r.next()||r.getBigDecimal(1).setScale(2,RoundingMode.HALF_UP).compareTo(tongSauGiam)!=0)throw new SQLException("Tổng hóa đơn không khớp chi tiết");}}
        if(maKhachHang!=null&&!maKhachHang.isBlank()){
            int diem=tongSauGiam.divideToIntegralValue(new BigDecimal("10000")).intValue();
            if(diem>0)try(PreparedStatement p=conn.prepareStatement("UPDATE KhachHang SET DiemTichLuy=DiemTichLuy+? WHERE MaKhachHang=?")){p.setInt(1,diem);p.setString(2,maKhachHang);if(p.executeUpdate()!=1)throw new SQLException("Không cập nhật được điểm khách hàng");}
        }
        return tongSauGiam;
    }
}
