package gui;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.awt.Component;
import java.io.*;
import java.nio.file.*;
import java.sql.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Chỉ hỏi nơi lưu và báo kết quả cho người dùng. */
public final class HoaDonPDF {
    private HoaDonPDF() {}

    public static void chonNoiLuu(Component parent, String id) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
        chooser.setSelectedFile(new File("HoaDon_" + id + ".pdf"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf"))
            file = new File(file.getPath() + ".pdf");
        if (file.exists()
                && JOptionPane.showConfirmDialog(
                                parent,
                                "Tệp đã tồn tại. Ghi đè?",
                                "Xuất PDF",
                                JOptionPane.YES_NO_OPTION)
                        != JOptionPane.YES_OPTION) return;
        try {
            xuat(id, file.toPath());
            JOptionPane.showMessageDialog(parent, "Đã xuất hóa đơn:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Không xuất được PDF: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Giữ API gọi cũ; toàn bộ tạo PDF nằm ở service. */
    public static void xuat(String id, Path file) throws Exception {
        new service.HoaDonPdfService().xuat(id, file);
    }
}
