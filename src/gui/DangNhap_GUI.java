package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;

/** Màn hình đăng nhập hiển thị trên nền trang chủ. */
public class DangNhap_GUI extends BackgroundPanel {
    private final JTextField tfTenDangNhap = new JTextField();
    private final JPasswordField pfMatKhau = new JPasswordField();
    private final JLabel lblLoi = new JLabel(" ");
    private final JButton btnDangNhap = new JButton("Đăng nhập");
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final MainFrame frame;

    public DangNhap_GUI(MainFrame frame) {
        super(0.45f);
        this.frame = frame;
        setLayout(new GridBagLayout());

        // ===== Thẻ đăng nhập =====
        JPanel the = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x0F, 0x1A, 0x40, 232));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 26, 26);
                g2.setColor(AppTheme.CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 26, 26);
                g2.dispose();
            }
        };
        the.setOpaque(false);
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));
        the.setPreferredSize(new Dimension(420, 520));

        JLabel lblLogo = new JLabel("RẠP CHIẾU PHIM");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLogo.setForeground(new Color(0x7FA8FF));
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTieuDe = new JLabel("Đăng nhập");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPhu = new JLabel("Vui lòng đăng nhập để sử dụng hệ thống");
        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPhu.setForeground(AppTheme.MUTED);
        lblPhu.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfTenDangNhap.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập");
        pfMatKhau.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");
        pfMatKhau.putClientProperty("JPasswordField.showRevealButton", true);
        for (JTextField tf : new JTextField[]{tfTenDangNhap, pfMatKhau}) {
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            tf.setAlignmentX(Component.LEFT_ALIGNMENT);
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            tf.setPreferredSize(new Dimension(300, 42));
        }

        lblLoi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLoi.setForeground(AppTheme.DANGER);
        lblLoi.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setBackground(AppTheme.ACCENT);
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnDangNhap.setPreferredSize(new Dimension(300, 46));

        JLabel bieuTuong = new JLabel(new Menu_GUI.LogoIcon(56));
        bieuTuong.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.add(bieuTuong);
        the.add(Box.createVerticalStrut(10));
        the.add(lblLogo);
        the.add(Box.createVerticalStrut(6));
        the.add(lblTieuDe);
        the.add(Box.createVerticalStrut(4));
        the.add(lblPhu);
        the.add(Box.createVerticalStrut(24));
        the.add(nhan("Tên đăng nhập"));
        the.add(Box.createVerticalStrut(6));
        the.add(tfTenDangNhap);
        the.add(Box.createVerticalStrut(16));
        the.add(nhan("Mật khẩu"));
        the.add(Box.createVerticalStrut(6));
        the.add(pfMatKhau);
        the.add(Box.createVerticalStrut(10));
        the.add(lblLoi);
        the.add(Box.createVerticalStrut(12));
        the.add(btnDangNhap);

        add(the);

        // Enter ở ô nào cũng đăng nhập
        btnDangNhap.addActionListener(e -> thucHienDangNhap());
        tfTenDangNhap.addActionListener(e -> pfMatKhau.requestFocusInWindow());
        pfMatKhau.addActionListener(e -> thucHienDangNhap());
    }

    private JLabel nhan(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Gọi khi hiện lại màn hình đăng nhập (sau đăng xuất). */
    public void datLai() {
        tfTenDangNhap.setText("");
        pfMatKhau.setText("");
        lblLoi.setText(" ");
        btnDangNhap.setEnabled(true);
        btnDangNhap.setText("Đăng nhập");
        SwingUtilities.invokeLater(() -> tfTenDangNhap.requestFocusInWindow());
    }

    private void thucHienDangNhap() {
        String user = tfTenDangNhap.getText().trim();
        String pass = new String(pfMatKhau.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            lblLoi.setText("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        lblLoi.setText(" ");
        btnDangNhap.setEnabled(false);
        btnDangNhap.setText("Đang đăng nhập...");

        // Truy vấn CSDL ở luồng nền để giao diện không bị đứng
        new SwingWorker<TaiKhoan, Void>() {
            @Override
            protected TaiKhoan doInBackground() throws Exception {
                return taiKhoanDAO.dangNhap(user, pass);
            }

            @Override
            protected void done() {
                btnDangNhap.setEnabled(true);
                btnDangNhap.setText("Đăng nhập");
                try {
                    TaiKhoan tk = get();
                    if (tk == null) {
                        lblLoi.setText("Sai tên đăng nhập hoặc mật khẩu.");
                        pfMatKhau.setText("");
                        pfMatKhau.requestFocusInWindow();
                    } else if (!tk.isHoatDong()) {
                        lblLoi.setText("Tài khoản đã bị khóa. Vui lòng liên hệ quản lý.");
                        pfMatKhau.setText("");
                    } else {
                        frame.dangNhapThanhCong(tk);
                    }
                } catch (Exception ex) {
                    Throwable goc = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = goc.getMessage() == null ? "" : goc.getMessage();
                    if (goc instanceof SQLException && msg.contains("TaiKhoan")) {
                        lblLoi.setText("Chưa có bảng TaiKhoan. Hãy chạy data/quanlybanvephimnew.sql.");
                    } else {
                        lblLoi.setText("Không thể kết nối cơ sở dữ liệu.");
                    }
                    goc.printStackTrace();
                }
            }
        }.execute();
    }
}


