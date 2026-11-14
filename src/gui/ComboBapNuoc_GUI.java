package gui;

import dao.ComboBapNuoc_DAO;
import entity.ComboBapNuoc;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/** Màn hình quản lý combo, đồng bộ màu sắc với màn tiếp nhận đặt vé. */
public class ComboBapNuoc_GUI extends JFrame {
    private static final Color NEN = new Color(244, 247, 251);
    private static final Color XANH_DAM = new Color(30, 52, 84);
    private static final Color XANH = new Color(42, 105, 173);
    private static final Color XANH_LA = new Color(47, 132, 104);
    private static final Color CHU = new Color(48, 58, 72);

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
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setSize(900, 590);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(NEN);
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(XANH_DAM);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("QUẢN LÝ COMBO BẮP NƯỚC");
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Thêm combo mới, cập nhật giá và trạng thái kinh doanh");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(220, 230, 242));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 228, 237)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel formTitle = new JLabel("THÔNG TIN COMBO");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formTitle.setForeground(XANH_DAM);

        JPanel form = new JPanel(new GridLayout(5, 2, 12, 9));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        addFormRow(form, "Mã combo", txtMa);
        addFormRow(form, "Tên combo", txtTen);
        addFormRow(form, "Giá bán (đồng)", txtGia);
        addFormRow(form, "Mô tả", txtMoTa);
        addFormRow(form, "Trạng thái", cboTrangThai);
        styleInput(txtMa);
        styleInput(txtTen);
        styleInput(txtGia);
        styleInput(txtMoTa);
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboTrangThai.setBackground(Color.WHITE);

        JPanel formSection = new JPanel(new BorderLayout(0, 8));
        formSection.setOpaque(false);
        formSection.add(formTitle, BorderLayout.NORTH);
        formSection.add(form, BorderLayout.CENTER);
        content.add(formSection, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[] {"Mã combo", "Tên combo", "Giá bán", "Mô tả", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(95);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(222, 228, 237)));
        scroll.getViewport().setBackground(Color.WHITE);
        content.add(scroll, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        actions.setOpaque(false);
        JButton btnThem = new JButton("Thêm combo");
        JButton btnCapNhat = new JButton("Cập nhật");
        JButton btnLamMoi = new JButton("Làm mới");
        styleButton(btnThem, XANH);
        styleButton(btnCapNhat, XANH_LA);
        styleButton(btnLamMoi, new Color(92, 108, 126));
        actions.add(btnThem);
        actions.add(btnCapNhat);
        actions.add(btnLamMoi);
        root.add(actions, BorderLayout.SOUTH);

        btnThem.addActionListener(event -> themCombo());
        btnCapNhat.addActionListener(event -> capNhatCombo());
        btnLamMoi.addActionListener(event -> lamMoi());
        table.getSelectionModel().addListSelectionListener(event -> napDongDangChon());
        taiDanhSach();
    }

    private void addFormRow(JPanel form, String text, java.awt.Component input) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(CHU);
        form.add(label);
        form.add(input);
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(CHU);
        field.setPreferredSize(new Dimension(240, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 216, 228)),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(CHU);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(226, 231, 238));
        table.setSelectionBackground(new Color(218, 234, 250));
        table.setSelectionForeground(XANH_DAM);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(232, 239, 248));
        table.getTableHeader().setForeground(XANH_DAM);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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