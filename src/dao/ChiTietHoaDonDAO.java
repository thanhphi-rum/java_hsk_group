package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Invoice lines: each row represents exactly one ticket or a quantity of one combo. */
public class ChiTietHoaDonDAO {
    public boolean themVe(Connection conn,String maHoaDon,String maVe,double donGia) throws SQLException {
        String sql="INSERT INTO ChiTietHoaDon(MaHoaDon,MaVe,MaCombo,SoLuong,DonGia) VALUES(?,?,NULL,1,?)";
        try(PreparedStatement ps=conn.prepareStatement(sql)){ps.setString(1,maHoaDon);ps.setString(2,maVe);ps.setBigDecimal(3,java.math.BigDecimal.valueOf(donGia));return ps.executeUpdate()==1;}
    }
    public boolean themCombo(Connection conn,String maHoaDon,String maCombo,int soLuong,double donGia) throws SQLException {
        if(soLuong<=0)throw new IllegalArgumentException("Số lượng combo phải lớn hơn 0");
        String sql="INSERT INTO ChiTietHoaDon(MaHoaDon,MaVe,MaCombo,SoLuong,DonGia) VALUES(?,NULL,?,?,?)";
        try(PreparedStatement ps=conn.prepareStatement(sql)){ps.setString(1,maHoaDon);ps.setString(2,maCombo);ps.setInt(3,soLuong);ps.setBigDecimal(4,java.math.BigDecimal.valueOf(donGia));return ps.executeUpdate()==1;}
    }
}
