package dao;

import connect.ConnectDB;
import entity.KhuyenMai;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {
    private KhuyenMai doc(ResultSet r)throws SQLException{KhuyenMai k=new KhuyenMai(r.getString("MaKhuyenMai"),r.getString("TenKhuyenMai"),r.getString("MoTa"),r.getDouble("PhanTramGiam"),r.getDate("NgayBatDau").toLocalDate(),r.getDate("NgayKetThuc").toLocalDate());k.setTrangThai(r.getString("TrangThai"));return k;}
    public boolean themKhuyenMai(KhuyenMai k){String s="INSERT INTO KhuyenMai(MaKhuyenMai,TenKhuyenMai,MoTa,PhanTramGiam,NgayBatDau,NgayKetThuc,TrangThai) VALUES(?,?,?,?,?,?,?)";try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setString(1,k.getMaKhuyenMai());p.setString(2,k.getTenKhuyenMai());p.setString(3,k.getMoTa());p.setDouble(4,k.getPhanTramGiam());p.setDate(5,Date.valueOf(k.getNgayBatDau()));p.setDate(6,Date.valueOf(k.getNgayKetThuc()));p.setString(7,k.getTrangThai()==null?"Hoạt động":k.getTrangThai());return p.executeUpdate()==1;}catch(SQLException e){e.printStackTrace();return false;}}
    public boolean suaKhuyenMai(KhuyenMai k){String s="UPDATE KhuyenMai SET TenKhuyenMai=?,MoTa=?,PhanTramGiam=?,NgayBatDau=?,NgayKetThuc=?,TrangThai=? WHERE MaKhuyenMai=?";try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setString(1,k.getTenKhuyenMai());p.setString(2,k.getMoTa());p.setDouble(3,k.getPhanTramGiam());p.setDate(4,Date.valueOf(k.getNgayBatDau()));p.setDate(5,Date.valueOf(k.getNgayKetThuc()));p.setString(6,k.getTrangThai());p.setString(7,k.getMaKhuyenMai());return p.executeUpdate()==1;}catch(SQLException e){e.printStackTrace();return false;}}
    public boolean xoaKhuyenMai(String id){try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM KhuyenMai WHERE MaKhuyenMai=?")){p.setString(1,id);return p.executeUpdate()==1;}catch(SQLException e){return false;}}
    public boolean kiemTraMaKhuyenMai(String id){try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement("SELECT 1 FROM KhuyenMai WHERE MaKhuyenMai=?")){p.setString(1,id);try(ResultSet r=p.executeQuery()){return r.next();}}catch(SQLException e){return false;}}
    public boolean conHieuLuc(KhuyenMai k,LocalDate now){return k!=null&&"Hoạt động".equalsIgnoreCase(k.getTrangThai())&&k.getNgayBatDau()!=null&&k.getNgayKetThuc()!=null&&k.getPhanTramGiam()>0&&k.getPhanTramGiam()<=100&&!now.isBefore(k.getNgayBatDau())&&!now.isAfter(k.getNgayKetThuc());}
    public List<KhuyenMai> layTatCaKhuyenMai(){List<KhuyenMai>d=new ArrayList<>();try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM KhuyenMai ORDER BY NgayBatDau DESC");ResultSet r=p.executeQuery()){while(r.next())d.add(doc(r));}catch(SQLException e){e.printStackTrace();}return d;}
    public List<KhuyenMai> layKhuyenMaiHieuLuc(LocalDate date){List<KhuyenMai>d=new ArrayList<>();String s="SELECT * FROM KhuyenMai WHERE TrangThai=N'Hoạt động' AND NgayBatDau<=? AND NgayKetThuc>=? AND PhanTramGiam>0 ORDER BY MaKhuyenMai";try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setDate(1,Date.valueOf(date));p.setDate(2,Date.valueOf(date));try(ResultSet r=p.executeQuery()){while(r.next())d.add(doc(r));}}catch(SQLException e){e.printStackTrace();}return d;}
}
