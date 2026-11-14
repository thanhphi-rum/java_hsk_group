package gui;

import dao.ComboBapNuoc_DAO;
import entity.ComboBapNuoc;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/** Màn hình Swing đơn giản để thêm/sửa và ngừng bán combo. */
public class ComboBapNuoc_GUI extends JFrame {
    private final ComboBapNuoc_DAO comboDAO;
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtGia = new JTextField();
    private final JTextField txtMoTa = new JTextField();
    private final JComboBox<String> cboTrangThai =
            new JComboBox<String>(new String[] {"Đang bán", "Ngừng bán"});
    private final DefaultTableModel model;
    private final JTable table;

    public ComboBapNuoc_GUI(Connection connection) {
        comboDAO = new ComboBapNuoc_DAO(connection);
        setTitle("Quản lý combo bắp nước");
        setSize(760, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Mã combo:"));
        form.add(txtMa);
        form.add(new JLabel("Tên combo:"));
        form.add(txtTen);
        form.add(new JLabel("Giá bán:"));
        form.add(txtGia);
        form.add(new JLabel("Mô tả:"));
        form.add(txtMoTa);
        form.add(new JLabel("Trạng thái:"));
        form.add(cboTrangThai);
        add(form, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[] {"Mã combo", "Tên combo", "Giá bán", "Mô tả", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton btnThem = new JButton("Thêm");
        JButton btnCapNhat = new JButton("Cập nhật");
        JButton btnLamMoi = new JButton("Làm mới");
        buttons.add(btnThem);
        buttons.add(btnCapNhat);
        buttons.add(btnLamMoi);
        add(buttons, BorderLayout.SOUTH);

        btnThem.addActionListener(event -> themCombo());
        btnCapNhat.addActionListener(event -> capNhatCombo());
        btnLamMoi.addActionListener(event -> lamMoi());
        table.getSelectionModel().addListSelectionListener(event -> napDongDangChon());
        taiDanhSach();
    }

    private ComboBapNuoc layComboTuForm() {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String moTa = txtMoTa.getText().trim();
        String trangThai = (String) cboTrangThai.getSelectedItem();
        if (ma.isEmpty() || ten.isEmpty()) {
            throw new IllegalArgumentException("Mã và tên combo không được để trống.");
        }
        BigDecimal gia;
        try {
            gia = new BigDecimal(txtGia.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá combo phải là số.");
        }
        if (gia.signum() < 0) {
            throw new IllegalArgumentException("Giá combo không được âm.");
        }
        return new ComboBapNuoc(ma, ten, gia, moTa, trangThai);
    }

    private void themCombo() {
        try {
            if (comboDAO.themCombo(layComboTuForm())) {
                JOptionPane.showMessageDialog(this, "Đã thêm combo.");
                taiDanhSach();
                lamMoiForm();
            }
        } catch (SQLException | IllegalArgumentException ex) {
            baoLoi(ex);
        }
    }

    private void capNhatCombo() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một combo trong bảng trước.");
            return;
        }
        try {
            if (comboDAO.capNhatCombo(layComboTuForm())) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật combo.");
                taiDanhSach();
                lamMoiForm();
            }
        } catch (SQLException | IllegalArgumentException ex) {
            baoLoi(ex);
        }
    }

    private void taiDanhSach() {
        try {
            ArrayList<ComboBapNuoc> danhSach = comboDAO.layTatCa();
            model.setRowCount(0);
            for (ComboBapNuoc combo : danhSach) {
                model.addRow(new Object[] {combo.getMaCombo(), combo.getTenCombo(),
                        combo.getGiaBan(), combo.getMoTa(), combo.getTrangThai()});
            }
        } catch (SQLException ex) {
            baoLoi(ex);
        }
    }

    private void napDongDangChon() {
        int row = table.getSelectedRow();
        if (row < 0 || table.getValueAt(row, 0) == null) {
            return;
        }
        txtMa.setText(table.getValueAt(row, 0).toString());
        txtTen.setText(table.getValueAt(row, 1).toString());
        txtGia.setText(table.getValueAt(row, 2).toString());
        txtMoTa.setText(table.getValueAt(row, 3) == null ? "" : table.getValueAt(row, 3).toString());
        cboTrangThai.setSelectedItem(table.getValueAt(row, 4).toString());
        txtMa.setEditable(false);
    }

    private void lamMoi() {
        lamMoiForm();
        taiDanhSach();
    }

    private void lamMoiForm() {
        txtMa.setText("");
        txtMa.setEditable(true);
        txtTen.setText("");
        txtGia.setText("");
        txtMoTa.setText("");
        cboTrangThai.setSelectedItem("Đang bán");
        table.clearSelection();
    }

    private void baoLoi(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể xử lý",
                JOptionPane.ERROR_MESSAGE);
    }
}
