package util;

import com.itextpdf.text.pdf.BarcodeQRCode;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

/** Tạo mã QR bằng iText (có sẵn trong lib/) và vẽ sắc nét: mỗi ô QR là một hình vuông nguyên pixel. */
public class QR {

    /** Ma trận QR: true = ô đen. */
    public static boolean[][] matrix(String noiDung) {
        Image raw = new BarcodeQRCode(noiDung, 1, 1, null).createAwtImage(Color.BLACK, Color.WHITE);
        ImageIcon icon = new ImageIcon(raw); // đợi ảnh tải xong
        int w = icon.getIconWidth(), h = icon.getIconHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.drawImage(icon.getImage(), 0, 0, null);
        g.dispose();
        boolean[][] m = new boolean[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) m[y][x] = (bi.getRGB(x, y) & 0xFF) < 128;
        return m;
    }

    /** Vẽ QR vào (x,y) với cạnh xấp xỉ kichThuoc; trả về cạnh thực tế đã vẽ. */
    public static int ve(Graphics2D g, String noiDung, int x, int y, int kichThuoc) {
        boolean[][] m = matrix(noiDung);
        int o = Math.max(1, kichThuoc / m.length);
        g.setColor(Color.WHITE);
        g.fillRect(x, y, o * m.length, o * m.length);
        g.setColor(Color.BLACK);
        for (int r = 0; r < m.length; r++)
            for (int c = 0; c < m[r].length; c++)
                if (m[r][c]) g.fillRect(x + c * o, y + r * o, o, o);
        return o * m.length;
    }

    /** Như ve() nhưng căn giữa theo tọa độ tâmX. */
    public static int veGiua(Graphics2D g, String noiDung, int tamX, int y, int kichThuoc) {
        int o = Math.max(1, kichThuoc / matrix(noiDung).length);
        return ve(g, noiDung, tamX - o * matrix(noiDung).length / 2, y, kichThuoc);
    }

    /** Ảnh QR (nền trắng, có viền trắng 3 ô) để hiện trên màn hình. */
    public static BufferedImage anh(String noiDung, int kichThuoc) {
        boolean[][] m = matrix(noiDung);
        int vien = 3, tong = m.length + 2 * vien;
        int o = Math.max(1, kichThuoc / tong);
        BufferedImage bi = new BufferedImage(o * tong, o * tong, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        g.setColor(Color.BLACK);
        for (int r = 0; r < m.length; r++)
            for (int c = 0; c < m[r].length; c++)
                if (m[r][c]) g.fillRect((c + vien) * o, (r + vien) * o, o, o);
        g.dispose();
        return bi;
    }
}
