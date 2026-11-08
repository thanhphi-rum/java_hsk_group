package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import dao.ChiTietHoaDonDAO;

import entity.ChiTietHoaDon;

import util.ProjectPaths;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;

/** Tạo PDF từ hóa đơn đã lưu; không hiện hộp thoại và không tự truy vấn SQL. */
public class HoaDonPdfService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void xuat(String id, Path file) throws Exception {
        ChiTietHoaDon invoice = new ChiTietHoaDonDAO().layTheoMa(id);
        Path destination = file.toAbsolutePath();
        Files.createDirectories(destination.getParent());
        Path temporary = Files.createTempFile(destination.getParent(), "invoice-", ".pdf");
        try {
            // Chỉ thay tệp đích sau khi tạo PDF hoàn chỉnh, tránh ghi đè bằng một tệp lỗi.
            render(invoice, temporary);
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void render(ChiTietHoaDon invoice, Path file) throws Exception {
        BaseFont base =
                BaseFont.createFont(
                        ProjectPaths.resolve("fonts/ARIAL.TTF").toString(),
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED);
        Font normal = new Font(base, 11);
        Font bold = new Font(base, 12, Font.BOLD);
        try (OutputStream output = Files.newOutputStream(file)) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            try {
                PdfWriter.getInstance(document, output);
                document.open();
                Paragraph title =
                        new Paragraph("HÓA ĐƠN BÁN VÉ PHIM", new Font(base, 18, Font.BOLD));
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(18);
                document.add(title);
                document.add(new Paragraph("Mã hóa đơn: " + invoice.id(), normal));
                document.add(new Paragraph("Ngày lập: " + invoice.created().format(TIME), normal));
                document.add(new Paragraph("Nhân viên: " + invoice.employee(), normal));
                document.add(
                        new Paragraph(
                                "Khách hàng: "
                                        + (invoice.customer() == null
                                                ? "Khách vãng lai"
                                                : invoice.customer()),
                                normal));
                document.add(new Paragraph("Thanh toán: " + invoice.paymentMethod(), normal));
                document.add(ticketTable(invoice, normal, bold));
                document.add(
                        new Paragraph(
                                "Tổng thanh toán: " + String.format("%,.0f đồng", invoice.total()),
                                bold));
                document.add(
                        new Paragraph("Giá vé trong bảng là giá sau khuyến mãi (nếu có).", normal));
                document.add(
                        new Paragraph("Cảm ơn quý khách. Chúc quý khách xem phim vui vẻ!", normal));
            } finally {
                document.close();
            }
        }
    }

    private PdfPTable ticketTable(ChiTietHoaDon invoice, Font normal, Font bold) {
        PdfPTable table = new PdfPTable(new float[] {3, 2, 1, 2});
        table.setWidthPercentage(100);
        table.setSpacingBefore(18);
        table.setSpacingAfter(14);
        table.setHeaderRows(1);
        for (String title : new String[] {"Phim / Phòng", "Suất chiếu", "Ghế", "Thành tiền"}) {
            PdfPCell cell = new PdfPCell(new Phrase(title, bold));
            cell.setPadding(7);
            cell.setBackgroundColor(new BaseColor(230, 238, 245));
            table.addCell(cell);
        }
        for (ChiTietHoaDon.DongVe ticket : invoice.tickets()) {
            for (String value :
                    new String[] {
                        ticket.movie() + "\n" + ticket.room(),
                        ticket.starts().format(TIME),
                        ticket.seat(),
                        String.format("%,.0f đ", ticket.price())
                    }) {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setPadding(7);
                table.addCell(cell);
            }
        }
        return table;
    }
}
