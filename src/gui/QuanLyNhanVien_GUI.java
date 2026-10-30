package gui;

import dao.NhanVienDAO;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class QuanLyNhanVien_GUI extends JFrame {
    private JTable tableNhanVien;
    private DefaultTableModel tableModel;
    private JTextField tfTimKiem;
    private NhanVienDAO nhanVienDAO;

    public QuanLyNhanVien_GUI() {
        nhanVienDAO = new NhanVienDAO();

        setTitle("Quản Lý Nhân Viên - Rạp Chiếu Phim");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng màn hình full
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Tạo menu điều hướng từ Menu_GUI
        JPanel menuPanel = new Menu_GUI(this);
        mainPanel.add(menuPanel, BorderLayout.NORTH);

        // Tạo bảng danh sách nhân viên
        String[] columnNames = {"Mã Nhân Viên", "Tên Nhân Viên", "Ngày Sinh", "Giới Tính", "Số Điện Thoại", "Email", "Địa Chỉ"};
        tableModel = new DefaultTableModel(columnNames, 0);
        tableNhanVien = new JTable(tableModel);
        tableNhanVien.setRowHeight(30); // Tăng chiều cao hàng để dễ nhìn hơn
        tableNhanVien.setFont(new Font("Arial", Font.PLAIN, 16)); // Tăng kích thước font để dễ nhìn hơn
        JScrollPane scrollPane = new JScrollPane(tableNhanVien);
        scrollPane.setPreferredSize(new Dimension(1200, 600)); // Tăng kích thước bảng

        // Khu vực tìm kiếm và các nút chức năng
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        tfTimKiem = new HintTextField("Nhập mã hoặc tên nhân viên");
        tfTimKiem.setPreferredSize(new Dimension(200, 30));
        controlPanel.add(new JLabel("Tìm Kiếm:"));
        controlPanel.add(tfTimKiem);

        JButton btnTimKiem = new JButton("Tìm Kiếm");
        btnTimKiem.addActionListener(e -> timKiemNhanVien());
        controlPanel.add(btnTimKiem);

        JButton btnThemMoi = new JButton("Thêm Mới");
        btnThemMoi.addActionListener(e -> themMoiNhanVien());
        controlPanel.add(btnThemMoi);

        JButton btnChinhSua = new JButton("Chỉnh Sửa");
        btnChinhSua.addActionListener(e -> chinhSuaNhanVien());
        controlPanel.add(btnChinhSua);

        JButton btnXoa = new JButton("Xóa");
        btnXoa.addActionListener(e -> xoaNhanVien());
        controlPanel.add(btnXoa);
        
        JButton btnLamMoi = new JButton("Làm Mới");
        btnLamMoi.addActionListener(e -> lamMoiDanhSach());
        controlPanel.add(btnLamMoi);


        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Load danh sách nhân viên khi khởi chạy
        lamMoiDanhSach();
    }

    private void themMoiNhanVien() {
        JTextField tfMaNhanVien = new JTextField();
        JTextField tfTenNhanVien = new JTextField();
        JDateChooser dateChooserNgaySinh = new JDateChooser();
        dateChooserNgaySinh.setDateFormatString("yyyy-MM-dd");
        JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
        JTextField tfSoDienThoai = new JTextField();
        JTextField tfEmail = new JTextField();
        JTextField tfDiaChi = new JTextField();

        Object[] message = {
            "Mã Nhân Viên:", tfMaNhanVien,
            "Tên Nhân Viên:", tfTenNhanVien,
            "Ngày Sinh:", dateChooserNgaySinh,
            "Giới Tính:", cbGioiTinh,
            "Số Điện Thoại:", tfSoDienThoai,
            "Email:", tfEmail,
            "Địa Chỉ:", tfDiaChi
        };

        while (true) {
            String[] options = {"Lưu", "Clear"};
            int option = JOptionPane.showOptionDialog(this, message, "Thêm Nhân Viên Mới",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);

            if (option == 1) { // Clear
                tfMaNhanVien.setText("");
                tfTenNhanVien.setText("");
                dateChooserNgaySinh.setDate(null);
                cbGioiTinh.setSelectedIndex(0);
                tfSoDienThoai.setText("");
                tfEmail.setText("");
                tfDiaChi.setText("");
                continue;
            } else if (option != 0) { // Đóng dialog
                break;
            }

            try {
                String maNhanVien = tfMaNhanVien.getText().trim();
                String tenNhanVien = tfTenNhanVien.getText().trim();
                Date dateNgaySinh = dateChooserNgaySinh.getDate();
                String gioiTinh = (String) cbGioiTinh.getSelectedItem();
                String soDienThoai = tfSoDienThoai.getText().trim();
                String email = tfEmail.getText().trim();
                String diaChi = tfDiaChi.getText().trim();

                // ====== RÀNG BUỘC ======
                if (maNhanVien.isEmpty() || tenNhanVien.isEmpty() || dateNgaySinh == null || soDienThoai.isEmpty()
                        || email.isEmpty() || diaChi.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (nhanVienDAO.layNhanVienTheoMa(maNhanVien) != null) {
                    JOptionPane.showMessageDialog(this, "Mã nhân viên đã tồn tại. Vui lòng chọn mã khác.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }


                if (!Pattern.matches("^[\\p{L} ]+$", tenNhanVien)) {
                    JOptionPane.showMessageDialog(this, "Tên nhân viên chỉ được chứa chữ và khoảng trắng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }


                if (!Pattern.matches("^[\\p{L}0-9 ,]+$", diaChi)) {
                    JOptionPane.showMessageDialog(this, "Địa chỉ chỉ được chứa chữ cái, số và khoảng trắng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!Pattern.matches("^0\\d{9}$", soDienThoai)) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
                    JOptionPane.showMessageDialog(this, "Email không đúng định dạng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                java.time.LocalDate ngaySinh = dateNgaySinh.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                // ====== LƯU NHÂN VIÊN ======
                NhanVien nv = new NhanVien(maNhanVien, tenNhanVien, ngaySinh, gioiTinh, soDienThoai, email, diaChi, "Nhân Viên");
                if (nhanVienDAO.themNhanVien(nv)) {
                    JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    lamMoiDanhSach();
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi định dạng dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private void chinhSuaNhanVien() {
        int selectedRow = tableNhanVien.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNhanVien = tableModel.getValueAt(selectedRow, 0).toString();
        NhanVien nhanVien = nhanVienDAO.layNhanVienTheoMa(maNhanVien);
        if (nhanVien == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField tfTenNhanVien = new JTextField(nhanVien.getTenNhanVien());
        
        Date dateHienTai = Date.from(nhanVien.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant());
        JDateChooser dateChooserNgaySinh = new JDateChooser(dateHienTai);
        dateChooserNgaySinh.setDateFormatString("yyyy-MM-dd");
        
        JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cbGioiTinh.setSelectedItem(nhanVien.getGioiTinh());
        JTextField tfSoDienThoai = new JTextField(nhanVien.getSoDienThoai());
        JTextField tfEmail = new JTextField(nhanVien.getEmail());
        JTextField tfDiaChi = new JTextField(nhanVien.getDiaChi());

        Object[] message = {
            "Tên Nhân Viên:", tfTenNhanVien,
            "Ngày Sinh:", dateChooserNgaySinh,
            "Giới Tính:", cbGioiTinh,
            "Số Điện Thoại:", tfSoDienThoai,
            "Email:", tfEmail,
            "Địa Chỉ:", tfDiaChi
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh Sửa Nhân Viên", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String tenNhanVien = tfTenNhanVien.getText().trim();
                Date dateNgaySinh = dateChooserNgaySinh.getDate();
                String gioiTinh = (String) cbGioiTinh.getSelectedItem();
                String soDienThoai = tfSoDienThoai.getText().trim();
                String email = tfEmail.getText().trim();
                String diaChi = tfDiaChi.getText().trim();
                


                if (!Pattern.matches("^[\\p{L} ]+$", tenNhanVien)) {
                    JOptionPane.showMessageDialog(this, "Tên nhân viên chỉ được chứa chữ và khoảng trắng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                if (!Pattern.matches("^[\\p{L}0-9 ,]+$", diaChi)) {
                    JOptionPane.showMessageDialog(this, "Địa chỉ chỉ được chứa chữ cái, số và khoảng trắng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Kiểm tra dữ liệu nhập
                if (tenNhanVien.isEmpty() || dateNgaySinh == null || soDienThoai.isEmpty() || email.isEmpty() || diaChi.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
                
                java.time.LocalDate ngaySinh = dateNgaySinh.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                nhanVien.setTenNhanVien(tenNhanVien);
                nhanVien.setNgaySinh(ngaySinh);
                nhanVien.setGioiTinh(gioiTinh);
                nhanVien.setSoDienThoai(soDienThoai);
                nhanVien.setEmail(email);
                nhanVien.setDiaChi(diaChi);

                if (nhanVienDAO.suaNhanVien(nhanVien)) {
                    JOptionPane.showMessageDialog(this, "Chỉnh sửa nhân viên thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    lamMoiDanhSach();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi chỉnh sửa nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi chỉnh sửa nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xoaNhanVien() {
        int selectedRow = tableNhanVien.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNhanVien = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác Nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (nhanVienDAO.xoaNhanVien(maNhanVien)) {
                JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                lamMoiDanhSach();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void timKiemNhanVien() {
        String keyword = tfTimKiem.getText().trim();
        List<NhanVien> danhSachNhanVien = nhanVienDAO.layTatCaNhanVien();
        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        for (NhanVien nhanVien : danhSachNhanVien) {
            if (nhanVien.getTenNhanVien().toLowerCase().contains(keyword.toLowerCase()) ||
                nhanVien.getMaNhanVien().contains(keyword)) {
                tableModel.addRow(nhanVien.toObjectArray());
            }
        }
    }

	    private void lamMoiDanhSach() {
	        List<NhanVien> danhSachNhanVien = nhanVienDAO.layTatCaNhanVien();
	        tableModel.setRowCount(0); // Xóa dữ liệu cũ
	
	        for (NhanVien nhanVien : danhSachNhanVien) {
	            tableModel.addRow(nhanVien.toObjectArray());
	        }
	    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new QuanLyNhanVien_GUI().setVisible(true);
        });
    }
}