package gui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/** Giao diện tối xanh navy (kiểu "glass") dùng chung cho toàn bộ ứng dụng. */
public class AppTheme {
    // ===== Bảng màu =====
    public static final Color NAV_BG = new Color(0x0A1230), NAV_HOVER = new Color(0x1B2B5E);
    public static final Color ACCENT = new Color(0x2F6BFF), ACCENT_DARK = new Color(0x1F55DC), ACCENT_LIGHT = new Color(0x4A80FF);
    public static final Color RED = new Color(0xB32035);
    public static final Color DANGER = new Color(0xEF4444);
    public static final Color PAGE_BG = new Color(0x0D1636);
    public static final Color CARD_BG = new Color(0x13204A), CARD_BORDER = new Color(0x2A3C72);
    public static final Color TEXT = new Color(0xE8EEFF), MUTED = new Color(0xA9B4CC);
    // Bảng dữ liệu: nền sáng chữ tối như ảnh mẫu
    public static final Color TABLE_ALT = new Color(0xEAF0FB), TABLE_HEAD = new Color(0xD9E4F7);
    public static final Color TABLE_GRID = new Color(0xD5DDEE), TABLE_TEXT = new Color(0x1F2A44);
    public static final Color OK_BG = new Color(0xDCF5E3), OK_FG = new Color(0x15803D);
    public static final Color WARN_BG = new Color(0xFDF1CF), WARN_FG = new Color(0xB45309);
    public static final Color OFF_BG = new Color(0xFBE0E0), OFF_FG = new Color(0xB91C1C);

    public static final String FONT = "Segoe UI";
    public static Font font(int style, int size) { return new Font(FONT, style, size); }

    public static void install() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("defaultFont", font(Font.PLAIN, 13));
            UIManager.put("Button.arc", 12); UIManager.put("Component.arc", 10); UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999); UIManager.put("ScrollBar.width", 11);
            UIManager.put("ScrollBar.thumb", new Color(0x34478A)); UIManager.put("ScrollBar.track", PAGE_BG);
            UIManager.put("Component.focusColor", ACCENT); UIManager.put("Component.focusWidth", 1);

            UIManager.put("Panel.background", PAGE_BG); UIManager.put("Panel.foreground", TEXT);
            UIManager.put("Label.foreground", TEXT);
            UIManager.put("OptionPane.background", CARD_BG); UIManager.put("OptionPane.messageForeground", TEXT);
            UIManager.put("TitledBorder.titleColor", TEXT); UIManager.put("TitledBorder.font", font(Font.BOLD, 13));
            UIManager.put("ScrollPane.background", PAGE_BG);
            UIManager.put("Viewport.background", PAGE_BG);
            UIManager.put("TabbedPane.background", PAGE_BG);

            // Ô nhập: nền sáng, chữ tối
            Color inBg = new Color(0xF4F7FF), inFg = new Color(0x1F2A44);
            for (String k : new String[]{"TextField", "PasswordField", "FormattedTextField", "TextArea", "TextPane", "Spinner", "ComboBox", "EditorPane"}) {
                UIManager.put(k + ".background", inBg); UIManager.put(k + ".foreground", inFg);
                UIManager.put(k + ".caretForeground", inFg);
            }
            UIManager.put("ComboBox.buttonBackground", inBg); UIManager.put("ComboBox.buttonArrowColor", new Color(0x5B6784));
            UIManager.put("ComboBox.selectionBackground", ACCENT); UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            UIManager.put("ComboBox.disabledBackground", new Color(0xDDE4F2));
            UIManager.put("TextField.placeholderForeground", new Color(0x8A94AD));
            UIManager.put("Spinner.buttonBackground", inBg);
            UIManager.put("List.background", inBg); UIManager.put("List.foreground", inFg);
            UIManager.put("List.selectionBackground", ACCENT); UIManager.put("List.selectionForeground", Color.WHITE);

            // Nút xanh dương như ảnh mẫu
            UIManager.put("Button.background", ACCENT); UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.hoverBackground", ACCENT_LIGHT); UIManager.put("Button.pressedBackground", ACCENT_DARK);
            UIManager.put("Button.default.background", ACCENT); UIManager.put("Button.default.foreground", Color.WHITE);
            UIManager.put("Button.disabledBackground", new Color(0x24315C)); UIManager.put("Button.disabledText", new Color(0x7C88AA));
            UIManager.put("Button.borderWidth", 0); UIManager.put("Button.focusedBorderColor", ACCENT_LIGHT);
            UIManager.put("Button.font", font(Font.BOLD, 13));

            // Bảng
            UIManager.put("Table.rowHeight", 36); UIManager.put("Table.showHorizontalLines", true); UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.background", new Color(0xF8FAFF)); UIManager.put("Table.foreground", TABLE_TEXT);
            UIManager.put("Table.alternateRowColor", TABLE_ALT); UIManager.put("Table.gridColor", TABLE_GRID);
            UIManager.put("Table.selectionBackground", new Color(0xBFD3FF)); UIManager.put("Table.selectionForeground", TABLE_TEXT);
            UIManager.put("Table.selectionInactiveBackground", new Color(0xCFDDFB));
            UIManager.put("Table.selectionInactiveForeground", TABLE_TEXT);
            UIManager.put("TableHeader.background", TABLE_HEAD); UIManager.put("TableHeader.foreground", TABLE_TEXT);
            UIManager.put("TableHeader.font", font(Font.BOLD, 13)); UIManager.put("TableHeader.height", 38);
            UIManager.put("TableHeader.bottomSeparatorColor", TABLE_GRID); UIManager.put("TableHeader.separatorColor", TABLE_GRID);
            UIManager.put("Table.scrollPaneBorder", BorderFactory.createLineBorder(CARD_BORDER));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ===== Thẻ bo góc =====
    public static class Card extends JPanel {
        private final Color fill; private final Color border; private final int arc;
        public Card(LayoutManager lm) { this(lm, new Color(0x13204A, false), CARD_BORDER, 18); }
        public Card(LayoutManager lm, Color fill, Color border, int arc) {
            super(lm); this.fill = fill; this.border = border; this.arc = arc; setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 225));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
            g2.setColor(border);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text); l.setFont(font(style, size)); l.setForeground(color); return l;
    }

    // ===== Bảng: render dùng chung =====
    public static void styleTable(JTable t) {
        t.setRowHeight(38); t.setFillsViewportHeight(true); t.setIntercellSpacing(new Dimension(0, 1));
        t.setFont(font(Font.PLAIN, 13));
        JTableHeader h = t.getTableHeader(); h.setReorderingAllowed(false); h.setFont(font(Font.BOLD, 13));
    }
    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c); sp.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        sp.getViewport().setBackground(new Color(0xF8FAFF)); sp.getVerticalScrollBar().setUnitIncrement(16); return sp;
    }

    /** Ô trạng thái dạng "viên thuốc": xanh (đang chiếu / hoạt động), vàng (sắp chiếu), đỏ (ngừng/hết). */
    public static class PillRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            String s = v == null ? "" : v.toString(); String lo = s.toLowerCase();
            Color bg, fg;
            if (lo.contains("sắp") || lo.contains("chưa")) { bg = WARN_BG; fg = WARN_FG; }
            else if (lo.contains("ngừng") || lo.contains("hết") || lo.contains("nghỉ") || lo.contains("hủy") || lo.contains("khóa")) { bg = OFF_BG; fg = OFF_FG; }
            else { bg = OK_BG; fg = OK_FG; }
            l.setHorizontalAlignment(SwingConstants.CENTER); l.setForeground(fg); l.setFont(font(Font.BOLD, 12));
            l.setOpaque(false); l.putClientProperty("pillBg", bg); return l;
        }
        @Override protected void paintComponent(Graphics g) {
            Object bg = getClientProperty("pillBg");
            g.setColor(getBackground()); g.fillRect(0, 0, getWidth(), getHeight());
            if (bg instanceof Color && getText() != null && !getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2.getFontMetrics(getFont()); int w = fm.stringWidth(getText()) + 26, h = 24;
                g2.setColor((Color) bg); g2.fillRoundRect((getWidth() - w) / 2, (getHeight() - h) / 2, w, h, h, h); g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // ===== Poster phim =====
    private static final Map<String, ImageIcon> CACHE = new HashMap<>();
    public static String posterPath(String maPhim) {
        if (maPhim == null) return "";
        switch (maPhim) {
            case "1": return "img/avengers.jpg"; case "2": return "img/frozen2.jpg"; case "3": return "img/joker.jpg";
            case "4": return "img/spiderman.jpg"; case "5": return "img/theLionKing.jpg"; case "6": return "img/doraemon.jpg";
            case "7": return "img/kamen.jpg"; default: return "";
        }
    }
    /** Poster cắt vừa khung (cover), bo góc nhẹ. Không có ảnh thì vẽ khung thay thế. */
    public static ImageIcon poster(String maPhim, int w, int h) {
        String key = maPhim + "|" + w + "x" + h;
        ImageIcon cached = CACHE.get(key); if (cached != null) return cached;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int arc = Math.max(6, w / 12);
        g.setClip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        Image img = null; String p = posterPath(maPhim);
        if (!p.isEmpty() && new java.io.File(p).exists()) img = new ImageIcon(p).getImage();
        if (img != null && img.getWidth(null) > 0) {
            int iw = img.getWidth(null), ih = img.getHeight(null); double s = Math.max((double) w / iw, (double) h / ih);
            int dw = (int) Math.ceil(iw * s), dh = (int) Math.ceil(ih * s); g.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(0x24408F), 0, h, new Color(0x0F1B45))); g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 120)); g.setFont(font(Font.BOLD, Math.max(12, w / 3)));
            FontMetrics fm = g.getFontMetrics(); g.drawString("▶", (w - fm.stringWidth("▶")) / 2, h / 2 + fm.getAscent() / 3);
        }
        g.dispose(); ImageIcon ic = new ImageIcon(out); CACHE.put(key, ic); return ic;
    }
}
