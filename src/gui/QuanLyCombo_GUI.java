package gui;

import dao.ComboBapNuocDAO;
import entity.ComboBapNuoc;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Quản lý danh mục combo; ngừng bán để giữ nguyên các dòng hóa đơn đã phát sinh. */
public class QuanLyCombo_GUI extends JPanel {
    private static final String TAT_CA = "Tất cả trạng thái";
    private final ComboBapNuocDAO dao;
    private final Map<String, ComboBapNuoc> duLieu = new LinkedHashMap<>();
    private final NumberFormat tien = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã combo", "Tên combo", "Giá bán (VNĐ)", "Trạng thái", "Mô tả"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
        @Override public Class<?> getColumnClass(int col) { return col == 2 ? BigDecimal.class : String.class; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField timKiem = new JTextField(20);
    private final JComboBox<String> trangThai = new JComboBox<>(new String[]{
            TAT_CA, ComboBapNuocDAO.DANG_BAN, ComboBapNuocDAO.NGUNG_BAN});
    private final JButton sua = new JButton("Sửa"), doiTrangThai = new JButton("Ngừng bán");
    private final JLabel tongSo = new JLabel(" ");

    public QuanLyCombo_GUI() { this(new ComboBapNuocDAO()); }

    QuanLyCombo_GUI(ComboBapNuocDAO dao) {
        this.dao = dao;
        tien.setMaximumFractionDigits(2);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.getColumnModel().getColumn(2).setPreferredWidth(125);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setCellRenderer(new AppTheme.PillRenderer());
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(4));
        table.setDefaultRenderer(BigDecimal.class, new DefaultTableCellRenderer() {
            @Override protected void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(value == null ? "" : tien.format(value));
            }
        });

        JButton tim = new JButton("Tìm kiếm"), them = new JButton("Thêm"), lamMoi = new JButton("Làm mới");
        timKiem.putClientProperty("JTextField.placeholderText", "Tìm mã, tên hoặc mô tả combo...");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(timKiem); actions.add(tim); actions.add(them); actions.add(sua);
        actions.add(doiTrangThai); actions.add(lamMoi);
        JPanel chiTiet = Khung.chiTiet(table, model, null,
                new String[]{"Mã combo:", "Tên combo:", "Giá bán:", "Trạng thái:", "Mô tả:"}, row -> new String[]{
                        model.getValueAt(row, 0).toString(), bocChu(model.getValueAt(row, 1)),
                        tien.format(model.getValueAt(row, 2)) + " VNĐ", model.getValueAt(row, 3).toString(),
                        bocChu(model.getValueAt(row, 4))});
        Khung.dung(this, "Quản Lý Combo Bắp Nước", actions, new JScrollPane(table), chiTiet, tongSo, trangThai);

        them.addActionListener(e -> moForm(null));
        sua.addActionListener(e -> { ComboBapNuoc combo = comboDangChon(); if (combo != null) moForm(combo); });
        doiTrangThai.addActionListener(e -> doiTrangThai());
        lamMoi.addActionListener(e -> { timKiem.setText(""); trangThai.setSelectedItem(TAT_CA); taiDuLieu(null); });
        tim.addActionListener(e -> loc());
        timKiem.addActionListener(e -> loc());
        timKiem.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loc(); }
            public void removeUpdate(DocumentEvent e) { loc(); }
            public void changedUpdate(DocumentEvent e) { loc(); }
        });
        trangThai.addActionListener(e -> loc());
        table.getSelectionModel().addListSelectionListener(e -> capNhatNut());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0) sua.doClick();
            }
        });
        capNhatNut();
        taiDuLieu(null);
    }

    private void taiDuLieu(String maChon) {
        try {
            List<ComboBapNuoc> danhSach = dao.layTatCaCombo();
            table.clearSelection();
            model.setRowCount(0);
            duLieu.clear();
            for (ComboBapNuoc combo : danhSach) {
                duLieu.put(combo.getMaCombo(), combo);
                model.addRow(new Object[]{combo.getMaCombo(), combo.getTenCombo(), combo.getGiaBan(),
                        combo.getTrangThai(), combo.getMoTa() == null ? "" : combo.getMoTa()});
            }
            loc();
            if (maChon != null) {
                for (int row = 0; row < model.getRowCount(); row++) {
                    if (maChon.equals(model.getValueAt(row, 0))) {
                        int viewRow = table.convertRowIndexToView(row);
                        if (viewRow >= 0) table.setRowSelectionInterval(viewRow, viewRow);
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            baoLoiSql("Không tải được danh sách combo. Hãy kiểm tra kết nối rồi bấm Làm mới.", ex);
        }
    }

    private void loc() {
        final String tuKhoa = chuanHoa(timKiem.getText().trim());
        final String locTrangThai = (String) trangThai.getSelectedItem();
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (!TAT_CA.equals(locTrangThai) && !locTrangThai.equals(entry.getStringValue(3))) return false;
                return tuKhoa.isEmpty() || chuanHoa(entry.getStringValue(0)).contains(tuKhoa)
                        || chuanHoa(entry.getStringValue(1)).contains(tuKhoa)
                        || chuanHoa(entry.getStringValue(4)).contains(tuKhoa);
            }
        });
        if (table.getRowCount() > 0 && table.getSelectedRow() < 0) table.setRowSelectionInterval(0, 0);
        tongSo.setText("Hiển thị " + table.getRowCount() + " / " + model.getRowCount() + " combo");
        capNhatNut();
    }

    private ComboBapNuoc comboDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return duLieu.get(model.getValueAt(table.convertRowIndexToModel(row), 0).toString());
    }

    private void capNhatNut() {
        ComboBapNuoc combo = comboDangChon();
        sua.setEnabled(combo != null);
        doiTrangThai.setEnabled(combo != null);
        doiTrangThai.setText(combo != null && ComboBapNuocDAO.NGUNG_BAN.equals(combo.getTrangThai()) ? "Bán lại" : "Ngừng bán");
    }

    private void moForm(ComboBapNuoc cu) {
        JTextField ma = new JTextField(cu == null ? "" : cu.getMaCombo(), 24);
        JTextField ten = new JTextField(cu == null ? "" : cu.getTenCombo(), 24);
        JTextField gia = new JTextField(cu == null ? "" : cu.getGiaBan().stripTrailingZeros().toPlainString(), 24);
        JTextArea moTa = new JTextArea(cu == null ? "" : cu.getMoTa(), 3, 24);
        moTa.setLineWrap(true); moTa.setWrapStyleWord(true);
        JComboBox<String> status = new JComboBox<>(new String[]{ComboBapNuocDAO.DANG_BAN, ComboBapNuocDAO.NGUNG_BAN});
        if (cu != null) status.setSelectedItem(cu.getTrangThai());
        ma.setEditable(cu == null);
        Object[] form = {"Mã combo:", ma, "Tên combo:", ten, "Giá bán (VNĐ, ví dụ 85000 hoặc 85000.50):", gia,
                "Trạng thái:", status, "Mô tả:", new JScrollPane(moTa)};
        while (JOptionPane.showConfirmDialog(this, form, cu == null ? "Thêm combo" : "Sửa combo",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                String giaNhap = gia.getText().trim();
                if (!giaNhap.matches("[0-9]+(?:[.][0-9]{1,2})?")) {
                    throw new IllegalArgumentException("Giá bán phải là số không âm, tối đa 2 chữ số thập phân. Nhập ví dụ 85000 hoặc 85000.50.");
                }
                ComboBapNuoc moi = new ComboBapNuoc(ma.getText().trim(), ten.getText().trim(),
                        new BigDecimal(giaNhap), moTa.getText().trim(), (String) status.getSelectedItem());
                ComboBapNuocDAO.kiemTraDuLieu(moi);
                boolean daLuu = cu == null ? dao.themCombo(moi) : dao.suaCombo(moi);
                if (!daLuu) {
                    JOptionPane.showMessageDialog(this, "Combo không còn tồn tại. Hãy làm mới danh sách.", "Không lưu được", JOptionPane.WARNING_MESSAGE);
                    taiDuLieu(null);
                    return;
                }
                taiDuLieu(moi.getMaCombo());
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                baoLoiSql("Không lưu được combo. Hãy kiểm tra kết nối rồi thử lại.", ex);
            }
        }
    }

    private void doiTrangThai() {
        ComboBapNuoc combo = comboDangChon();
        if (combo == null) return;
        String moi = ComboBapNuocDAO.DANG_BAN.equals(combo.getTrangThai()) ? ComboBapNuocDAO.NGUNG_BAN : ComboBapNuocDAO.DANG_BAN;
        String hanhDong = ComboBapNuocDAO.NGUNG_BAN.equals(moi) ? "Ngừng bán" : "Bán lại";
        if (JOptionPane.showConfirmDialog(this, hanhDong + " combo " + combo.getMaCombo() + " - " + combo.getTenCombo() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            if (!dao.capNhatTrangThai(combo.getMaCombo(), moi)) {
                JOptionPane.showMessageDialog(this, "Combo không còn tồn tại. Hãy làm mới danh sách.", "Không cập nhật được", JOptionPane.WARNING_MESSAGE);
            }
            taiDuLieu(combo.getMaCombo());
        } catch (SQLException ex) {
            baoLoiSql("Không cập nhật được trạng thái combo. Hãy kiểm tra kết nối rồi thử lại.", ex);
        }
    }

    private void baoLoiSql(String thongBao, SQLException ex) {
        ex.printStackTrace();
        for (SQLException loi = ex; loi != null; loi = loi.getNextException()) {
            if (loi.getErrorCode() == 2601 || loi.getErrorCode() == 2627) {
                thongBao = "Mã combo đã tồn tại. Hãy nhập mã khác.";
                break;
            }
        }
        JOptionPane.showMessageDialog(this, thongBao, "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private static String chuanHoa(String text) {
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd');
    }

    private static String bocChu(Object value) {
        String text = value == null ? "" : value.toString();
        return "<html><div style='width:160px'>" + text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\n", "<br>") + "</div></html>";
    }
}