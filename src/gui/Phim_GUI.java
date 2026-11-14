package gui;

import connect.ConnectDB;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Danh sách phim: tìm kiếm + lọc thể loại, xem poster/chi tiết và bấm "Đặt vé". */
public class Phim_GUI extends JPanel {
    private static final String TAT_CA = "Tất cả thể loại";
    private static final int C_POSTER = 0, C_TEN = 1, C_LOAI = 2, C_TL = 3, C_TT = 4;

    /** Một dòng dữ liệu phim. */
    private static class Dong {
        String ma, ten, loai, trangThai, moTa, tuoi, daoDien, dienVien; int phut; java.sql.Date ngay;
    }

    private final List<Dong> data = new ArrayList<>();
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Poster", "Tên phim", "Thể loại", "Thời lượng", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) { return c == C_POSTER ? Icon.class : String.class; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField tfTim = new JTextField();
    private final JComboBox<String> cbLoai = new JComboBox<>();
    private final JLabel lblPoster = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblTen = AppTheme.label(" ", Font.BOLD, 16, Color.WHITE);
    private final JLabel lblInfo = AppTheme.label(" ", Font.PLAIN, 12, AppTheme.MUTED);
    private final JButton btnChiTiet = new JButton("Xem chi tiết");
    private final JButton btnDatVe = new JButton("Đặt vé");

    public Phim_GUI() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(AppTheme.label("Phim", Font.BOLD, 26, Color.WHITE), BorderLayout.NORTH);

        // ===== Cột trái: tìm kiếm + bảng =====
        AppTheme.Card trai = new AppTheme.Card(new BorderLayout(0, 12));
        tfTim.putClientProperty("JTextField.placeholderText", "Tìm kiếm phim...");
        tfTim.setPreferredSize(new Dimension(300, 36));
        cbLoai.setPreferredSize(new Dimension(200, 36));
        JPanel tim = new JPanel(new BorderLayout(10, 0)); tim.setOpaque(false);
        tim.add(tfTim, BorderLayout.CENTER); tim.add(cbLoai, BorderLayout.EAST);
        trai.add(tim, BorderLayout.NORTH);

        table.setModel(model); table.setRowSorter(sorter);
        AppTheme.styleTable(table); table.setRowHeight(64);
        table.getColumnModel().getColumn(C_POSTER).setPreferredWidth(70); table.getColumnModel().getColumn(C_POSTER).setMaxWidth(80);
        table.getColumnModel().getColumn(C_TEN).setPreferredWidth(220);
        table.getColumnModel().getColumn(C_TT).setCellRenderer(new AppTheme.PillRenderer());
        DefaultTableCellRenderer pos = new DefaultTableCellRenderer(); pos.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(C_POSTER).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, null, s, f, r, c);
                l.setIcon((Icon) v); l.setHorizontalAlignment(CENTER); return l;
            }
        });
        DefaultTableCellRenderer ten = new DefaultTableCellRenderer(); ten.setFont(AppTheme.font(Font.BOLD, 13));
        table.getColumnModel().getColumn(C_TEN).setCellRenderer(ten);
        trai.add(AppTheme.scroll(table), BorderLayout.CENTER);

        // ===== Cột phải: xem trước =====
        AppTheme.Card phai = new AppTheme.Card(new BorderLayout(0, 10));
        phai.setPreferredSize(new Dimension(290, 0));
        lblPoster.setPreferredSize(new Dimension(240, 340));
        JPanel giua = new JPanel(); giua.setOpaque(false); giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{lblPoster, lblTen, lblInfo}) c.setAlignmentX(Component.CENTER_ALIGNMENT);
        giua.add(lblPoster); giua.add(Box.createVerticalStrut(10)); giua.add(lblTen); giua.add(Box.createVerticalStrut(2)); giua.add(lblInfo);
        phai.add(giua, BorderLayout.CENTER);
        JPanel nut = new JPanel(new GridLayout(2, 1, 0, 8)); nut.setOpaque(false);
        btnChiTiet.setPreferredSize(new Dimension(100, 38)); btnDatVe.setPreferredSize(new Dimension(100, 40));
        nut.add(btnChiTiet); nut.add(btnDatVe);
        phai.add(nut, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(14, 0)); body.setOpaque(false);
        body.add(trai, BorderLayout.CENTER); body.add(phai, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        // ===== Sự kiện =====
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) hienXemTruoc(); });
        tfTim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loc(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loc(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loc(); }
        });
        cbLoai.addActionListener(e -> loc());
        btnChiTiet.addActionListener(e -> xemChiTiet());
        btnDatVe.addActionListener(e -> datVe());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) datVe(); }
        });

        taiDuLieu();
    }

    private void taiDuLieu() {
        String sql = "SELECT p.MaPhim,p.TenPhim,p.ThoiLuong,p.TrangThai,p.MoTa,p.DaoDien,p.DienVien,p.GioiHanTuoi,p.NgayKhoiChieu,l.TenLoaiPhim "
                + "FROM Phim p LEFT JOIN LoaiPhim l ON l.MaLoaiPhim=p.MaLoaiPhim ORDER BY CASE WHEN p.TrangThai=N'Đang Chiếu' THEN 0 ELSE 1 END, p.TenPhim";
        Set<String> loai = new LinkedHashSet<>();
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                Dong d = new Dong();
                d.ma = r.getString("MaPhim"); d.ten = r.getString("TenPhim"); d.phut = r.getInt("ThoiLuong"); d.trangThai = r.getString("TrangThai");
                d.moTa = r.getString("MoTa"); d.daoDien = r.getString("DaoDien"); d.dienVien = r.getString("DienVien");
                d.tuoi = r.getString("GioiHanTuoi"); d.ngay = r.getDate("NgayKhoiChieu"); d.loai = r.getString("TenLoaiPhim");
                data.add(d); if (d.loai != null) loai.add(d.loai);
                model.addRow(new Object[]{AppTheme.poster(d.ma, 40, 56), d.ten, d.loai == null ? "" : d.loai, d.phut + " phút", d.trangThai});
            }
        } catch (Exception e) { e.printStackTrace(); }
        cbLoai.addItem(TAT_CA);
        for (String l : loai) cbLoai.addItem(l);
        if (model.getRowCount() > 0) table.setRowSelectionInterval(0, 0); else hienXemTruoc();
    }

    private void loc() {
        List<RowFilter<DefaultTableModel, Integer>> fs = new ArrayList<>();
        String k = tfTim.getText().trim();
        if (!k.isEmpty()) fs.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(k), C_TEN, C_LOAI));
        Object l = cbLoai.getSelectedItem();
        if (l != null && !TAT_CA.equals(l)) fs.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(l.toString()) + "$", C_LOAI));
        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.andFilter(fs));
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0, 0); else hienXemTruoc();
    }

    private Dong dangChon() {
        int r = table.getSelectedRow();
        return r < 0 ? null : data.get(table.convertRowIndexToModel(r));
    }

    private void hienXemTruoc() {
        Dong d = dangChon();
        boolean co = d != null;
        btnChiTiet.setEnabled(co); btnDatVe.setEnabled(co && "Đang Chiếu".equalsIgnoreCase(d.trangThai));
        if (!co) { lblPoster.setIcon(null); lblTen.setText(" "); lblInfo.setText(" "); return; }
        lblPoster.setIcon(AppTheme.poster(d.ma, 240, 340));
        lblTen.setText(d.ten);
        lblInfo.setText((d.loai == null ? "" : d.loai + " · ") + d.phut + " phút" + (d.tuoi == null || d.tuoi.isBlank() ? "" : " · " + d.tuoi));
    }

    private void xemChiTiet() {
        Dong d = dangChon(); if (d == null) return;
        String h = "<html><body style='width:340px'><h2 style='margin:0'>" + esc(d.ten) + "</h2><br>"
                + dong("Thể loại", d.loai) + dong("Thời lượng", d.phut + " phút") + dong("Giới hạn tuổi", d.tuoi)
                + dong("Khởi chiếu", d.ngay == null ? "" : d.ngay.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                + dong("Trạng thái", d.trangThai) + dong("Đạo diễn", d.daoDien) + dong("Diễn viên", d.dienVien)
                + (d.moTa == null || d.moTa.isBlank() ? "" : "<br>" + esc(d.moTa)) + "</body></html>";
        JOptionPane.showMessageDialog(this, new JLabel(h, AppTheme.poster(d.ma, 120, 170), SwingConstants.LEFT), "Chi tiết phim", JOptionPane.PLAIN_MESSAGE);
    }

    private static String dong(String k, String v) { return v == null || v.isBlank() ? "" : "<b>" + k + ":</b> " + esc(v) + "<br>"; }
    private static String esc(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    private void datVe() {
        Dong d = dangChon();
        if (d == null) return;
        if (!"Đang Chiếu".equalsIgnoreCase(d.trangThai)) {
            JOptionPane.showMessageDialog(this, "Phim này chưa chiếu nên chưa thể đặt vé.", "Đặt vé", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MainFrame.getInstance().showBanVe(d.ma);
    }
}
