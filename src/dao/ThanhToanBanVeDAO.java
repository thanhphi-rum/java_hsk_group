package dao;

import connect.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.NhanVien;
import entity.SuatChieu;
import entity.Ghe;
import entity.GheSuatChieu;
import entity.Ve;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import util.GiaVe;

/** Atomic checkout: lock seats, validate live prices/promotions, write invoice lines and loyalty points. */
public class ThanhToanBanVeDAO {
    /** Suất đã chiếu quá số phút này (quảng cáo, trailer) thì ngừng bán vé. */
    public static final int PHUT_TRE_TOI_DA = 15;


    private final HoaDonDAO hoaDonDAO=new HoaDonDAO();
    private final VeDAO veDAO=new VeDAO();
    private final ChiTietHoaDonDAO chiTietDAO=new ChiTietHoaDonDAO();

    public BigDecimal thanhToan(Connection conn,String maHoaDon,String maNhanVien,String maKhachHang,
            String maKhuyenMai,String maSuatChieu,String phuongThuc,List<String> maGheDaChon,
            Map<String,Integer> comboSoLuong,BigDecimal tongDuKien)throws SQLException {
        if(conn.getAutoCommit())throw new SQLException("Thanh toán phải chạy trong transaction");
        if(maGheDaChon==null||maGheDaChon.isEmpty())throw new SQLException("Chưa chọn ghế");
        BigDecimal giaCoBan;
        String trangThaiSuat;
        LocalDateTime gioBatDau;
        try(PreparedStatement p=conn.prepareStatement("SELECT GiaVe,TrangThai,GioBatDau FROM SuatChieu WITH (UPDLOCK,HOLDLOCK) WHERE MaSuatChieu=?")){
            p.setString(1,maSuatChieu);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next())throw new SQLException("Không tìm thấy suất chiếu");
                giaCoBan=r.getBigDecimal("GiaVe");
                trangThaiSuat=r.getString("TrangThai");
                gioBatDau=r.getTimestamp("GioBatDau").toLocalDateTime();
            }
        }
        String loiSuat=kiemTraSuat(trangThaiSuat,gioBatDau);
        if(loiSuat!=null)throw new SQLException(loiSuat);
        BigDecimal phanTram=BigDecimal.ZERO;
        KhuyenMai khuyenMaiApDung=null;
        if(maKhuyenMai!=null&&!maKhuyenMai.isBlank()){
            try(PreparedStatement p=conn.prepareStatement("SELECT PhanTramGiam,HinhThuc,SoComboTang FROM KhuyenMai WITH (UPDLOCK,HOLDLOCK) WHERE MaKhuyenMai=? AND "
                    + "TrangThai=N'Hoạt động' AND NgayBatDau<=CAST(GETDATE() AS date) AND "
                    + "NgayKetThuc>=CAST(GETDATE() AS date)")) {
                p.setString(1,maKhuyenMai);
                try(ResultSet r=p.executeQuery()) {
                    if(!r.next())throw new SQLException("Khuyến mãi đã hết hạn, chưa bắt đầu hoặc đã ngừng hoạt động");
                    phanTram=r.getBigDecimal("PhanTramGiam");
                    khuyenMaiApDung=new KhuyenMai(maKhuyenMai);
                    khuyenMaiApDung.setPhanTramGiam(phanTram.doubleValue());
                    khuyenMaiApDung.setHinhThuc(r.getString("HinhThuc"));
                    khuyenMaiApDung.setSoComboTang(r.getInt("SoComboTang"));
                    if(!Arrays.asList(KhuyenMai.DS_HINH_THUC).contains(khuyenMaiApDung.getHinhThuc())
                            ||(khuyenMaiApDung.laTangCombo()?khuyenMaiApDung.getSoComboTang()<=0
                            :phanTram.signum()<=0||phanTram.compareTo(new BigDecimal("100"))>0))
                        throw new SQLException("Khuyến mãi có mức ưu đãi hoặc hình thức không hợp lệ");
                }
            }
        }
        List<Ve> veTao=new ArrayList<>();
        BigDecimal tongVe=BigDecimal.ZERO;
        Set<String> uniqueSeats=new HashSet<>();
        for(String maGhe:maGheDaChon){
            if(!uniqueSeats.add(maGhe))throw new SQLException("Ghế bị lặp trong đơn");
            String loaiGhe;
            try(PreparedStatement p=conn.prepareStatement("SELECT g.LoaiGhe,gs.TrangThai FROM Ghe_SuatChieu gs WITH (UPDLOCK,HOLDLOCK) JOIN Ghe g ON "
                    + "g.MaGhe=gs.MaGhe WHERE gs.MaSuatChieu=? AND gs.MaGhe=?")) {
                p.setString(1,maSuatChieu);
                p.setString(2,maGhe);
                try(ResultSet r=p.executeQuery()) {
                    if(!r.next())throw new SQLException("Ghế không thuộc suất chiếu: "+maGhe);
                    if(!"Trống".equalsIgnoreCase(r.getString("TrangThai")))throw new SQLException("Ghế vừa được người khác giữ/bán: "+maGhe);
                    loaiGhe=r.getString("LoaiGhe");
                }
            }
            BigDecimal gia=GiaVe.tinh(giaCoBan,loaiGhe);
            tongVe=tongVe.add(gia);
            String maVe="V"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.ROOT);
            veTao.add(new Ve(maVe,new GheSuatChieu(new Ghe(maGhe),new SuatChieu(maSuatChieu),"Trống"),gia.doubleValue(),"Đã bán"));
        }
        Map<String,BigDecimal> comboGia=new LinkedHashMap<>();
        BigDecimal tongCombo=BigDecimal.ZERO;
        BigDecimal giaComboReNhat=null;
        int soCombo=0;
        if(comboSoLuong!=null)for(Map.Entry<String,Integer> e:comboSoLuong.entrySet()){
            if(e.getValue()==null||e.getValue()<=0)throw new SQLException("Số lượng combo không hợp lệ");
            try(PreparedStatement p=conn.prepareStatement("SELECT GiaBan FROM ComboBapNuoc WITH (UPDLOCK,HOLDLOCK) WHERE MaCombo=? AND TrangThai=N'Đang bán'")) {
                p.setString(1,e.getKey());
                try(ResultSet r=p.executeQuery()) {
                    if(!r.next())throw new SQLException("Combo không còn bán: "+e.getKey());
                    BigDecimal gia=r.getBigDecimal(1);
                    comboGia.put(e.getKey(),gia);
                    tongCombo=tongCombo.add(gia.multiply(BigDecimal.valueOf(e.getValue())));
                    if(giaComboReNhat==null||gia.compareTo(giaComboReNhat)<0)giaComboReNhat=gia;
                    try {soCombo=Math.addExact(soCombo,e.getValue());}
                    catch(ArithmeticException ex){throw new SQLException("Số lượng combo quá lớn",ex);}
                }
            }
        }
        BigDecimal tongSauGiam=khuyenMaiApDung==null?tongVe.add(tongCombo).setScale(2,RoundingMode.HALF_UP)
                :khuyenMaiApDung.tongSauGiam(tongVe,tongCombo,giaComboReNhat,soCombo);
        if(tongDuKien!=null&&tongDuKien.setScale(2,RoundingMode.HALF_UP).compareTo(tongSauGiam)!=0)throw new SQLException("Tổng tiền vừa thay đổi. Hãy kiểm tra lại trước khi thanh toán.");
        HoaDon h=new HoaDon(maHoaDon,new NhanVien(maNhanVien),maKhachHang==null||maKhachHang.isBlank()?null:new KhachHang(maKhachHang),khuyenMaiApDung,LocalDateTime.now(),tongSauGiam.doubleValue(),phuongThuc);
        if(!hoaDonDAO.themHoaDon(conn,h,phanTram,
                khuyenMaiApDung==null?KhuyenMai.HT_HOA_DON:khuyenMaiApDung.getHinhThuc(),
                khuyenMaiApDung==null?0:khuyenMaiApDung.getSoComboTang()))
            throw new SQLException("Không tạo được hóa đơn");
        for(Ve ve:veTao){
            if(!veDAO.themVe(conn,ve))throw new SQLException("Không tạo được vé");
            chiTietDAO.themVe(conn,maHoaDon,ve.getMaVe(),ve.getGiaTien());
            try(PreparedStatement p=conn.prepareStatement("UPDATE Ghe_SuatChieu SET TrangThai=N'Đã bán' WHERE MaSuatChieu=? AND MaGhe=? AND TrangThai=N'Trống'")) {
                p.setString(1,maSuatChieu);
                p.setString(2,ve.getGhe().getMaGhe());
                if(p.executeUpdate()!=1)throw new SQLException("Ghế vừa được giao dịch khác cập nhật: "+ve.getGhe().getMaGhe());
            }
        }
        if(comboSoLuong!=null)for(Map.Entry<String,Integer> e:comboSoLuong.entrySet())chiTietDAO.themCombo(conn,maHoaDon,e.getKey(),e.getValue(),comboGia.get(e.getKey()).doubleValue());
        try(PreparedStatement p=conn.prepareStatement("SELECT TongTien FROM vw_HoaDon_TongTien WHERE MaHoaDon=?")) {
            p.setString(1,maHoaDon);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next()||r.getBigDecimal(1).setScale(2,RoundingMode.HALF_UP).compareTo(tongSauGiam)!=0)throw new SQLException("Tổng hóa đơn không khớp chi tiết");
            }
        }
        if(maKhachHang!=null&&!maKhachHang.isBlank()){
            int diem=tongSauGiam.divideToIntegralValue(new BigDecimal("10000")).intValue();
            if(diem>0)try(PreparedStatement p=conn.prepareStatement("UPDATE KhachHang SET DiemTichLuy=DiemTichLuy+? WHERE MaKhachHang=?")) {
                p.setInt(1,diem);
                p.setString(2,maKhachHang);
                if(p.executeUpdate()!=1)throw new SQLException("Không cập nhật được điểm khách hàng");
            }
        }
        return tongSauGiam;
    }

    /** Luật bán theo suất: phải đang mở bán và chưa chiếu quá PHUT_TRE_TOI_DA phút. Trả về lý do không bán được, hoặc null. */
    static String kiemTraSuat(String trangThai,LocalDateTime gioBatDau){
        if(!"Đang Mở Bán".equalsIgnoreCase(trangThai))return "Suất chiếu không còn mở bán";
        if(LocalDateTime.now().isAfter(gioBatDau.plusMinutes(PHUT_TRE_TOI_DA)))return "Suất chiếu đã bắt đầu quá "+PHUT_TRE_TOI_DA+" phút, không bán thêm vé";
        return null;
    }

    /**
     * Kiểm tra nhanh (không khóa) TRƯỚC khi nhận tiền của khách, để khỏi thu tiền rồi mới biết không bán được.
     * Việc kiểm tra chính thức, có khóa, vẫn nằm trong thanhToan().
     * @return lý do không bán được, hoặc null nếu bán được
     */
    public String lyDoKhongBanDuoc(String maSuatChieu,List<String> maGhe){
        try(Connection c=ConnectDB.getConnection()){
            try(PreparedStatement p=c.prepareStatement("SELECT TrangThai,GioBatDau FROM SuatChieu WHERE MaSuatChieu=?")){
                p.setString(1,maSuatChieu);
                try(ResultSet r=p.executeQuery()){
                    if(!r.next())return "Không tìm thấy suất chiếu";
                    String loi=kiemTraSuat(r.getString(1),r.getTimestamp(2).toLocalDateTime());
                    if(loi!=null)return loi;
                }
            }
            try(PreparedStatement p=c.prepareStatement("SELECT TrangThai FROM Ghe_SuatChieu WHERE MaSuatChieu=? AND MaGhe=?")){
                for(String ghe:maGhe){
                    p.setString(1,maSuatChieu);
                    p.setString(2,ghe);
                    try(ResultSet r=p.executeQuery()){
                        if(!r.next()||!"Trống".equalsIgnoreCase(r.getString(1)))return "Có ghế vừa được bán ở quầy khác";
                    }
                }
            }
        }catch(SQLException e){return "Không kiểm tra được suất chiếu: "+e.getMessage();}
        return null;
    }

    /** Giờ lập hóa đơn đúng như đã lưu trong database (để in hóa đơn khớp với dữ liệu). */
    public LocalDateTime layNgayLap(String maHoaDon){
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement("SELECT NgayLap FROM HoaDon WHERE MaHoaDon=?")){
            p.setString(1,maHoaDon);
            try(ResultSet r=p.executeQuery()){if(r.next())return r.getTimestamp(1).toLocalDateTime();}
        }catch(SQLException e){e.printStackTrace();}
        return LocalDateTime.now();
    }
}
