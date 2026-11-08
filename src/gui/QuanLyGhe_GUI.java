package gui;

import dao.PhongGheDAO;

import service.Session;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLyGhe_GUI extends JPanel {
    private final PhongGheDAO dao = new PhongGheDAO();
    private final JComboBox<String> rooms = new JComboBox<>();
    private final DefaultTableModel model =
            new DefaultTableModel(new String[] {"Mã ghế", "Vị trí", "Loại ghế"}, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
    private final JLabel total = new JLabel();

    public QuanLyGhe_GUI() {
        super(new BorderLayout(10, 10));
        Session.requireStaff();
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField row = new JTextField(3);
        JSpinner count = new JSpinner(new SpinnerNumberModel(10, 1, 30, 1));
        JButton add = new JButton("Thêm dãy ghế");
        JButton refresh = new JButton("Làm mới");
        controls.add(new JLabel("Phòng:"));
        controls.add(rooms);
        controls.add(new JLabel("Dãy A–Z:"));
        controls.add(row);
        controls.add(new JLabel("Số ghế:"));
        controls.add(count);
        controls.add(add);
        controls.add(refresh);
        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(total, BorderLayout.SOUTH);
        try {
            for (String[] room : dao.phong()) rooms.addItem(room[0] + " - " + room[1]);
        } catch (Exception ex) {
            error(ex);
        }
        rooms.addActionListener(e -> load());
        refresh.addActionListener(e -> load());
        add.addActionListener(
                e -> {
                    if (rooms.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "Chưa có phòng chiếu.");
                        return;
                    }
                    try {
                        dao.themDay(
                                roomId(),
                                row.getText().trim().toUpperCase(java.util.Locale.ROOT),
                                (Integer) count.getValue());
                        row.setText("");
                        load();
                        JOptionPane.showMessageDialog(
                                this, "Đã thêm dãy ghế và cập nhật các suất chiếu chưa bắt đầu.");
                    } catch (Exception ex) {
                        error(ex);
                    }
                });
        load();
    }

    private String roomId() {
        return rooms.getSelectedItem().toString().split(" - ", 2)[0];
    }

    private void load() {
        model.setRowCount(0);
        if (rooms.getSelectedItem() == null) return;
        try {
            for (String[] seat : dao.ghe(roomId())) model.addRow(seat);
            total.setText(
                    "Tổng số ghế: "
                            + model.getRowCount()
                            + ". Dãy mới được áp dụng cho các suất chiếu trong tương lai.");
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(
                this, ex.getMessage(), "Không thể cập nhật ghế", JOptionPane.ERROR_MESSAGE);
    }
}
