package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.LinkedHashMap;
import java.util.Map;

import entity.TaiKhoan;

/** Menu dọc bên trái (logo + mục menu, mục đang mở có nền đỏ bo góc). Bấm mục nào MainFrame đổi nội dung bên phải. */
public class Menu_GUI extends JPanel {
    public static final String TRANG_CHU = "Trang chủ";
    public static final String PHIM = "Phim";
    public static final String HOA_DON = "Hóa Đơn";
    public static final String PHIEU_DAT = "Phiếu đặt";
    public static final String QL_PHIM = "Quản lý Phim";
    public static final String QL_SUAT_CHIEU = "Quản lý Suất Chiếu";
    public static final String QL_KHACH_HANG = "Quản lý Khách Hàng";
    public static final String QL_NHAN_VIEN = "Quản lý Nhân Viên";
    public static final String QL_TAI_KHOAN = "Quản lý Tài Khoản";
    public static final String QL_KHUYEN_MAI = "Quản lý Khuyến Mãi";
    public static final String THONG_KE = "Thống Kê";

    private static final String[] MUC = {TRANG_CHU, PHIM, HOA_DON, PHIEU_DAT, QL_PHIM, QL_SUAT_CHIEU,
            QL_KHACH_HANG, QL_NHAN_VIEN, QL_TAI_KHOAN, QL_KHUYEN_MAI, THONG_KE};

    private final Map<String, NavButton> nutMenu = new LinkedHashMap<>();
    private final JLabel lblTen = new JLabel(" ");
    private final JLabel lblChucVu = new JLabel(" ");

    public Menu_GUI(MainFrame frame) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(AppTheme.NAV_BG);
        setPreferredSize(new Dimension(236, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0x1B2B5E)));

        // ===== Logo =====
        JPanel logo = new JPanel();
        logo.setOpaque(false);
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBorder(BorderFactory.createEmptyBorder(22, 10, 18, 10));
        JLabel icon = new JLabel(new LogoIcon(58));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel ten = AppTheme.label("RẠP CHIẾU PHIM", Font.BOLD, 19, Color.WHITE);
        ten.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel phu = AppTheme.label("Movie Theater", Font.PLAIN, 12, AppTheme.MUTED);
        phu.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.add(icon);
        logo.add(Box.createVerticalStrut(8));
        logo.add(ten);
        logo.add(phu);
        add(logo, BorderLayout.NORTH);

        // ===== Danh sách mục =====
        JPanel ds = new JPanel();
        ds.setOpaque(false);
        ds.setLayout(new BoxLayout(ds, BoxLayout.Y_AXIS));
        ds.setBorder(BorderFactory.createEmptyBorder(4, 12, 0, 12));
        for (String t : MUC) {
            NavButton btn = new NavButton(bieuTuong(t), t, false);
            btn.addActionListener(e -> frame.dieuHuong(t));
            nutMenu.put(t, btn);
            ds.add(btn);
            ds.add(Box.createVerticalStrut(2));
        }
        JScrollPane cuonMenu = new JScrollPane(ds);
        cuonMenu.setBorder(null);
        cuonMenu.setOpaque(false);
        cuonMenu.getViewport().setOpaque(false);
        cuonMenu.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cuonMenu.getVerticalScrollBar().setUnitIncrement(24);
        add(cuonMenu, BorderLayout.CENTER);

        // ===== Người dùng + đổi mật khẩu + đăng xuất =====
        JPanel duoi = new JPanel();
        duoi.setOpaque(false);
        duoi.setLayout(new BoxLayout(duoi, BoxLayout.Y_AXIS));
        duoi.setBorder(BorderFactory.createEmptyBorder(8, 12, 16, 12));

        lblTen.setFont(AppTheme.font(Font.BOLD, 14));
        lblTen.setForeground(Color.WHITE);
        lblChucVu.setFont(AppTheme.font(Font.PLAIN, 12));
        lblChucVu.setForeground(AppTheme.MUTED);
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(0, 14, 8, 0));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(lblTen);
        info.add(lblChucVu);

        NavButton doiMk = new NavButton("⚿", "Đổi mật khẩu", false);
        doiMk.addActionListener(e -> frame.doiMatKhau());
        NavButton dangXuat = new NavButton("⎋", "Đăng xuất", false);
        dangXuat.addActionListener(e -> frame.dangXuat());
        duoi.add(info);
        duoi.add(doiMk);
        duoi.add(Box.createVerticalStrut(2));
        duoi.add(dangXuat);
        add(duoi, BorderLayout.SOUTH);

        // Màn hình thấp (vd. laptop 1366x768): thu gọn logo, nút và khoảng cách để mục cuối không bị khối tên người dùng che
        addComponentListener(new java.awt.event.ComponentAdapter() {
            private boolean gon;
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                boolean can = getHeight() > 0 && getHeight() < 800;
                if (can == gon) return;
                gon = can;
                icon.setIcon(new LogoIcon(gon ? 44 : 58));
                logo.setBorder(gon ? BorderFactory.createEmptyBorder(10, 10, 6, 10) : BorderFactory.createEmptyBorder(22, 10, 18, 10));
                duoi.setBorder(gon ? BorderFactory.createEmptyBorder(2, 12, 6, 12) : BorderFactory.createEmptyBorder(8, 12, 16, 12));
                int cao = gon ? 36 : 42;
                for (NavButton b : nutMenu.values()) { b.setPreferredSize(new Dimension(210, cao)); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao)); }
                doiMk.setPreferredSize(new Dimension(210, cao)); doiMk.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
                dangXuat.setPreferredSize(new Dimension(210, cao)); dangXuat.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
                revalidate();
                repaint();
            }
        });
    }

    private static String bieuTuong(String ten) {
        if (TRANG_CHU.equals(ten)) return "⌂";
        if (PHIM.equals(ten)) return "◉";
        if (HOA_DON.equals(ten)) return "▤";
        if (PHIEU_DAT.equals(ten)) return "▣";
        if (QL_PHIM.equals(ten)) return "▣";
        if (QL_SUAT_CHIEU.equals(ten)) return "◷";
        if (QL_KHACH_HANG.equals(ten)) return "♙";
        if (QL_NHAN_VIEN.equals(ten)) return "♟";
        if (QL_TAI_KHOAN.equals(ten)) return "⚙";
        if (QL_KHUYEN_MAI.equals(ten)) return "％";
        return "▥";
    }

    /** Đánh dấu mục đang mở. */
    public void setActive(String ten) {
        for (Map.Entry<String, NavButton> e : nutMenu.entrySet()) e.getValue().setActive(e.getKey().equals(ten));
    }

    /** Mục chỉ dành cho quản lý. */
    public static boolean laMucQuanTri(String ten) {
        return ten.equals(QL_PHIM) || ten.equals(QL_SUAT_CHIEU) || ten.equals(QL_NHAN_VIEN)
                || ten.equals(QL_TAI_KHOAN) || ten.equals(QL_KHUYEN_MAI) || ten.equals(THONG_KE);
    }

    public void setNguoiDung(TaiKhoan tk) {
        boolean quanLy = tk != null && tk.isQuanLy();
        for (Map.Entry<String, NavButton> e : nutMenu.entrySet())
            e.getValue().setVisible(quanLy || !laMucQuanTri(e.getKey()));
        if (tk == null || tk.getNhanVien() == null) {
            lblTen.setText(" ");
            lblChucVu.setText(" ");
            return;
        }
        lblTen.setText(tk.getNhanVien().getTenNhanVien());
        String cv = tk.getNhanVien().getChucVu();
        lblChucVu.setText(cv == null || cv.isBlank() ? " " : cv);
    }

    /** Logo: ô vuông bo góc gradient xanh, cuộn phim trắng 6 lỗ, nút play đỏ ở giữa, tia sáng vàng. Vẽ bằng vector nên đổi size thoải mái. */
    static class LogoIcon implements Icon {
        private final int size;
        LogoIcon(int size) { this.size = size; }
        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }

        /** Tạo ảnh logo để đặt làm icon cửa sổ / thanh taskbar. */
        static java.awt.image.BufferedImage taoAnh(int size) {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            new LogoIcon(size).paintIcon(null, g, 0, 0);
            g.dispose();
            return img;
        }

        public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.translate(x, y);
            double s = size;

            // 1. Nền bo góc, gradient xanh đậm -> xanh sáng
            java.awt.geom.RoundRectangle2D nen = new java.awt.geom.RoundRectangle2D.Double(0, 0, s, s, s * 0.30, s * 0.30);
            g.setPaint(new GradientPaint(0, 0, new Color(0x5B9BFF), (float) s, (float) s, new Color(0x1636B8)));
            g.fill(nen);

            // 2. Vệt sáng nửa trên
            Shape cu = g.getClip();
            g.setClip(nen);
            g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 70), 0, (float) (s * 0.35), new Color(255, 255, 255, 0)));
            g.fill(new Ellipse2D.Double(-s * 0.25, -s * 0.55, s * 1.5, s * 1.1));
            g.setClip(cu);

            // 3. Viền mảnh
            g.setStroke(new BasicStroke((float) Math.max(1, s * 0.02)));
            g.setColor(new Color(255, 255, 255, 60));
            g.draw(new java.awt.geom.RoundRectangle2D.Double(s * 0.01, s * 0.01, s * 0.98, s * 0.98, s * 0.30, s * 0.30));

            // 4. Cuộn phim: đĩa trắng, đục 6 lỗ + lỗ giữa
            double cx = s / 2, cy = s / 2, R = s * 0.34;
            java.awt.geom.Area cuon = new java.awt.geom.Area(new Ellipse2D.Double(cx - R, cy - R, 2 * R, 2 * R));
            double d = s * 0.205, r = s * 0.068;
            for (int i = 0; i < 6; i++) {
                double a = Math.toRadians(-90 + i * 60);
                cuon.subtract(new java.awt.geom.Area(new Ellipse2D.Double(cx + d * Math.cos(a) - r, cy + d * Math.sin(a) - r, 2 * r, 2 * r)));
            }
            double rh = s * 0.10;
            cuon.subtract(new java.awt.geom.Area(new Ellipse2D.Double(cx - rh, cy - rh, 2 * rh, 2 * rh)));
            g.translate(0, s * 0.025);                       // bóng đổ nhẹ
            g.setColor(new Color(10, 20, 80, 70));
            g.fill(cuon);
            g.translate(0, -s * 0.025);
            g.setPaint(new GradientPaint(0, (float) (cy - R), Color.WHITE, 0, (float) (cy + R), new Color(0xDCE8FF)));
            g.fill(cuon);

            // 5. Nút play đỏ ở giữa
            double t = s * 0.085;
            java.awt.geom.Path2D tam = new java.awt.geom.Path2D.Double();
            tam.moveTo(cx - t * 0.55, cy - t);
            tam.lineTo(cx + t * 1.05, cy);
            tam.lineTo(cx - t * 0.55, cy + t);
            tam.closePath();
            g.setPaint(new GradientPaint(0, (float) (cy - t), new Color(0xFF4D63), 0, (float) (cy + t), new Color(0xC81E36)));
            g.fill(tam);

            // 6. Tia sáng vàng ở góc trên phải
            double sx = s * 0.80, sy = s * 0.20, sr = s * 0.075;
            java.awt.geom.Path2D sao = new java.awt.geom.Path2D.Double();
            for (int i = 0; i < 8; i++) {
                double rad = (i % 2 == 0) ? sr : sr * 0.28;
                double a = Math.toRadians(-90 + i * 45);
                double px = sx + rad * Math.cos(a), py = sy + rad * Math.sin(a);
                if (i == 0) sao.moveTo(px, py);
                else sao.lineTo(px, py);
            }
            sao.closePath();
            g.setColor(new Color(0xFFD65C));
            g.fill(sao);
            g.dispose();
        }
    }

    /** Nút menu tự vẽ: mục đang mở nền đỏ bo góc, di chuột thì sáng lên. */
    private static class NavButton extends JButton {
        private boolean active, hover;
        private final String glyph, label;

        NavButton(String glyph, String label, boolean active) {
            super();
            this.glyph = glyph;
            this.label = label;
            this.active = active;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(210, 42));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean a) {
            active = a;
            repaint();
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (active) {
                g.setColor(AppTheme.RED);
                g.fillRoundRect(0, 1, getWidth(), getHeight() - 2, 10, 10);
            }
            else if (hover) {
                g.setColor(AppTheme.NAV_HOVER);
                g.fillRoundRect(0, 1, getWidth(), getHeight() - 2, 10, 10);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
            g.drawString(glyph, 14, getHeight() / 2 + 6);
            g.setFont(AppTheme.font(active ? Font.BOLD : Font.PLAIN, 14));
            g.drawString(label, 44, getHeight() / 2 + 5);
            g.dispose();
        }
    }
}
