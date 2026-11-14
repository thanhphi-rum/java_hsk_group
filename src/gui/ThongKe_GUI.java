package gui;

import connect.ConnectDB;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Revenue reports by period, movie, show and employee. Refunded invoices are excluded. */
public class ThongKe_GUI extends JPanel {
    private final JComboBox<String> dimension = new JComboBox<>(new String[]{"Thời gian", "Phim", "Suất chiếu", "Nhân viên"});
    private final JComboBox<String> period = new JComboBox<>(new String[]{"Ngày", "Tuần", "Tháng", "Năm"});
    private final com.toedter.calendar.JDateChooser dcTu = new com.toedter.calendar.JDateChooser(), dcDen = new com.toedter.calendar.JDateChooser();
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Nhóm", "Số hóa đơn", "Số vé bán", "Doanh thu"}, 0);
    private final JPanel chart = new JPanel(new BorderLayout()), pie = new JPanel(new BorderLayout());
    private final JLabel lDoanhThu = AppTheme.label("—", Font.BOLD, 20, Color.WHITE), lHoaDon = AppTheme.label("—", Font.BOLD, 20, Color.WHITE), lVe = AppTheme.label("—", Font.BOLD, 20, Color.WHITE);
    private String ngayTu, ngayDen;

    public ThongKe_GUI() {
        setLayout(new BorderLayout(0, 10)); setOpaque(false); setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(AppTheme.label("Thống Kê Doanh Thu", Font.BOLD, 26, Color.WHITE), BorderLayout.NORTH);

        // ===== Thanh lọc =====
        java.time.LocalDate hn = java.time.LocalDate.now();
        dcTu.setDateFormatString("dd/MM/yyyy"); dcDen.setDateFormatString("dd/MM/yyyy");
        dcTu.setDate(java.sql.Date.valueOf(hn.withDayOfMonth(1))); dcDen.setDate(java.sql.Date.valueOf(hn));
        dcTu.setPreferredSize(new Dimension(130, 36)); dcDen.setPreferredSize(new Dimension(130, 36));
        dimension.setPreferredSize(new Dimension(130, 36)); period.setPreferredSize(new Dimension(100, 36));
        JButton load = new JButton("Thống kê"); load.setPreferredSize(new Dimension(110, 36));
        JPanel top = new AppTheme.Card(new FlowLayout(FlowLayout.LEFT, 10, 0), AppTheme.CARD_BG, AppTheme.CARD_BORDER, 16);
        ((JComponent) top).setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        top.add(AppTheme.label("Từ ngày:", Font.PLAIN, 13, AppTheme.MUTED)); top.add(dcTu);
        top.add(AppTheme.label("Đến ngày:", Font.PLAIN, 13, AppTheme.MUTED)); top.add(dcDen);
        top.add(AppTheme.label("Theo:", Font.PLAIN, 13, AppTheme.MUTED)); top.add(dimension); top.add(period); top.add(load);

        // ===== Thẻ số liệu bên trái =====
        JPanel kpi = new JPanel(new GridLayout(3, 1, 0, 12)); kpi.setOpaque(false); kpi.setPreferredSize(new Dimension(230, 0));
        kpi.add(theSoLieu("Doanh thu", lDoanhThu)); kpi.add(theSoLieu("Số hóa đơn", lHoaDon)); kpi.add(theSoLieu("Số vé bán", lVe));

        AppTheme.Card cBar = new AppTheme.Card(new BorderLayout(0, 6)); chart.setOpaque(false); cBar.add(chart, BorderLayout.CENTER);
        AppTheme.Card cPie = new AppTheme.Card(new BorderLayout(0, 6)); pie.setOpaque(false); cPie.setPreferredSize(new Dimension(340, 0));
        cPie.add(AppTheme.label("Doanh thu theo loại", Font.BOLD, 15, Color.WHITE), BorderLayout.NORTH); cPie.add(pie, BorderLayout.CENTER);

        JPanel hang = new JPanel(new BorderLayout(12, 0)); hang.setOpaque(false);
        hang.add(kpi, BorderLayout.WEST); hang.add(cBar, BorderLayout.CENTER); hang.add(cPie, BorderLayout.EAST);

        JTable table = new JTable(model); AppTheme.styleTable(table); table.setRowHeight(30);
        JScrollPane sp = AppTheme.scroll(table); sp.setPreferredSize(new Dimension(0, 150));

        JPanel body = new JPanel(new BorderLayout(0, 10)); body.setOpaque(false);
        body.add(top, BorderLayout.NORTH); body.add(hang, BorderLayout.CENTER); body.add(sp, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        dimension.addActionListener(e -> period.setEnabled("Thời gian".equals(dimension.getSelectedItem())));
        load.addActionListener(e -> tai()); tai();
    }

    private JPanel theSoLieu(String nhan, JLabel gia) {
        AppTheme.Card c = new AppTheme.Card(new BorderLayout(0, 4));
        c.add(AppTheme.label(nhan, Font.PLAIN, 13, AppTheme.MUTED), BorderLayout.NORTH); c.add(gia, BorderLayout.CENTER);
        return c;
    }

    /** Thêm điều kiện khoảng ngày vào mọi truy vấn hóa đơn đã thanh toán. */
    private String locNgay(String sql) {
        return sql.replace("h.TrangThai=N'Đã thanh toán'", "h.TrangThai=N'Đã thanh toán' AND CONVERT(date,h.NgayLap) BETWEEN '" + ngayTu + "' AND '" + ngayDen + "'")
                  .replace("hx.TrangThai=N'Đã thanh toán'", "hx.TrangThai=N'Đã thanh toán' AND CONVERT(date,hx.NgayLap) BETWEEN '" + ngayTu + "' AND '" + ngayDen + "'");
    }

    private String sqlTheoThoiGian(String p) {
        String key, label;
        if ("Tháng".equals(p)) { key="DATEFROMPARTS(YEAR(h.NgayLap),MONTH(h.NgayLap),1)"; label="CONVERT(char(7),i.Ky,120)"; }
        else if ("Năm".equals(p)) { key="DATEFROMPARTS(YEAR(h.NgayLap),1,1)"; label="CONVERT(char(4),YEAR(i.Ky))"; }
        else if ("Tuần".equals(p)) { key="DATEADD(day,-(DATEDIFF(day,CONVERT(date,'19000101'),CONVERT(date,h.NgayLap)) % 7),CONVERT(date,h.NgayLap))"; label="CONVERT(char(10),i.Ky,23)"; }
        else { key="CONVERT(date,h.NgayLap)"; label="CONVERT(char(10),i.Ky,23)"; }
        String keyT=key.replace("h.NgayLap", "hx.NgayLap");
        return "WITH i AS (SELECT "+key+" Ky,COUNT(*) SoHoaDon,SUM(v.TongTien) DoanhThu FROM HoaDon h JOIN vw_HoaDon_TongTien v ON v.MaHoaDon=h.MaHoaDon WHERE h.TrangThai=N'Đã thanh toán' GROUP BY "+key+"), "
             +"t AS (SELECT "+keyT+" Ky,SUM(ct.SoLuong) SoVe FROM HoaDon hx JOIN ChiTietHoaDon ct ON ct.MaHoaDon=hx.MaHoaDon AND ct.MaVe IS NOT NULL WHERE hx.TrangThai=N'Đã thanh toán' GROUP BY "+keyT+") "
             +"SELECT "+label+" Nhom,i.SoHoaDon,ISNULL(t.SoVe,0) SoVe,i.DoanhThu FROM i LEFT JOIN t ON t.Ky=i.Ky ORDER BY i.Ky";
    }

    private String taoSql() {
        String dim=(String)dimension.getSelectedItem();
        if ("Thời gian".equals(dim)) return sqlTheoThoiGian((String)period.getSelectedItem());
        if ("Nhân viên".equals(dim)) return "SELECT nv.TenNhanVien Nhom,COUNT(*) SoHoaDon,SUM(ISNULL(tv.SoVe,0)) SoVe,SUM(v.TongTien) DoanhThu FROM HoaDon h JOIN vw_HoaDon_TongTien v ON v.MaHoaDon=h.MaHoaDon JOIN NhanVien nv ON nv.MaNhanVien=h.MaNhanVien LEFT JOIN (SELECT MaHoaDon,SUM(SoLuong) SoVe FROM ChiTietHoaDon WHERE MaVe IS NOT NULL GROUP BY MaHoaDon) tv ON tv.MaHoaDon=h.MaHoaDon WHERE h.TrangThai=N'Đã thanh toán' GROUP BY nv.TenNhanVien ORDER BY nv.TenNhanVien";
        String nhom="Phim".equals(dim)?"f.TenPhim":"sc.MaSuatChieu";
        String joins="JOIN Ve v ON v.MaVe=ct.MaVe JOIN SuatChieu sc ON sc.MaSuatChieu=v.MaSuatChieu "+("Phim".equals(dim)?"JOIN Phim f ON f.MaPhim=sc.MaPhim ":"");
        return "SELECT "+nhom+" Nhom,COUNT(DISTINCT h.MaHoaDon) SoHoaDon,SUM(ct.SoLuong) SoVe,SUM(ct.SoLuong*ct.DonGia*(1-ISNULL(km.PhanTramGiam,0)/100.0)) DoanhThu FROM HoaDon h JOIN ChiTietHoaDon ct ON ct.MaHoaDon=h.MaHoaDon AND ct.MaVe IS NOT NULL "+joins+"LEFT JOIN KhuyenMai km ON km.MaKhuyenMai=h.MaKhuyenMai WHERE h.TrangThai=N'Đã thanh toán' GROUP BY "+nhom+" ORDER BY "+nhom;
    }

    private void tai() {
        if (dcTu.getDate() == null || dcDen.getDate() == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng ngày.", "Thống kê", JOptionPane.WARNING_MESSAGE); return; }
        ngayTu = new java.text.SimpleDateFormat("yyyy-MM-dd").format(dcTu.getDate());
        ngayDen = new java.text.SimpleDateFormat("yyyy-MM-dd").format(dcDen.getDate());
        if (ngayTu.compareTo(ngayDen) > 0) { JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước ngày kết thúc.", "Thống kê", JOptionPane.WARNING_MESSAGE); return; }
        DefaultCategoryDataset data = new DefaultCategoryDataset(); model.setRowCount(0);
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(locNgay(taoSql()))) {
            while (r.next()) { String name = r.getString("Nhom"); double revenue = r.getDouble("DoanhThu");
                data.addValue(revenue, "Doanh thu", name);
                model.addRow(new Object[]{name, r.getInt("SoHoaDon"), r.getInt("SoVe"), String.format("%,.0f VNĐ", revenue)});
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Không tải được thống kê: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); return; }

        // Tổng quan: doanh thu, số hóa đơn, số vé
        String tq = locNgay("SELECT COUNT(*) SoHoaDon, ISNULL(SUM(v.TongTien),0) DoanhThu, ISNULL(SUM(tv.SoVe),0) SoVe FROM HoaDon h JOIN vw_HoaDon_TongTien v ON v.MaHoaDon=h.MaHoaDon "
                + "LEFT JOIN (SELECT MaHoaDon,SUM(SoLuong) SoVe FROM ChiTietHoaDon WHERE MaVe IS NOT NULL GROUP BY MaHoaDon) tv ON tv.MaHoaDon=h.MaHoaDon WHERE h.TrangThai=N'Đã thanh toán'");
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(tq)) {
            if (r.next()) { lDoanhThu.setText(String.format("%,.0fđ", r.getDouble("DoanhThu"))); lHoaDon.setText(String.format("%,d", r.getInt("SoHoaDon"))); lVe.setText(String.format("%,d", r.getInt("SoVe"))); }
        } catch (Exception e) { /* giữ nguyên */ }

        // Doanh thu theo loại: vé / combo (trước khuyến mãi)
        double ve = 0, combo = 0;
        String lq = locNgay("SELECT ISNULL(SUM(CASE WHEN ct.MaVe IS NOT NULL THEN ct.SoLuong*ct.DonGia END),0) Ve, ISNULL(SUM(CASE WHEN ct.MaVe IS NULL THEN ct.SoLuong*ct.DonGia END),0) Combo "
                + "FROM HoaDon h JOIN ChiTietHoaDon ct ON ct.MaHoaDon=h.MaHoaDon WHERE h.TrangThai=N'Đã thanh toán'");
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(lq)) {
            if (r.next()) { ve = r.getDouble("Ve"); combo = r.getDouble("Combo"); }
        } catch (Exception e) { /* để trống */ }
        org.jfree.data.general.DefaultPieDataset pd = new org.jfree.data.general.DefaultPieDataset();
        pd.setValue("Vé phim", ve); pd.setValue("Combo", combo);
        JFreeChart pc = ChartFactory.createPieChart(null, pd, true, true, false);
        pc.setBackgroundPaint(new Color(0, 0, 0, 0)); pc.setBorderVisible(false);
        org.jfree.chart.plot.PiePlot pp = (org.jfree.chart.plot.PiePlot) pc.getPlot();
        pp.setBackgroundPaint(new Color(0, 0, 0, 0)); pp.setOutlineVisible(false); pp.setShadowPaint(null); pp.setLabelGenerator(null);
        pp.setSectionPaint("Vé phim", new Color(0x3B82F6)); pp.setSectionPaint("Combo", new Color(0xE0384F)); pp.setSectionOutlinePaint("Vé phim", new Color(0x13204A)); pp.setSectionOutlinePaint("Combo", new Color(0x13204A));
        pc.getLegend().setBackgroundPaint(new Color(0, 0, 0, 0)); pc.getLegend().setItemPaint(Color.WHITE); pc.getLegend().setItemFont(AppTheme.font(Font.PLAIN, 12));
        pc.getLegend().setPosition(org.jfree.ui.RectangleEdge.BOTTOM);
        ChartPanel pcp = new ChartPanel(pc); pcp.setOpaque(false);
        pie.removeAll(); pie.add(pcp, BorderLayout.CENTER); pie.revalidate(); pie.repaint();

        String dim = (String) dimension.getSelectedItem(); String title = "Thời gian".equals(dim) ? "Doanh thu theo " + String.valueOf(period.getSelectedItem()).toLowerCase() : "Doanh thu theo " + dim.toLowerCase();
        JFreeChart ch = ChartFactory.createBarChart(title, null, null, data);
        ch.setBackgroundPaint(new Color(0, 0, 0, 0)); ch.setBorderVisible(false); ch.removeLegend();
        ch.getTitle().setPaint(Color.WHITE); ch.getTitle().setFont(AppTheme.font(Font.BOLD, 15)); ch.getTitle().setHorizontalAlignment(org.jfree.ui.HorizontalAlignment.LEFT);
        CategoryPlot plot = ch.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0, 0, 0, 0)); plot.setOutlineVisible(false); plot.setRangeGridlinePaint(new Color(0x34478A));
        Color tick = new Color(0xC9D3EE);
        plot.getDomainAxis().setTickLabelPaint(tick); plot.getDomainAxis().setAxisLinePaint(new Color(0x34478A));
        plot.getRangeAxis().setTickLabelPaint(tick); plot.getRangeAxis().setAxisLineVisible(false);
        BarRenderer br = (BarRenderer) plot.getRenderer(); br.setSeriesPaint(0, new Color(0x3B82F6)); br.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter()); br.setShadowVisible(false); br.setMaximumBarWidth(0.06);
        ChartPanel cp = new ChartPanel(ch); cp.setOpaque(false);
        chart.removeAll(); chart.add(cp, BorderLayout.CENTER); chart.revalidate(); chart.repaint();
    }
}
