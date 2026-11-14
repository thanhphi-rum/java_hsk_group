package dao;
import connect.ConnectDB;
import entity.ComboBapNuoc;
import java.sql.*;
import java.util.*;
public class ComboBapNuocDAO {
    public List<ComboBapNuoc> layComboDangBan(){List<ComboBapNuoc>d=new ArrayList<>();String sql="SELECT MaCombo,TenCombo,GiaBan,MoTa,TrangThai FROM ComboBapNuoc WHERE TrangThai=N'Đang bán' AND GiaBan>=0 ORDER BY TenCombo";try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())d.add(new ComboBapNuoc(r.getString("MaCombo"),r.getString("TenCombo"),r.getBigDecimal("GiaBan"),r.getString("MoTa"),r.getString("TrangThai")));}catch(SQLException e){e.printStackTrace();}return d;}
}
