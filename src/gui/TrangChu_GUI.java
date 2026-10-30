package gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrangChu_GUI extends JFrame {
    public TrangChu_GUI() {
        setTitle("Trang Chủ - Rạp Chiếu Phim");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng màn hình full
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Tạo panel chính với background là hình ảnh rạp chiếu phim
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon background = new ImageIcon("img/background4.jpg");
                g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Thêm menu điều hướng từ Menu_GUI
        mainPanel.add(new Menu_GUI(this), BorderLayout.NORTH);

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TrangChu_GUI().setVisible(true);
        });
    }
}