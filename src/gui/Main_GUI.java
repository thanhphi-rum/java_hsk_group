package gui;

import service.Session;

import java.awt.*;
import java.util.function.Supplier;

import javax.swing.*;

/** Một cửa sổ chính; mỗi chức năng là một JPanel. */
public class Main_GUI extends JFrame {
    private static Main_GUI instance;
    private final JPanel content = new JPanel(new BorderLayout());
    private final JLabel heading = new JLabel("Trang chủ");
    private final JPanel navigation = new JPanel(new GridLayout(0, 1, 0, 6));

    public Main_GUI() {

        Session.requireStaff();
        instance = this;
        setTitle("Quản lý bán vé phim");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        JPanel sidebar = new JPanel(new BorderLayout(0, 20));
        sidebar.setPreferredSize(new Dimension(185, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 16, 12));
        JLabel brand = new JLabel("RẠP CHIẾU PHIM");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 17));
        sidebar.add(brand, BorderLayout.NORTH);
        JPanel navTop = new JPanel(new BorderLayout());
        navTop.add(navigation, BorderLayout.NORTH);
        sidebar.add(navTop, BorderLayout.CENTER);
        addNav("Trang chủ", TrangChu_GUI::new);
        addNav("Bán vé", Phim_GUI::new);
        if (Session.isStaff()) {

            addNav("Hóa đơn", HoaDon_GUI::new);
            addNav("Quản lý phim", () -> new DanhMuc_GUI("Phim"));
            addNav("Loại phim", () -> new DanhMuc_GUI("LoaiPhim"));
            addNav("Khuyến mãi", () -> new DanhMuc_GUI("KhuyenMai"));
            addNav("Phòng chiếu", () -> new DanhMuc_GUI("PhongChieu"));
            addNav("Suất chiếu", QuanLySuatChieu_GUI::new);
            addNav("Phòng / dãy ghế", QuanLyGhe_GUI::new);
            addNav("Khách hàng", () -> new DanhMuc_GUI("KhachHang"));
            addNav("Nhân viên", () -> new DanhMuc_GUI("NhanVien"));
            addNav("Thống kê", ThongKe_GUI::new);
        }
        JButton logout = new JButton(Session.isStaff() ? "Đăng xuất" : "Đăng nhập nhân viên");
        logout.addActionListener(
                e -> {
                    if (Session.isStaff()) {
                        if (JOptionPane.showConfirmDialog(
                                        this,
                                        "Đăng xuất khỏi ứng dụng?",
                                        "Đăng xuất",
                                        JOptionPane.YES_NO_OPTION)
                                != JOptionPane.YES_OPTION) return;
                        Session.clear();
                        dispose();
                        new DangNhap_GUI().setVisible(true);
                    } else {
                        dispose();
                        new DangNhap_GUI().setVisible(true);
                    }
                });
        sidebar.add(logout, BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel top = new JPanel(new BorderLayout());
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        top.add(heading, BorderLayout.WEST);
        top.add(
                new JLabel(
                        Session.isStaff()
                                ? "Nhân viên: " + Session.tenNhanVien
                                : ""),
                BorderLayout.EAST);
        body.add(top, BorderLayout.NORTH);
        body.add(content, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        showPage("Bán vé", new Phim_GUI());
    }

    private void addNav(String name, Supplier<JPanel> factory) {
        JButton button = new JButton(name);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(160, 38));
        button.addActionListener(
                e -> {
                    try {
                        showPage(name, factory.get());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Không tải được dữ liệu. Kiểm tra kết nối và script SQL.\n"
                                        + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
        navigation.add(button);
    }

    public static void open(String name, JPanel page) {
        if (instance == null) throw new IllegalStateException("Chưa mở ứng dụng");
        instance.showPage(name, page);
    }

    private void showPage(String name, JPanel page) {
        if (!Session.isStaff()
                && !(page instanceof Phim_GUI
                        || page instanceof BanVe_GUI
                        || page instanceof TrangChu_GUI))
            throw new SecurityException("Vui lòng đăng nhập nhân viên.");
        heading.setText(!Session.isStaff() && name.equals("Bán vé") ? "Đặt vé" : name);
        content.removeAll();
        content.add(page);
        for (Component component : navigation.getComponents()) {
            JButton b = (JButton) component;
            b.setEnabled(!b.getText().equals(name));
        }
        AppTheme.style(page);
        content.revalidate();
        content.repaint();
    }

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> new DangNhap_GUI().setVisible(true));
    }
}
