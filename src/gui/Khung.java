package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.function.Function;

/** Khung bố cục dùng chung cho các màn quản lý: tiêu đề, thẻ bảng bên trái, thẻ chi tiết bên phải. */
public final class Khung {
    private Khung() {}

    /**
     * Dựng bố cục cho màn quản lý. Thanh điều khiển cũ (control) được tách: phần tìm kiếm (ô nhập + nút tìm đầu tiên)
     * lên đầu thẻ bảng, các nút còn lại xuống cuối thẻ.
     */
    public static void dung(JPanel host, String title, JPanel control, JComponent bang, JComponent phai, JComponent duoiBang, JComponent themTrenDau) {
        host.removeAll();
        host.setLayout(new BorderLayout(0, 10));
        host.setOpaque(false);
        host.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        if (title != null) host.add(AppTheme.label(title, Font.BOLD, 26, Color.WHITE), BorderLayout.NORTH);

        Component[] cs = control == null ? new Component[0] : control.getComponents();
        int btn = -1; boolean coO = false;
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] instanceof JTextField) coO = true;
            if (cs[i] instanceof JButton) { if (coO) btn = i; break; }
        }
        JPanel top = null;
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);
        if (btn >= 0) {
            top = new JPanel(new BorderLayout(10, 0)); top.setOpaque(false);
            JPanel east = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); east.setOpaque(false);
            if (themTrenDau != null) east.add(themTrenDau);
            for (int i = 0; i < cs.length; i++) {
                Component c = cs[i];
                if (i <= btn) {
                    if (c instanceof JTextField) { ((JComponent) c).setPreferredSize(new Dimension(200, 36)); top.add(c, BorderLayout.CENTER); }
                    else if (c instanceof JButton) { ((JComponent) c).setPreferredSize(new Dimension(110, 36)); east.add(c); }
                } else bottom.add(c);
            }
            top.add(east, BorderLayout.EAST);
        } else {
            for (Component c : cs) bottom.add(c);
            if (themTrenDau != null) { top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); top.setOpaque(false); top.add(themTrenDau); }
        }
        for (Component c : bottom.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                b.setPreferredSize(new Dimension(Math.max(78, b.getPreferredSize().width + 8), 36));
                String t = b.getText() == null ? "" : b.getText();
                if (t.startsWith("Xóa") || t.startsWith("Hủy")) { b.setBackground(new Color(0xD9344A)); }
            }
        }

        AppTheme.Card trai = new AppTheme.Card(new BorderLayout(0, 12));
        if (top != null) trai.add(top, BorderLayout.NORTH);
        JPanel giua = new JPanel(new BorderLayout(0, 12)); giua.setOpaque(false);
        if (bang instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) bang;
            sp.setBorder(BorderFactory.createLineBorder(AppTheme.CARD_BORDER));
            sp.getViewport().setBackground(new Color(0xF8FAFF));
            sp.getVerticalScrollBar().setUnitIncrement(16);
            if (sp.getViewport().getView() instanceof JTable) AppTheme.styleTable((JTable) sp.getViewport().getView());
        }
        giua.add(bang, BorderLayout.CENTER);
        if (duoiBang != null) giua.add(duoiBang, BorderLayout.SOUTH);
        trai.add(giua, BorderLayout.CENTER);
        if (bottom.getComponentCount() > 0) trai.add(bottom, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(14, 0)); body.setOpaque(false);
        body.add(trai, BorderLayout.CENTER);
        if (phai != null) body.add(phai, BorderLayout.EAST);
        host.add(body, BorderLayout.CENTER);
        host.revalidate();
    }

    public static void dung(JPanel host, String title, JPanel control, JComponent bang, JComponent phai) {
        dung(host, title, control, bang, phai, null, null);
    }

    /** Thẻ chi tiết: đầu thẻ (ảnh/avatar) + các cặp nhãn: giá trị, tự cập nhật theo dòng đang chọn. */
    public static JPanel chiTiet(JTable t, DefaultTableModel m, JComponent dau, String[] nhan, Function<Integer, String[]> giaTri) {
        AppTheme.Card card = new AppTheme.Card(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(330, 0));
        if (dau != null) {
            JPanel d = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); d.setOpaque(false); d.add(dau);
            card.add(d, BorderLayout.NORTH);
        }
        JPanel luoi = new JPanel(new GridBagLayout()); luoi.setOpaque(false);
        JLabel[] vals = new JLabel[nhan.length];
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 10); c.anchor = GridBagConstraints.NORTHWEST;
        for (int i = 0; i < nhan.length; i++) {
            c.gridy = i; c.gridx = 0; c.weightx = 0;
            luoi.add(AppTheme.label(nhan[i], Font.PLAIN, 13, AppTheme.MUTED), c);
            vals[i] = AppTheme.label(" ", Font.BOLD, 14, Color.WHITE);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            luoi.add(vals[i], c); c.fill = GridBagConstraints.NONE;
        }
        c.gridy = nhan.length; c.gridx = 0; c.weighty = 1; luoi.add(Box.createGlue(), c);
        card.add(luoi, BorderLayout.CENTER);

        Runnable capNhat = () -> {
            int r = t.getSelectedRow();
            String[] v = null;
            if (r >= 0) {
                try { v = giaTri.apply(t.convertRowIndexToModel(r)); } catch (Exception ex) { v = null; }
            }
            for (int i = 0; i < vals.length; i++) vals[i].setText(v == null || i >= v.length || v[i] == null || v[i].isEmpty() ? " " : v[i]);
        };
        t.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) capNhat.run(); });
        m.addTableModelListener(e -> SwingUtilities.invokeLater(() -> {
            if (t.getRowCount() > 0 && t.getSelectedRow() < 0) t.setRowSelectionInterval(0, 0); else capNhat.run();
        }));
        return card;
    }

    /** Ảnh đại diện tròn (hình người). */
    public static class Avatar extends JComponent {
        private final int size;
        public Avatar(int size) { this.size = size; setPreferredSize(new Dimension(size, size)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setClip(new Ellipse2D.Float(0, 0, size, size));
            g.setColor(new Color(0xDCE6FA)); g.fillRect(0, 0, size, size);
            g.setColor(new Color(0x2B3A67));
            g.fill(new Ellipse2D.Float(size * 0.33f, size * 0.2f, size * 0.34f, size * 0.34f));
            g.fill(new Ellipse2D.Float(size * 0.16f, size * 0.62f, size * 0.68f, size * 0.62f));
            g.setClip(null); g.setColor(new Color(0x4A80FF)); g.setStroke(new BasicStroke(2f));
            g.draw(new Ellipse2D.Float(1, 1, size - 2, size - 2));
            g.dispose();
        }
    }
}
