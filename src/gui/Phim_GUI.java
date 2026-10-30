package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import connect.ConnectDB;

public class Phim_GUI extends JFrame {
    private static final int COT = 5;           // số thẻ phim trên 1 hàng
    private static final int ANH_RONG = 200;
    private static final int ANH_CAO = 260;

    private JPanel luoiPhim;

    public Phim_GUI() {
        setTitle("Danh Sách Phim Đang Chiếu - Rạp Chiếu Phim");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng màn hình full
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        add(new Menu_GUI(this), BorderLayout.NORTH);

        // Tiêu đề trang
        JLabel lblTieuDe = new JLabel("Phim đang chiếu");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTieuDe.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        // Lưới các thẻ phim (đặt ở NORTH để thẻ giữ đúng kích thước, không bị kéo giãn)
        luoiPhim = new JPanel(new GridLayout(0, COT, 24, 24));
        luoiPhim.setOpaque(false);

        loadPhimDangChieu();

        JPanel noiDung = new JPanel(new BorderLayout());
        noiDung.setBackground(AppTheme.PAGE_BG);
        noiDung.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
        noiDung.add(luoiPhim, BorderLayout.NORTH);

        JPanel trang = new JPanel(new BorderLayout());
        trang.setBackground(AppTheme.PAGE_BG);
        trang.add(lblTieuDe, BorderLayout.NORTH);
        trang.add(noiDung, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(trang);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadPhimDangChieu() {
        // Có tiền tố N để không bị sai dấu tiếng Việt
        String sql = "SELECT MaPhim, TenPhim FROM Phim WHERE TrangThai = N'Đang Chiếu'";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                luoiPhim.add(taoThePhim(rs.getString("MaPhim"), rs.getString("TenPhim")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel taoThePhim(String maPhim, String tenPhim) {
        JPanel the = new JPanel(new BorderLayout(0, 8));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.CARD_BORDER),
                BorderFactory.createEmptyBorder(10, 10, 12, 10)));

        // Ảnh phim
        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(ANH_RONG, ANH_CAO));
        lblImage.setOpaque(true);
        lblImage.setBackground(new Color(0xE5E7EB));

        String imagePath = layDuongDanAnh(maPhim);
        if (!imagePath.isEmpty()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image img = icon.getImage().getScaledInstance(ANH_RONG, ANH_CAO, Image.SCALE_SMOOTH);
            lblImage.setIcon(new ImageIcon(img));
        } else {
            lblImage.setText("Không có ảnh");
            lblImage.setForeground(Color.GRAY);
        }

        // Tên phim (dùng HTML để tự xuống dòng khi tên dài)
        JLabel lblTenPhim = new JLabel("<html><div style='text-align:center;width:190px'>" + tenPhim + "</div></html>",
                SwingConstants.CENTER);
        lblTenPhim.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Nút đặt vé
        JButton btnDatVe = new JButton("Đặt Vé");
        btnDatVe.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDatVe.setForeground(Color.WHITE);
        btnDatVe.setBackground(AppTheme.ACCENT);
        btnDatVe.setFocusPainted(false);
        btnDatVe.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDatVe.setPreferredSize(new Dimension(100, 38));
        btnDatVe.addActionListener(e -> datVe(maPhim));

        the.add(lblImage, BorderLayout.NORTH);
        the.add(lblTenPhim, BorderLayout.CENTER);
        the.add(btnDatVe, BorderLayout.SOUTH);
        return the;
    }

    // Gán ảnh dựa theo MaPhim
    private String layDuongDanAnh(String maPhim) {
        if ("1".equals(maPhim)) return "img/avengers.jpg";
        if ("2".equals(maPhim)) return "img/frozen2.jpg";
        if ("3".equals(maPhim)) return "img/joker.jpg";
        if ("4".equals(maPhim)) return "img/spiderman.jpg";
        if ("5".equals(maPhim)) return "img/theLionKing.jpg";
        if ("6".equals(maPhim)) return "img/doraemon.jpg";
        if ("7".equals(maPhim)) return "img/kamen.jpg";
        return "";
    }

    private void datVe(String maPhim) {
        // Logic đặt vé cho phim đã chọn
        new BanVe_GUI(maPhim).setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> {
            new Phim_GUI().setVisible(true);
        });
    }
}
