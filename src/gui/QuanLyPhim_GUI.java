package gui;

import dao.PhimDAO;
import entity.Phim;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuanLyPhim_GUI extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfTimKiem;
    private PhimDAO phimDAO;

    public QuanLyPhim_GUI() {
        phimDAO = new PhimDAO();
        setTitle("Quản Lý Phim - Rạp Chiếu Phim");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng màn hình full
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Tạo menu điều hướng từ Menu_GUI
        Menu_GUI menuPanel = new Menu_GUI(this);
        mainPanel.add(menuPanel, BorderLayout.NORTH);

        // Tạo bảng danh sách phim
        String[] columnNames = {"Mã Phim", "Tên Phim", "Thể Loại", "Thời Lượng", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30); // Tăng chiều cao hàng để dễ nhìn hơn
        table.setFont(new Font("Arial", Font.PLAIN, 16)); // Tăng kích thước font để dễ nhìn hơn
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1200, 600)); // Tăng kích thước bảng

        // Khu vực tìm kiếm và các nút chức năng
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        tfTimKiem = new HintTextField("Tìm theo mã, tên hoặc thể loại...");
        tfTimKiem.setPreferredSize(new Dimension(200, 30));
        controlPanel.add(new JLabel("Tìm Kiếm:"));
        controlPanel.add(tfTimKiem);

        JButton btnTimKiem = new JButton("Tìm Kiếm");
        btnTimKiem.addActionListener(e -> timKiemPhim());
        controlPanel.add(btnTimKiem);

        JButton btnThemMoi = new JButton("Thêm Mới");
        btnThemMoi.addActionListener(e -> themMoiPhim());
        controlPanel.add(btnThemMoi);

        JButton btnChinhSua = new JButton("Chỉnh Sửa");
        btnChinhSua.addActionListener(e -> chinhSuaPhim());
        controlPanel.add(btnChinhSua);

        JButton btnXoa = new JButton("Xóa");
        btnXoa.addActionListener(e -> xoaPhim());
        controlPanel.add(btnXoa);

        JButton btnLamMoi = new JButton("Làm Mới");
        btnLamMoi.addActionListener(e -> lamMoiDanhSach());
        controlPanel.add(btnLamMoi);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Load danh sách phim khi khởi chạy
        lamMoiDanhSach();
    }

    private void themMoiPhim() {
        JTextField tfMaPhim = new JTextField();
        JTextField tfTenPhim = new JTextField();
        JTextField tfTheLoai = new JTextField();
        JTextField tfThoiLuong = new JTextField();
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Sắp Chiếu", "Đang Chiếu", "Ngừng Chiếu"});

        Object[] message = {
            "Mã Phim:", tfMaPhim,
            "Tên Phim:", tfTenPhim,
            "Thể Loại:", tfTheLoai,
            "Thời Lượng (phút):", tfThoiLuong,
            "Trạng Thái:", cbTrangThai
        };

        while (true) {
            String[] options = {"Lưu", "Clear"};
            int option = JOptionPane.showOptionDialog(this, message, "Thêm Phim Mới",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);

            if (option == 1) { // Clear
                tfMaPhim.setText("");
                tfTenPhim.setText("");
                tfTheLoai.setText("");
                tfThoiLuong.setText("");
                cbTrangThai.setSelectedIndex(0);
                continue;
            } else if (option != 0) {
                break;
            }

            try {
                String maPhim = tfMaPhim.getText().trim();
                String tenPhim = tfTenPhim.getText().trim();
                String theLoai = tfTheLoai.getText().trim();
                String thoiLuongStr = tfThoiLuong.getText().trim();
                String trangThai = (String) cbTrangThai.getSelectedItem();

                // Kiểm tra rỗng
                if (maPhim.isEmpty() || tenPhim.isEmpty() || theLoai.isEmpty() || thoiLuongStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Kiểm tra định dạng
                if (!tenPhim.matches("^[\\p{L}0-9 ,\\.]+$")) {
                    JOptionPane.showMessageDialog(this, "Tên phim chỉ được chứa chữ cái, số, dấu cách, dấu phẩy và dấu chấm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!theLoai.matches("^[\\p{L} ]+$")) {
                    JOptionPane.showMessageDialog(this, "Thể loại chỉ được chứa chữ cái.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!thoiLuongStr.matches("^\\d{2,3}$")) {
                    JOptionPane.showMessageDialog(this, "Thời lượng phải là số nguyên có 2 hoặc 3 chữ số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                int thoiLuong = Integer.parseInt(thoiLuongStr);

                // Kiểm tra trùng mã phim
                if (phimDAO.layPhimTheoMa(maPhim) != null) {
                    JOptionPane.showMessageDialog(this, "Mã phim đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                Phim phim = new Phim(maPhim, tenPhim, theLoai, thoiLuong, trangThai, "", "", "", "", null);
                boolean success = phimDAO.themPhim(phim);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Thêm phim mới thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    lamMoiDanhSach();
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm phim.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Thời lượng phải là số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void chinhSuaPhim() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phim cần chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPhim = tableModel.getValueAt(selectedRow, 0).toString();
        JTextField tfTenPhim = new JTextField(tableModel.getValueAt(selectedRow, 1).toString());
        JTextField tfTheLoai = new JTextField(tableModel.getValueAt(selectedRow, 2).toString());
        JTextField tfThoiLuong = new JTextField(tableModel.getValueAt(selectedRow, 3).toString());
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Sắp Chiếu", "Đang Chiếu", "Ngừng Chiếu"});
        cbTrangThai.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());

        Object[] message = {
            "Tên Phim:", tfTenPhim,
            "Thể Loại:", tfTheLoai,
            "Thời Lượng:", tfThoiLuong,
            "Trạng Thái:", cbTrangThai
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh Sửa Phim", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
            return;
        }

        String tenPhim = tfTenPhim.getText().trim();
        String theLoai = tfTheLoai.getText().trim();
        String thoiLuongStr = tfThoiLuong.getText().trim();

        // === RÀNG BUỘC ===
        if (!tenPhim.matches("^[\\p{L}0-9 ,\\.]+$")) {
            JOptionPane.showMessageDialog(this, "Tên phim chỉ được chứa chữ cái, số, dấu cách, dấu phẩy và dấu chấm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            chinhSuaPhim();
            return;
        }

        if (!theLoai.matches("^[\\p{L} ]+$")) {
            JOptionPane.showMessageDialog(this, "Thể loại chỉ được chứa chữ cái và dấu cách.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            chinhSuaPhim();
            return;
        }

        if (!thoiLuongStr.matches("^\\d{2,3}$")) {
            JOptionPane.showMessageDialog(this, "Thời lượng phải là số nguyên có 2 hoặc 3 chữ số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            chinhSuaPhim();
            return;
        }

        try {
            int thoiLuong = Integer.parseInt(thoiLuongStr);
            String trangThai = (String) cbTrangThai.getSelectedItem();

            Phim phim = new Phim(maPhim, tenPhim, theLoai, thoiLuong, trangThai, "", "", "", "", null);
            boolean success = phimDAO.suaPhim(phim);
            if (success) {
                JOptionPane.showMessageDialog(this, "Chỉnh sửa phim thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                lamMoiDanhSach();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi chỉnh sửa phim.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Thời lượng phải là số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void xoaPhim() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phim cần xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPhim = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phim này, hãy đảm bảo không còn suất chiếu nào cho phim này?", "Xóa Phim", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = phimDAO.xoaPhim(maPhim);
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa phim thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                lamMoiDanhSach();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy mã phim để xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void lamMoiDanhSach() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        List<Phim> danhSachPhim = phimDAO.layTatCaPhim();
        for (Phim phim : danhSachPhim) {
            tableModel.addRow(new Object[]{phim.getMaPhim(), phim.getTenPhim(), phim.getTheLoai(), phim.getThoiLuong(), phim.getTrangThai()});
        }
    }

    private void timKiemPhim() {
        String keyword = tfTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa để tìm kiếm.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        List<Phim> danhSachPhim = phimDAO.timKiemPhim(keyword);
        for (Phim phim : danhSachPhim) {
            tableModel.addRow(new Object[]{phim.getMaPhim(), phim.getTenPhim(), phim.getTheLoai(), phim.getThoiLuong(), phim.getTrangThai()});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new QuanLyPhim_GUI().setVisible(true);
        });
    }
}