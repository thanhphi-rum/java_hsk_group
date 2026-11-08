package gui;

import dao.DanhMucDAO;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/** Biểu mẫu danh mục cơ bản, không tùy biến màu hoặc giao diện nút. */
public class DanhMuc_GUI extends JPanel {
    private final DanhMucDAO dao;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField keyword = new JTextField(18);
    private final JComboBox<String> field = new JComboBox<>();
    private final JTextField value = new JTextField(16);
    private final JLabel count = new JLabel();

    public DanhMuc_GUI(String name) {
        super(new BorderLayout(8, 8));
        service.Session.requireStaff();
        dao = new DanhMucDAO(name);
        model = new DefaultTableModel(dao.fields().stream().map(DanhMucDAO.Field::label).toArray(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setPreferredWidth(150);
        sorter = new TableRowSorter<>(model); table.setRowSorter(sorter);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Từ khóa:")); filters.add(keyword);
        dao.fields().forEach(f -> field.addItem(f.label()));
        filters.add(new JLabel("Kết hợp cột:")); filters.add(field); filters.add(value);
        JButton search = button("Tìm kiếm", 'T', e -> filter()); filters.add(search);
        keyword.addActionListener(e -> filter()); value.addActionListener(e -> filter());
        add(filters, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(button("Thêm", 'M', e -> form(false, false)));
        actions.add(button("Sửa", 'S', e -> form(true, false)));
        actions.add(button("Chi tiết", 'C', e -> form(true, true)));
        actions.add(button("Xóa", 'X', e -> remove()));
        actions.add(button("Làm mới", 'L', e -> load())); actions.add(count);
        add(actions, BorderLayout.SOUTH);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new AbstractAction(){public void actionPerformed(ActionEvent e){load();}});
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control F"), "find");
        getActionMap().put("find", new AbstractAction(){public void actionPerformed(ActionEvent e){keyword.requestFocusInWindow();}});
        load();
    }

    private JButton button(String title, char key, ActionListener action) {
        JButton b = new JButton(title); b.setMnemonic(key); b.addActionListener(action); return b;
    }

    private void load() {
        try {
            List<Object[]> rows = dao.list();
            model.setRowCount(0); rows.forEach(model::addRow); filter();
        } catch (Exception ex) { error(ex); }
    }

    private void filter() {
        String all = keyword.getText().trim().toLowerCase(Locale.ROOT);
        String extra = value.getText().trim().toLowerCase(Locale.ROOT);
        int col = field.getSelectedIndex();
        sorter.setRowFilter(new RowFilter<>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> row) {
                boolean match = all.isEmpty();
                for (int i = 0; i < row.getValueCount(); i++) match |= row.getStringValue(i).toLowerCase(Locale.ROOT).contains(all);
                return match && (extra.isEmpty() || row.getStringValue(col).toLowerCase(Locale.ROOT).contains(extra));
            }
        });
        count.setText(table.getRowCount() + " / " + model.getRowCount() + " bản ghi");
    }

    private void form(boolean edit, boolean readOnly) {
        int row = table.getSelectedRow();
        if (edit && row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng trước."); return; }
        if (edit) row = table.convertRowIndexToModel(row);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        JPanel content = new JPanel(new BorderLayout(16, 8));
        content.add(panel, BorderLayout.CENTER);
        List<JComponent> inputs = new ArrayList<>();
        for (int i = 0; i < dao.fields().size(); i++) {
            DanhMucDAO.Field f = dao.fields().get(i);
            Object old = edit ? model.getValueAt(row, i) : null;
            JComponent input;
            if (f.name().equals("PosterPath")) {
                PosterPicker picker = new PosterPicker(old == null ? null : old.toString(), readOnly);
                picker.setBorder(BorderFactory.createTitledBorder("Ảnh phim"));
                content.add(picker, BorderLayout.EAST); inputs.add(picker); continue;
            } else if (f.name().equals("TrangThai")) {
                JComboBox<String> box = new JComboBox<>(dao.table().equals("Phim") ? new String[]{"Đang Chiếu", "Sắp Chiếu", "Ngừng Chiếu"} : new String[]{"Hoạt động", "Bảo trì"});
                if (old != null) box.setSelectedItem(old.toString()); input = box;
            } else if (f.name().equals("MaLoaiPhim")) {
                JComboBox<String> box = new JComboBox<>();
                try { for (Object[] genre : new DanhMucDAO("LoaiPhim").list()) box.addItem(genre[0] + " - " + genre[1]); }
                catch (Exception ex) { error(ex); return; }
                if (old != null) for (int j = 0; j < box.getItemCount(); j++) if (box.getItemAt(j).startsWith(old + " - ")) box.setSelectedIndex(j);
                input = box;
            } else input = new JTextField(old == null ? (f.name().equals("SucChua") || f.name().equals("DiemTichLuy") ? "0" : "") : old.toString(), 24);
            input.setEnabled(!readOnly && !(edit && i == 0) && !f.name().equals("SucChua"));
            JLabel label = new JLabel(f.label() + (f.required() ? " *" : "") + (f.type().equals("d") ? " yyyy-MM-dd" : ""));
            label.setLabelFor(input); panel.add(label); panel.add(input); inputs.add(input);
        }
        if (readOnly) { JOptionPane.showMessageDialog(this, content, "Chi tiết", JOptionPane.PLAIN_MESSAGE); return; }
        while (JOptionPane.showConfirmDialog(this, content, edit ? "Cập nhật" : "Thêm mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Object[] values = new Object[inputs.size()];
                for (int i = 0; i < values.length; i++) {
                    JComponent input = inputs.get(i);
                    Object selected = input instanceof PosterPicker picker ? picker.value() : input instanceof JComboBox<?> box ? box.getSelectedItem() : ((JTextField) input).getText();
                    String text = selected == null ? "" : selected.toString().trim();
                    DanhMucDAO.Field f = dao.fields().get(i);
                    if (f.name().equals("MaLoaiPhim")) text = text.split(" - ", 2)[0];
                    if (text.isEmpty() && f.required()) throw new IllegalArgumentException("Nhập " + f.label());
                    values[i] = text.isEmpty() ? null : switch (f.type()) {
                        case "i" -> Integer.valueOf(text); case "n" -> new BigDecimal(text);
                        case "d" -> java.sql.Date.valueOf(LocalDate.parse(text)); default -> text;
                    };
                }
                if (!values[0].toString().matches("[A-Za-z0-9_-]{1,10}")) throw new IllegalArgumentException("Mã gồm 1–10 chữ không dấu, số, gạch ngang hoặc gạch dưới.");
                for (int i = 0; i < values.length; i++) {
                    String name = dao.fields().get(i).name();
                    if (name.equals("SoDienThoai") && !values[i].toString().matches("[0-9+]{9,15}")) throw new IllegalArgumentException("Số điện thoại gồm 9–15 chữ số, có thể bắt đầu bằng +.");
                    if (name.equals("Email") && values[i] != null && !values[i].toString().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Email không hợp lệ.");
                    if (name.equals("NgaySinh") && ((java.sql.Date) values[i]).toLocalDate().isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngày sinh không được trong tương lai.");
                    if (name.equals("DiemTichLuy") && (Integer) values[i] < 0) throw new IllegalArgumentException("Điểm tích lũy không âm.");
                }
                if (dao.table().equals("Phim") && (Integer) values[3] <= 0) throw new IllegalArgumentException("Thời lượng phải lớn hơn 0.");
                if (dao.table().equals("KhuyenMai")) {
                    BigDecimal percent = (BigDecimal) values[3];
                    if (percent.signum() < 0 || percent.compareTo(new BigDecimal("100")) > 0) throw new IllegalArgumentException("Phần trăm từ 0 đến 100.");
                    if (((java.sql.Date) values[4]).after((java.sql.Date) values[5])) throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
                }
                String copiedImage = null;
                try {
                    for (int i = 0; i < inputs.size(); i++) {
                        if (inputs.get(i) instanceof PosterPicker picker) {
                            values[i] = picker.saveImage();
                            if (picker.hasNewImage()) copiedImage = (String) values[i];
                        }
                    }
                    dao.save(values, edit);
                } catch (Exception ex) {
                    if (copiedImage != null) {
                        try { java.nio.file.Files.deleteIfExists(util.ProjectPaths.resolve(copiedImage)); }
                        catch (java.io.IOException cleanup) { ex.addSuppressed(cleanup); }
                    }
                    throw ex;
                }
                load(); return;
            } catch (Exception ex) { error(ex); }
        }
    }

    private void remove() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa."); return; }
        Object id = table.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa " + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { dao.delete(id); load(); } catch (Exception ex) { error(ex); }
    }

    private void error(Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thực hiện được", JOptionPane.ERROR_MESSAGE); }
}
