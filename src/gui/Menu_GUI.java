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
    public static final String QL_PHIM = "Quản lý Phim";
    public static final String QL_SUAT_CHIEU = "Quản lý Suất Chiếu";
    public static final String QL_KHACH_HANG = "Quản lý Khách Hàng";
    public static final String QL_NHAN_VIEN = "Quản lý Nhân Viên";
    public static final String QL_TAI_KHOAN = "Quản lý Tài Khoản";
    public static final String QL_KHUYEN_MAI = "Quản lý Khuyến Mãi";
    public static final String THONG_KE = "Thống Kê";

    private static final String[] MUC = {TRANG_CHU, PHIM, HOA_DON, QL_PHIM, QL_SUAT_CHIEU,
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
        logo.add(icon); logo.add(Box.createVerticalStrut(8)); logo.add(ten); logo.add(phu);
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
            ds.add(btn); ds.add(Box.createVerticalStrut(2));
        }
        add(ds, BorderLayout.CENTER);

        // ===== Người dùng + đổi mật khẩu + đăng xuất =====
        JPanel duoi = new JPanel();
        duoi.setOpaque(false);
        duoi.setLayout(new BoxLayout(duoi, BoxLayout.Y_AXIS));
        duoi.setBorder(BorderFactory.createEmptyBorder(8, 12, 16, 12));

        lblTen.setFont(AppTheme.font(Font.BOLD, 14)); lblTen.setForeground(Color.WHITE);
        lblChucVu.setFont(AppTheme.font(Font.PLAIN, 12)); lblChucVu.setForeground(AppTheme.MUTED);
        JPanel info = new JPanel(); info.setOpaque(false); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(0, 14, 8, 0));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(lblTen); info.add(lblChucVu);

        NavButton doiMk = new NavButton("⚿", "Đổi mật khẩu", false);
        doiMk.addActionListener(e -> frame.doiMatKhau());
        NavButton dangXuat = new NavButton("⎋", "Đăng xuất", false);
        dangXuat.addActionListener(e -> frame.dangXuat());
        duoi.add(info); duoi.add(doiMk); duoi.add(Box.createVerticalStrut(2)); duoi.add(dangXuat);
        add(duoi, BorderLayout.SOUTH);
    }

    private static String bieuTuong(String ten) {
        if (TRANG_CHU.equals(ten)) return "⌂";
        if (PHIM.equals(ten)) return "◉";
        if (HOA_DON.equals(ten)) return "▤";
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
        if (tk == null || tk.getNhanVien() == null) { lblTen.setText(" "); lblChucVu.setText(" "); return; }
        lblTen.setText(tk.getNhanVien().getTenNhanVien());
        String cv = tk.getNhanVien().getChucVu();
        lblChucVu.setText(cv == null || cv.isBlank() ? " " : cv);
    }

    /** Biểu tượng cuộn phim: vòng tròn xanh với 5 lỗ. */
    static class LogoIcon implements Icon {
        private final int size;
        LogoIcon(int size) { this.size = size; }
        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }
        public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(x, y);
            g.setPaint(new GradientPaint(0, 0, new Color(0x4A8BFF), 0, size, new Color(0x1E45C9)));
            g.fill(new Ellipse2D.Float(0, 0, size, size));
            g.setColor(new Color(255, 255, 255, 235));
            double cx = size / 2.0, cy = size / 2.0, R = size * 0.27, r = size * 0.115;
            for (int i = 0; i < 5; i++) {
                double a = Math.toRadians(-90 + i * 72);
                g.fill(new Ellipse2D.Double(cx + R * Math.cos(a) - r, cy + R * Math.sin(a) - r, 2 * r, 2 * r));
            }
            g.fill(new Ellipse2D.Double(cx - r * 0.7, cy - r * 0.7, r * 1.4, r * 1.4));
            g.dispose();
        }
    }

    /** Nút menu tự vẽ: mục đang mở nền đỏ bo góc, di chuột thì sáng lên. */
    private static class NavButton extends JButton {
        private boolean active, hover;
        private final String glyph, label;

        NavButton(String glyph, String label, boolean active) {
            super();
            this.glyph = glyph; this.label = label; this.active = active;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(210, 42));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean a) { active = a; repaint(); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (active) { g.setColor(AppTheme.RED); g.fillRoundRect(0, 1, getWidth(), getHeight() - 2, 10, 10); }
            else if (hover) { g.setColor(AppTheme.NAV_HOVER); g.fillRoundRect(0, 1, getWidth(), getHeight() - 2, 10, 10); }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
            g.drawString(glyph, 14, getHeight() / 2 + 6);
            g.setFont(AppTheme.font(active ? Font.BOLD : Font.PLAIN, 14));
            g.drawString(label, 44, getHeight() / 2 + 5);
            g.dispose();
        }
    }
}
