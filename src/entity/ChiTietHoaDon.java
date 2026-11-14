package entity;

import java.math.BigDecimal;

/** Một dòng hóa đơn: vé đơn lẻ hoặc combo với số lượng. */
public class ChiTietHoaDon {
    private int maChiTietHoaDon;
    private String maHoaDon, maVe, maCombo;
    private int soLuong;
    private BigDecimal donGia;
    public ChiTietHoaDon() {}
    public ChiTietHoaDon(String maHoaDon,String maVe,String maCombo,int soLuong,BigDecimal donGia){this.maHoaDon=maHoaDon;this.maVe=maVe;this.maCombo=maCombo;this.soLuong=soLuong;this.donGia=donGia;}
    public int getMaChiTietHoaDon(){return maChiTietHoaDon;} public void setMaChiTietHoaDon(int v){maChiTietHoaDon=v;}
    public String getMaHoaDon(){return maHoaDon;} public void setMaHoaDon(String v){maHoaDon=v;}
    public String getMaVe(){return maVe;} public void setMaVe(String v){maVe=v;}
    public String getMaCombo(){return maCombo;} public void setMaCombo(String v){maCombo=v;}
    public int getSoLuong(){return soLuong;} public void setSoLuong(int v){soLuong=v;}
    public BigDecimal getDonGia(){return donGia;} public void setDonGia(BigDecimal v){donGia=v;}
}
