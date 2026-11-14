package gui;

import dao.ComboBapNuoc_DAO;
import dao.PhieuDatVe_DAO;
import entity.ComboBapNuoc;
import entity.PhieuDatVe;
import service.TaoPhieuDatVeService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Màn hình đặt vé đơn giản; ghế và giá lấy từ dữ liệu database. */
public class PhieuDatVe_GUI extends JFrame {
    private final TaoPhieuDatVeService service;
    private final PhieuDatVe_DAO phieuDAO;
    private final ComboBapNuoc_DAO comboDAO;
    private final JTextField txtMaKhachHang = new JTextField();
    private final JComboBox<String> cboSuatChieu = new JComboBox<String>();
    private final JLabel lblGiaVe = new JLabel("Chọn suất chiếu");
    private final DefaultListModel<String> gheModel = new DefaultListModel<String>();
    private final JList<String> listGhe = new JList<String>(gheModel);
    private final DefaultTableModel phieuModel;
    private final JTable phieuTable;
    private final DefaultTableModel comboModel;
    private final JTable comboTable;
    private String maPhieuVuaTao;
    private Map<String, Integer> comboVuaChon = new LinkedHashMap<String, Integer>();

    public PhieuDatVe_GUI(Connection connection) {
        service = new TaoPhieuDatVeService(connection);
        phieuDAO = new PhieuDatVe_DAO(connection);
        comboDAO = new ComboBapNuoc_DAO(connection);

        setTitle("Tiếp nhận đặt vé tại quầy");
        setSize(900, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(2, 4, 8, 8));
        form.add(new JLabel("Mã khách hàng:"));
        form.add(txtMaKhachHang);
        form.add(new JLabel("Suất chiếu đang mở bán:"));
        form.add(cboSuatChieu);
        form.add(new JLabel("Ghế còn trống (chọn nhiều):"));
        listGhe.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        form.add(new JScrollPane(listGhe));
        form.add(new JLabel("Giá vé theo suất:"));
        form.add(lblGiaVe);
        add(form, BorderLayout.NORTH);

        phieuModel = new DefaultTableModel(
                new String[] {"Mã phiếu", "Ngày đặt", "Mã khách hàng", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        phieuTable = new JTable(phieuModel);

        comboModel = new DefaultTableModel(
                new Object[] {"Chọn", "Mã combo", "Tên combo", "Giá", "Số lượng"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 4;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Boolean.class;
                if (column == 4) return Integer.class;
                return String.class;
            }
        };
        comboTable = new JTable(comboModel);
        comboTable.setRowHeight(24);

        JPanel tables = new JPanel(new GridLayout(2, 1, 6, 6));
        JPanel pPhieu = new JPanel(new BorderLayout());
        pPhieu.add(new JLabel("Phiếu đặt gần đây"), BorderLayout.NORTH);
        pPhieu.add(new JScrollPane(phieuTable), BorderLayout.CENTER);
        JPanel pCombo = new JPanel(new BorderLayout());
        pCombo.add(new JLabel("Chọn combo để bàn giao cho bước lập hóa đơn"), BorderLayout.NORTH);
        pCombo.add(new JScrollPane(comboTable), BorderLayout.CENTER);
        tables.add(pPhieu);
        tables.add(pCombo);
        add(tables, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton btnTao = new JButton("Tạo phiếu đặt");
        JButton btnHuy = new JButton("Hủy phiếu đã chọn");
        JButton btnTaiLai = new JButton("Tải lại danh sách");
        buttons.add(btnTao);
        buttons.add(btnHuy);
        buttons.add(btnTaiLai);
        add(buttons, BorderLayout.SOUTH);

        cboSuatChieu.addActionListener(event -> taiGheVaGia());
        btnTao.addActionListener(event -> taoPhieu());
        btnHuy.addActionListener(event -> huyPhieuDangChon());
        btnTaiLai.addActionListener(event -> taiDuLieu());
        taiDuLieu();
    }

    private void taoPhieu() {
        try {
            String maKhachHang = txtMaKhachHang.getText().trim();
            String maSuatChieu = (String) cboSuatChieu.getSelectedItem();
            List<String> danhSachGhe = listGhe.getSelectedValuesList();
            Map<String, Integer> comboChonTam = docComboDuocChon();

            if (maSuatChieu == null) {
                throw new IllegalArgumentException("Chọn suất chiếu trước.");
            }
            maPhieuVuaTao = service.taoPhieu(maKhachHang, maSuatChieu, danhSachGhe);
            comboVuaChon = comboChonTam;
            taiDuLieu();

            String thongBao = "Đã tạo phiếu " + maPhieuVuaTao + ".";
            if (!comboVuaChon.isEmpty()) {
                thongBao += " Đã chọn " + comboVuaChon.size()
                        + " loại combo để bàn giao bước lập hóa đơn.";
            }
            JOptionPane.showMessageDialog(this, thongBao);
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể tạo phiếu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, Integer> docComboDuocChon() {
        Map<String, Integer> danhSach = new LinkedHashMap<String, Integer>();
        for (int row = 0; row < comboModel.getRowCount(); row++) {
            Object daChon = comboModel.getValueAt(row, 0);
            if (Boolean.TRUE.equals(daChon)) {
                String maCombo = comboModel.getValueAt(row, 1).toString();
                int soLuong = Integer.parseInt(comboModel.getValueAt(row, 4).toString());
                if (soLuong <= 0) {
                    throw new IllegalArgumentException("Số lượng combo phải lớn hơn 0.");
                }
                danhSach.put(maCombo, soLuong);
            }
        }
        return danhSach;
    }

    private void huyPhieuDangChon() {
        int row = phieuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần hủy trong bảng.");
            return;
        }
        String maPhieu = phieuModel.getValueAt(phieuTable.convertRowIndexToModel(row), 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Hủy phiếu " + maPhieu + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.huyPhieu(maPhieu);
            if (maPhieu.equals(maPhieuVuaTao)) {
                maPhieuVuaTao = null;
                comboVuaChon.clear();
            }
            taiDanhSachPhieu();
            taiGheVaGia();
            JOptionPane.showMessageDialog(this, "Đã hủy phiếu.");
        } catch (SQLException | IllegalArgumentException ex) {
            baoLoi(ex);
        }
    }

    private void taiDuLieu() {
        taiSuatChieu();
        taiDanhSachPhieu();
        taiDanhSachCombo();
    }

    private void taiSuatChieu() {
        try {
            String dangChon = (String) cboSuatChieu.getSelectedItem();
            ArrayList<String> danhSach = service.laySuatChieuDangMoBan();
            cboSuatChieu.removeAllItems();
            for (String maSuat : danhSach) {
                cboSuatChieu.addItem(maSuat);
            }
            if (dangChon != null) {
                cboSuatChieu.setSelectedItem(dangChon);
            }
            taiGheVaGia();
        } catch (SQLException ex) {
            baoLoi(ex);
        }
    }

    private void taiGheVaGia() {
        gheModel.clear();
        String maSuat = (String) cboSuatChieu.getSelectedItem();
        if (maSuat == null) {
            lblGiaVe.setText("Chọn suất chiếu");
            return;
        }
        try {
            for (String maGhe : service.layGheTrongSuat(maSuat)) {
                gheModel.addElement(maGhe);
            }
            lblGiaVe.setText(service.layGiaVeTheoSuat(maSuat).toPlainString() + " đồng");
        } catch (SQLException ex) {
            baoLoi(ex);
        }
    }

    private void taiDanhSachPhieu() {
        try {
            ArrayList<PhieuDatVe> danhSach = phieuDAO.layTatCa();
            phieuModel.setRowCount(0);
            for (PhieuDatVe phieu : danhSach) {
                String maKhachHang = phieu.getKhachHang() == null
                        ? "" : phieu.getKhachHang().getMaKhachHang();
                phieuModel.addRow(new Object[] {phieu.getMaPhieuDatVe(),
                        phieu.getNgayDat(), maKhachHang, phieu.getTrangThai()});
            }
        } catch (SQLException ex) {
            baoLoi(ex);
        }
    }

    private void taiDanhSachCombo() {
        try {
            ArrayList<ComboBapNuoc> danhSach = comboDAO.layComboDangBan();
            comboModel.setRowCount(0);
            for (ComboBapNuoc combo : danhSach) {
                comboModel.addRow(new Object[] {Boolean.FALSE, combo.getMaCombo(),
                        combo.getTenCombo(), combo.getGiaBan().toString(), 1});
            }
        } catch (SQLException ex) {
            baoLoi(ex);
        }
    }

    public String getMaPhieuVuaTao() {
        return maPhieuVuaTao;
    }

    /** Trả mã combo và số lượng cho màn lập hóa đơn gọi tiếp. */
    public Map<String, Integer> getComboVuaChon() {
        return new LinkedHashMap<String, Integer>(comboVuaChon);
    }

    private void baoLoi(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu",
                JOptionPane.ERROR_MESSAGE);
    }
}
