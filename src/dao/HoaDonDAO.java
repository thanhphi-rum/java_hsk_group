package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import connect.ConnectDB;
import entity.*;

/** Invoice persistence. Totals are always derived from invoice details. */
public class HoaDonDAO {
    private static final String SELECT="SELECT "
            + "h.MaHoaDon,h.MaNhanVien,h.MaKhachHang,h.MaKhuyenMai,h.PhanTramGiamApDung,h.HinhThucApDung,h.SoComboTangApDung,h.NgayLap,h.PhuongThucThanhToan,h.MaPhieuDatVe,h.TrangThai,ISNULL(v.TongTien,0) "
            + "TongTien FROM HoaDon h LEFT JOIN vw_HoaDon_TongTien v ON v.MaHoaDon=h.MaHoaDon";
    private HoaDon taoHoaDon(ResultSet rs)throws SQLException{
        String kh=rs.getString("MaKhachHang"),km=rs.getString("MaKhuyenMai");
        Timestamp ts=rs.getTimestamp("NgayLap");
        HoaDon h=new HoaDon(rs.getString("MaHoaDon"),new NhanVien(rs.getString("MaNhanVien")),kh==null?null:new KhachHang(kh),km==null?null:new KhuyenMai(km),ts==null?null:ts.toLocalDateTime(),rs.getDouble("TongTien"),rs.getString("PhuongThucThanhToan"));
        if(h.getKhuyenMai()!=null){
            h.getKhuyenMai().setPhanTramGiam(rs.getDouble("PhanTramGiamApDung"));
            h.getKhuyenMai().setHinhThuc(rs.getString("HinhThucApDung"));
            h.getKhuyenMai().setSoComboTang(rs.getInt("SoComboTangApDung"));
        }
        String pdv=rs.getString("MaPhieuDatVe");
        if(pdv!=null)h.setPhieuDatVe(new PhieuDatVe(pdv));
        h.setTrangThai(rs.getString("TrangThai"));
        return h;
    }
    /** Read the applied promotion once, then persist its immutable invoice snapshot. */
    public boolean themHoaDon(Connection c,HoaDon h)throws SQLException{
        double phanTram=0;
        String hinhThuc=KhuyenMai.HT_HOA_DON;
        int soComboTang=0;
        if(h.getKhuyenMai()!=null){
            try(PreparedStatement p=c.prepareStatement("SELECT PhanTramGiam,HinhThuc,SoComboTang FROM KhuyenMai WITH (UPDLOCK,HOLDLOCK) "
                    + "WHERE MaKhuyenMai=? AND TrangThai=N'Hoạt động' AND NgayBatDau<=CAST(GETDATE() AS date) "
                    + "AND NgayKetThuc>=CAST(GETDATE() AS date)")){
                p.setString(1,h.getKhuyenMai().getMaKhuyenMai());
                try(ResultSet r=p.executeQuery()){
                    if(!r.next())throw new SQLException("Khuyến mãi không còn hiệu lực");
                    phanTram=r.getDouble("PhanTramGiam");
                    hinhThuc=r.getString("HinhThuc");
                    soComboTang=r.getInt("SoComboTang");
                }
            }
        }
        return themHoaDon(c,h,phanTram,hinhThuc,soComboTang);
    }

    /** Store the exact promotion terms validated while the checkout transaction holds its locks. */
    public boolean themHoaDon(Connection c,HoaDon h,double phanTram,String hinhThuc,int soComboTang)throws SQLException{
        if(phanTram<0||phanTram>100
                ||!java.util.Arrays.asList(KhuyenMai.DS_HINH_THUC).contains(hinhThuc)||soComboTang<0)
            throw new SQLException("Điều kiện khuyến mãi áp dụng không hợp lệ");
        String sql="INSERT INTO HoaDon(MaHoaDon,MaNhanVien,MaKhachHang,MaKhuyenMai,NgayLap,PhuongThucThanhToan,"
                + "MaPhieuDatVe,TrangThai,PhanTramGiamApDung,HinhThucApDung,SoComboTangApDung) "
                + "VALUES(?,?,?,?,?,?,?,N'Đã thanh toán',?,?,?)";
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,h.getMaHoaDon());
            p.setString(2,h.getNhanVien().getMaNhanVien());
            p.setString(3,h.getKhachHang()==null?null:h.getKhachHang().getMaKhachHang());
            p.setString(4,h.getKhuyenMai()==null?null:h.getKhuyenMai().getMaKhuyenMai());
            p.setTimestamp(5,Timestamp.valueOf(h.getNgayLap()==null?LocalDateTime.now():h.getNgayLap()));
            p.setString(6,h.getPhongThucThanhToan()==null?"Tiền mặt":h.getPhongThucThanhToan());
            p.setString(7,h.getPhieuDatVe()==null?null:h.getPhieuDatVe().getMaPhieuDatVe());
            p.setDouble(8,phanTram);
            p.setString(9,hinhThuc);
            p.setInt(10,soComboTang);
            return p.executeUpdate()==1;
        }
    }
    public double layTongPhieuChoThanhToan(String maPhieu) throws SQLException {
        String sql="SELECT p.TrangThai TrangThaiPhieu,d.DonGia,v.TrangThai TrangThaiVe,gs.TrangThai TrangThaiGhe,"
                + "sc.TrangThai TrangThaiSuat,sc.GioBatDau FROM PhieuDatVe p "
                + "JOIN ChiTietPhieuDatVe d ON d.MaPhieuDatVe=p.MaPhieuDatVe JOIN Ve v ON v.MaVe=d.MaVe "
                + "JOIN Ghe_SuatChieu gs ON gs.MaSuatChieu=v.MaSuatChieu AND gs.MaGhe=v.MaGhe "
                + "JOIN SuatChieu sc ON sc.MaSuatChieu=v.MaSuatChieu WHERE p.MaPhieuDatVe=?";
        double tong=0;
        int soVe=0;
        try(Connection conn=ConnectDB.getConnection();PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setString(1,maPhieu);
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    String trangThai=rs.getString("TrangThaiPhieu");
                    if(!"Chờ xác nhận".equals(trangThai)&&!"Đã xác nhận".equals(trangThai))throw new SQLException("Phiếu đã thanh toán hoặc đã hủy");
                    if(!"Đã đặt".equals(rs.getString("TrangThaiVe"))||!"Đã đặt".equals(rs.getString("TrangThaiGhe")))throw new SQLException("Trạng thái vé hoặc ghế không hợp lệ");
                    String loiSuat=ThanhToanBanVeDAO.kiemTraSuat(rs.getString("TrangThaiSuat"),rs.getTimestamp("GioBatDau").toLocalDateTime());
                    if(loiSuat!=null)throw new SQLException(loiSuat);
                    tong+=rs.getDouble("DonGia");
                    soVe++;
                }
            }
        }
        if(soVe==0)throw new SQLException("Không tìm thấy phiếu hoặc phiếu chưa có vé");
        return tong;
    }

    /** Convert a confirmed booking into a paid invoice, atomically selling its reserved tickets and seats. */
    public boolean thanhToanPhieuDat(Connection c,HoaDon h,String maPhieu)throws SQLException{
        if(c.getAutoCommit())throw new SQLException("Thanh toán phiếu phải chạy trong transaction");
        String kh;
        try(PreparedStatement p=c.prepareStatement("SELECT MaKhachHang,TrangThai FROM PhieuDatVe WITH (UPDLOCK,HOLDLOCK) WHERE MaPhieuDatVe=?")) {
            p.setString(1,maPhieu);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next())throw new SQLException("Không tìm thấy phiếu đặt");
                String status=r.getString("TrangThai");
                if(!"Chờ xác nhận".equals(status)&&!"Đã xác nhận".equals(status))throw new SQLException("Phiếu đặt không ở trạng thái có thể thanh toán");
                kh=r.getString("MaKhachHang");
            }
        }
        List<String[]> details=new ArrayList<>();
        String query="SELECT d.MaVe,d.DonGia,v.MaSuatChieu,v.MaGhe,v.TrangThai TrangThaiVe,gs.TrangThai "
                + "TrangThaiGhe,sc.TrangThai TrangThaiSuat,sc.GioBatDau FROM ChiTietPhieuDatVe d "
                + "JOIN Ve v WITH (UPDLOCK,HOLDLOCK) ON v.MaVe=d.MaVe JOIN Ghe_SuatChieu gs WITH (UPDLOCK,HOLDLOCK) ON "
                + "gs.MaSuatChieu=v.MaSuatChieu AND gs.MaGhe=v.MaGhe JOIN SuatChieu sc WITH (UPDLOCK,HOLDLOCK) ON sc.MaSuatChieu=v.MaSuatChieu "
                + "WHERE d.MaPhieuDatVe=?";
        try(PreparedStatement p=c.prepareStatement(query)) {
            p.setString(1,maPhieu);
            try(ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    String loiSuat=ThanhToanBanVeDAO.kiemTraSuat(r.getString("TrangThaiSuat"),r.getTimestamp("GioBatDau").toLocalDateTime());
                    if(loiSuat!=null)throw new SQLException(loiSuat);
                    if(!"Đã đặt".equals(r.getString("TrangThaiVe"))||!"Đã đặt".equals(r.getString("TrangThaiGhe")))throw new SQLException("Vé hoặc ghế trong phiếu không còn ở trạng thái Đã đặt");
                    details.add(new String[]{r.getString("MaVe"),String.valueOf(r.getDouble("DonGia")),r.getString("MaSuatChieu"),r.getString("MaGhe")});
                }
            }
        }
        if(details.isEmpty())throw new SQLException("Phiếu đặt chưa có vé");
        double tongPhieu=0;
        for(String[] detail:details)tongPhieu+=Double.parseDouble(detail[1]);
        if(Math.abs(h.getTongTien()-tongPhieu)>0.01)throw new SQLException("Tổng tiền phiếu đã thay đổi; hãy kiểm tra lại trước khi thu tiền");
        String sql="INSERT INTO "
                + "HoaDon(MaHoaDon,MaNhanVien,MaKhachHang,MaKhuyenMai,NgayLap,PhuongThucThanhToan,MaPhieuDatVe,TrangThai,PhanTramGiamApDung,HinhThucApDung,SoComboTangApDung) "
                + "VALUES(?,?,?,NULL,?,?,?,N'Đã thanh toán',0,N'Giảm hóa đơn',0)";
        try(PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,h.getMaHoaDon());
            p.setString(2,h.getNhanVien().getMaNhanVien());
            p.setString(3,kh);
            p.setTimestamp(4,Timestamp.valueOf(h.getNgayLap()==null?LocalDateTime.now():h.getNgayLap()));
            p.setString(5,h.getPhongThucThanhToan());
            p.setString(6,maPhieu);
            p.executeUpdate();
        }
        String ins="INSERT INTO ChiTietHoaDon(MaHoaDon,MaVe,MaCombo,SoLuong,DonGia) VALUES(?,?,NULL,1,?)";
        try(PreparedStatement p=c.prepareStatement(ins)) {
            for(String[]d:details) {
                p.setString(1,h.getMaHoaDon());
                p.setString(2,d[0]);
                p.setDouble(3,Double.parseDouble(d[1]));
                p.addBatch();
            }p.executeBatch();
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE Ve SET TrangThai=N'Đã bán' WHERE MaVe=? AND TrangThai=N'Đã đặt'")) {
            for(String[]d:details) {
                p.setString(1,d[0]);
                p.addBatch();
            }
            int[]rows=p.executeBatch();
            for(int n:rows)if(n!=1&&n!=Statement.SUCCESS_NO_INFO)throw new SQLException("Không chuyển được vé sang Đã bán");
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE Ghe_SuatChieu SET TrangThai=N'Đã bán' WHERE MaSuatChieu=? AND MaGhe=? AND TrangThai=N'Đã đặt'")) {
            for(String[]d:details) {
                p.setString(1,d[2]);
                p.setString(2,d[3]);
                p.addBatch();
            }
            int[]rows=p.executeBatch();
            for(int n:rows)if(n!=1&&n!=Statement.SUCCESS_NO_INFO)throw new SQLException("Không chuyển được ghế sang Đã bán");
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE PhieuDatVe SET TrangThai=N'Đã thanh toán' WHERE MaPhieuDatVe=?")) {
            p.setString(1,maPhieu);
            p.executeUpdate();
        }
        if(kh!=null)try(PreparedStatement p=c.prepareStatement("UPDATE KhachHang SET DiemTichLuy=DiemTichLuy+FLOOR((SELECT TongTien FROM "
                + "vw_HoaDon_TongTien WHERE MaHoaDon=?)/10000.0) WHERE MaKhachHang=?")) {
            p.setString(1,h.getMaHoaDon());
            p.setString(2,kh);
            p.executeUpdate();
        }
        return true;
    }
    /** Record a manual refund, cancel ticket records and release their seats without deleting audit history. */
    public boolean hoanTienVaHuy(Connection c,String id,String lyDo)throws SQLException{
        if(c.getAutoCommit())throw new SQLException("Hoàn tiền phải chạy trong transaction");
        try(PreparedStatement p=c.prepareStatement("SELECT TrangThai,MaPhieuDatVe FROM HoaDon WITH (UPDLOCK,HOLDLOCK) WHERE MaHoaDon=?")) {
            p.setString(1,id);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next())throw new SQLException("Không tìm thấy hóa đơn");
                if(!"Đã thanh toán".equals(r.getString("TrangThai")))throw new SQLException("Hóa đơn đã hủy/hoàn tiền trước đó");
            }
        }
        String cancel="UPDATE Ve SET TrangThai=N'Đã hủy' WHERE TrangThai=N'Đã bán' AND MaVe IN (SELECT MaVe FROM "
                + "ChiTietHoaDon WHERE MaHoaDon=? AND MaVe IS NOT NULL)";
        try(PreparedStatement p=c.prepareStatement(cancel)) {
            p.setString(1,id);
            p.executeUpdate();
        }
        String free="UPDATE gs SET TrangThai=N'Trống' FROM Ghe_SuatChieu gs WHERE EXISTS(SELECT 1 FROM "
                + "ChiTietHoaDon ct JOIN Ve v ON v.MaVe=ct.MaVe WHERE ct.MaHoaDon=? AND "
                + "v.MaSuatChieu=gs.MaSuatChieu AND v.MaGhe=gs.MaGhe AND v.TrangThai=N'Đã hủy') AND NOT "
                + "EXISTS(SELECT 1 FROM Ve active WHERE active.MaSuatChieu=gs.MaSuatChieu AND "
                + "active.MaGhe=gs.MaGhe AND active.TrangThai IN (N'Đã đặt',N'Đã bán'))";
        try(PreparedStatement p=c.prepareStatement(free)) {
            p.setString(1,id);
            p.executeUpdate();
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE kh SET DiemTichLuy=CASE WHEN kh.DiemTichLuy>=FLOOR(v.TongTien/10000.0) THEN "
                + "kh.DiemTichLuy-FLOOR(v.TongTien/10000.0) ELSE 0 END FROM KhachHang kh JOIN HoaDon h ON "
                + "h.MaKhachHang=kh.MaKhachHang JOIN vw_HoaDon_TongTien v ON v.MaHoaDon=h.MaHoaDon WHERE "
                + "h.MaHoaDon=?")) {
            p.setString(1,id);
            p.executeUpdate();
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE HoaDon SET TrangThai=N'Đã hoàn tiền',GhiChuHuy=? WHERE MaHoaDon=? AND TrangThai=N'Đã thanh toán'")) {
            p.setString(1,lyDo);
            p.setString(2,id);
            if(p.executeUpdate()!=1)throw new SQLException("Không ghi nhận được hủy/hoàn tiền");
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE PhieuDatVe SET TrangThai=N'Đã hủy' WHERE MaPhieuDatVe=(SELECT MaPhieuDatVe FROM "
                + "HoaDon WHERE MaHoaDon=?) AND MaPhieuDatVe IS NOT NULL")) {
            p.setString(1,id);
            p.executeUpdate();
        }
        return true;
    }
    public List<String> layChiTietHienThi(String id) {
        List<String>out=new ArrayList<>();
        String q="SELECT "
                + "ct.SoLuong,ct.DonGia,ct.MaVe,ct.MaCombo,g.ViTri,sc.MaSuatChieu,p.TenPhim,cb.TenCombo FROM "
                + "ChiTietHoaDon ct LEFT JOIN Ve v ON v.MaVe=ct.MaVe LEFT JOIN Ghe g ON g.MaGhe=v.MaGhe LEFT "
                + "JOIN SuatChieu sc ON sc.MaSuatChieu=v.MaSuatChieu LEFT JOIN Phim p ON p.MaPhim=sc.MaPhim "
                + "LEFT JOIN ComboBapNuoc cb ON cb.MaCombo=ct.MaCombo WHERE ct.MaHoaDon=? ORDER BY "
                + "ct.MaChiTietHoaDon";
        try(Connection c=ConnectDB.getConnection();PreparedStatement s=c.prepareStatement(q)) {
            s.setString(1,id);
            try(ResultSet r=s.executeQuery()) {
                while(r.next()) {
                    String item;
                    if(r.getString("MaVe")!=null)item="Vé "+r.getString("MaVe")+" · ghế "+r.getString("ViTri")+" · suất "+r.getString("MaSuatChieu")+" · "+r.getString("TenPhim");
                    else item="Combo "+r.getString("TenCombo")+" × "+r.getInt("SoLuong");
                    out.add(item+" — "+String.format("%,.0f VNĐ",r.getDouble("DonGia")*r.getInt("SoLuong")));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
    /** Chi tiết hóa đơn dạng bảng: {tên dịch vụ, số lượng, đơn giá, thành tiền}. */
    public List<Object[]> layChiTietBang(String id) {
        List<Object[]> out=new ArrayList<>();
        String q="SELECT CASE WHEN ct.MaVe IS NOT NULL THEN N'Vé' ELSE N'Combo '+ISNULL(cb.TenCombo,'') END "
                + "Ten,SUM(ct.SoLuong) SL,ct.DonGia FROM ChiTietHoaDon ct LEFT JOIN ComboBapNuoc cb ON "
                + "cb.MaCombo=ct.MaCombo WHERE ct.MaHoaDon=? GROUP BY CASE WHEN ct.MaVe IS NOT NULL THEN "
                + "N'Vé' ELSE N'Combo '+ISNULL(cb.TenCombo,'') END,ct.DonGia ORDER BY 1 DESC";
        try(Connection c=ConnectDB.getConnection();PreparedStatement s=c.prepareStatement(q)) {
            s.setString(1,id);
            try(ResultSet r=s.executeQuery()) {
                while(r.next()) {
                    int sl=r.getInt("SL");
                    double dg=r.getDouble("DonGia");
                    out.add(new Object[]{r.getString("Ten"),sl,dg,dg*sl});
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return out;
    }
    public List<HoaDon> layTatCaHoaDon(){return truyVan(SELECT+" ORDER BY h.NgayLap DESC",null);}
    public List<HoaDon> layHoaDonTheoMaKhachHang(String id){return truyVan(SELECT+" WHERE h.MaKhachHang=? ORDER BY h.NgayLap DESC",id);}
    /** Search invoice id, date (yyyy-MM-dd), customer id/name, or phone. */
    public List<HoaDon> timKiemHoaDon(String keyword){
        String q="SELECT "
                + "h.MaHoaDon,h.MaNhanVien,h.MaKhachHang,h.MaKhuyenMai,h.PhanTramGiamApDung,h.HinhThucApDung,h.SoComboTangApDung,h.NgayLap,h.PhuongThucThanhToan,h.MaPhieuDatVe,h.TrangThai,ISNULL(v.TongTien,0) "
                + "TongTien FROM HoaDon h LEFT JOIN vw_HoaDon_TongTien v ON v.MaHoaDon=h.MaHoaDon LEFT JOIN "
                + "KhachHang kh ON kh.MaKhachHang=h.MaKhachHang WHERE h.MaHoaDon LIKE ? OR "
                + "CONVERT(char(10),h.NgayLap,23) LIKE ? OR kh.MaKhachHang LIKE ? OR kh.TenKhachHang LIKE ? "
                + "OR kh.SoDienThoai LIKE ? ORDER BY h.NgayLap DESC";
        String like="%"+(keyword==null?"":keyword.trim())+"%";
        List<HoaDon> ds=new ArrayList<>();
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(q)) {
            for(int i=1;i<=5;i++)p.setString(i,like);
            try(ResultSet r=p.executeQuery()) {
                while(r.next())ds.add(taoHoaDon(r));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
    private List<HoaDon> truyVan(String sql,String arg) {
        List<HoaDon>ds=new ArrayList<>();
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            if(arg!=null)p.setString(1,arg);
            try(ResultSet r=p.executeQuery()) {
                while(r.next())ds.add(taoHoaDon(r));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}

