package gui;

import dao.GheDAO;
import dao.HoaDonDAO;

import entity.HoaDon;

import service.Session;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HoaDon_GUI extends JPanel {
    private JTable tableHoaDon;
    private DefaultTableModel tableModel;
    private JTextField tfTimKiem;
    private HoaDonDAO hoaDonDAO;
    private GheDAO gheDAO;
    private JButton btnTimKiem, btnXoa, btnLamMoi;

    public HoaDon_GUI() {
        Session.requireStaff();
        setLayout(new BorderLayout(10, 10));
        hoaDonDAO = new HoaDonDAO();
        gheDAO = new GheDAO();

        JPanel mainPanel = new JPanel(new BorderLayout());


        // ====== Table ======
        String[] columnNames = {
            "Mã Hóa Đơn",
            "Ngày Lập",
            "Nhân Viên",
            "Khách Hàng",
            "Khuyến Mãi",
            "Tổng Tiền",
            "Phương Thức"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        tableHoaDon = new JTable(tableModel);
        tableHoaDon.setRowHeight(30);
        tableHoaDon.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
        scrollPane.setPreferredSize(new Dimension(1200, 600));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ====== Control Panel ======
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        tfTimKiem = new HintTextField("Nhập mã hóa đơn");
        tfTimKiem.setPreferredSize(new Dimension(200, 30));
        controlPanel.add(new JLabel("Tìm Kiếm:"));
        controlPanel.add(tfTimKiem);

        btnTimKiem = new JButton("Tìm Kiếm");
        btnTimKiem.addActionListener(e -> timKiemHoaDon());
        controlPanel.add(btnTimKiem);

        btnXoa = new JButton("Xóa");
        btnXoa.addActionListener(e -> xoaHoaDon());
        controlPanel.add(btnXoa);
        btnLamMoi = new JButton("Làm Mới");
        btnLamMoi.addActionListener(e -> lamMoiDanhSach());
        controlPanel.add(btnLamMoi);
        JButton export = new JButton("Xuất PDF");
        export.addActionListener(
                e -> {
                    int row = tableHoaDon.getSelectedRow();
                    if (row < 0) {
                        JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần xuất.");
                        return;
                    }
                    HoaDonPDF.chonNoiLuu(this, tableHoaDon.getValueAt(row, 0).toString());
                });
        controlPanel.add(export);
        JButton detail = new JButton("Chi tiết");
        detail.addActionListener(e -> {
            int row = tableHoaDon.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn."); return; }
            try {
                var invoice = new dao.ChiTietHoaDonDAO().layTheoMa(tableHoaDon.getValueAt(row, 0).toString());
                DefaultTableModel lines = new DefaultTableModel(new String[]{"Phim", "Phòng", "Suất", "Ghế", "Thành tiền"}, 0) {
                    public boolean isCellEditable(int r, int c) { return false; }
                };
                for (var ticket : invoice.tickets()) lines.addRow(new Object[]{ticket.movie(), ticket.room(), ticket.starts(), ticket.seat(), ticket.price()});
                JScrollPane view = new JScrollPane(new JTable(lines)); view.setPreferredSize(new Dimension(800, 300));
                JOptionPane.showMessageDialog(this, view, "Hóa đơn " + invoice.id(), JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });
        controlPanel.add(detail);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        lamMoiDanhSach();
    }

    private void timKiemHoaDon() {
        String keyword = tfTimKiem.getText().trim();
        List<HoaDon> danhSachHoaDon = hoaDonDAO.timKiemHoaDon(keyword);
        capNhatBangHoaDon(danhSachHoaDon);
    }

    private void xoaHoaDon() {
        int selectedRow = tableHoaDon.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn hóa đơn cần xóa.",
                    "Thông Báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHoaDon = tableHoaDon.getValueAt(selectedRow, 0).toString();
        if (JOptionPane.showConfirmDialog(
                        this,
                        "Xóa hóa đơn sẽ xóa vé và trả lại ghế. Tiếp tục?",
                        "Xác nhận",
                        JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;

        // Việc trả ghế, xóa vé và xóa hóa đơn phải thành công cùng nhau ở DAO.
        try {
            hoaDonDAO.xoaHoaDonVaVe(maHoaDon);
            JOptionPane.showMessageDialog(this, "Đã xóa hóa đơn và trả lại ghế.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Không xóa được hóa đơn", JOptionPane.ERROR_MESSAGE);
        }

        lamMoiDanhSach();
    }

    private void lamMoiDanhSach() {
        List<HoaDon> danhSachHoaDon = hoaDonDAO.layTatCaHoaDon();
        capNhatBangHoaDon(danhSachHoaDon);
    }

    private void capNhatBangHoaDon(List<HoaDon> danhSachHoaDon) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (HoaDon hoaDon : danhSachHoaDon) {
            tableModel.addRow(
                    new Object[] {
                        hoaDon.getMaHoaDon(),
                        hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(formatter) : "",
                        hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getMaNhanVien() : "",
                        hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getMaKhachHang() : "",
                        hoaDon.getKhuyenMai() != null ? hoaDon.getKhuyenMai().getMaKhuyenMai() : "",
                        String.format("%,.0f VNĐ", hoaDon.getTongTien()),
                        hoaDon.getPhongThucThanhToan()
                    });
        }
    }

    public static void main(String[] args) {
        DangNhap_GUI.main(args);
    }
}
