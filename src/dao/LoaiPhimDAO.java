package dao;

import connect.ConnectDB;
import entity.LoaiPhim;
import java.sql.*;
import java.util.*;

/** API cũ được ánh xạ sang danh mục thể loại chung của bản gộp. */
public class LoaiPhimDAO {
    public List<LoaiPhim> layTatCaLoaiPhim() {
        List<LoaiPhim> ds = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT MaTheLoai,TenTheLoai,MoTa FROM TheLoai ORDER BY MaTheLoai")) {
            while(r.next()) ds.add(new LoaiPhim(r.getString(1),r.getString(2),r.getString(3)));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }
    public LoaiPhim layLoaiPhimTheoMa(String ma) {
        try(Connection c=ConnectDB.getConnection();PreparedStatement s=c.prepareStatement("SELECT MaTheLoai,TenTheLoai,MoTa FROM TheLoai WHERE MaTheLoai=?")){
            s.setString(1,ma);try(ResultSet r=s.executeQuery()){if(r.next())return new LoaiPhim(r.getString(1),r.getString(2),r.getString(3));}
        }catch(SQLException e){e.printStackTrace();}
        return null;
    }
}
