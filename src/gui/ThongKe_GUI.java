package gui;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Thống kê dạng bảng, xuất báo cáo CSV. */
public class ThongKe_GUI extends JPanel {
    private final JComboBox<String> period = new JComboBox<>(new String[]{"Ngày", "Tuần", "Tháng", "Năm"});
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Kỳ thống kê", "Doanh thu VND"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JLabel total = new JLabel();
    public ThongKe_GUI() {
        super(new BorderLayout(8, 8)); service.Session.requireStaff();
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Thống kê theo:")); controls.add(period);
        JButton load = new JButton("Thống kê"); load.setMnemonic('T'); controls.add(load);
        JButton export = new JButton("Xuất CSV"); export.setMnemonic('X'); controls.add(export);
        load.addActionListener(e -> load()); export.addActionListener(e -> export());
        add(controls, BorderLayout.NORTH); add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(total, BorderLayout.SOUTH); load();
    }
    private void load() {
        try {
            var rows = new dao.ThongKeDAO().doanhThu(period.getSelectedItem().toString());
            model.setRowCount(0); BigDecimal sum = BigDecimal.ZERO;
            for (var row : rows) { model.addRow(new Object[]{row.period(), row.revenue().toPlainString()}); sum = sum.add(row.revenue()); }
            total.setText("Tổng doanh thu: " + String.format("%,.0f VND", sum));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi thống kê", JOptionPane.ERROR_MESSAGE); }
    }
    private void export() {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new java.io.File("doanh-thu.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (Files.exists(path) && JOptionPane.showConfirmDialog(this, "Ghi đè tệp đã có?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            StringBuilder csv = new StringBuilder("\uFEFFKy thong ke,Doanh thu VND\r\n");
            for (int i = 0; i < model.getRowCount(); i++) csv.append(model.getValueAt(i, 0)).append(',').append(model.getValueAt(i, 1)).append("\r\n");
            Files.writeString(path, csv, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Đã lưu báo cáo: " + path);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi xuất", JOptionPane.ERROR_MESSAGE); }
    }
    public static void main(String[] args) { Main_GUI.main(args); }
}
