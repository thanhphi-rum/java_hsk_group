package gui;

import connect.ConnectDB;
import dao.HoaDonDAO;
import dao.PhieuDatVeDAO;
import entity.HoaDon;
import entity.ChiTietPhieuDatVe;
import entity.KhachHang;
import entity.PhieuDatVe;
import entity.Ve;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class QuanLyPhieuDat_GUI extends JPanel {
    private static final String TAT_CA = "Tất cả";
    private static final String CHO_XAC_NHAN = "Chờ xác nhận";
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuDatVeDAO dao = new PhieuDatVeDAO();
    private final JTextField tfTim = new HintTextField("Tìm mã phiếu, tên khách hoặc số điện thoại");
    private final JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{
            TAT_CA, CHO_XAC_NHAN, "Đã xác nhận", "Đã thanh toán", "Đã hủy"});
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã phiếu", "Ngày đặt", "Khách hàng", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextArea txtChiTiet = new JTextArea();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final JButton btnXacNhan = new JButton("Xác nhận");
    private final JButton btnThanhToan = new JButton("Thanh toán");
    private final JButton btnIn = new JButton("In phiếu");
    private final JButton btnHuy = new JButton("Hủy phiếu");
    private final List<PhieuDatVe> danhSach = new ArrayList<>();

    public QuanLyPhieuDat_GUI() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);

        txtChiTiet.setEditable(false);
        txtChiTiet.setLineWrap(true);
        txtChiTiet.setWrapStyleWord(true);
        txtChiTiet.setFont(AppTheme.font(Font.PLAIN, 14));
        txtChiTiet.setForeground(Color.WHITE);
        txtChiTiet.setOpaque(false);
        txtChiTiet.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane spChiTiet = new JScrollPane(txtChiTiet);
        spChiTiet.setBorder(null);
        spChiTiet.setOpaque(false);
        spChiTiet.getViewport().setOpaque(false);
        AppTheme.Card phai = new AppTheme.Card(new BorderLayout(0, 10));
        phai.setPreferredSize(new java.awt.Dimension(300, 0));
        phai.add(AppTheme.label("Chi tiết phiếu", Font.BOLD, 16, Color.WHITE), BorderLayout.NORTH);
        phai.add(spChiTiet, BorderLayout.CENTER);
        JPanel chanPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        chanPhai.setOpaque(false);
        btnIn.setPreferredSize(new java.awt.Dimension(110, 36));
        chanPhai.add(btnIn);
        phai.add(chanPhai, BorderLayout.SOUTH);

        // Ô tìm + nút Tìm Kiếm lên đầu thẻ, các nút còn lại xuống cuối thẻ (do Khung.dung tách)
        JPanel dieuKhien = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        dieuKhien.add(tfTim);
        JButton btnTim = new JButton("Tìm Kiếm");
        btnTim.addActionListener(e -> taiDanhSach());
        dieuKhien.add(btnTim);
        dieuKhien.add(btnXacNhan);
        dieuKhien.add(btnThanhToan);
        dieuKhien.add(btnHuy);
        JButton btnTaiLai = new JButton("Làm mới");
        btnTaiLai.addActionListener(e -> { tfTim.setText(""); cbTrangThai.setSelectedIndex(0); taiDanhSach(); });
        dieuKhien.add(btnTaiLai);
        btnXacNhan.setEnabled(false);
        btnThanhToan.setEnabled(false);
        btnIn.setEnabled(false);
        btnHuy.setEnabled(false);
        btnXacNhan.addActionListener(e -> xacNhanPhieu());
        btnThanhToan.addActionListener(e -> thanhToanPhieu());
        btnIn.addActionListener(e -> inPhieu());
        btnHuy.addActionListener(e -> huyPhieu());

        JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        loc.setOpaque(false);
        loc.add(AppTheme.label("Trạng thái:", Font.PLAIN, 13, AppTheme.MUTED));
        loc.add(cbTrangThai);
        Khung.dung(this, "Phiếu Đặt Vé", dieuKhien, new JScrollPane(table), phai, null, loc);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienChiTiet();
        });
        cbTrangThai.addActionListener(e -> taiDanhSach());
        tfTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { taiDanhSach(); }
            @Override public void removeUpdate(DocumentEvent e) { taiDanhSach(); }
            @Override public void changedUpdate(DocumentEvent e) { taiDanhSach(); }
        });
        taiDanhSach();
    }

    private void taiDanhSach() {
        model.setRowCount(0);
        danhSach.clear();
        txtChiTiet.setText("");
        capNhatNut(null);
        String trangThai = cbTrangThai.getSelectedItem() == null || TAT_CA.equals(cbTrangThai.getSelectedItem())
                ? null : cbTrangThai.getSelectedItem().toString();
        try {
            danhSach.addAll(dao.layDanhSach(tfTim.getText(), trangThai));
            for (PhieuDatVe phieu : danhSach) {
                KhachHang khach = phieu.getKhachHang();
                String tenKhach = khach == null ? "Khách vãng lai" : khach.getTenKhachHang();
                model.addRow(new Object[]{phieu.getMaPhieuDatVe(),
                        phieu.getNgayDat() == null ? "" : phieu.getNgayDat().format(NGAY),
                        tenKhach == null ? "" : tenKhach, phieu.getTrangThai()});
            }
            if (model.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            txtChiTiet.setText("Không tải được danh sách phiếu: " + ex.getMessage());
        }
    }

    private PhieuDatVe phieuDangChon() {
        int row = table.getSelectedRow();
        return row < 0 ? null : danhSach.get(table.convertRowIndexToModel(row));
    }

    private void capNhatNut(PhieuDatVe phieu) {
        String tt = phieu == null ? null : phieu.getTrangThai();
        boolean choXacNhan = CHO_XAC_NHAN.equals(tt);
        boolean conHieuLuc = choXacNhan || "Đã xác nhận".equals(tt);
        btnXacNhan.setEnabled(choXacNhan);
        btnThanhToan.setEnabled(conHieuLuc);
        btnHuy.setEnabled(conHieuLuc);
        btnIn.setEnabled(phieu != null);
    }

    private void hienChiTiet() {
        PhieuDatVe phieu = phieuDangChon();
        capNhatNut(phieu);
        if (phieu == null) {
            txtChiTiet.setText("");
            return;
        }
        KhachHang khach = phieu.getKhachHang();
        StringBuilder text = new StringBuilder("Mã phiếu: ").append(phieu.getMaPhieuDatVe())
                .append("\nNgày đặt: ").append(phieu.getNgayDat() == null ? "" : phieu.getNgayDat().format(NGAY))
                .append("\nKhách hàng: ").append(khach == null ? "Khách vãng lai" : khach.getTenKhachHang())
                .append("\nTrạng thái: ").append(phieu.getTrangThai()).append("\n\nVé trong phiếu:\n");
        double tong = 0;
        try {
            List<ChiTietPhieuDatVe> chiTiet = dao.layChiTiet(phieu.getMaPhieuDatVe());
            if (chiTiet.isEmpty()) text.append("Không có vé trong phiếu.");
            for (ChiTietPhieuDatVe dong : chiTiet) {
                Ve ve = dong.getVe();
                String maSuat = ve.getGheSuatChieu().getSuatChieu().getMaSuatChieu();
                String maGhe = ve.getGhe().getMaGhe();
                text.append("- Vé ").append(ve.getMaVe()).append(" | Suất ").append(maSuat)
                        .append(" | Ghế ").append(maGhe).append(" | ")
                        .append(String.format("%,.0f VNĐ", dong.getDonGia())).append("\n");
                tong += dong.getDonGia();
            }
            text.append("\nTổng tiền vé: ").append(String.format("%,.0f VNĐ", tong));
        } catch (SQLException ex) {
            text.append("Không tải được chi tiết: ").append(ex.getMessage());
        }
        txtChiTiet.setText(text.toString());
        txtChiTiet.setCaretPosition(0);
    }

    private void huyPhieu() {
        PhieuDatVe phieu = phieuDangChon();
        if (phieu == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hủy phiếu " + phieu.getMaPhieuDatVe() + " và trả các ghế về trạng thái trống?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn == null) throw new SQLException("Không có kết nối tới cơ sở dữ liệu");
            conn.setAutoCommit(false);
            try {
                dao.huyPhieuDat(conn, phieu.getMaPhieuDatVe());
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Không hủy được phiếu; dữ liệu đã rollback. " + ex.getMessage(),
                        "Lỗi hủy phiếu", JOptionPane.ERROR_MESSAGE);
                return;
            }
            taiDanhSach();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể hủy phiếu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xacNhanPhieu() {
        PhieuDatVe phieu = phieuDangChon();
        if (phieu == null) return;
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn == null) throw new SQLException("Không có kết nối tới cơ sở dữ liệu");
            dao.xacNhanPhieu(conn, phieu.getMaPhieuDatVe());
            taiDanhSach();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không xác nhận được phiếu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void thanhToanPhieu() {
        PhieuDatVe phieu = phieuDangChon();
        if (phieu == null) return;
        String maPhieu = phieu.getMaPhieuDatVe();
        double tong;
        try {
            tong = hoaDonDAO.layTongPhieuChoThanhToan(maPhieu);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không đọc được phiếu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (MainFrame.getInstance() == null || MainFrame.getInstance().getTaiKhoan() == null) return;
        JComboBox<String> pay = new JComboBox<>(util.PhuongThucThanhToan.TAT_CA);
        if (JOptionPane.showConfirmDialog(this, pay, "Chọn phương thức thanh toán", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
        HoaDon hd = new HoaDon("HD" + System.currentTimeMillis(),
                MainFrame.getInstance().getTaiKhoan().getNhanVien(), null, null,
                java.time.LocalDateTime.now(), tong, String.valueOf(pay.getSelectedItem()));
        ThuTienDialog.KetQua thu = ThuTienDialog.hienThi(this, hd.getPhongThucThanhToan(), tong, hd.getMaHoaDon());
        if (thu == null) return;
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn == null) throw new SQLException("Không có kết nối tới cơ sở dữ liệu");
            conn.setAutoCommit(false);
            try {
                hoaDonDAO.thanhToanPhieuDat(conn, hd, maPhieu);
                conn.commit();
                JOptionPane.showMessageDialog(this, "Đã thanh toán phiếu và lập hóa đơn " + hd.getMaHoaDon());
                taiDanhSach();
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Thanh toán thất bại; giao dịch đã rollback: " + ex.getMessage()
                        + "\nNếu đã nhận tiền, hãy trả lại khách hoặc thanh toán lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được hóa đơn: " + ex.getMessage()
                    + "\nNếu đã nhận tiền, hãy trả lại khách hoặc thanh toán lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inPhieu() {
        if (phieuDangChon() == null) return;
        try {
            txtChiTiet.print();
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Không in được: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
