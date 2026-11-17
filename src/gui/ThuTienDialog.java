package gui;

import util.PhuongThucThanhToan;
import util.QR;
import util.VietQR;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.TreeSet;

/** Bước thu tiền tại quầy, mỗi phương thức một cách: tiền mặt (tính tiền thừa) và chuyển khoản (mã VietQR). */
public final class ThuTienDialog {
    private static final Color XANH = new Color(0x4CD97B);
    private static final Color DO = new Color(0xFF6B6B);

    /** Kết quả khi nhân viên xác nhận đã thu tiền. */
    public static final class KetQua {
        /** Số tiền khách đưa; chỉ có khi trả tiền mặt, các phương thức khác là null. */
        public final Double tienKhachDua;

        KetQua(Double tienKhachDua) { this.tienKhachDua = tienKhachDua; }
    }

    private ThuTienDialog() { }

    /** @return kết quả khi đã thu tiền, hoặc null nếu nhân viên hủy. */
    public static KetQua hienThi(Component parent, String phuongThuc, double soTien, String maHD) {
        if (PhuongThucThanhToan.TIEN_MAT.equals(phuongThuc)) return thuTienMat(parent, soTien);
        return thuChuyenKhoan(parent, soTien, maHD);
    }

    // ------------------------------------------------------------ tiền mặt
    private static KetQua thuTienMat(Component parent, double soTien) {
        JDialog d = taoHopThoai(parent, "Thu tiền mặt");
        JPanel pn = noiDung(d);
        pn.add(nhan("Số tiền cần thu: " + tien(soTien), 18, true));
        pn.add(Box.createVerticalStrut(12));
        pn.add(nhan("Tiền khách đưa:", 13, false));

        JTextField tf = new JTextField();
        tf.putClientProperty("JTextField.placeholderText", "Nhập số tiền khách đưa");
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        tf.setFont(tf.getFont().deriveFont(Font.BOLD, 18f));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pn.add(tf);

        JPanel goiY = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        goiY.setOpaque(false);
        goiY.setAlignmentX(Component.LEFT_ALIGNMENT);
        goiY.add(nutGoiY("Đủ tiền", tf, (long) soTien));
        for (long v : menhGiaGoiY((long) soTien)) goiY.add(nutGoiY(String.format("%,d", v), tf, v));
        pn.add(Box.createVerticalStrut(8));
        pn.add(goiY);

        JLabel lblDua = nhan(" ", 13, false);
        JLabel lblThua = nhan(" ", 16, true);
        pn.add(Box.createVerticalStrut(8));
        pn.add(lblDua);
        pn.add(lblThua);

        JButton ok = new JButton("Đã thu tiền");
        JButton huy = new JButton("Hủy");
        KetQua[] kq = {null};
        Runnable capNhat = () -> {
            double dua = docTien(tf.getText());
            double chenh = dua - soTien;
            boolean du = chenh >= 0;
            lblDua.setText(dua == 0 ? " " : "Khách đưa: " + tien(dua));
            lblThua.setText(dua == 0 ? " " : du ? "Tiền thừa trả khách: " + tien(chenh) : "Còn thiếu: " + tien(-chenh));
            lblThua.setForeground(du ? XANH : DO);
            ok.setEnabled(du);
        };
        khiThayDoi(tf, capNhat);
        capNhat.run();
        ok.addActionListener(e -> {
            kq[0] = new KetQua(docTien(tf.getText()));
            d.dispose();
        });
        huy.addActionListener(e -> d.dispose());
        d.getRootPane().setDefaultButton(ok);
        return chay(d, parent, pn, ok, huy, kq);
    }

    /** Các mệnh giá khách thường đưa (làm tròn lên 10k/50k/100k/200k/500k), lớn hơn số tiền cần thu. */
    private static long[] menhGiaGoiY(long soTien) {
        TreeSet<Long> ds = new TreeSet<>();
        for (long buoc : new long[]{10_000, 50_000, 100_000, 200_000, 500_000}) {
            long v = (soTien + buoc - 1) / buoc * buoc;
            if (v > soTien) ds.add(v);
        }
        return ds.stream().limit(4).mapToLong(Long::longValue).toArray();
    }

    private static JButton nutGoiY(String chu, JTextField tf, long giaTri) {
        JButton b = new JButton(chu);
        b.addActionListener(e -> tf.setText(String.valueOf(giaTri)));
        return b;
    }

    // ------------------------------------------------------------ chuyển khoản
    private static KetQua thuChuyenKhoan(Component parent, double soTien, String maHD) {
        JDialog d = taoHopThoai(parent, "Thu tiền - Chuyển khoản");
        JPanel pn = noiDung(d);

        // Mọi thành phần canh giữa
        JLabel lblTien = nhan("Số tiền cần thu: " + tien(soTien), 18, true);
        JLabel qr = new JLabel(new ImageIcon(QR.anh(VietQR.taoChuoi((long) soTien, maHD), 280)));
        qr.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel lblNganHang = nhan(VietQR.TEN_NGAN_HANG + " · STK " + VietQR.SO_TAI_KHOAN, 14, false);
        JLabel lblChuTK = nhan(VietQR.TEN_CHU_TK, 14, false);
        JLabel lblNoiDung = nhan("Nội dung: " + maHD, 14, false);

        for (JLabel l : new JLabel[]{lblTien, qr, lblNganHang, lblChuTK, lblNoiDung}) {
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            pn.add(l);
        }
        pn.add(Box.createVerticalStrut(8));
        return xacNhanDonGian(d, parent, pn, "Đã nhận tiền");
    }

    /** Hộp thoại chỉ có hai nút xác nhận/hủy; không đặt nút mặc định để tránh bấm Enter nhầm. */
    private static KetQua xacNhanDonGian(JDialog d, Component parent, JPanel pn, String chuNutXacNhan) {
        JButton ok = new JButton(chuNutXacNhan);
        JButton huy = new JButton("Hủy");
        KetQua[] kq = {null};
        ok.addActionListener(e -> {
            kq[0] = new KetQua(null);
            d.dispose();
        });
        huy.addActionListener(e -> d.dispose());
        return chay(d, parent, pn, ok, huy, kq);
    }

    // ------------------------------------------------------------ tiện ích dựng hộp thoại
    private static JDialog taoHopThoai(Component parent, String tieuDe) {
        JDialog d = new JDialog(parent == null ? null : SwingUtilities.getWindowAncestor(parent), tieuDe, Dialog.ModalityType.APPLICATION_MODAL);
        d.setResizable(false);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        return d;
    }

    private static JPanel noiDung(JDialog d) {
        JPanel pn = new JPanel();
        pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
        pn.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        return pn;
    }

    private static KetQua chay(JDialog d, Component parent, JPanel pn, JButton ok, JButton huy, KetQua[] kq) {
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        nut.add(ok);
        nut.add(huy);
        d.add(pn, BorderLayout.CENTER);
        d.add(nut, BorderLayout.SOUTH);
        d.pack();
        d.setMinimumSize(new Dimension(380, 0));
        d.setLocationRelativeTo(parent);
        d.setVisible(true);   // chặn tại đây cho đến khi hộp thoại đóng
        return kq[0];
    }

    private static JLabel nhan(String chu, int co, boolean dam) {
        JLabel l = new JLabel(chu);
        l.setFont(l.getFont().deriveFont(dam ? Font.BOLD : Font.PLAIN, (float) co));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return l;
    }

    private static void khiThayDoi(JTextField tf, Runnable viec) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { viec.run(); }
            public void removeUpdate(DocumentEvent e) { viec.run(); }
            public void changedUpdate(DocumentEvent e) { viec.run(); }
        });
    }

    private static double docTien(String chu) {
        String so = chu.replaceAll("\\D", "");
        return so.isEmpty() || so.length() > 12 ? 0 : Double.parseDouble(so);
    }

    private static String tien(double v) { return String.format("%,.0f VNĐ", v); }
}
