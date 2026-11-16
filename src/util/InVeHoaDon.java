package util;

import javax.swing.JOptionPane;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;

/**
 * In vé (mỗi ghế một vé có mã QR) rồi đến một hóa đơn, gửi tới máy in. Hóa đơn còn được lưu thành file PDF trong thư mục hoadon/ của dự án.
 * Khổ giấy: cuộn nhiệt 80mm. Vé cao cố định, hóa đơn cao theo số dòng.
 */
public class InVeHoaDon {
    static final int RONG = 226;      // 80mm (điểm in, 1pt = 1/72 inch)
    static final int CAO_VE = 330;
    static final int LE = 12;

    /**
     * @param dsVe      mỗi phần tử là một vé: {tên phim, giờ chiếu "yyyy-MM-dd HH:mm", phòng, ghế, giá vé}
     * @param noiDungHD các dòng hóa đơn cách nhau "\n". Dòng "---" = đường kẻ; "trái\tphải" = căn phải giá trị;
     *                  dòng bắt đầu bằng "#" = in đậm.
     */
    /** @return true nếu đã gửi lệnh in; false nếu nhân viên hủy hộp thoại in hoặc máy in báo lỗi. */
    public static boolean in(Component parent, String maHD, List<String[]> dsVe, String noiDungHD) {
        try {
            luuHoaDon(maHD, noiDungHD);   // lưu bản hóa đơn vào thư mục hoadon của dự án, bất kể sau đó in được hay không
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Không lưu được file hóa đơn vào thư mục \"" + THU_MUC_HOA_DON + "\": " + ex.getMessage(),
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Ve va hoa don " + maHD);
        job.setPageable(taoTapIn(maHD, dsVe, noiDungHD));
        try {
            if (job.printDialog()) {
                job.print();
                return true;
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(parent, "Không in được: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /** Book passes the absolute page index to each Printable, including later tickets and the invoice. */
    static Book taoTapIn(String maHD, List<String[]> dsVe, String noiDungHD) {
        Book book = new Book();
        for (String[] v : dsVe) {
            final int trangVe = book.getNumberOfPages();
            book.append((g, pf, trang) -> {
                if (trang != trangVe) return Printable.NO_SUCH_PAGE;
                Graphics2D banVe = (Graphics2D) g.create();
                try {
                    veVe(banVe, pf.getImageableX(), pf.getImageableY(), maHD, v);
                } finally {
                    banVe.dispose();
                }
                return Printable.PAGE_EXISTS;
            }, trangGiay(CAO_VE));
        }
        String[] dong = noiDungHD.split("\n");
        int caoHD = (int) chieuCaoHoaDon(dong) + 2 * LE;
        final int trangHoaDon = book.getNumberOfPages();
        book.append((g, pf, trang) -> {
            if (trang != trangHoaDon) return Printable.NO_SUCH_PAGE;
            Graphics2D hoaDon = (Graphics2D) g.create();
            try {
                veHoaDon(hoaDon, pf.getImageableX(), pf.getImageableY(), maHD, dong);
            } finally {
                hoaDon.dispose();
            }
            return Printable.PAGE_EXISTS;
        }, trangGiay(caoHD));
        return book;
    }
    /** Thư mục (tính từ thư mục chạy chương trình = thư mục dự án) chứa hóa đơn đã in, mỗi hóa đơn một file PDF. */
    public static final String THU_MUC_HOA_DON = "hoadon";

    /** Vẽ hóa đơn giống bản in rồi lưu thành hoadon/<mã hóa đơn>.pdf (ghi đè nếu in lại). */
    public static java.io.File luuHoaDon(String maHD, String noiDungHD) throws Exception {
        String[] dong = noiDungHD.split("\n");
        int cao = (int) chieuCaoHoaDon(dong) + 2 * LE;
        final int tyLe = 4;   // vẽ ở độ phân giải gấp 4 để chữ và mã QR sắc nét khi phóng to
        BufferedImage anh = new BufferedImage(RONG * tyLe, cao * tyLe, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = anh.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, anh.getWidth(), anh.getHeight());
        g.scale(tyLe, tyLe);
        veHoaDon(g, 0, 0, maHD, dong);
        g.dispose();

        java.io.ByteArrayOutputStream png = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(anh, "png", png);

        java.io.File thuMuc = new java.io.File(THU_MUC_HOA_DON);
        if (!thuMuc.isDirectory() && !thuMuc.mkdirs()) throw new java.io.IOException("không tạo được thư mục " + thuMuc.getAbsolutePath());
        java.io.File file = new java.io.File(thuMuc, maHD.replaceAll("[^A-Za-z0-9_-]", "_") + ".pdf");

        com.itextpdf.text.Document doc = new com.itextpdf.text.Document(new com.itextpdf.text.Rectangle(RONG, cao), 0, 0, 0, 0);
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, out);
            doc.open();
            com.itextpdf.text.Image hinh = com.itextpdf.text.Image.getInstance(png.toByteArray());
            hinh.scaleAbsolute(RONG, cao);
            hinh.setAbsolutePosition(0, 0);
            doc.add(hinh);
            doc.close();
        }
        return file;
    }

    private static PageFormat trangGiay(int cao) {
        Paper p = new Paper();
        p.setSize(RONG, cao);
        p.setImageableArea(0, 0, RONG, cao);
        PageFormat pf = new PageFormat();
        pf.setPaper(p);
        pf.setOrientation(PageFormat.PORTRAIT);
        return pf;
    }

    // ---------------------------------------------------------------- VÉ
    static void veVe(Graphics2D g, double ox, double oy, String maHD, String[] v) {
        batChatLuong(g);
        g.translate(ox, oy);
        int w = RONG - 2 * LE, y = LE;
        y = giua(g, "RẠP CHIẾU PHIM", Font.BOLD, 15, y, true);
        y = giua(g, "VÉ XEM PHIM", Font.BOLD, 12, y, true);
        y = duongKe(g, y, true);
        y = xuongDong(g, v[0].toUpperCase(), Font.BOLD, 13, LE, y + 2, w, true);   // tên phim, tự xuống dòng
        String ngay = v[1].length() >= 16 ? v[1].substring(8, 10) + "/" + v[1].substring(5, 7) + "/" + v[1].substring(0, 4) : v[1];
        String gio = v[1].length() >= 16 ? v[1].substring(11, 16) : "";
        y = traiPhai(g, "Ngày chiếu", ngay, Font.PLAIN, 11, y + 4, true);
        y = traiPhai(g, "Giờ chiếu", gio, Font.BOLD, 11, y, true);
        y = traiPhai(g, "Phòng", v[2], Font.PLAIN, 11, y, true);
        y = traiPhai(g, "Giá vé", v[4], Font.PLAIN, 11, y, true);
        y = giua(g, "GHẾ  " + v[3], Font.BOLD, 20, y + 6, true);
        y = duongKe(g, y, true);
        int kt = QR.veGiua(g, maHD + "-" + v[3], RONG / 2, y + 4, 100);
        y += 4 + kt + 2;
        giua(g, maHD + "-" + v[3], Font.PLAIN, 8, y, true);
    }

    // ---------------------------------------------------------------- HÓA ĐƠN
    static void veHoaDon(Graphics2D g, double ox, double oy, String maHD, String[] dong) {
        batChatLuong(g);
        g.translate(ox, oy);
        noiDungHoaDon(g, maHD, dong, true);
    }

    static double chieuCaoHoaDon(String[] dong) {
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        batChatLuong(g);
        int y = noiDungHoaDon(g, "x", dong, false);
        g.dispose();
        return y;
    }

    /** Vẽ (hoặc chỉ đo khi ve=false) hóa đơn, trả về tọa độ y cuối cùng. */
    private static int noiDungHoaDon(Graphics2D g, String maHD, String[] dong, boolean ve) {
        int w = RONG - 2 * LE, y = LE;
        y = giua(g, "RẠP CHIẾU PHIM", Font.BOLD, 15, y, ve);
        y = giua(g, "HÓA ĐƠN BÁN VÉ", Font.BOLD, 12, y, ve);
        y = giua(g, maHD, Font.PLAIN, 9, y, ve);
        y = duongKe(g, y, ve);
        for (String d : dong) {
            if (d.equals("---")) {
                y = duongKe(g, y, ve);
                continue;
            }
            boolean dam = d.startsWith("#");
            if (dam) d = d.substring(1);
            int co = dam ? 13 : 11, kieu = dam ? Font.BOLD : Font.PLAIN;
            int tab = d.indexOf('\t');
            if (tab >= 0) y = traiPhai(g, d.substring(0, tab), d.substring(tab + 1), kieu, co, y, ve);
            else y = xuongDong(g, d, kieu, co, LE, y, w, ve);
        }
        y = duongKe(g, y, ve);
        int kt = ve ? QR.veGiua(g, maHD, RONG / 2, y + 4, 90) : 90;
        y += 4 + kt + 2;
        return giua(g, "Cảm ơn quý khách - Hẹn gặp lại!", Font.ITALIC, 9, y, ve);
    }

    // ---------------------------------------------------------------- tiện ích vẽ
    // Quy ước: y = mép trên của dòng; mỗi hàm vẽ xong trả về mép trên của dòng kế tiếp.
    private static void batChatLuong(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
    }

    private static Font font(int kieu, int co) { return new Font("Arial", kieu, co); }

    private static int giua(Graphics2D g, String s, int kieu, int co, int y, boolean ve) {
        g.setFont(font(kieu, co));
        int x = (RONG - g.getFontMetrics().stringWidth(s)) / 2;
        if (ve) g.drawString(s, Math.max(LE / 2, x), y + co);
        return y + co + 5;
    }

    private static int traiPhai(Graphics2D g, String trai, String phai, int kieu, int co, int y, boolean ve) {
        g.setFont(font(kieu, co));
        if (ve) {
            g.drawString(trai, LE, y + co);
            g.drawString(phai, RONG - LE - g.getFontMetrics().stringWidth(phai), y + co);
        }
        return y + co + 5;
    }

    private static int duongKe(Graphics2D g, int y, boolean ve) {
        if (ve) {
            g.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
            g.drawLine(LE, y + 4, RONG - LE, y + 4);
        }
        return y + 10;
    }

    /** Vẽ chữ tự xuống dòng trong bề rộng cho phép. */
    private static int xuongDong(Graphics2D g, String s, int kieu, int co, int x, int y, int rong, boolean ve) {
        g.setFont(font(kieu, co));
        FontMetrics fm = g.getFontMetrics();
        StringBuilder dong = new StringBuilder();
        for (String tu : s.split(" ")) {
            String thu = dong.length() == 0 ? tu : dong + " " + tu;
            if (fm.stringWidth(thu) > rong && dong.length() > 0) {
                if (ve) g.drawString(dong.toString(), x, y + co);
                y += co + 3;
                dong = new StringBuilder(tu);
            } else dong = new StringBuilder(thu);
        }
        if (ve) g.drawString(dong.toString(), x, y + co);
        return y + co + 5;
    }
}
