package gui;

import java.awt.*;

import javax.swing.*;

/** Trang chủ chỉ hiển thị ảnh nền sẵn có, không thêm chữ. */
public class TrangChu_GUI extends JPanel {
    private final Image background =
            new ImageIcon(util.ProjectPaths.resolve("img/background4.jpg").toString()).getImage();

    public TrangChu_GUI() {
        super(new BorderLayout(0, 0));
        service.Session.requireStaff();
        JPanel picture =
                new JPanel() {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        super.paintComponent(graphics);
                        int iw = background.getWidth(this), ih = background.getHeight(this);
                        if (iw <= 0 || ih <= 0) return;
                        double scale =
                                Math.max((double) getWidth() / iw, (double) getHeight() / ih);
                        int width = (int) Math.ceil(iw * scale),
                                height = (int) Math.ceil(ih * scale);
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(
                                RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g.drawImage(
                                background,
                                (getWidth() - width) / 2,
                                (getHeight() - height) / 2,
                                width,
                                height,
                                this);
                        g.dispose();
                    }
                };
        picture.setBackground(new Color(50, 25, 25));
        add(picture, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        Main_GUI.main(args);
    }
}
