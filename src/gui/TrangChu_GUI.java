package gui;

import connect.ConnectDB;
import entity.TaiKhoan;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Trang chủ: lời chào, 4 thẻ số liệu, biểu đồ doanh thu 7 ngày và danh sách phim đang chiếu (giao diện tối). */
public class TrangChu_GUI extends JPanel {
    private static final NumberFormat VND = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public TrangChu_GUI() {
        setLayout(new BorderLayout());
        setOpaque(false);
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(8, 8, 16, 8));
        page.add(header()); page.add(Box.createVerticalStrut(18));
        page.add(metrics()); page.add(Box.createVerticalStrut(18));
        page.add(reports());
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ================= Lời chào =================
    private JPanel header() {
        JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        TaiKhoan tk = MainFrame.getInstance() == null ? null : MainFrame.getInstance().getTaiKhoan();
        String name = tk == null || tk.getNhanVien() == null ? "" : tk.getNhanVien().getTenNhanVien();
        JLabel title = AppTheme.label("Chào mừng trở lại" + (name == null || name.isBlank() ? "!" : ", " + name + "!"), Font.BOLD, 30, Color.WHITE);
        JLabel sub = AppTheme.label("Cùng tạo nên những khoảnh khắc điện ảnh tuyệt vời ★", Font.PLAIN, 14, new Color(0xD7DEF2));
        title.setAlignmentX(Component.LEFT_ALIGNMENT); sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title); p.add(Box.createVerticalStrut(4)); p.add(sub);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        return p;
    }

    // ================= 4 thẻ số liệu =================
    private JPanel metrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 14));
        row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 104)); row.setPreferredSize(new Dimension(900, 104));
        double[] hn = soLieu(0), hq = soLieu(1);
        String[] labels = {"Doanh thu hôm nay", "Tổng vé bán", "Hóa đơn", "Khách hàng"};
        String[] glyph = {"✦", "▤", "≣", "♙"};
        Color[] mau = {new Color(0xE0384F), new Color(0x2F6BFF), new Color(0x7C4DE8), new Color(0x12A776)};
        for (int i = 0; i < 4; i++) {
            String value = hn[i] < 0 ? "—" : (i == 0 ? VND.format(hn[i]) + "đ" : VND.format(hn[i]));
            String trend = i == 3 ? "" : xuHuong(hn[i], hq[i]);
            row.add(thSoLieu(glyph[i], mau[i], labels[i], value, trend));
        }
        return row;
    }

    private static String xuHuong(double nay, double qua) {
        if (nay < 0 || qua <= 0) return "";
        double pct = (nay - qua) * 100.0 / qua;
        return (pct >= 0 ? "↑ +" : "↓ ") + Math.round(pct) + "%";
    }

    private JPanel thSoLieu(String glyph, Color mau, String label, String value, String trend) {
        AppTheme.Card card = new AppTheme.Card(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel icon = new JLabel(glyph, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mau); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16)); g2.dispose(); super.paintComponent(g);
            }
        };
        icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24)); icon.setForeground(Color.WHITE);
        icon.setPreferredSize(new Dimension(58, 58));
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel l1 = AppTheme.label(label, Font.PLAIN, 13, new Color(0xD7DEF2));
        JLabel l2 = AppTheme.label(value, Font.BOLD, 22, Color.WHITE);
        JLabel l3 = AppTheme.label(trend.isEmpty() ? " " : trend, Font.BOLD, 12, trend.startsWith("↓") ? new Color(0xFF6B6B) : new Color(0x34D399));
        text.add(Box.createVerticalGlue()); text.add(l1); text.add(l2); text.add(l3); text.add(Box.createVerticalGlue());
        JPanel iconBox = new JPanel(new GridBagLayout()); iconBox.setOpaque(false); iconBox.add(icon);
        card.add(iconBox, BorderLayout.WEST); card.add(text, BorderLayout.CENTER);
        return card;
    }

    /** {doanh thu, số vé, số hóa đơn, số khách} của ngày cách hôm nay daysAgo ngày; -1 nếu lỗi. */
    private double[] soLieu(int daysAgo) {
        double[] out = {-1, -1, -1, -1};
        String d = "CAST(DATEADD(day,-?,GETDATE()) AS date)";
        String q = "SELECT (SELECT ISNULL(SUM(v.TongTien),0) FROM HoaDon h JOIN vw_HoaDon_TongTien v ON v.MaHoaDon=h.MaHoaDon WHERE h.TrangThai=N'Đã thanh toán' AND CAST(h.NgayLap AS date)=" + d + ") DoanhThu,"
                + "(SELECT ISNULL(SUM(ct.SoLuong),0) FROM HoaDon h JOIN ChiTietHoaDon ct ON ct.MaHoaDon=h.MaHoaDon AND ct.MaVe IS NOT NULL WHERE h.TrangThai=N'Đã thanh toán' AND CAST(h.NgayLap AS date)=" + d + ") SoVe,"
                + "(SELECT COUNT(*) FROM HoaDon WHERE TrangThai=N'Đã thanh toán' AND CAST(NgayLap AS date)=" + d + ") SoHoaDon,"
                + "(SELECT COUNT(*) FROM KhachHang) SoKhach";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            for (int i = 1; i <= 3; i++) ps.setInt(i, daysAgo);
            try (ResultSet r = ps.executeQuery()) {
                if (r.next()) { out[0] = r.getDouble("DoanhThu"); out[1] = r.getInt("SoVe"); out[2] = r.getInt("SoHoaDon"); out[3] = r.getInt("SoKhach"); }
            }
        } catch (Exception ex) { /* giữ "—" */ }
        return out;
    }

    // ================= Biểu đồ + phim đang chiếu =================
    private JPanel reports() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setPreferredSize(new Dimension(900, 360)); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; c.weighty = 1; c.gridy = 0;
        c.gridx = 0; c.weightx = 0.62; c.insets = new Insets(0, 0, 0, 7); row.add(revenuePanel(), c);
        c.gridx = 1; c.weightx = 0.38; c.insets = new Insets(0, 7, 0, 0); row.add(nowPlayingPanel(), c);
        return row;
    }

    private AppTheme.Card cardPanel(String title) {
        AppTheme.Card p = new AppTheme.Card(new BorderLayout(8, 10));
        p.add(AppTheme.label(title, Font.BOLD, 16, Color.WHITE), BorderLayout.NORTH);
        return p;
    }

    private static String thu(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        return w == DayOfWeek.SUNDAY ? "CN" : "T" + (w.getValue() + 1);
    }

    private JPanel revenuePanel() {
        AppTheme.Card card = cardPanel("Doanh thu 7 ngày gần nhất");
        Map<LocalDate, Double> values = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) values.put(today.minusDays(i), 0d);
        String q = "SELECT CAST(h.NgayLap AS date) Ngay,SUM(v.TongTien) DoanhThu FROM HoaDon h JOIN vw_HoaDon_TongTien v ON v.MaHoaDon=h.MaHoaDon "
                + "WHERE h.TrangThai=N'Đã thanh toán' AND h.NgayLap>=DATEADD(day,-6,CAST(GETDATE() AS date)) GROUP BY CAST(h.NgayLap AS date)";
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(q)) {
            while (r.next()) { java.sql.Date d = r.getDate("Ngay"); if (d != null) values.put(d.toLocalDate(), r.getDouble("DoanhThu")); }
        } catch (Exception ex) { }
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Map.Entry<LocalDate, Double> e : values.entrySet()) ds.addValue(e.getValue(), "Doanh thu", thu(e.getKey()));

        JFreeChart chart = ChartFactory.createLineChart(null, null, null, ds, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(new Color(0, 0, 0, 0)); chart.setBorderVisible(false);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0, 0, 0, 0)); plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(0x34478A)); plot.setRangeGridlineStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{3f, 3f}, 0f));
        Color tick = new Color(0xC9D3EE); Font f = AppTheme.font(Font.PLAIN, 12);
        plot.getDomainAxis().setTickLabelPaint(tick); plot.getDomainAxis().setTickLabelFont(f); plot.getDomainAxis().setAxisLinePaint(new Color(0x34478A));
        NumberAxis y = (NumberAxis) plot.getRangeAxis();
        y.setTickLabelPaint(tick); y.setTickLabelFont(f); y.setAxisLineVisible(false); y.setAutoRangeIncludesZero(true);
        y.setNumberFormatOverride(new NumberFormat() {
            @Override public StringBuffer format(double n, StringBuffer sb, FieldPosition fp) {
                if (n >= 1e6) return sb.append(Math.round(n / 1e6)).append("M");
                if (n >= 1e3) return sb.append(Math.round(n / 1e3)).append("K");
                return sb.append(Math.round(n));
            }
            @Override public StringBuffer format(long n, StringBuffer sb, FieldPosition fp) { return format((double) n, sb, fp); }
            @Override public Number parse(String s, ParsePosition p) { return null; }
        });
        LineAndShapeRenderer rd = (LineAndShapeRenderer) plot.getRenderer();
        rd.setSeriesPaint(0, new Color(0x3B82F6)); rd.setSeriesStroke(0, new BasicStroke(2.6f));
        rd.setSeriesShapesVisible(0, true); rd.setSeriesShape(0, new Ellipse2D.Double(-4.5, -4.5, 9, 9));
        rd.setSeriesFillPaint(0, Color.WHITE); rd.setUseFillPaint(true); rd.setSeriesOutlinePaint(0, new Color(0x3B82F6)); rd.setUseOutlinePaint(true);
        ChartPanel panel = new ChartPanel(chart);
        panel.setOpaque(false); panel.setBorder(null); panel.setPreferredSize(new Dimension(500, 260));
        panel.setMouseWheelEnabled(false); panel.setDomainZoomable(false); panel.setRangeZoomable(false);
        card.add(panel, BorderLayout.CENTER);
        return card;
    }

    private JPanel nowPlayingPanel() {
        AppTheme.Card card = cardPanel("Phim đang chiếu");
        JPanel list = new JPanel(); list.setOpaque(false); list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        String q = "SELECT TOP 4 MaPhim, TenPhim, ThoiLuong FROM Phim WHERE TrangThai=N'Đang Chiếu' ORDER BY NgayKhoiChieu DESC, TenPhim";
        int count = 0;
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(q)) {
            while (r.next()) { list.add(movieRow(r.getString("MaPhim"), r.getString("TenPhim"), r.getInt("ThoiLuong"))); list.add(Box.createVerticalStrut(10)); count++; }
        } catch (Exception ex) { }
        if (count == 0) list.add(AppTheme.label("Chưa có phim đang chiếu", Font.PLAIN, 13, AppTheme.MUTED));
        card.add(list, BorderLayout.CENTER);

        JButton more = new JButton("Xem thêm +");
        more.putClientProperty("JButton.buttonType", "borderless");
        more.setForeground(new Color(0x5B9BFF)); more.setOpaque(false); more.setContentAreaFilled(false);
        more.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        more.addActionListener(e -> { if (MainFrame.getInstance() != null) MainFrame.getInstance().dieuHuong(Menu_GUI.PHIM); });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); south.setOpaque(false); south.add(more);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private JPanel movieRow(String ma, String name, int minutes) {
        JPanel p = new JPanel(new BorderLayout(12, 0)); p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        JLabel poster = new JLabel(AppTheme.poster(ma, 46, 64)); poster.setPreferredSize(new Dimension(46, 64));
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(AppTheme.label(name, Font.BOLD, 14, Color.WHITE));
        text.add(Box.createVerticalStrut(3));
        text.add(AppTheme.label("2D - " + minutes + " phút", Font.PLAIN, 12, AppTheme.MUTED));
        text.add(Box.createVerticalGlue());
        p.add(poster, BorderLayout.WEST); p.add(text, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) { MainFrame.main(args); }
}
