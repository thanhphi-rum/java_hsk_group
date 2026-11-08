package gui;

import service.Session;

import java.awt.*;
import java.math.BigDecimal;

import javax.swing.*;
import javax.swing.event.*;

/** Xác nhận thu tiền mặt; hủy hoặc thiếu tiền không được ghi hóa đơn. */
public final class ThanhToanDialog {
    private ThanhToanDialog() {}

    /** Giao diện gọi quy tắc tính tiền ở service, không tự tính lại. */
    public static BigDecimal tienThoi(BigDecimal total, String input) {
        return service.ThanhToanService.tinhTienThoi(total, input);
    }

    public static boolean xacNhan(Component parent, BigDecimal total) {
        Session.requireStaff();
        JTextField received = new JTextField(18);
        JLabel change = new JLabel("Chưa nhập tiền khách đưa");
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
        form.add(new JLabel("Thanh toán tiền mặt tại quầy"));
        form.add(new JLabel("Tổng phải thu: " + String.format("%,.0f đồng", total)));
        form.add(new JLabel("Tiền khách đưa (nhập số, ví dụ 100000):"));
        form.add(received);
        form.add(change);
        received.getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            private void update() {
                                try {
                                    change.setText(
                                            "Tiền trả lại: "
                                                    + String.format(
                                                            "%,.0f đồng",
                                                            tienThoi(total, received.getText())));
                                } catch (IllegalArgumentException ex) {
                                    change.setText(ex.getMessage());
                                }
                            }

                            public void insertUpdate(DocumentEvent e) {
                                update();
                            }

                            public void removeUpdate(DocumentEvent e) {
                                update();
                            }

                            public void changedUpdate(DocumentEvent e) {
                                update();
                            }
                        });
        while (true) {
            int option =
                    JOptionPane.showOptionDialog(
                            parent,
                            form,
                            "Kiểm tra và thu tiền",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            new String[] {"Xác nhận đã thu tiền", "Hủy"},
                            "Hủy");
            if (option != 0) return false;
            try {
                tienThoi(total, received.getText());
                return true;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                        parent,
                        ex.getMessage(),
                        "Chưa thể thanh toán",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
