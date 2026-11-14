package gui;

import dao.ComboBapNuoc_DAO;
import dao.KhachHang_DAO;
import dao.PhieuDatVe_DAO;
import entity.ComboBapNuoc;
import entity.KhachHang;
import entity.PhieuDatVe;
import service.TaoPhieuDatVeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Màn hình đặt vé tại quầy; ghế và giá được lấy từ database. */
public class PhieuDatVe_GUI extends JFrame {
    private static final Color NEN = new Color(244, 247, 251);
    private static final Color XANH_DAM = new Color(30, 52, 84);
    private static final Color XANH = new Color(42, 105, 173);
    private static final Color CHU = new Color(48, 58, 72);

    private final TaoPhieuDatVeService service;
    private final Connection connection;
    private final PhieuDatVe_DAO phieuDAO;
    private final ComboBapNuoc_DAO comboDAO;
    private final KhachHang_DAO khachHangDAO;
    private final JTextField txtMaKhachHang = new JTextField();
    private final JTextField txtTimPhieu = new JTextField(14);
    private final JComboBox<String> cboSuatChieu = new JComboBox<String>();
    private final JLabel lblGiaVe = new JLabel("Chọn suất chiếu");
    private final JLabel lblThongTinKhachHang = new JLabel("Chưa chọn khách hàng");
    private final JPanel panelSoDoGhe = new JPanel(new GridBagLayout());
    private final Map<String, JToggleButton> nutGhe = new LinkedHashMap<String, JToggleButton>();
    private final JLabel lblGheDaChon = new JLabel("Chưa chọn ghế");
    private final JLabel lblTamTinh = new JLabel("Tạm tính: 0 đồng");
    private final DefaultTableModel phieuModel;
    private final JTable phieuTable;
    private final DefaultTableModel comboModel;
    private final JTable comboTable;
    private final JButton btnHuy = new JButton("Hủy phiếu đã chọn");
    private KhachHang khachHangDaChon;
    private KhachHang khachHangVuaTao;
    private BigDecimal giaVeHienTai = BigDecimal.ZERO;
    private boolean dangTaiSuatChieu;
    private String maPhieuVuaTao;
    private String maSuatChieuVuaTao;
    private List<String> gheVuaTao = new ArrayList<String>();
    private BigDecimal giaVeVuaTao = BigDecimal.ZERO;
    private Map<String, Integer> comboVuaChon = new LinkedHashMap<String, Integer>();
    private Map<String, BigDecimal> giaComboVuaChon = new LinkedHashMap<String, BigDecimal>();

    public PhieuDatVe_GUI(Connection connection) {
        this.connection = connection;
        service = new TaoPhieuDatVeService(connection);
        phieuDAO = new PhieuDatVe_DAO(connection);
        comboDAO = new ComboBapNuoc_DAO(connection);
        khachHangDAO = new KhachHang_DAO(connection);

        setTitle("Tiếp nhận đặt vé tại quầy");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension kichThuocManHinh = Toolkit.getDefaultToolkit().getScreenSize();
        int chieuRong = Math.max(760, Math.min(1120, kichThuocManHinh.width - 40));
        int chieuCao = Math.max(560, Math.min(790, kichThuocManHinh.height - 80));
        setMinimumSize(new Dimension(Math.min(900, chieuRong), Math.min(600, chieuCao)));
        setSize(chieuRong, chieuCao);
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
        JLabel lblMoTa = new JLabel("Nhân viên tiếp nhận khách, chọn suất chiếu, ghế và combo");
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMoTa.setForeground(new Color(220, 230, 242));
        header.add(lblTieuDe, BorderLayout.NORTH);
        header.add(lblMoTa, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel thongTin = new JPanel(new GridLayout(1, 2, 14, 0));
        thongTin.setOpaque(false);

        JPanel pKhachHang = taoThe(new BorderLayout(0, 10));
        JLabel lblMaKhach = taoNhan("Khách hàng");
        txtMaKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMaKhachHang.setForeground(CHU);
        txtMaKhachHang.setEditable(false);
        txtMaKhachHang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 216, 228)),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        txtMaKhachHang.setPreferredSize(new Dimension(120, 36));
        JPanel pNhapKhach = new JPanel(new BorderLayout(0, 6));
        pNhapKhach.setOpaque(false);
        pNhapKhach.add(lblMaKhach, BorderLayout.NORTH);
        JPanel pNutKhach = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pNutKhach.setOpaque(false);
        JButton btnTimKhach = new JButton("Tìm khách");
        JButton btnQuanLyKhach = new JButton("Thêm / sửa khách");
        styleButton(btnTimKhach, XANH);
        styleButton(btnQuanLyKhach, new Color(92, 108, 126));
        pNutKhach.add(txtMaKhachHang);
        pNutKhach.add(btnTimKhach);
        pNutKhach.add(btnQuanLyKhach);
        pNhapKhach.add(pNutKhach, BorderLayout.CENTER);
        lblThongTinKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongTinKhachHang.setForeground(new Color(105, 116, 130));
        pKhachHang.add(pNhapKhach, BorderLayout.NORTH);
        pKhachHang.add(lblThongTinKhachHang, BorderLayout.CENTER);

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

        thongTin.add(pKhachHang);
        thongTin.add(pSuatGia);

        JLabel lblChonGhe = taoNhan("Sơ đồ ghế — bấm ghế trống để chọn");
        panelSoDoGhe.setBackground(Color.WHITE);
        panelSoDoGhe.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollGhe = new JScrollPane(panelSoDoGhe,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollGhe.setBorder(BorderFactory.createLineBorder(new Color(218, 225, 235)));
        scrollGhe.getViewport().setBackground(Color.WHITE);
        JPanel pGhe = taoThe(new BorderLayout(0, 8));
        JPanel pTieuDeGhe = new JPanel(new BorderLayout());
        pTieuDeGhe.setOpaque(false);
        pTieuDeGhe.add(lblChonGhe, BorderLayout.WEST);
        pTieuDeGhe.add(taoChuThichGhe(), BorderLayout.EAST);
        pGhe.add(pTieuDeGhe, BorderLayout.NORTH);
        pGhe.add(scrollGhe, BorderLayout.CENTER);
        JPanel pTongKetGhe = new JPanel(new BorderLayout(8, 0));
        pTongKetGhe.setOpaque(false);
        lblGheDaChon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGheDaChon.setForeground(XANH_DAM);
        lblTamTinh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTamTinh.setForeground(XANH);
        pTongKetGhe.add(lblGheDaChon, BorderLayout.CENTER);
        pTongKetGhe.add(lblTamTinh, BorderLayout.EAST);
        pGhe.add(pTongKetGhe, BorderLayout.SOUTH);

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

        JPanel pPhieu = taoKhuPhieu(new JScrollPane(phieuTable));
        JButton btnQuanLyCombo = new JButton("Quản lý combo");
        styleButton(btnQuanLyCombo, new Color(92, 108, 126));
        JPanel pCombo = taoKhuBang(
                "COMBO BÀN GIAO CHO BƯỚC LẬP HÓA ĐƠN",
                new JScrollPane(comboTable), btnQuanLyCombo);
        JSplitPane chiaBang = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pCombo, pPhieu);
        chiaBang.setResizeWeight(0.52);
        chiaBang.setDividerSize(8);
        chiaBang.setBorder(null);
        chiaBang.setBackground(NEN);

        JSplitPane chiaNoiDung = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pGhe, chiaBang);
        chiaNoiDung.setResizeWeight(0.58);
        chiaNoiDung.setDividerSize(8);
        chiaNoiDung.setBorder(null);
        chiaNoiDung.setBackground(NEN);

        JPanel noiDung = new JPanel(new BorderLayout(0, 14));
        noiDung.setOpaque(false);
        noiDung.add(thongTin, BorderLayout.NORTH);
        noiDung.add(chiaNoiDung, BorderLayout.CENTER);
        root.add(noiDung, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnTao = new JButton("Tạo phiếu đặt");
        JButton btnTaiLai = new JButton("Làm mới dữ liệu");
        styleButton(btnTao, XANH);
        styleButton(btnHuy, new Color(184, 78, 73));
        styleButton(btnTaiLai, new Color(92, 108, 126));
        btnHuy.setEnabled(false);
        actions.add(btnTao);
        actions.add(btnHuy);
        actions.add(btnTaiLai);
        root.add(actions, BorderLayout.SOUTH);

        cboSuatChieu.addActionListener(event -> {
            if (!dangTaiSuatChieu) {
                taiGheVaGia();
            }
        });
        btnTimKhach.addActionListener(event -> timVaChonKhachHang());
        btnQuanLyKhach.addActionListener(event -> new KhachHang_GUI().setVisible(true));
        txtTimPhieu.addActionListener(event -> taiDanhSachPhieu(txtTimPhieu.getText()));
        phieuTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                capNhatTrangThaiNutHuy();
            }
        });
        comboModel.addTableModelListener(event -> capNhatTongTienDuKien());
        btnQuanLyCombo.addActionListener(event -> moManHinhQuanLyCombo());
        btnTao.addActionListener(event -> taoPhieu());
        btnHuy.addActionListener(event -> huyPhieuDangChon());
        btnTaiLai.addActionListener(event -> {
            txtTimPhieu.setText("");
            taiDuLieu();
        });
        taiDuLieu();
        SwingUtilities.invokeLater(() -> {
            chiaNoiDung.setDividerLocation(0.58);
            chiaBang.setDividerLocation(0.52);
        });
    }

    private JPanel taoThe(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 228, 237)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return panel;
    }

    private JPanel taoKhuBang(String tieuDe, JScrollPane noiDung, JButton nutHanhDong) {
        JPanel panel = taoThe(new BorderLayout(0, 9));
        JPanel dauBang = new JPanel(new BorderLayout(8, 0));
        dauBang.setOpaque(false);
        JLabel label = new JLabel(tieuDe);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(XANH_DAM);
        nutHanhDong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nutHanhDong.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        nutHanhDong.setToolTipText("Mở màn hình thêm và cập nhật combo");
        dauBang.add(label, BorderLayout.CENTER);
        dauBang.add(nutHanhDong, BorderLayout.EAST);
        noiDung.setBorder(BorderFactory.createLineBorder(new Color(222, 228, 237)));
        panel.add(dauBang, BorderLayout.NORTH);
        panel.add(noiDung, BorderLayout.CENTER);
        return panel;
    }

    private void moManHinhQuanLyCombo() {
        ComboBapNuoc_GUI cuaSoCombo = new ComboBapNuoc_GUI(connection);
        cuaSoCombo.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                taiDanhSachCombo();
            }
        });
        cuaSoCombo.setLocationRelativeTo(this);
        cuaSoCombo.setVisible(true);
    }

    private JPanel taoKhuPhieu(JScrollPane noiDung) {
        JPanel panel = taoThe(new BorderLayout(0, 8));
        JLabel tieuDe = new JLabel("TRA CỨU PHIẾU ĐẶT");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tieuDe.setForeground(XANH_DAM);
        JPanel thanhTim = new JPanel(new BorderLayout(6, 0));
        thanhTim.setOpaque(false);
        thanhTim.add(new JLabel("Mã phiếu / mã khách:"), BorderLayout.WEST);
        JButton btnTim = new JButton("Tìm");
        styleButton(btnTim, XANH);
        thanhTim.add(txtTimPhieu, BorderLayout.CENTER);
        thanhTim.add(btnTim, BorderLayout.EAST);
        JPanel phanDau = new JPanel(new BorderLayout(0, 7));
        phanDau.setOpaque(false);
        phanDau.add(tieuDe, BorderLayout.NORTH);
        phanDau.add(thanhTim, BorderLayout.CENTER);
        noiDung.setBorder(BorderFactory.createLineBorder(new Color(222, 228, 237)));
        panel.add(phanDau, BorderLayout.NORTH);
        panel.add(noiDung, BorderLayout.CENTER);
        btnTim.addActionListener(event -> taiDanhSachPhieu(txtTimPhieu.getText()));
        return panel;
    }

    private void timVaChonKhachHang() {
        String tuKhoa = JOptionPane.showInputDialog(this,
                "Nhập mã, tên hoặc số điện thoại khách hàng:",
                "Tìm khách hàng", JOptionPane.QUESTION_MESSAGE);
        if (tuKhoa == null) {
            return;
        }
        if (tuKhoa.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập thông tin để tìm khách hàng.");
            return;
        }
        try {
            ArrayList<KhachHang> danhSach = khachHangDAO.timKhachHangChoTiepNhan(tuKhoa);
            if (danhSach.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy khách hàng. Chọn ‘Thêm / sửa khách’ để cập nhật dữ liệu.");
                return;
            }
            String[] luaChon = new String[danhSach.size()];
            for (int i = 0; i < danhSach.size(); i++) {
                KhachHang khachHang = danhSach.get(i);
                luaChon[i] = khachHang.getMaKhachHang() + " — "
                        + khachHang.getTenKhachHang() + " — "
                        + khachHang.getSoDienThoai();
            }
            String daChon = (String) JOptionPane.showInputDialog(this,
                    "Chọn đúng khách hàng:", "Kết quả tìm kiếm",
                    JOptionPane.PLAIN_MESSAGE, null, luaChon, luaChon[0]);
            if (daChon == null) {
                return;
            }
            for (int i = 0; i < luaChon.length; i++) {
                if (luaChon[i].equals(daChon)) {
                    khachHangDaChon = danhSach.get(i);
                    txtMaKhachHang.setText(khachHangDaChon.getMaKhachHang());
                    lblThongTinKhachHang.setText(khachHangDaChon.getTenKhachHang()
                            + "  •  " + khachHangDaChon.getSoDienThoai());
                    return;
                }
            }
        } catch (SQLException ex) {
            baoLoi(ex);
        }
    }

    private void capNhatTrangThaiNutHuy() {
        int hang = phieuTable.getSelectedRow();
        if (hang < 0) {
            btnHuy.setEnabled(false);
            return;
        }
        int dongModel = phieuTable.convertRowIndexToModel(hang);
        String trangThai = phieuModel.getValueAt(dongModel, 3).toString();
        btnHuy.setEnabled("Chờ thanh toán".equals(trangThai));
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
        lblGheDaChon.setText(danhSach.isEmpty() ? "Đã chọn 0 ghế"
                : "Đã chọn " + danhSach.size() + " ghế: " + String.join(", ", danhSach));
        capNhatTongTienDuKien();
    }

    private void capNhatTongTienDuKien() {
        BigDecimal tongTien = giaVeHienTai.multiply(BigDecimal.valueOf(layGheDaChon().size()));
        if (comboModel != null) {
            for (int row = 0; row < comboModel.getRowCount(); row++) {
                if (Boolean.TRUE.equals(comboModel.getValueAt(row, 0))) {
                    try {
                        BigDecimal giaCombo = new BigDecimal(comboModel.getValueAt(row, 3).toString());
                        int soLuong = Integer.parseInt(comboModel.getValueAt(row, 4).toString());
                        if (soLuong > 0) {
                            tongTien = tongTien.add(giaCombo.multiply(BigDecimal.valueOf(soLuong)));
                        }
                    } catch (NumberFormatException | NullPointerException ex) {
                        // Khi ô số lượng đang được sửa, sẽ tính lại sau khi giá trị hợp lệ được nhập.
                    }
                }
            }
        }
        lblTamTinh.setText("Tạm tính: " + tongTien.toPlainString() + " đồng");
    }

    private void taoPhieu() {
        try {
            if (comboTable.isEditing()) {
                comboTable.getCellEditor().stopCellEditing();
            }
            if (khachHangDaChon == null) {
                throw new IllegalArgumentException("Tìm và chọn khách hàng trước khi tạo phiếu.");
            }
            String maKhachHang = khachHangDaChon.getMaKhachHang();
            String maSuatChieu = layMaSuatChieuDangChon();
            List<String> danhSachGhe = layGheDaChon();
            Map<String, Integer> comboChonTam = docComboDuocChon();
            Map<String, BigDecimal> giaComboChonTam = docGiaComboDuocChon(comboChonTam);

            if (maSuatChieu == null) {
                throw new IllegalArgumentException("Chọn suất chiếu trước.");
            }
            if (danhSachGhe.isEmpty()) {
                throw new IllegalArgumentException("Chọn ít nhất một ghế còn trống.");
            }
            String xacNhan = "Khách: " + khachHangDaChon.getTenKhachHang()
                    + " (" + maKhachHang + ")\nGhế: " + String.join(", ", danhSachGhe)
                    + "\n" + lblTamTinh.getText()
                    + "\nTạo phiếu chờ thanh toán và chuyển combo sang bước lập hóa đơn?";
            if (JOptionPane.showConfirmDialog(this, xacNhan, "Xác nhận đặt vé",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            maPhieuVuaTao = service.taoPhieu(maKhachHang, maSuatChieu, danhSachGhe);
            khachHangVuaTao = khachHangDaChon;
            comboVuaChon = comboChonTam;
            giaComboVuaChon = giaComboChonTam;
            maSuatChieuVuaTao = maSuatChieu;
            gheVuaTao = new ArrayList<String>(danhSachGhe);
            giaVeVuaTao = service.getGiaVePhieuVuaTao();
            taiDuLieu();

            String thongBao = "Đã tạo phiếu " + maPhieuVuaTao + ".\nGhế: "
                    + String.join(", ", gheVuaTao)
                    + "\nPhiếu đang chờ thanh toán; dữ liệu combo đã sẵn sàng cho bước lập hóa đơn.";
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

    private Map<String, BigDecimal> docGiaComboDuocChon(Map<String, Integer> comboDuocChon) {
        Map<String, BigDecimal> giaTheoMa = new LinkedHashMap<String, BigDecimal>();
        for (int row = 0; row < comboModel.getRowCount(); row++) {
            String maCombo = comboModel.getValueAt(row, 1).toString();
            if (comboDuocChon.containsKey(maCombo)) {
                giaTheoMa.put(maCombo,
                        new BigDecimal(comboModel.getValueAt(row, 3).toString()));
            }
        }
        return giaTheoMa;
    }

    private void huyPhieuDangChon() {
        int row = phieuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần hủy trong bảng.");
            return;
        }
        String maPhieu = phieuModel.getValueAt(phieuTable.convertRowIndexToModel(row), 0).toString();
        int dongModel = phieuTable.convertRowIndexToModel(row);
        if (!"Chờ thanh toán".equals(phieuModel.getValueAt(dongModel, 3).toString())) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ phiếu đang chờ thanh toán mới được hủy tại bước tiếp nhận.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hủy phiếu " + maPhieu + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.huyPhieu(maPhieu);
            if (maPhieu.equals(maPhieuVuaTao)) {
                maPhieuVuaTao = null;
                khachHangVuaTao = null;
                maSuatChieuVuaTao = null;
                gheVuaTao.clear();
                giaVeVuaTao = BigDecimal.ZERO;
                comboVuaChon.clear();
                giaComboVuaChon.clear();
            }
            taiDanhSachPhieu(txtTimPhieu.getText());
            taiGheVaGia();
            JOptionPane.showMessageDialog(this, "Đã hủy phiếu.");
        } catch (SQLException | IllegalArgumentException ex) {
            baoLoi(ex);
        }
    }

    private void taiDuLieu() {
        taiSuatChieu();
        taiDanhSachPhieu(txtTimPhieu.getText());
        taiDanhSachCombo();
    }

    private void taiSuatChieu() {
        try {
            String maDangChon = layMaSuatChieuDangChon();
            ArrayList<String> danhSach = service.laySuatChieuDangMoBan();
            dangTaiSuatChieu = true;
            cboSuatChieu.removeAllItems();
            for (String moTaSuat : danhSach) {
                cboSuatChieu.addItem(moTaSuat);
            }
            if (maDangChon != null) {
                for (int i = 0; i < cboSuatChieu.getItemCount(); i++) {
                    if (maDangChon.equals(layMaSuatChieu(cboSuatChieu.getItemAt(i)))) {
                        cboSuatChieu.setSelectedIndex(i);
                        break;
                    }
                }
            }
            dangTaiSuatChieu = false;
            taiGheVaGia();
        } catch (SQLException ex) {
            dangTaiSuatChieu = false;
            baoLoi(ex);
        }
    }

    private void taiGheVaGia() {
        String maSuat = layMaSuatChieuDangChon();
        if (maSuat == null) {
            giaVeHienTai = BigDecimal.ZERO;
            lblGiaVe.setText("Chọn suất chiếu");
            hienThiSoDoGhe(new ArrayList<String>(), new ArrayList<String>());
            capNhatTongTienDuKien();
            return;
        }
        try {
            ArrayList<String> tatCaGhe = service.layTatCaGheTheoSuat(maSuat);
            ArrayList<String> gheTrong = service.layGheTrongSuat(maSuat);
            hienThiSoDoGhe(tatCaGhe, gheTrong);
            giaVeHienTai = service.layGiaVeTheoSuat(maSuat);
            lblGiaVe.setText(giaVeHienTai.toPlainString() + " đồng/vé");
            capNhatTongTienDuKien();
        } catch (SQLException ex) {
            giaVeHienTai = BigDecimal.ZERO;
            hienThiSoDoGhe(new ArrayList<String>(), new ArrayList<String>());
            lblGiaVe.setText("Không tải được giá vé");
            capNhatTongTienDuKien();
            baoLoi(ex);
        }
    }

    private String layMaSuatChieuDangChon() {
        Object dangChon = cboSuatChieu.getSelectedItem();
        return dangChon == null ? null : layMaSuatChieu(dangChon.toString());
    }

    private String layMaSuatChieu(String moTaSuat) {
        int viTriPhanCach = moTaSuat.indexOf(" | ");
        return viTriPhanCach < 0 ? moTaSuat : moTaSuat.substring(0, viTriPhanCach).trim();
    }

    private void taiDanhSachPhieu(String tuKhoa) {
        try {
            ArrayList<PhieuDatVe> danhSach = tuKhoa == null || tuKhoa.trim().isEmpty()
                    ? phieuDAO.layTatCa() : phieuDAO.timKiem(tuKhoa);
            phieuModel.setRowCount(0);
            for (PhieuDatVe phieu : danhSach) {
                String maKhachHang = phieu.getKhachHang() == null
                        ? "" : phieu.getKhachHang().getMaKhachHang();
                phieuModel.addRow(new Object[] {phieu.getMaPhieuDatVe(),
                        phieu.getNgayDat(), maKhachHang, phieu.getTrangThai()});
            }
            capNhatTrangThaiNutHuy();
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

    public KhachHang getKhachHangVuaTao() {
        return khachHangVuaTao;
    }

    public String getMaSuatChieuVuaTao() {
        return maSuatChieuVuaTao;
    }

    public List<String> getGheVuaTao() {
        return new ArrayList<String>(gheVuaTao);
    }

    public BigDecimal getGiaVeVuaTao() {
        return giaVeVuaTao;
    }

    /** Trả mã combo và số lượng cho màn lập hóa đơn gọi tiếp. */
    public Map<String, Integer> getComboVuaChon() {
        return new LinkedHashMap<String, Integer>(comboVuaChon);
    }

    public Map<String, BigDecimal> getGiaComboVuaChon() {
        return new LinkedHashMap<String, BigDecimal>(giaComboVuaChon);
    }

    private void baoLoi(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu",
                JOptionPane.ERROR_MESSAGE);
    }
}
