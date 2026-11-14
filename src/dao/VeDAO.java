package dao;

import entity.Ve;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Persists tickets against the active show/seat; invoice membership lives in ChiTietHoaDon. */
public class VeDAO {
    public boolean themVe(Connection conn,Ve ve){
        String sql="INSERT INTO Ve(MaVe,MaSuatChieu,MaGhe,GiaTien,TrangThai) VALUES(?,?,?,?,N'Đã bán')";
        try(PreparedStatement ps=conn.prepareStatement(sql)){ps.setString(1,ve.getMaVe());ps.setString(2,ve.getSuatChieu().getMaSuatChieu());ps.setString(3,ve.getGhe().getMaGhe());ps.setBigDecimal(4,java.math.BigDecimal.valueOf(ve.getGiaTien()));return ps.executeUpdate()==1;}catch(SQLException e){e.printStackTrace();return false;}
    }
}
