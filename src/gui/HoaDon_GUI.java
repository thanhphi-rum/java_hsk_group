package gui;
	
import dao.GheDAO;
import dao.HoaDonDAO;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import connect.ConnectDB;

import java.awt.*;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class HoaDon_GUI extends JFrame {
    private JTable tableHoaDon;
    private DefaultTableModel tableModel;
    private JTextField tfTimKiem;
    private HoaDonDAO hoaDonDAO;
    private GheDAO gheDAO;
    private JButton btnTimKiem, btnXoa, btnLamMoi;

    public HoaDon_GUI() {
        hoaDonDAO = new HoaDonDAO();
        gheDAO = new GheDAO();

        setTitle("Quản Lý Hóa Đơn - Rạp Chiếu Phim");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel menuPanel = new Menu_GUI(this);
        mainPanel.add(menuPanel, BorderLayout.NORTH);

        // ====== Table ======
        String[] columnNames = {"Mã Hóa Đơn", "Ngày Lập", "Nhân Viên", "Khách Hàng", "Khuyến Mãi", "Tổng Tiền", "Phương Thức"};
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHoaDon = tableHoaDon.getValueAt(selectedRow, 0).toString();

        try (Connection conn = ConnectDB.getConnection()) {
            conn.setAutoCommit(false);

            // Cập nhật trạng thái ghế thành "Trống" dựa vào bảng Ve
            boolean capNhatGhe = gheDAO.capNhatTrangThaiGheSauKhiXoaHoaDon(conn, maHoaDon);

            // Xóa vé thuộc hóa đơn (ON DELETE CASCADE trong SQL hoặc xóa tay ở đây)
            String sqlDeleteVe = "DELETE FROM Ve WHERE MaHoaDon = ?";
            try(PreparedStatement psVe = conn.prepareStatement(sqlDeleteVe)){
                psVe.setString(1, maHoaDon);
                psVe.executeUpdate();
            }

            // Xóa hóa đơn 
            boolean xoaHoaDonThanhCong = hoaDonDAO.xoaHoaDon(conn, maHoaDon);

            if (xoaHoaDonThanhCong) {
                conn.commit();
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn và cập nhật ghế thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi thực hiện giao dịch.", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            tableModel.addRow(new Object[]{
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
        SwingUtilities.invokeLater(() -> {
            new HoaDon_GUI().setVisible(true);
        });
    }
}
