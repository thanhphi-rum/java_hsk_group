package gui;
	
import dao.HoaDonDAO;
import entity.HoaDon;

import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import javax.swing.table.DefaultTableModel;

import connect.ConnectDB;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class HoaDon_GUI extends JPanel {
    private JTable tableHoaDon;
    private DefaultTableModel tableModel;
    private JTextField tfTimKiem;
    private HoaDonDAO hoaDonDAO;
    private JButton btnTimKiem, btnXoa, btnLamMoi;
    private final JDateChooser dcTu = new JDateChooser(), dcDen = new JDateChooser();

    public HoaDon_GUI() {
        hoaDonDAO = new HoaDonDAO();

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());


        // ====== Table ======
        String[] columnNames = {"Mã Hóa Đơn", "Ngày Lập", "Nhân Viên", "Khách Hàng", "Khuyến Mãi", "Tổng Tiền", "Phương Thức", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0);
        tableHoaDon = new JTable(tableModel);
        tableHoaDon.setRowHeight(30);
        tableHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ====== Control Panel ======
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        tfTimKiem = new HintTextField("Tìm mã hóa đơn, khách hàng, SĐT...");
        tfTimKiem.setPreferredSize(new Dimension(310, 30));
        controlPanel.add(new JLabel("Tra cứu:"));
        controlPanel.add(tfTimKiem);

        btnTimKiem = new JButton("Tìm Kiếm");
        btnTimKiem.addActionListener(e -> timKiemHoaDon());
        controlPanel.add(btnTimKiem);
        JButton btnPhieuDat = new JButton("Thanh toán phiếu đặt");
        btnPhieuDat.addActionListener(e -> thanhToanPhieuDat());
        controlPanel.add(btnPhieuDat);

        btnXoa = new JButton("Ghi nhận hoàn tiền");
        btnXoa.addActionListener(e -> xoaHoaDon());
        controlPanel.add(btnXoa);
        JButton btnDetail=new JButton("Chi tiết"); btnDetail.addActionListener(e->xemChiTiet(true)); controlPanel.add(btnDetail);
        JButton btnInLai=new JButton("In lại"); btnInLai.addActionListener(e->xemChiTiet(false)); controlPanel.add(btnInLai);
        btnXoa.setVisible(MainFrame.laQuanLy()); // nhân viên bán vé không được xóa hóa đơn
        
        btnLamMoi = new JButton("Làm Mới");
        btnLamMoi.addActionListener(e -> lamMoiDanhSach());
        controlPanel.add(btnLamMoi);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        AppTheme.styleTable(tableHoaDon);
        tableHoaDon.getColumnModel().getColumn(7).setCellRenderer(new AppTheme.PillRenderer());
        dcTu.setDateFormatString("dd/MM/yyyy"); dcDen.setDateFormatString("dd/MM/yyyy");
        dcTu.setPreferredSize(new Dimension(130, 36)); dcDen.setPreferredSize(new Dimension(130, 36));
        JPanel ngay = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); ngay.setOpaque(false);
        ngay.add(AppTheme.label("Từ ngày:", Font.PLAIN, 13, AppTheme.MUTED)); ngay.add(dcTu);
        ngay.add(AppTheme.label("Đến ngày:", Font.PLAIN, 13, AppTheme.MUTED)); ngay.add(dcDen);
        Khung.dung(this, "Hóa Đơn", controlPanel, scrollPane, chiTietHoaDon(), null, ngay);
        lamMoiDanhSach();
    }

    private void thanhToanPhieuDat() {
        String maPhieu=JOptionPane.showInputDialog(this,"Nhập mã phiếu đặt đã được xác nhận:","Thanh toán phiếu đặt",JOptionPane.QUESTION_MESSAGE);
        if(maPhieu==null || maPhieu.isBlank()) return;
        JComboBox<String> pay=new JComboBox<>(new String[]{"Tiền mặt","Thẻ ngân hàng","Chuyển khoản"});
        if(JOptionPane.showConfirmDialog(this,pay,"Xác nhận phương thức thanh toán",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        if(MainFrame.getInstance()==null || MainFrame.getInstance().getTaiKhoan()==null)return;
        HoaDon invoice=new HoaDon("HD"+System.currentTimeMillis(),MainFrame.getInstance().getTaiKhoan().getNhanVien(),null,null,java.time.LocalDateTime.now(),0,String.valueOf(pay.getSelectedItem()));
        try(Connection conn=ConnectDB.getConnection()) { conn.setAutoCommit(false); try { hoaDonDAO.thanhToanPhieuDat(conn,invoice,maPhieu.trim()); conn.commit(); JOptionPane.showMessageDialog(this,"Đã thanh toán phiếu đặt và lập hóa đơn "+invoice.getMaHoaDon()); lamMoiDanhSach(); } catch(SQLException ex) { conn.rollback(); JOptionPane.showMessageDialog(this,"Thanh toán thất bại; giao dịch đã rollback: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE); } } catch(SQLException ex) { JOptionPane.showMessageDialog(this,"Không kết nối được cơ sở dữ liệu: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE); }
    }
    /** Chỉ giữ hóa đơn nằm trong khoảng ngày đã chọn (bỏ trống = không giới hạn). */
    private List<HoaDon> locTheoNgay(List<HoaDon> ds) {
        java.time.LocalDate tu = dcTu.getDate() == null ? null : dcTu.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.LocalDate den = dcDen.getDate() == null ? null : dcDen.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        List<HoaDon> out = new ArrayList<>();
        for (HoaDon h : ds) {
            if (h.getNgayLap() == null) { out.add(h); continue; }
            java.time.LocalDate d = h.getNgayLap().toLocalDate();
            if ((tu == null || !d.isBefore(tu)) && (den == null || !d.isAfter(den))) out.add(h);
        }
        return out;
    }

    /** Thẻ chi tiết hóa đơn bên phải: bảng dịch vụ + tổng tiền, khuyến mãi, thành tiền. */
    private JPanel chiTietHoaDon() {
        AppTheme.Card card = new AppTheme.Card(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(380, 0));
        card.add(AppTheme.label("Chi tiết hóa đơn", Font.BOLD, 16, Color.WHITE), BorderLayout.NORTH);
        DefaultTableModel ctModel = new DefaultTableModel(new String[]{"Tên dịch vụ", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable ct = new JTable(ctModel); AppTheme.styleTable(ct); ct.setRowHeight(32);
        ct.getColumnModel().getColumn(1).setMaxWidth(40);
        JLabel lTong = AppTheme.label(" ", Font.BOLD, 14, Color.WHITE), lKm = AppTheme.label(" ", Font.BOLD, 14, Color.WHITE),
               lThanh = AppTheme.label(" ", Font.BOLD, 20, new Color(0xFF5A6E));
        JPanel tong = new JPanel(new GridLayout(3, 2, 0, 6)); tong.setOpaque(false);
        tong.add(AppTheme.label("Tổng tiền:", Font.PLAIN, 13, AppTheme.MUTED)); tong.add(lTong);
        tong.add(AppTheme.label("Khuyến mãi:", Font.PLAIN, 13, AppTheme.MUTED)); tong.add(lKm);
        tong.add(AppTheme.label("Thành tiền:", Font.BOLD, 14, Color.WHITE)); tong.add(lThanh);
        for (JLabel l : new JLabel[]{lTong, lKm, lThanh}) l.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(AppTheme.scroll(ct), BorderLayout.CENTER);
        card.add(tong, BorderLayout.SOUTH);
        Runnable capNhat = () -> {
            ctModel.setRowCount(0);
            int r = tableHoaDon.getSelectedRow();
            if (r < 0) { lTong.setText(" "); lKm.setText(" "); lThanh.setText(" "); return; }
            r = tableHoaDon.convertRowIndexToModel(r);
            String id = tableModel.getValueAt(r, 0).toString();
            double sum = 0;
            for (Object[] row : hoaDonDAO.layChiTietBang(id)) {
                ctModel.addRow(new Object[]{row[0], row[1], String.format("%,.0fđ", (Double) row[2]), String.format("%,.0fđ", (Double) row[3])});
                sum += (Double) row[3];
            }
            String t = tableModel.getValueAt(r, 5).toString().replaceAll("[^0-9]", "");
            double thanh = t.isEmpty() ? 0 : Double.parseDouble(t);
            lTong.setText(String.format("%,.0fđ", sum));
            lKm.setText(String.format("-%,.0fđ", Math.max(0, sum - thanh)));
            lThanh.setText(String.format("%,.0fđ", thanh));
        };
        tableHoaDon.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) capNhat.run(); });
        tableModel.addTableModelListener(e -> SwingUtilities.invokeLater(() -> {
            if (tableHoaDon.getRowCount() > 0 && tableHoaDon.getSelectedRow() < 0) tableHoaDon.setRowSelectionInterval(0, 0); else capNhat.run();
        }));
        return card;
    }

    private void timKiemHoaDon() {
        String keyword = tfTimKiem.getText().trim();
        List<HoaDon> danhSachHoaDon = locTheoNgay(hoaDonDAO.timKiemHoaDon(keyword));
        capNhatBangHoaDon(danhSachHoaDon);
    }

    private void xoaHoaDon() {
        int row=tableHoaDon.getSelectedRow();if(row<0)return;row=tableHoaDon.convertRowIndexToModel(row);
        String id=tableModel.getValueAt(row,0).toString(),status=tableModel.getValueAt(row,7).toString();
        if(!"Đã thanh toán".equals(status)){JOptionPane.showMessageDialog(this,"Hóa đơn này đã được xử lý.");return;}
        JTextField reason=new JTextField();Object[] form={"Chỉ bấm tiếp sau khi đã hoàn tiền qua phương thức thanh toán ban đầu.","Lý do:",reason};
        if(JOptionPane.showConfirmDialog(this,form,"Ghi nhận hủy vé và hoàn tiền",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.OK_OPTION)return;
        if(reason.getText().isBlank()){JOptionPane.showMessageDialog(this,"Cần nhập lý do hủy.");return;}
        try(Connection c=ConnectDB.getConnection()){c.setAutoCommit(false);try{hoaDonDAO.hoanTienVaHuy(c,id,reason.getText().trim());c.commit();JOptionPane.showMessageDialog(this,"Đã lưu trạng thái hoàn tiền, hủy vé và giải phóng ghế. Hóa đơn được giữ lại để đối soát.");}catch(SQLException ex){c.rollback();JOptionPane.showMessageDialog(this,"Không xử lý được: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}}catch(SQLException ex){JOptionPane.showMessageDialog(this,"Lỗi kết nối: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}lamMoiDanhSach();
    }
    private void xemChiTiet(boolean show){
        int row=tableHoaDon.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Chọn một hóa đơn trước.");return;}row=tableHoaDon.convertRowIndexToModel(row);String id=tableModel.getValueAt(row,0).toString();
        StringBuilder text=new StringBuilder("HÓA ĐƠN ").append(id).append("\nTrạng thái: ").append(tableModel.getValueAt(row,7)).append("\nTổng: ").append(tableModel.getValueAt(row,5)).append("\nPhương thức: ").append(tableModel.getValueAt(row,6)).append("\n\nCHI TIẾT\n");for(String line:hoaDonDAO.layChiTietHienThi(id))text.append("• ").append(line).append("\n");
        JTextArea area=new JTextArea(text.toString(),18,58);area.setEditable(false);if(show)JOptionPane.showMessageDialog(this,new JScrollPane(area),"Chi tiết hóa đơn",JOptionPane.INFORMATION_MESSAGE);else try{area.print();}catch(java.awt.print.PrinterException ex){JOptionPane.showMessageDialog(this,"Không in được: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
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
                    hoaDon.getPhongThucThanhToan(),
                    hoaDon.getTrangThai()
            });
        }
    }

}








