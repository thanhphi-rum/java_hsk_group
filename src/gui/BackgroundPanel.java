package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Panel vẽ ảnh nền (img/background4.jpg) phủ kín, có lớp phủ tối để chữ dễ đọc. */
public class BackgroundPanel extends JPanel {
    private static Image anhGoc;
    private final float doPhuToi;
    private BufferedImage anhDaCo;
    private int cacheW = -1, cacheH = -1;

    public BackgroundPanel(float doPhuToi) {
        this.doPhuToi = doPhuToi;
        setOpaque(true);
        setBackground(AppTheme.NAV_BG);
    }

    private static Image layAnh() {
        if (anhGoc == null) {
            anhGoc = new ImageIcon("img/background4.jpg").getImage();
        }
        return anhGoc;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        Image img = layAnh();
        int iw = img.getWidth(null), ih = img.getHeight(null);
        if (iw > 0 && ih > 0) {
            // Chỉ co giãn lại khi kích thước thay đổi để kéo/phóng cửa sổ vẫn mượt
            if (anhDaCo == null || cacheW != w || cacheH != h) {
                double s = Math.max((double) w / iw, (double) h / ih);
                int dw = (int) Math.ceil(iw * s), dh = (int) Math.ceil(ih * s);
                anhDaCo = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = anhDaCo.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
                g2.dispose();
                cacheW = w;
                cacheH = h;
            }
            g.drawImage(anhDaCo, 0, 0, null);
        }
        g.setColor(new Color(0, 0, 0, Math.round(doPhuToi * 255)));
        g.fillRect(0, 0, w, h);
    }
}
