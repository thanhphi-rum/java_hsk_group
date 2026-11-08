package gui;

import service.Session;

import java.awt.*;

import javax.swing.*;

public class ThanhToanDatVe_GUI extends JPanel {
    private final JTextField code = new JTextField(22);
    private final JTextArea detail = new JTextArea();
    private final JButton pay = new JButton("Đã nhận tiền / Xuất vé");
    private final JButton print = new JButton("Xuất PDF");
    private entity.DatCho booking;

    public ThanhToanDatVe_GUI() {
        super(new BorderLayout(12, 12));
        Session.requireStaff();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton find = new JButton("Tra mã đặt vé");
        top.add(new JLabel("Mã khách cung cấp:"));
        top.add(code);
        top.add(find);
        add(top, BorderLayout.NORTH);
        detail.setEditable(false);
        detail.setMargin(new Insets(18, 18, 18, 18));
        add(new JScrollPane(detail));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(pay);
        bottom.add(print);
        add(bottom, BorderLayout.SOUTH);
        pay.setEnabled(false);
        print.setEnabled(false);
        find.addActionListener(e -> load());
        code.addActionListener(e -> load());
        pay.addActionListener(
                e -> {
                    if (booking == null) return;
                    if (!ThanhToanDialog.xacNhan(this, booking.total())) return;
                    try {
                        String invoice = new service.BanVeService().thanhToanDon(booking.id());
                        load();
                        HoaDonPDF.chonNoiLuu(this, invoice);
                    } catch (Exception ex) {
                        error(ex);
                        load();
                    }
                });
        print.addActionListener(
                e -> {
                    if (booking != null && booking.invoice() != null)
                        HoaDonPDF.chonNoiLuu(this, booking.invoice());
                });
    }

    private void load() {
        booking = null;
        pay.setEnabled(false);
        print.setEnabled(false);
        detail.setText("");
        try {
            booking =
                    new service.BanVeService()
                            .traDon(code.getText().trim().toUpperCase(java.util.Locale.ROOT));
            detail.setText(booking.description());
            pay.setEnabled("CHO_THANH_TOAN".equals(booking.status()));
            print.setEnabled(booking.invoice() != null);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(
                this, ex.getMessage(), "Không thể xử lý", JOptionPane.ERROR_MESSAGE);
    }
}
