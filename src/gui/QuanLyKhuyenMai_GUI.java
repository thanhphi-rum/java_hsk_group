package gui;

import com.toedter.calendar.JDateChooser;
import dao.KhuyenMaiDAO;
import entity.KhuyenMai;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Quản lý khuyến mãi: hình thức (giảm giá vé / giảm giá combo / giảm hóa đơn / tặng combo), mức ưu đãi, thời gian áp dụng. */
public class QuanLyKhuyenMai_GUI extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final KhuyenMaiDAO dao = new KhuyenMaiDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã KM", "Tên chương trình", "Hình thức", "Mức ưu đãi", "Bắt đầu", "Kết thúc", "Hiệu lực", "Mô tả"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public QuanLyKhuyenMai_GUI() {
        setLayout(new BorderLayout(8, 8));
        JButton add = new JButton("Thêm"), edit = new JButton("Sửa"), del = new JButton("Xóa"), refresh = new JButton("Làm mới");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(add); actions.add(edit); actions.add(del); actions.add(refresh);
        AppTheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(6).setCellRenderer(new AppTheme.PillRenderer());
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(7)); // mô tả chỉ hiện ở thẻ chi tiết
        String[] nhan = {"Mã KM:", "Tên CT:", "Hình thức:", "Mức ưu đãi:", "Từ ngày:", "Đến ngày:", "Hiệu lực:", "Mô tả:"};
        JPanel phai = Khung.chiTiet(table, model, null, nhan, r -> {
            String[] v = new String[8];
            for (int i = 0; i < 8; i++) v[i] = String.valueOf(model.getValueAt(r, i));
            return v;
        });
        Khung.dung(this, "Quản Lý Khuyến Mãi", actions, new JScrollPane(table), phai);
        add.addActionListener(e -> sua(null));
        edit.addActionListener(e -> { int r = table.getSelectedRow(); if (r >= 0) sua((String) model.getValueAt(table.convertRowIndexToModel(r), 0)); });
        del.addActionListener(e -> xoa());
        refresh.addActionListener(e -> load());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) edit.doClick(); }
        });
        load();
    }

    private void load() {
        model.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (KhuyenMai k : dao.layTatCaKhuyenMai()) {
            String state = !"Hoạt động".equalsIgnoreCase(k.getTrangThai()) ? "Ngừng hoạt động"
                    : today.isBefore(k.getNgayBatDau()) ? "Chưa bắt đầu" : today.isAfter(k.getNgayKetThuc()) ? "Hết hạn" : "Hợp lệ";
            model.addRow(new Object[]{k.getMaKhuyenMai(), k.getTenKhuyenMai(), k.getHinhThuc(), k.mucUuDai(),
                    k.getNgayBatDau().format(NGAY), k.getNgayKetThuc().format(NGAY), state, k.getMoTa() == null ? "" : k.getMoTa()});
        }
    }

    private static LocalDate toLocal(java.util.Date d) { return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); }

    private void sua(String id) {
        KhuyenMai old = null;
        if (id != null) for (KhuyenMai k : dao.layTatCaKhuyenMai()) if (k.getMaKhuyenMai().equals(id)) old = k;
        JTextField ma = new JTextField(old == null ? "" : old.getMaKhuyenMai()), ten = new JTextField(old == null ? "" : old.getTenKhuyenMai()),
                muc = new JTextField(old == null ? "" : (old.laTangCombo() ? String.valueOf(old.getSoComboTang()) : new java.math.BigDecimal(String.valueOf(old.getPhanTramGiam())).stripTrailingZeros().toPlainString())),
                mota = new JTextField(old == null ? "" : old.getMoTa());
        JComboBox<String> hinhThuc = new JComboBox<>(KhuyenMai.DS_HINH_THUC), status = new JComboBox<>(new String[]{"Hoạt động", "Ngừng hoạt động"});
        if (old != null) { hinhThuc.setSelectedItem(old.getHinhThuc()); status.setSelectedItem(old.getTrangThai()); }
        JDateChooser bd = new JDateChooser(java.util.Date.from(( old == null ? LocalDate.now() : old.getNgayBatDau()).atStartOfDay(ZoneId.systemDefault()).toInstant()), "dd/MM/yyyy");
        JDateChooser kt = new JDateChooser(java.util.Date.from(( old == null ? LocalDate.now().plusDays(30) : old.getNgayKetThuc()).atStartOfDay(ZoneId.systemDefault()).toInstant()), "dd/MM/yyyy");
        JLabel nhanMuc = new JLabel();
        Runnable doiNhan = () -> nhanMuc.setText(KhuyenMai.HT_TANG_COMBO.equals(hinhThuc.getSelectedItem()) ? "Số combo tặng:" : "Phần trăm giảm (%):");
        hinhThuc.addActionListener(e -> doiNhan.run()); doiNhan.run();
        ma.setEditable(old == null);
        Object[] f = {"Mã:", ma, "Tên chương trình:", ten, "Hình thức:", hinhThuc, nhanMuc, muc, "Bắt đầu:", bd, "Kết thúc:", kt, "Trạng thái:", status, "Mô tả:", mota};
        if (JOptionPane.showConfirmDialog(this, f, old == null ? "Thêm khuyến mãi" : "Sửa khuyến mãi", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            boolean tang = KhuyenMai.HT_TANG_COMBO.equals(hinhThuc.getSelectedItem());
            LocalDate a = toLocal(bd.getDate()), b = toLocal(kt.getDate());
            if (ma.getText().isBlank() || ten.getText().isBlank()) throw new IllegalArgumentException("Nhập mã và tên chương trình.");
            if (a == null || b == null || b.isBefore(a)) throw new IllegalArgumentException("Ngày bắt đầu/kết thúc không hợp lệ.");
            double p = 0; int soTang = 0;
            if (tang) { soTang = Integer.parseInt(muc.getText().trim()); if (soTang <= 0) throw new IllegalArgumentException("Số combo tặng phải lớn hơn 0."); }
            else { p = Double.parseDouble(muc.getText().trim()); if (!Double.isFinite(p) || p <= 0 || p > 100) throw new IllegalArgumentException("Phần trăm giảm phải trong khoảng 1–100."); }
            KhuyenMai k = new KhuyenMai(ma.getText().trim(), ten.getText().trim(), mota.getText().trim(), p, a, b);
            k.setTrangThai((String) status.getSelectedItem()); k.setHinhThuc((String) hinhThuc.getSelectedItem()); k.setSoComboTang(soTang);
            if (old == null && dao.kiemTraMaKhuyenMai(k.getMaKhuyenMai())) throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại.");
            boolean ok = old == null ? dao.themKhuyenMai(k) : dao.suaKhuyenMai(k);
            if (!ok) throw new IllegalStateException("Không lưu được khuyến mãi.");
            load();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mức ưu đãi phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoa() {
        int r = table.getSelectedRow(); if (r < 0) return;
        String id = (String) model.getValueAt(table.convertRowIndexToModel(r), 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa khuyến mãi " + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (!dao.xoaKhuyenMai(id)) JOptionPane.showMessageDialog(this, "Không xóa được khuyến mãi đã dùng trong hóa đơn; hãy chuyển sang Ngừng hoạt động.");
        load();
    }
}
