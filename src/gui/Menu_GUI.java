package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Menu_GUI extends JPanel {

    public Menu_GUI(JFrame currentFrame) {
        setLayout(new GridLayout(1, 0, 4, 0));
        setBackground(AppTheme.NAV_BG);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        String[] buttons = {"Phim", "Hóa Đơn", "Quản lý Phim", "Quản lý Suất Chiếu",
                "Quản lý Khách Hàng", "Quản lý Nhân Viên", "Thống Kê", "Trang chủ"};

        String currentClass = currentFrame.getClass().getSimpleName();

        for (String btnText : buttons) {
            boolean active = currentClass.equals(tenLop(btnText))
                    // Màn bán vé thuộc nhóm "Phim"
                    || (btnText.equals("Phim") && currentClass.equals("BanVe_GUI"));

            JButton button = new JButton(btnText);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(true);
            button.setOpaque(true);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setForeground(Color.WHITE);
            button.setPreferredSize(new Dimension(100, 40));

            final Color normal = active ? AppTheme.ACCENT : AppTheme.NAV_BG;
            final Color hover = active ? AppTheme.ACCENT_DARK : AppTheme.NAV_HOVER;
            button.setBackground(normal);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(hover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(normal);
                }
            });

            button.addActionListener(new NavigationActionListener(currentFrame, btnText));
            add(button);
        }
    }

    // Tên lớp màn hình tương ứng với từng nút (để biết nút nào đang được chọn)
    private static String tenLop(String screenName) {
        switch (screenName) {
            case "Phim":                return "Phim_GUI";
            case "Hóa Đơn":             return "HoaDon_GUI";
            case "Quản lý Phim":        return "QuanLyPhim_GUI";
            case "Quản lý Suất Chiếu":  return "QuanLySuatChieu_GUI";
            case "Quản lý Khách Hàng":  return "QuanLyKhachHang_GUI";
            case "Quản lý Nhân Viên":   return "QuanLyNhanVien_GUI";
            case "Thống Kê":            return "ThongKe_GUI";
            case "Trang chủ":           return "TrangChu_GUI";
            default:                    return "";
        }
    }

    private class NavigationActionListener implements ActionListener {
        private JFrame currentFrame;
        private String screenName;

        public NavigationActionListener(JFrame currentFrame, String screenName) {
            this.currentFrame = currentFrame;
            this.screenName = screenName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (screenName) {
                case "Phim":
                    new Phim_GUI().setVisible(true);
                    break;
                case "Quản lý Phim":
                    new QuanLyPhim_GUI().setVisible(true);
                    break;
                case "Quản lý Suất Chiếu":
                    new QuanLySuatChieu_GUI().setVisible(true);
                    break;
                case "Quản lý Khách Hàng":
                    new QuanLyKhachHang_GUI().setVisible(true);
                    break;
                case "Quản lý Nhân Viên":
                    new QuanLyNhanVien_GUI().setVisible(true);
                    break;
                case "Hóa Đơn":
                    new HoaDon_GUI().setVisible(true);
                    break;
                case "Thống Kê":
                    new ThongKe_GUI().setVisible(true);
                    break;
                case "Trang chủ":
                    new TrangChu_GUI().setVisible(true);
                    break;
            }
            currentFrame.dispose(); // Đóng màn hình hiện tại
        }
    }
}
