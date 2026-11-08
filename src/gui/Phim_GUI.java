package gui;

import java.awt.*;

import javax.swing.*;

public class Phim_GUI extends JPanel {
    private static final int COT = 3; // số thẻ phim trên 1 hàng
    private static final int ANH_RONG = 170;
    private static final int ANH_CAO = 220;

    private JPanel luoiPhim;

    public Phim_GUI() {
        setLayout(new BorderLayout(10, 10));

        service.Session.requireStaff();

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

    /** Lấy phim từ DAO; màn hình chỉ tạo thẻ poster cho phim đang chiếu. */
    private void loadPhimDangChieu() {
        for (entity.Phim movie : new dao.PhimDAO().layTatCaPhim()) {
            if ("Đang Chiếu".equalsIgnoreCase(movie.getTrangThai()))
                luoiPhim.add(
                        taoThePhim(movie.getMaPhim(), movie.getTenPhim(), movie.getPosterPath()));
        }
    }

    private JPanel taoThePhim(String maPhim, String tenPhim, String poster) {
        JPanel the = new JPanel(new BorderLayout(0, 8));
        the.setBackground(Color.WHITE);
        the.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 210, 210)),
                        BorderFactory.createEmptyBorder(10, 10, 12, 10)));

        // Ảnh phim
        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(ANH_RONG, ANH_CAO));
        lblImage.setOpaque(true);
        lblImage.setBackground(new Color(0xE5E7EB));

        String imagePath = poster == null || poster.isBlank() ? layDuongDanAnh(maPhim) : poster;
        if (!imagePath.isEmpty()
                && java.nio.file.Files.isRegularFile(util.ProjectPaths.resolve(imagePath))) {
            ImageIcon icon = new ImageIcon(util.ProjectPaths.resolve(imagePath).toString());
            double scale = Math.min((double) ANH_RONG / Math.max(1, icon.getIconWidth()), (double) ANH_CAO / Math.max(1, icon.getIconHeight()));
            Image img = icon.getImage().getScaledInstance(Math.max(1, (int) (icon.getIconWidth() * scale)), Math.max(1, (int) (icon.getIconHeight() * scale)), Image.SCALE_SMOOTH);
            lblImage.setIcon(new ImageIcon(img));
        } else {
            lblImage.setText("Không có ảnh");
            lblImage.setForeground(Color.GRAY);
        }

        // Tên phim (dùng HTML để tự xuống dòng khi tên dài)
        JLabel lblTenPhim =
                new JLabel(
                        "<html><div style='text-align:center;width:190px'>"
                                + tenPhim
                                + "</div></html>",
                        SwingConstants.CENTER);
        lblTenPhim.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Nút đặt vé
        JButton btnDatVe = new JButton("Đặt Vé");
        btnDatVe.addActionListener(e -> datVe(maPhim));

        the.add(lblImage, BorderLayout.NORTH);
        the.add(lblTenPhim, BorderLayout.CENTER);
        the.add(btnDatVe, BorderLayout.SOUTH);
        return the;
    }

    // Gán ảnh dựa theo MaPhim
    private String layDuongDanAnh(String maPhim) {
        if ("1".equals(maPhim) || "PH001".equals(maPhim)) return "img/avengers.jpg";
        if ("2".equals(maPhim) || "PH002".equals(maPhim)) return "img/frozen2.jpg";
        if ("3".equals(maPhim)) return "img/joker.jpg";
        if ("4".equals(maPhim)) return "img/spiderman.jpg";
        if ("5".equals(maPhim)) return "img/theLionKing.jpg";
        if ("6".equals(maPhim)) return "img/doraemon.jpg";
        if ("7".equals(maPhim)) return "img/kamen.jpg";
        return "";
    }

    private void datVe(String maPhim) {
        // Logic đặt vé cho phim đã chọn
        Main_GUI.open("Bán vé", new BanVe_GUI(maPhim));
    }

    public static void main(String[] args) {
        Main_GUI.main(args);
    }
}
