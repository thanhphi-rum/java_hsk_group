package gui;

import dao.ComboBapNuoc_DAO;
import dao.PhieuDatVe_DAO;
import entity.ComboBapNuoc;
import entity.PhieuDatVe;
import service.TaoPhieuDatVeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Màn hình đặt vé tại quầy; ghế và giá được lấy từ database. */
public class PhieuDatVe_GUI extends JFrame {
    private static final Color NEN = new Color(244, 247, 251);
    private static final Color XANH_DAM = new Color(30, 52, 84);
    private static final Color XANH = new Color(42, 105, 173);
    private static final Color CHU = new Color(48, 58, 72);

    private final TaoPhieuDatVeService service;
    private final PhieuDatVe_DAO phieuDAO;
    private final ComboBapNuoc_DAO comboDAO;
    private final JTextField txtMaKhachHang = new JTextField();
    private final JComboBox<String> cboSuatChieu = new JComboBox<String>();
    private final JLabel lblGiaVe = new JLabel("Chọn suất chiếu");
    private final JPanel panelSoDoGhe = new JPanel(new GridBagLayout());
    private final Map<String, JToggleButton> nutGhe = new LinkedHashMap<String, JToggleButton>();
    private final JLabel lblGheDaChon = new JLabel("Chưa chọn ghế");
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
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 700));
        setSize(1120, 790);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(NEN);
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(XANH_DAM);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel lblTieuDe = new JLabel("TIẾP NHẬN ĐẶT VÉ TẠI QUẦY");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblTieuDe.setForeground(Color.WHITE);
        JLabel lblMoTa = new JLabel("Chọn khách hàng, suất chiếu, ghế và combo");
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMoTa.setForeground(new Color(220, 230, 242));
        header.add(lblTieuDe, BorderLayout.NORTH);
        header.add(lblMoTa, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel thongTin = new JPanel(new GridLayout(1, 2, 14, 0));
        thongTin.setOpaque(false);

        JPanel pKhachGhe = taoThe(new BorderLayout(0, 12));
        JLabel lblMaKhach = taoNhan("Mã khách hàng");
        txtMaKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMaKhachHang.setForeground(CHU);
        txtMaKhachHang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 216, 228)),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        txtMaKhachHang.setPreferredSize(new Dimension(220, 36));
        JPanel pNhapKhach = new JPanel(new BorderLayout(0, 6));
        pNhapKhach.setOpaque(false);
        pNhapKhach.add(lblMaKhach, BorderLayout.NORTH);
        pNhapKhach.add(txtMaKhachHang, BorderLayout.CENTER);

        JLabel lblChonGhe = taoNhan("Sơ đồ ghế — chọn một hoặc nhiều ghế");
        panelSoDoGhe.setBackground(Color.WHITE);
        panelSoDoGhe.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollGhe = new JScrollPane(panelSoDoGhe,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollGhe.setPreferredSize(new Dimension(300, 170));
        scrollGhe.setBorder(BorderFactory.createLineBorder(new Color(218, 225, 235)));
        scrollGhe.getViewport().setBackground(Color.WHITE);
        JPanel pGhe = new JPanel(new BorderLayout(0, 7));
        pGhe.setOpaque(false);
        pGhe.add(lblChonGhe, BorderLayout.NORTH);
        pGhe.add(scrollGhe, BorderLayout.CENTER);
        JPanel pChuThichGhe = new JPanel(new BorderLayout(8, 0));
        pChuThichGhe.setOpaque(false);
        pChuThichGhe.add(taoChuThichGhe(), BorderLayout.WEST);
        lblGheDaChon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGheDaChon.setForeground(XANH_DAM);
        pChuThichGhe.add(lblGheDaChon, BorderLayout.EAST);
        pGhe.add(pChuThichGhe, BorderLayout.SOUTH);
        pKhachGhe.add(pNhapKhach, BorderLayout.NORTH);
        pKhachGhe.add(pGhe, BorderLayout.CENTER);

        JPanel pSuatGia = taoThe(new BorderLayout(0, 12));
        JLabel lblSuat = taoNhan("Suất chiếu đang mở bán");
        cboSuatChieu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboSuatChieu.setBackground(Color.WHITE);
        cboSuatChieu.setForeground(CHU);
        cboSuatChieu.setPreferredSize(new Dimension(260, 36));
        JPanel pChonSuat = new JPanel(new BorderLayout(0, 6));
        pChonSuat.setOpaque(false);
        pChonSuat.add(lblSuat, BorderLayout.NORTH);
        pChonSuat.add(cboSuatChieu, BorderLayout.CENTER);

        JPanel pGia = new JPanel(new BorderLayout(0, 6));
        pGia.setOpaque(false);
        JLabel lblGiaTieuDe = taoNhan("Giá vé theo suất");
        lblGiaVe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGiaVe.setForeground(XANH);
        pGia.add(lblGiaTieuDe, BorderLayout.NORTH);
        pGia.add(lblGiaVe, BorderLayout.CENTER);
        JLabel lblGhiChuSuat = new JLabel("Giá và ghế trống được cập nhật theo suất chiếu");
        lblGhiChuSuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChuSuat.setForeground(new Color(105, 116, 130));
        pGia.add(lblGhiChuSuat, BorderLayout.SOUTH);
        pSuatGia.add(pChonSuat, BorderLayout.NORTH);
        pSuatGia.add(pGia, BorderLayout.CENTER);

        thongTin.add(pKhachGhe);
        thongTin.add(pSuatGia);

        phieuModel = new DefaultTableModel(
                new String[] {"Mã phiếu", "Ngày đặt", "Mã khách hàng", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        phieuTable = new JTable(phieuModel);
        phieuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(phieuTable);

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
        styleTable(comboTable);

        JPanel pPhieu = taoKhuBang("PHIẾU ĐẶT GẦN ĐÂY", new JScrollPane(phieuTable));
        JPanel pCombo = taoKhuBang(
                "CHỌN COMBO ĐỂ BÀN GIAO CHO BƯỚC LẬP HÓA ĐƠN",
                new JScrollPane(comboTable));
        JSplitPane chiaBang = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pPhieu, pCombo);
        chiaBang.setResizeWeight(0.46);
        chiaBang.setDividerSize(8);
        chiaBang.setBorder(null);
        chiaBang.setBackground(NEN);

        JPanel noiDung = new JPanel(new BorderLayout(0, 14));
        noiDung.setOpaque(false);
        noiDung.add(thongTin, BorderLayout.NORTH);
        noiDung.add(chiaBang, BorderLayout.CENTER);
        root.add(noiDung, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnTao = new JButton("Tạo phiếu đặt");
        JButton btnHuy = new JButton("Hủy phiếu đã chọn");
        JButton btnTaiLai = new JButton("Tải lại danh sách");
        styleButton(btnTao, XANH);
        styleButton(btnHuy, new Color(184, 78, 73));
        styleButton(btnTaiLai, new Color(92, 108, 126));
        actions.add(btnTao);
        actions.add(btnHuy);
        actions.add(btnTaiLai);
        root.add(actions, BorderLayout.SOUTH);

        cboSuatChieu.addActionListener(event -> taiGheVaGia());
        btnTao.addActionListener(event -> taoPhieu());
        btnHuy.addActionListener(event -> huyPhieuDangChon());
        btnTaiLai.addActionListener(event -> taiDuLieu());
        taiDuLieu();
    }

    private JPanel taoThe(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 228, 237)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return panel;
    }

    private JPanel taoKhuBang(String tieuDe, JScrollPane noiDung) {
        JPanel panel = taoThe(new BorderLayout(0, 9));
        JLabel label = new JLabel(tieuDe);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(XANH_DAM);
        noiDung.setBorder(BorderFactory.createLineBorder(new Color(222, 228, 237)));
        panel.add(label, BorderLayout.NORTH);
        panel.add(noiDung, BorderLayout.CENTER);
        return panel;
    }

    private JLabel taoNhan(String noiDung) {
        JLabel label = new JLabel(noiDung);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(CHU);
        return label;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(CHU);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(226, 231, 238));
        table.setSelectionBackground(new Color(218, 234, 250));
        table.setSelectionForeground(XANH_DAM);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(232, 239, 248));
        table.getTableHeader().setForeground(XANH_DAM);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel taoChuThichGhe() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.add(taoMucChuThich(Color.WHITE, new Color(115, 140, 168), "Trống"));
        panel.add(taoMucChuThich(XANH, XANH, "Đang chọn"));
        panel.add(taoMucChuThich(new Color(214, 220, 228), new Color(190, 197, 207), "Đã giữ/đặt"));
        return panel;
    }

    private JPanel taoMucChuThich(Color mauNen, Color mauVien, String ten) {
        JPanel muc = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        muc.setOpaque(false);
        JLabel mau = new JLabel("  ");
        mau.setOpaque(true);
        mau.setBackground(mauNen);
        mau.setBorder(BorderFactory.createLineBorder(mauVien));
        JLabel nhan = new JLabel(ten);
        nhan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nhan.setForeground(CHU);
        muc.add(mau);
        muc.add(nhan);
        return muc;
    }

    private void hienThiSoDoGhe(List<String> tatCaGhe, List<String> gheTrong) {
        panelSoDoGhe.removeAll();
        nutGhe.clear();
        if (tatCaGhe == null || tatCaGhe.isEmpty()) {
            JLabel rong = new JLabel("Chưa có ghế cho suất chiếu này", SwingConstants.CENTER);
            rong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            rong.setForeground(new Color(105, 116, 130));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            panelSoDoGhe.add(rong, c);
            lblGheDaChon.setText("Chưa chọn ghế");
            panelSoDoGhe.revalidate();
            panelSoDoGhe.repaint();
            return;
        }

        Set<String> gheTrongSet = new HashSet<String>();
        if (gheTrong != null) {
            gheTrongSet.addAll(gheTrong);
        }
        Map<String, List<String>> gheTheoHang = new TreeMap<String, List<String>>();
        for (String maGhe : tatCaGhe) {
            String tenHang = layHangGhe(maGhe);
            if (!gheTheoHang.containsKey(tenHang)) {
                gheTheoHang.put(tenHang, new ArrayList<String>());
            }
            gheTheoHang.get(tenHang).add(maGhe);
        }

        int soCotLonNhat = 1;
        for (List<String> gheTrongHang : gheTheoHang.values()) {
            Collections.sort(gheTrongHang, new Comparator<String>() {
                @Override
                public int compare(String ghe1, String ghe2) {
                    int soSanh = Integer.compare(laySoThuTuGhe(ghe1), laySoThuTuGhe(ghe2));
                    return soSanh == 0 ? ghe1.compareToIgnoreCase(ghe2) : soSanh;
                }
            });
            soCotLonNhat = Math.max(soCotLonNhat,
                    gheTrongHang.size() + (gheTrongHang.size() >= 8 ? 1 : 0));
        }

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = soCotLonNhat + 1;
        c.weightx = 1;
        JPanel manHinh = new JPanel(new BorderLayout());
        manHinh.setBackground(new Color(232, 239, 248));
        manHinh.setBorder(BorderFactory.createLineBorder(new Color(198, 211, 228)));
        JLabel nhanManHinh = new JLabel("MÀN HÌNH", SwingConstants.CENTER);
        nhanManHinh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nhanManHinh.setForeground(new Color(94, 111, 133));
        manHinh.add(nhanManHinh, BorderLayout.CENTER);
        manHinh.setPreferredSize(new Dimension(Math.max(250, soCotLonNhat * 52), 24));
        panelSoDoGhe.add(manHinh, c);

        int hangHienTai = 1;
        for (Map.Entry<String, List<String>> mucHang : gheTheoHang.entrySet()) {
            JLabel nhanHang = new JLabel(mucHang.getKey(), SwingConstants.CENTER);
            nhanHang.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nhanHang.setForeground(new Color(105, 116, 130));
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = hangHienTai;
            c.insets = new Insets(4, 4, 4, 8);
            panelSoDoGhe.add(nhanHang, c);

            List<String> gheTrongHang = mucHang.getValue();
            int cot = 1;
            for (int i = 0; i < gheTrongHang.size(); i++) {
                if (gheTrongHang.size() >= 8 && i == gheTrongHang.size() / 2) {
                    cot++;
                }
                String maGhe = gheTrongHang.get(i);
                JToggleButton nut = taoNutGhe(maGhe, gheTrongSet.contains(maGhe));
                c = new GridBagConstraints();
                c.gridx = cot++;
                c.gridy = hangHienTai;
                c.insets = new Insets(4, 4, 4, 4);
                c.anchor = GridBagConstraints.CENTER;
                panelSoDoGhe.add(nut, c);
            }
            hangHienTai++;
        }
        capNhatGheDaChon();
        panelSoDoGhe.revalidate();
        panelSoDoGhe.repaint();
    }

    private JToggleButton taoNutGhe(String maGhe, boolean gheTrong) {
        JToggleButton nut = new JToggleButton(maGhe);
        nut.setPreferredSize(new Dimension(46, 34));
        nut.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nut.setFocusPainted(false);
        nut.setOpaque(true);
        nut.setContentAreaFilled(true);
        nut.setToolTipText(gheTrong ? "Ghế trống" : "Ghế đã được giữ hoặc đặt");
        if (gheTrong) {
            nut.addActionListener(event -> {
                capNhatMauNutGhe(nut, true);
                capNhatGheDaChon();
            });
        } else {
            nut.setEnabled(false);
        }
        capNhatMauNutGhe(nut, gheTrong);
        nutGhe.put(maGhe, nut);
        return nut;
    }

    private void capNhatMauNutGhe(JToggleButton nut, boolean gheTrong) {
        if (!gheTrong) {
            nut.setBackground(new Color(214, 220, 228));
            nut.setForeground(new Color(112, 121, 133));
            nut.setBorder(BorderFactory.createLineBorder(new Color(190, 197, 207), 1, true));
        } else if (nut.isSelected()) {
            nut.setBackground(XANH);
            nut.setForeground(Color.WHITE);
            nut.setBorder(BorderFactory.createLineBorder(XANH, 1, true));
        } else {
            nut.setBackground(Color.WHITE);
            nut.setForeground(XANH_DAM);
            nut.setBorder(BorderFactory.createLineBorder(new Color(115, 140, 168), 1, true));
        }
    }

    private String layHangGhe(String maGhe) {
        int viTriSo = 0;
        while (viTriSo < maGhe.length() && !Character.isDigit(maGhe.charAt(viTriSo))) {
            viTriSo++;
        }
        return viTriSo == 0 ? "Khác" : maGhe.substring(0, viTriSo).toUpperCase();
    }

    private int laySoThuTuGhe(String maGhe) {
        int viTriSo = 0;
        while (viTriSo < maGhe.length() && !Character.isDigit(maGhe.charAt(viTriSo))) {
            viTriSo++;
        }
        try {
            return Integer.parseInt(maGhe.substring(viTriSo));
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private List<String> layGheDaChon() {
        List<String> danhSach = new ArrayList<String>();
        for (Map.Entry<String, JToggleButton> muc : nutGhe.entrySet()) {
            if (muc.getValue().isSelected()) {
                danhSach.add(muc.getKey());
            }
        }
        return danhSach;
    }

    private void capNhatGheDaChon() {
        List<String> danhSach = layGheDaChon();
        lblGheDaChon.setText(danhSach.isEmpty()
                ? "Chưa chọn ghế" : "Đang chọn: " + String.join(", ", danhSach));
    }
    private void taoPhieu() {
        try {
            String maKhachHang = txtMaKhachHang.getText().trim();
            String maSuatChieu = (String) cboSuatChieu.getSelectedItem();
            List<String> danhSachGhe = layGheDaChon();
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
        String maSuat = (String) cboSuatChieu.getSelectedItem();
        if (maSuat == null) {
            lblGiaVe.setText("Chọn suất chiếu");
            hienThiSoDoGhe(new ArrayList<String>(), new ArrayList<String>());
            return;
        }
        try {
            ArrayList<String> tatCaGhe = service.layTatCaGheTheoSuat(maSuat);
            ArrayList<String> gheTrong = service.layGheTrongSuat(maSuat);
            hienThiSoDoGhe(tatCaGhe, gheTrong);
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