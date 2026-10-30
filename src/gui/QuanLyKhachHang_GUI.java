package gui;

import dao.KhachHangDAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class QuanLyKhachHang_GUI extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfTimKiem;
    private KhachHangDAO khachHangDAO;

    public QuanLyKhachHang_GUI() {
        khachHangDAO = new KhachHangDAO();

        setTitle("Quản Lý Khách Hàng - Rạp Chiếu Phim");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng màn hình full
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Tạo menu điều hướng từ Menu_GUI
        JPanel menuPanel = new Menu_GUI(this);
        mainPanel.add(menuPanel, BorderLayout.NORTH);

        // Tạo bảng danh sách khách hàng
        String[] columnNames = {"Mã Khách Hàng", "Tên Khách Hàng", "Số Điện Thoại", "Email", "Điểm tích lũy"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30); // Tăng chiều cao hàng để dễ nhìn hơn
        table.setFont(new Font("Arial", Font.PLAIN, 16)); // Tăng kích thước font để dễ nhìn hơn
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1200, 600)); // Tăng kích thước bảng

        // Khu vực tìm kiếm và các nút chức năng
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        tfTimKiem = new HintTextField("Nhập tên, sdt hoặc email khách");
        tfTimKiem.setPreferredSize(new Dimension(200, 30));
        controlPanel.add(new JLabel("Tìm Kiếm:"));
        controlPanel.add(tfTimKiem);

        JButton btnTimKiem = new JButton("Tìm Kiếm");
        btnTimKiem.addActionListener(e -> timKiemKhachHang());
        controlPanel.add(btnTimKiem);

        JButton btnThemMoi = new JButton("Thêm Mới");
        btnThemMoi.addActionListener(e -> themMoiKhachHang());
        controlPanel.add(btnThemMoi);

        JButton btnChinhSua = new JButton("Chỉnh Sửa");
        btnChinhSua.addActionListener(e -> chinhSuaKhachHang());
        controlPanel.add(btnChinhSua);

        JButton btnXoa = new JButton("Xóa");
        btnXoa.addActionListener(e -> xoaKhachHang());
        controlPanel.add(btnXoa);
        
        JButton btnLamMoi = new JButton("Làm Mới");
        btnLamMoi.addActionListener(e -> lamMoiDanhSach());
        controlPanel.add(btnLamMoi);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Load danh sách khách hàng khi khởi chạy
        lamMoiDanhSach();
    }

    private void themMoiKhachHang() {
        JTextField tfMaKH = new JTextField();
        JTextField tfHoTen = new JTextField();
        JTextField tfSĐT = new JTextField();
        JTextField tfEmail = new JTextField();
        JTextField tfDiemTL = new JTextField();

        Object[] message = {
            "Mã Khách Hàng:", tfMaKH,
            "Tên Khách Hàng:", tfHoTen,
            "Số Điện Thoại:", tfSĐT,
            "Email:", tfEmail
        };

        while (true) {
            String[] options = {"Lưu", "Clear"};
            int option = JOptionPane.showOptionDialog(this, message, "Thêm Khách Hàng Mới",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);

            if (option == 1) { // Clear
            	tfMaKH.setText("");
            	tfHoTen.setText("");
            	tfSĐT.setText("");
                tfEmail.setText("");
                tfDiemTL.setText("");
                continue;
            } else if (option != 0) { // Đóng dialog
                break;
            }

            try {
                String maKhachHang = tfMaKH.getText().trim();
                String tenKhachHang = tfHoTen.getText().trim();
                String soDienThoai = tfSĐT.getText().trim();
                String email = tfEmail.getText().trim();
                int diemTichLuy = 0;
                if (!tfDiemTL.getText().trim().isEmpty()) {
                   diemTichLuy = Integer.parseInt(tfDiemTL.getText().trim()); 
                }

                // Kiểm tra rỗng
                if (maKhachHang.isEmpty() || tenKhachHang.isEmpty() || soDienThoai.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Ràng buộc mã khách hàng không trùng
                if (khachHangDAO.layKhachHangTheoMa(maKhachHang)  != null ) {
                    JOptionPane.showMessageDialog(this, "Mã khách hàng đã tồn tại. Vui lòng chọn mã khác.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Ràng buộc tên khách hàng
                if (!Pattern.matches("^[\\p{L} ]+$", tenKhachHang)) {
                    JOptionPane.showMessageDialog(this, "Tên khách hàng chỉ chứa chữ cái và khoảng trắng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Số điện thoại
                if (!Pattern.matches("^0\\d{9}$", soDienThoai)) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Email
                if (!Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
                    JOptionPane.showMessageDialog(this, "Email không đúng định dạng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Tạo đối tượng & lưu
                KhachHang khachHang = new KhachHang(maKhachHang, tenKhachHang, soDienThoai, email, diemTichLuy);
                if (khachHangDAO.themKhachHang(khachHang)) {
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    lamMoiDanhSach(); // Làm mới dữ liệu bảng
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm khách hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void chinhSuaKhachHang() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKH = (String) tableModel.getValueAt(selectedRow, 0);
        KhachHang khachHang = khachHangDAO.layKhachHangTheoMa(maKH);
        if (khachHang == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField tfHoTen = new JTextField(khachHang.getTenKhachHang());
        JTextField tfSĐT = new JTextField(khachHang.getSoDienThoai());
        JTextField tfEmail = new JTextField(khachHang.getEmail());
        JTextField tfDiemTL = new JTextField(String.valueOf(khachHang.getDiemTichLuy()));

        Object[] message = {
            "Tên Khách Hàng:", tfHoTen,
            "Số Điện Thoại:", tfSĐT,
            "Email:", tfEmail,
            "Điểm tích lũy:", tfDiemTL
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh Sửa Khách Hàng", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String tenKhachHang = tfHoTen.getText().trim();
                String soDienThoai = tfSĐT.getText().trim();
                String email = tfEmail.getText().trim();

                // Kiểm tra dữ liệu nhập
                if (tenKhachHang.isEmpty() || soDienThoai.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
             // Ràng buộc tên khách hàng
                if (!Pattern.matches("^[\\p{L} ]+$", tenKhachHang)) {
                    JOptionPane.showMessageDialog(this, "Tên khách hàng chỉ chứa chữ cái và khoảng trắng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Kiểm tra số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0
                if (!Pattern.matches("^0\\d{9}$", soDienThoai)) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Kiểm tra email hợp lệ
                if (!Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
                    JOptionPane.showMessageDialog(this, "Email không đúng định dạng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                khachHang.setTenKhachHang(tenKhachHang);
                khachHang.setSoDienThoai(soDienThoai);
                khachHang.setEmail(email);

                if (khachHangDAO.suaKhachHang(khachHang)) {
                    JOptionPane.showMessageDialog(this, "Chỉnh sửa khách hàng thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    lamMoiDanhSach();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi chỉnh sửa khách hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi chỉnh sửa khách hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xoaKhachHang() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKhachHang = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng này?", "Xác Nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (khachHangDAO.xoaKhachHang(maKhachHang)) {
                JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                lamMoiDanhSach();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa khách hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void timKiemKhachHang() {
        String keyword = tfTimKiem.getText().trim();
        List<KhachHang> danhSachKhachHang = khachHangDAO.layTatCaKhachHang();
        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        for (KhachHang khachHang : danhSachKhachHang) {
            if (khachHang.getTenKhachHang().toLowerCase().contains(keyword.toLowerCase()) ||
                khachHang.getMaKhachHang().contains(keyword) ||
                khachHang.getSoDienThoai().contains(keyword) ||
                khachHang.getEmail().toLowerCase().contains(keyword.toLowerCase())) {
                tableModel.addRow(new Object[]{khachHang.getMaKhachHang(), khachHang.getTenKhachHang(), khachHang.getSoDienThoai(), khachHang.getEmail()});
            }
        }
    }

    private void lamMoiDanhSach() {
        List<KhachHang> danhSachKhachHang = khachHangDAO.layTatCaKhachHang();
        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        for (KhachHang khachHang : danhSachKhachHang) {
            tableModel.addRow(new Object[]{khachHang.getMaKhachHang(), khachHang.getTenKhachHang(), khachHang.getSoDienThoai(), khachHang.getEmail()});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new QuanLyKhachHang_GUI().setVisible(true);
        });
    }
}