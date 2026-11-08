package gui;

import dao.TaiKhoanDAO;

import service.Session;

import java.awt.*;
import java.sql.*;
import java.util.Arrays;

import javax.swing.*;

public class DangNhap_GUI extends JFrame {
    private final JTextField username = new JTextField(22);
    private final JPasswordField password = new JPasswordField(22);
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    public DangNhap_GUI() {
        setTitle("Đăng nhập - Quản lý bán vé phim");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
        form.setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));
        JLabel title = new JLabel("QUẢN LÝ BÁN VÉ PHIM", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        form.add(title);
        form.add(new JLabel("Tên đăng nhập"));
        form.add(username);
        form.add(new JLabel("Mật khẩu"));
        form.add(password);
        JButton login = new JButton("Đăng nhập");
        form.add(login);
        JButton setup = new JButton("Thiết lập tài khoản lần đầu");
        form.add(setup);
        login.setMnemonic(java.awt.event.KeyEvent.VK_D);
        login.addActionListener(e -> login());
        setup.addActionListener(e -> setup());
        setContentPane(form);
        getRootPane().setDefaultButton(login);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void login() {
        char[] secret = password.getPassword();
        try {
            String[] employee = dao.dangNhap(username.getText().trim(), secret);
            if (employee == null) {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng.");
                return;
            }
            Session.maNhanVien = employee[0];
            Session.tenNhanVien = employee[1];
            new Main_GUI().setVisible(true);
            dispose();
        } catch (Exception ex) {
            showError(ex);
        } finally {
            Arrays.fill(secret, '\0');
            password.setText("");
        }
    }

    private void setup() {
        char[] secret = null;
        try {
            if (!dao.chuaCoTaiKhoan()) {
                JOptionPane.showMessageDialog(this, "Đã có tài khoản. Vui lòng đăng nhập.");
                return;
            }
            JTextField user = new JTextField();
            JPasswordField pass = new JPasswordField();
            JPasswordField confirm = new JPasswordField();
            JComboBox<String> employees = new JComboBox<>();
            // Nhân viên chỉ dùng để gắn tài khoản nội bộ; không tạo tài khoản cho khách.
            for (entity.NhanVien employee : new dao.NhanVienDAO().layTatCaNhanVien()) {
                employees.addItem(employee.getMaNhanVien() + " - " + employee.getTenNhanVien());
            }
            if (employees.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "Cần có nhân viên trong cơ sở dữ liệu trước.");
                return;
            }
            if (JOptionPane.showConfirmDialog(
                            this,
                            new Object[] {
                                "Nhân viên",
                                employees,
                                "Tên đăng nhập (3–50 ký tự)",
                                user,
                                "Mật khẩu (ít nhất 8 ký tự)",
                                pass,
                                "Nhập lại mật khẩu",
                                confirm
                            },
                            "Thiết lập lần đầu",
                            JOptionPane.OK_CANCEL_OPTION)
                    != JOptionPane.OK_OPTION) return;
            secret = pass.getPassword();
            char[] repeated = confirm.getPassword();
            boolean same = Arrays.equals(secret, repeated);
            Arrays.fill(repeated, '\0');
            if (!user.getText().trim().matches("[A-Za-z0-9_.-]{3,50}")
                    || secret.length < 8
                    || !same) {
                JOptionPane.showMessageDialog(
                        this, "Kiểm tra tên đăng nhập, độ dài và xác nhận mật khẩu.");
                return;
            }
            dao.taoTaiKhoanDauTien(
                    user.getText().trim(),
                    secret,
                    employees.getSelectedItem().toString().split(" - ", 2)[0]);
            username.setText(user.getText().trim());
            JOptionPane.showMessageDialog(this, "Đã tạo tài khoản. Bạn có thể đăng nhập.");
        } catch (Exception ex) {
            showError(ex);
        } finally {
            if (secret != null) Arrays.fill(secret, '\0');
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "Không thực hiện được đăng nhập/thiết lập tài khoản.\n" + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> new DangNhap_GUI().setVisible(true));
    }
}
