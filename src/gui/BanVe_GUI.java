package gui;

import connect.ConnectDB;
import dao.GheDAO;
import dao.KhuyenMaiDAO;
import dao.HoaDonDAO;
import dao.ChiTietHoaDonDAO;
import dao.ComboBapNuocDAO;
import dao.ThanhToanBanVeDAO;
import dao.VeDAO;
import dao.PhieuDatVeDAO;
import entity.Ghe;
import entity.GheSuatChieu;
import entity.KhuyenMai;
import entity.TaiKhoan;
import entity.ComboBapNuoc;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.InVeHoaDon;
import util.GiaVe;
import java.time.LocalDateTime;
import util.PhuongThucThanhToan;

// Màn bán vé: cột trái chọn phim/ngày/phòng/suất, giữa là sơ đồ ghế, phải là "Thông tin đặt vé" và nút Thanh toán
public class BanVe_GUI extends JPanel {
    private final JComboBox<String> cbPhim = new JComboBox<>(), cbNgay = new JComboBox<>(), cbPhongChieu = new JComboBox<>(),
            cbSuatChieu = new JComboBox<>(), cbNhanVien = new JComboBox<>(), cbKhachHang = new JComboBox<>(), cbKhuyenMai = new JComboBox<>(),
            cbPhuongThuc = new JComboBox<>(PhuongThucThanhToan.TAT_CA);
    private JPanel pnlSoDoGhe;
    private JButton btnThanhToan, btnLamMoi, btnCombo, btnInLai, btnTaoPhieu;
    private final JLabel lblPoster = new JLabel("", SwingConstants.CENTER), lblTenPhim = AppTheme.label(" ", java.awt.Font.BOLD, 17, Color.WHITE),
            lblThoiLuong = AppTheme.label(" ", java.awt.Font.PLAIN, 13, new Color(0xD7DEF2)), lblTheLoai = AppTheme.label(" ", java.awt.Font.PLAIN, 13, new Color(0xD7DEF2));
    private final JLabel lblGhe = giaTri(), lblSoLuong = giaTri(), lblDonGia = giaTri(), lblTongVe = giaTri(), lblThanhTien = AppTheme.label("0đ", java.awt.Font.BOLD, 26, new Color(0xFF5A6E));
    private String maHoaDonVuaThanhToan = null, noiDungHoaDon = ""; // dữ liệu giao dịch thanh toán gần nhất, dùng để in vé/hóa đơn
    private final List<String[]> veVuaThanhToan = new ArrayList<>();
    private double tongTien = 0.0;
    private double giaCoBan = 100000;
    private static final Color GHE_TRONG = new Color(0x3B82F6);
    private static final Color GHE_DANG_CHON = new Color(0xE0384F);
    private static final Color GHE_DA_DAT = new Color(0x8B5CF6);
    private static final Color GHE_DA_BAN = new Color(0x6B7A9E);
    private static final Color GHE_VIP = new Color(0xE0A93B);

    private final KhuyenMaiDAO khuyenMaiDAO = new KhuyenMaiDAO();
    private final GheDAO gheDAO = new GheDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final ChiTietHoaDonDAO chiTietHoaDonDAO = new ChiTietHoaDonDAO();
    private final VeDAO veDAO = new VeDAO();
    private final ComboBapNuocDAO comboDAO = new ComboBapNuocDAO();
    private final ThanhToanBanVeDAO thanhToanDAO = new ThanhToanBanVeDAO();
    private final PhieuDatVeDAO phieuDatVeDAO = new PhieuDatVeDAO();
    private final Map<String, Integer> comboSoLuong = new LinkedHashMap<>();
    private final Map<String, ComboBapNuoc> comboDuLieu = new LinkedHashMap<>();
    private final Map<String, KhuyenMai> kmDuLieu = new LinkedHashMap<>();
    private final JLabel lblGiam = giaTri();

    private static JLabel giaTri() {
        JLabel l = AppTheme.label("—", java.awt.Font.BOLD, 14, Color.WHITE);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    // Combo hiển thị gọn: chuyển chuỗi lưu trong combo thành chuỗi dễ đọc
    private static void hienThi(JComboBox<String> cb, java.util.function.Function<String, String> f) {
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean fo) {
                return super.getListCellRendererComponent(l, v == null ? null : f.apply(v.toString()), i, s, fo);
            }
        });
    }

    public BanVe_GUI(String maPhim) {
        setLayout(new BorderLayout(0, 10)); setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(AppTheme.label("Bán Vé", java.awt.Font.BOLD, 26, Color.WHITE), BorderLayout.NORTH);

        hienThi(cbNgay, s -> { try { return LocalDate.parse(s).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")); } catch (Exception e) { return s; } });
        hienThi(cbPhongChieu, s -> "Phòng " + s);
        hienThi(cbSuatChieu, s -> { String[] p = s.split(" - "); return p.length >= 3 ? p[1].substring(11, 16) + " – " + p[2].substring(11, 16) + "  (" + p[0] + ")" : s; });
        hienThi(cbKhuyenMai, s -> { KhuyenMai k = kmDuLieu.get(s.split(" - ")[0]); return k == null ? s : k.getTenKhuyenMai() + " (" + k.mucUuDai() + ")"; });

        // Cột trái: poster + thông tin phim + ngày / phòng / suất 
        lblPoster.setPreferredSize(new Dimension(120, 172));
        JPanel tenBox = new JPanel(); tenBox.setOpaque(false); tenBox.setLayout(new BoxLayout(tenBox, BoxLayout.Y_AXIS));
        tenBox.add(Box.createVerticalGlue()); tenBox.add(lblTenPhim); tenBox.add(Box.createVerticalStrut(8));
        tenBox.add(lblThoiLuong); tenBox.add(Box.createVerticalStrut(2)); tenBox.add(lblTheLoai); tenBox.add(Box.createVerticalGlue());
        JPanel dauPhim = new JPanel(new BorderLayout(12, 0)); dauPhim.setOpaque(false);
        dauPhim.add(lblPoster, BorderLayout.WEST); dauPhim.add(tenBox, BorderLayout.CENTER);
        JPanel formTrai = new JPanel(new GridBagLayout()); formTrai.setOpaque(false);
        themDong(formTrai, 0, "Ngày chiếu", cbNgay); themDong(formTrai, 1, "Phòng chiếu", cbPhongChieu); themDong(formTrai, 2, "Suất chiếu", cbSuatChieu);
        for (JComboBox<String> c : java.util.List.of(cbNgay, cbPhongChieu, cbSuatChieu)) c.setPreferredSize(new Dimension(150, 34));
        JPanel formWrap = new JPanel(new BorderLayout()); formWrap.setOpaque(false); formWrap.add(formTrai, BorderLayout.NORTH);
        AppTheme.Card theTrai = new AppTheme.Card(new BorderLayout(0, 18));
        theTrai.setPreferredSize(new Dimension(310, 0));
        theTrai.add(dauPhim, BorderLayout.NORTH); theTrai.add(formWrap, BorderLayout.CENTER);

        // Giữa: màn hình + sơ đồ ghế + chú thích
        pnlSoDoGhe = new JPanel();
        pnlSoDoGhe.setLayout(new BoxLayout(pnlSoDoGhe, BoxLayout.Y_AXIS));
        pnlSoDoGhe.setBackground(new Color(0x101B40));
        pnlSoDoGhe.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel stage = new JLabel("MÀN HÌNH", SwingConstants.CENTER);
        stage.setOpaque(true); stage.setBackground(new Color(0x8A94B0)); stage.setForeground(Color.WHITE);
        stage.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16)); stage.setBorder(BorderFactory.createEmptyBorder(9, 4, 9, 4));
        JScrollPane seatScroll = new JScrollPane(pnlSoDoGhe);
        seatScroll.setBorder(BorderFactory.createLineBorder(AppTheme.CARD_BORDER));
        seatScroll.getViewport().setBackground(new Color(0x101B40));
        AppTheme.Card theGhe = new AppTheme.Card(new BorderLayout(0, 10));
        theGhe.add(stage, BorderLayout.NORTH); theGhe.add(seatScroll, BorderLayout.CENTER); theGhe.add(taoChuThichGhe(), BorderLayout.SOUTH);

        // Phải: Thông tin đặt vé
        JPanel luoi = new JPanel(new GridBagLayout()); luoi.setOpaque(false);
        btnCombo = new JButton("Không chọn"); btnCombo.setHorizontalAlignment(SwingConstants.LEFT);
        btnCombo.setBackground(new Color(0xF4F7FF)); btnCombo.setForeground(new Color(0x1F2A44)); btnCombo.setFont(AppTheme.font(java.awt.Font.PLAIN, 13));
        btnCombo.setToolTipText("Chọn, đổi số lượng hoặc bỏ combo bắp nước");
        themDong(luoi, 0, "Ghế đã chọn:", lblGhe); themDong(luoi, 1, "Số lượng:", lblSoLuong); themDong(luoi, 2, "Đơn giá:", lblDonGia);
        themDong(luoi, 3, "Tổng tiền vé:", lblTongVe); themDong(luoi, 4, "Khuyến mãi:", cbKhuyenMai); themDong(luoi, 5, "Combo:", btnCombo);
        themDong(luoi, 6, "Giảm giá:", lblGiam); themDong(luoi, 7, "Khách hàng:", cbKhachHang); themDong(luoi, 8, "Thanh toán:", cbPhuongThuc);
        for (JComponent c : new JComponent[]{cbKhuyenMai, btnCombo, cbKhachHang, cbPhuongThuc}) c.setPreferredSize(new Dimension(170, 34));

        JPanel thanhTien = new JPanel(new BorderLayout()); thanhTien.setOpaque(false);
        thanhTien.add(AppTheme.label("Thành tiền:", java.awt.Font.BOLD, 15, Color.WHITE), BorderLayout.WEST); thanhTien.add(lblThanhTien, BorderLayout.EAST);
        btnThanhToan = new JButton("Thanh toán");
        btnThanhToan.setBackground(new Color(0xE0384F)); btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFont(AppTheme.font(java.awt.Font.BOLD, 16)); btnThanhToan.setPreferredSize(new Dimension(100, 46));
        btnThanhToan.setToolTipText("Thu tiền, lưu hóa đơn, cập nhật ghế rồi in vé (kèm mã QR) cho từng ghế");
        btnLamMoi = new JButton("Làm mới"); btnInLai = new JButton("In lại vé"); btnInLai.setEnabled(false);
        JPanel phu = new JPanel(new GridLayout(1, 2, 8, 0)); phu.setOpaque(false); phu.add(btnLamMoi); phu.add(btnInLai);
        JPanel nam = new JPanel(new GridLayout(0, 1, 0, 10)); nam.setOpaque(false);
        btnTaoPhieu = new JButton("Tạo phiếu đặt");
        btnTaoPhieu.setToolTipText("Giữ ghế và thanh toán sau tại mục Hóa đơn");
        nam.add(thanhTien); nam.add(btnThanhToan); nam.add(btnTaoPhieu); nam.add(phu);
        JPanel luoiWrap = new JPanel(new BorderLayout()); luoiWrap.setOpaque(false); luoiWrap.add(luoi, BorderLayout.NORTH);
        AppTheme.Card theHd = new AppTheme.Card(new BorderLayout(0, 14));
        theHd.setPreferredSize(new Dimension(320, 0));
        theHd.add(AppTheme.label("Thông tin đặt vé", java.awt.Font.BOLD, 18, Color.WHITE), BorderLayout.NORTH);
        theHd.add(luoiWrap, BorderLayout.CENTER); theHd.add(nam, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(12, 0)); body.setOpaque(false);
        body.add(theTrai, BorderLayout.WEST); body.add(theGhe, BorderLayout.CENTER); body.add(theHd, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        // Dữ liệu ban đầu (chưa gắn sự kiện nên không chạy dây chuyền)
        loadDanhSachPhim();
        for (int i = 0; i < cbPhim.getItemCount(); i++) {
            if (cbPhim.getItemAt(i).startsWith(maPhim + " -")) { cbPhim.setSelectedIndex(i); break; }
        }
        loadDanhSachNhanVien();
        loadDanhSachKhachHang();
        loadDanhSachKhuyenMai();

        // Sự kiện: Phim -> Ngày -> Phòng -> Suất -> Sơ đồ ghế
        cbNgay.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) return;
            loadDanhSachPhongChieu(maPhimHienTai());
            if (cbPhongChieu.getItemCount() == 0) { cbSuatChieu.removeAllItems(); resetSoDoGhe(); }
            updateThongTinHoaDon();
        });
        cbPhongChieu.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) return;
            loadDanhSachSuatChieu(e.getItem().toString().split(" - ")[0], maPhimHienTai());
            if (cbSuatChieu.getItemCount() == 0) resetSoDoGhe();
            updateThongTinHoaDon();
        });
        cbSuatChieu.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) return;
            loadSoDoGhe(e.getItem().toString().split(" - ")[0]);
            updateThongTinHoaDon();
        });
        for (JComboBox<String> c : java.util.List.of(cbKhachHang, cbKhuyenMai, cbPhuongThuc))
            c.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) updateThongTinHoaDon(); });

        btnCombo.addActionListener(e -> themCombo());
        btnLamMoi.addActionListener(e -> {
            comboSoLuong.clear(); comboDuLieu.clear();
            if (cbSuatChieu.getSelectedItem() != null) loadSoDoGhe(cbSuatChieu.getSelectedItem().toString().split(" - ")[0]);
            if (cbKhuyenMai.getItemCount() > 0) cbKhuyenMai.setSelectedIndex(0);
            updateThongTinHoaDon();
        });
        btnThanhToan.addActionListener(e -> thanhToan());
        btnTaoPhieu.addActionListener(e -> taoPhieuDat());
        btnInLai.addActionListener(e -> { if (maHoaDonVuaThanhToan != null) InVeHoaDon.in(this, maHoaDonVuaThanhToan, veVuaThanhToan, noiDungHoaDon); });

        capNhatThongTinPhim(maPhimHienTai());
        loadDanhSachNgay(maPhimHienTai());
        updateThongTinHoaDon();
    }

    private String maPhimHienTai() {
        Object it = cbPhim.getSelectedItem();
        return it == null ? "" : it.toString().split(" - ")[0];
    }

    private void themDong(JPanel p, int y, String nhan, JComponent v) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y; c.gridx = 0; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(7, 0, 7, 0);
        p.add(AppTheme.label(nhan, java.awt.Font.BOLD, 13, Color.WHITE), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(7, 12, 7, 0);
        p.add(v, c);
    }

    // Poster, tên, thời lượng, thể loại của phim đang chọn
    private void capNhatThongTinPhim(String maPhim) {
        lblPoster.setIcon(AppTheme.poster(maPhim, 120, 172));
        lblTenPhim.setText("<html><body style='width:130px'>" + (cbPhim.getSelectedItem() == null ? "" : cbPhim.getSelectedItem().toString().split(" - ", 2)[1]) + "</html>");
        String sql = "SELECT p.ThoiLuong, " + dao.PhimDAO.SQL_TEN_THE_LOAI + " AS TenTheLoai FROM Phim p WHERE p.MaPhim = ?";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblThoiLuong.setText("Thời lượng: " + rs.getInt("ThoiLuong") + " phút");
                    lblTheLoai.setText("Thể loại: " + (rs.getString("TenTheLoai") == null ? "—" : rs.getString("TenTheLoai")));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Một hộp thoại duy nhất: mỗi combo đang bán có một ô số lượng. Chọn/đổi/bỏ combo chỉ là sửa số lượng (0 = bỏ)
    private void themCombo(){
        List<ComboBapNuoc> ds=comboDAO.layComboDangBan();
        if(ds.isEmpty()){JOptionPane.showMessageDialog(this,"Hiện không có combo đang bán.");return;}
        JPanel pn=new JPanel(new GridLayout(0,2,12,8));
        List<JSpinner> spinners=new ArrayList<>();
        for(ComboBapNuoc c:ds){
            int hienCo=comboSoLuong.getOrDefault(c.getMaCombo(),0);
            JSpinner sp=new JSpinner(new SpinnerNumberModel(hienCo,0,50,1));
            spinners.add(sp);
            pn.add(new JLabel(c.toString()));
            pn.add(sp);
        }
        Object[] form={"Chọn số lượng cho từng combo (0 = không chọn):",pn};
        if(JOptionPane.showConfirmDialog(this,form,"Chọn combo",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
        comboSoLuong.clear();comboDuLieu.clear();
        for(int i=0;i<ds.size();i++){
            int sl=(Integer)spinners.get(i).getValue();
            if(sl>0){comboSoLuong.put(ds.get(i).getMaCombo(),sl);comboDuLieu.put(ds.get(i).getMaCombo(),ds.get(i));}
        }
        updateThongTinHoaDon();
    }

    // Nút Thanh toán: kiểm tra đủ thông tin -> thu tiền -> ghi hóa đơn/vé trong một giao dịch -> in vé
    private void thanhToan() {
        if (cbSuatChieu.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Chưa có suất chiếu để bán vé.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE); return; }
        if (gheDangChon().isEmpty()) { JOptionPane.showMessageDialog(this, "Hãy chọn ít nhất một ghế.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE); return; }
        if (MainFrame.getInstance() == null || MainFrame.getInstance().getTaiKhoan() == null || MainFrame.getInstance().getTaiKhoan().getNhanVien() == null) { JOptionPane.showMessageDialog(this, "Chưa đăng nhập.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
        if ("Cần chọn combo".equals(lblGiam.getText())) { JOptionPane.showMessageDialog(this, "Khuyến mãi này tặng combo: hãy chọn ít nhất 1 combo trong đơn (hoặc đổi khuyến mãi).", "Khuyến mãi", JOptionPane.WARNING_MESSAGE); return; }
        if (!xacNhanThanhToan()) return;
        btnInLai.setEnabled(true);
        InVeHoaDon.in(this, maHoaDonVuaThanhToan, veVuaThanhToan, noiDungHoaDon);
    }

    private void taoPhieuDat() {
        if (MainFrame.getInstance() == null || MainFrame.getInstance().getTaiKhoan() == null || MainFrame.getInstance().getTaiKhoan().getNhanVien() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập bằng tài khoản nhân viên.", "Chưa đăng nhập", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<JButton> gheChon = gheDangChon();
        if (cbSuatChieu.getSelectedItem() == null || gheChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn suất chiếu và ít nhất một ghế.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!comboSoLuong.isEmpty() || maDangChon(cbKhuyenMai, "Không") != null) {
            int luaChon = JOptionPane.showConfirmDialog(this,
                    "Phiếu đặt chỉ giữ vé; combo và khuyến mãi không được lưu. Tiếp tục tạo phiếu?",
                    "Xác nhận phiếu đặt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (luaChon != JOptionPane.YES_OPTION) return;
        }

        String maSuat = cbSuatChieu.getSelectedItem().toString().split(" - ")[0];
        List<String> maGhe = new ArrayList<>();
        double tongDat = 0;
        for (JButton button : gheChon) {
            maGhe.add(button.getActionCommand());
            tongDat += (Double) button.getClientProperty("giaVe");
        }
        String lyDo = thanhToanDAO.lyDoKhongBanDuoc(maSuat, maGhe);
        if (lyDo != null) {
            loadSoDoGhe(maSuat);
            JOptionPane.showMessageDialog(this, lyDo + ". Sơ đồ ghế đã được cập nhật.", "Không thể tạo phiếu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPhieu = "PD" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 18);
        String maKhach = maDangChon(cbKhachHang, "Khách vãng lai");
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn == null) throw new SQLException("Không có kết nối tới cơ sở dữ liệu");
            conn.setAutoCommit(false);
            try {
                tongDat = phieuDatVeDAO.taoPhieuDat(conn, maPhieu, maKhach, maSuat, maGhe);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Không tạo được phiếu; dữ liệu đã rollback. " + ex.getMessage(),
                        "Lỗi tạo phiếu", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu phiếu đặt: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        comboSoLuong.clear();
        comboDuLieu.clear();
        loadSoDoGhe(maSuat);
        JOptionPane.showMessageDialog(this,
                "Đã tạo phiếu " + maPhieu + " với " + maGhe.size() + " vé, tổng "
                        + String.format("%,.0f VNĐ", tongDat)
                        + ". Mở mục Phiếu đặt để tra cứu hoặc mục Hóa đơn để thanh toán.",
                "Tạo phiếu thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // Kiểm tra ghế, thu tiền theo phương thức đã chọn rồi lưu giao dịch. Trả về true nếu đã lưu thành công
    private boolean xacNhanThanhToan() {
        if (MainFrame.getInstance() == null || MainFrame.getInstance().getTaiKhoan() == null || MainFrame.getInstance().getTaiKhoan().getNhanVien() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập bằng tài khoản nhân viên.", "Chưa đăng nhập", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        List<JButton> gheChon = gheDangChon();
        if (cbSuatChieu.getSelectedItem() == null || gheChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn suất chiếu và ít nhất một ghế.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String maSuat = cbSuatChieu.getSelectedItem().toString().split(" - ")[0];
        List<String> maGhe = new ArrayList<>();
        for (JButton b : gheChon) maGhe.add(b.getActionCommand());

        // Kiểm tra lại TRƯỚC khi nhận tiền (quầy khác có thể vừa bán ghế, hoặc suất đã quá giờ bán)
        String lyDo = thanhToanDAO.lyDoKhongBanDuoc(maSuat, maGhe);
        if (lyDo != null) {
            loadSoDoGhe(maSuat);
            JOptionPane.showMessageDialog(this, lyDo + ". Sơ đồ ghế đã được cập nhật.", "Không thể bán vé", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        double tongPhaiThu = GiaVe.lamTron(tongTien);
        String maHD = "HD" + System.currentTimeMillis();
        String phuongThuc = String.valueOf(cbPhuongThuc.getSelectedItem());
        ThuTienDialog.KetQua thu = ThuTienDialog.hienThi(this, phuongThuc, tongPhaiThu, maHD);
        if (thu == null) return false;   // nhân viên hủy, chưa thu tiền
        return luuGiaoDich(maHD, maSuat, maGhe, phuongThuc, tongPhaiThu, thu);
    }

    // Ghi hóa đơn, vé, combo và điểm tích lũy trong một transaction; thành công thì giữ dữ liệu để in và làm mới sơ đồ ghế
    private boolean luuGiaoDich(String maHD, String maSuat, List<String> maGhe, String phuongThuc,
                                double tongPhaiThu, ThuTienDialog.KetQua thu) {
        String maNV = MainFrame.getInstance().getTaiKhoan().getNhanVien().getMaNhanVien();
        String maKH = maDangChon(cbKhachHang, "Khách vãng lai");
        String maKM = maDangChon(cbKhuyenMai, "Không");
        try (Connection conn = ConnectDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                thanhToanDAO.thanhToan(conn, maHD, maNV, maKH, maKM, maSuat, phuongThuc, maGhe,
                        new LinkedHashMap<>(comboSoLuong), tongPhaiThu);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                baoLoiLuu("Thanh toán thất bại, giao dịch đã rollback: " + ex.getMessage());
                return false;
            }
        } catch (SQLException ex) {
            baoLoiLuu("Không thể mở giao dịch: " + ex.getMessage());
            return false;
        }
        luuDuLieuIn(maHD, thu.tienKhachDua, thanhToanDAO.layNgayLap(maHD));
        comboSoLuong.clear();
        comboDuLieu.clear();
        loadSoDoGhe(maSuat);
        return true;
    }

    // Lỗi sau bước thu tiền: nhắc nhân viên xử lý tiền đã nhận, vì hóa đơn chưa được lưu
    private void baoLoiLuu(String thongBao) {
        JOptionPane.showMessageDialog(this,
                thongBao + "\n\nHóa đơn CHƯA được lưu. Nếu đã nhận tiền của khách, hãy trả lại hoặc thanh toán lại.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // Mã (phần trước " - ") của mục đang chọn; null nếu chưa chọn hoặc là mục mặc định như "Khách vãng lai", "Không"
    private static String maDangChon(JComboBox<String> cb, String mucMacDinh) {
        Object chon = cb.getSelectedItem();
        if (chon == null || chon.toString().equals(mucMacDinh)) return null;
        return chon.toString().split(" - ")[0];
    }
    private void loadDanhSachPhim() {
        String sql = "SELECT MaPhim, TenPhim FROM Phim WHERE TrangThai = N'Đang Chiếu'";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cbPhim.addItem(rs.getString("MaPhim") + " - " + rs.getString("TenPhim"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Các ngày có suất đang mở bán của phim; chọn sẵn hôm nay (hoặc ngày gần nhất sắp tới)
    private void loadDanhSachNgay(String maPhim) {
        cbNgay.removeAllItems();
        List<String> ngay = new ArrayList<>();
        String sql = "SELECT DISTINCT CAST(GioBatDau AS date) AS Ngay FROM SuatChieu WHERE MaPhim = ? AND TrangThai = N'Đang Mở Bán' AND GioBatDau >= DATEADD(MINUTE,-15,SYSDATETIME()) ORDER BY Ngay";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhim);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ngay.add(rs.getString("Ngay")); }
        } catch (SQLException e) { e.printStackTrace(); }
        if (ngay.isEmpty()) { cbPhongChieu.removeAllItems(); cbSuatChieu.removeAllItems(); resetSoDoGhe(); return; }
        String homNay = LocalDate.now().toString();
        int chon = 0;
        for (int i = 0; i < ngay.size(); i++) if (ngay.get(i).compareTo(homNay) >= 0) { chon = i; break; }
        for (String n : ngay) cbNgay.addItem(n);
        cbNgay.setSelectedIndex(chon);
    }

    private void loadDanhSachPhongChieu(String maPhim) {
        cbPhongChieu.removeAllItems();
        String ngay = cbNgay.getSelectedItem() == null ? null : cbNgay.getSelectedItem().toString();
        if (ngay == null) return;
        String sql = "SELECT DISTINCT MaPhongChieu FROM SuatChieu WHERE MaPhim = ? AND TrangThai = N'Đang Mở Bán' AND GioBatDau >= DATEADD(MINUTE,-15,SYSDATETIME()) AND CAST(GioBatDau AS date) = ? ORDER BY MaPhongChieu";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhim); ps.setString(2, ngay);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) cbPhongChieu.addItem(rs.getString("MaPhongChieu")); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadDanhSachSuatChieu(String maPhongChieu, String maPhim) {
        cbSuatChieu.removeAllItems();
        String ngay = cbNgay.getSelectedItem() == null ? null : cbNgay.getSelectedItem().toString();
        if (ngay == null) return;
        String sql = "SELECT MaSuatChieu, GioBatDau, GioKetThuc FROM SuatChieu WHERE MaPhongChieu = ? AND MaPhim = ? AND TrangThai = N'Đang Mở Bán' AND GioBatDau >= DATEADD(MINUTE,-15,SYSDATETIME()) AND CAST(GioBatDau AS date) = ? ORDER BY GioBatDau";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongChieu); ps.setString(2, maPhim); ps.setString(3, ngay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cbSuatChieu.addItem(rs.getString("MaSuatChieu") + " - " + rs.getString("GioBatDau") + " - " + rs.getString("GioKetThuc"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadDanhSachNhanVien() {
        String sql = "SELECT MaNhanVien, TenNhanVien FROM NhanVien";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maNhanVien = rs.getString("MaNhanVien");
                String tenNhanVien = rs.getString("TenNhanVien");
                cbNhanVien.addItem(maNhanVien + " - " + tenNhanVien);
            }
            // Hóa đơn phải ghi đúng người bán: chọn sẵn nhân viên đang đăng nhập và không cho đổi
            TaiKhoan tk = MainFrame.getInstance() == null ? null : MainFrame.getInstance().getTaiKhoan();
            if (tk != null && tk.getNhanVien() != null) {
                String ma = tk.getNhanVien().getMaNhanVien();
                for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
                    if (cbNhanVien.getItemAt(i).startsWith(ma + " - ")) {
                        cbNhanVien.setSelectedIndex(i);
                        break;
                    }
                }
                cbNhanVien.setEnabled(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDanhSachKhachHang() {
        cbKhachHang.addItem("Khách vãng lai"); // Cho phép không chọn khách hàng
        String sql = "SELECT MaKhachHang, TenKhachHang FROM KhachHang";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maKhachHang = rs.getString("MaKhachHang");
                String tenKhachHang = rs.getString("TenKhachHang");
                cbKhachHang.addItem(maKhachHang + " - " + tenKhachHang);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDanhSachKhuyenMai() {
        cbKhuyenMai.removeAllItems(); kmDuLieu.clear();
        cbKhuyenMai.addItem("Không"); // Cho phép không chọn KM
        for (KhuyenMai khuyenMai : khuyenMaiDAO.layKhuyenMaiHieuLuc(LocalDate.now())) {
            kmDuLieu.put(khuyenMai.getMaKhuyenMai(), khuyenMai);
            cbKhuyenMai.addItem(khuyenMai.getMaKhuyenMai() + " - " + khuyenMai.getTenKhuyenMai() + " - " + khuyenMai.mucUuDai());
        }
    }


    private JPanel taoChuThichGhe() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4)); legend.setOpaque(false);
        legend.add(chuThich("Trống", GHE_TRONG)); legend.add(chuThich("Đang chọn", GHE_DANG_CHON)); legend.add(chuThich("Đã đặt", GHE_DA_DAT));
        legend.add(chuThich("Đã bán", GHE_DA_BAN)); legend.add(chuThich("VIP", GHE_VIP));
        return legend;
    }

    private JPanel chuThich(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); item.setOpaque(false);
        JLabel swatch = new JLabel(); swatch.setOpaque(true); swatch.setBackground(color); swatch.setPreferredSize(new Dimension(16, 16));
        item.add(swatch); item.add(AppTheme.label(text, java.awt.Font.PLAIN, 12, new Color(0xD7DEF2)));
        return item;
    }

    // Nút ghế bo góc, tự vẽ để màu nền luôn đúng kể cả khi bị vô hiệu hóa
    private static class GheButton extends JButton {
        GheButton(String text) { super(text); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(getForeground()); g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    private void loadSoDoGhe(String maSuatChieu) {
        pnlSoDoGhe.removeAll();
        List<GheSuatChieu> all = gheDAO.layGheTheoMaSuatChieu(maSuatChieu);
        Map<String, List<GheSuatChieu>> rows = new java.util.TreeMap<>();
        for (GheSuatChieu gsc : all) {
            String pos = gsc.getGhe().getViTri() == null ? "?" : gsc.getGhe().getViTri().trim();
            String row = pos.replaceAll("[0-9].*$", ""); if (row.isEmpty()) row = "Khác";
            rows.computeIfAbsent(row, k -> new ArrayList<>()).add(gsc);
        }
        java.util.Comparator<GheSuatChieu> order = java.util.Comparator.comparingInt((GheSuatChieu g) -> {
            String n = g.getGhe().getViTri().replaceAll("\\D+", ""); try { return Integer.parseInt(n); } catch (Exception ex) { return Integer.MAX_VALUE; }
        }).thenComparing(g -> g.getGhe().getViTri());
        double baseFare = layGiaVeSuatChieu();
        giaCoBan = baseFare;
        for (Map.Entry<String, List<GheSuatChieu>> entry : rows.entrySet()) {
            entry.getValue().sort(order);
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 3)); rowPanel.setOpaque(false);
            rowPanel.add(tenHang(entry.getKey()));
            for (GheSuatChieu gsc : entry.getValue()) {
                Ghe seat = gsc.getGhe(); String position = seat.getViTri() == null ? seat.getMaGhe() : seat.getViTri();
                double unit = GiaVe.tinh(baseFare, seat.getLoaiGhe());
                boolean vip = GiaVe.laVip(seat.getLoaiGhe());

                Color mauGoc = vip ? GHE_VIP : GHE_TRONG;
                GheButton button = new GheButton(position);
                button.setActionCommand(seat.getMaGhe()); button.putClientProperty("giaVe", unit); button.putClientProperty("mauGoc", mauGoc);
                button.setToolTipText("Ghế " + position + " · " + (vip ? "VIP" : "Thường") + " · " + String.format("%,.0f VNĐ", unit));
                button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11)); button.setForeground(Color.WHITE); button.setPreferredSize(new Dimension(38, 34));
                String status = gsc.getTrangThai();
                if ("Đã bán".equalsIgnoreCase(status)) { button.setBackground(GHE_DA_BAN); button.setEnabled(false); }
                else if ("Đã đặt".equalsIgnoreCase(status)) { button.setBackground(GHE_DA_DAT); button.setEnabled(false); }
                else {
                    button.setBackground(mauGoc);
                    button.addActionListener(e -> {
                        if (button.getBackground().equals(mauGoc)) button.setBackground(GHE_DANG_CHON);
                        else if (button.getBackground().equals(GHE_DANG_CHON)) button.setBackground(mauGoc);
                        updateThongTinHoaDon();
                    });
                }
                rowPanel.add(button);
            }
            rowPanel.add(tenHang(entry.getKey()));
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT); rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height)); pnlSoDoGhe.add(rowPanel);
        }
        pnlSoDoGhe.revalidate(); pnlSoDoGhe.repaint(); updateThongTinHoaDon();
    }

    private JLabel tenHang(String ten) {
        JLabel l = new JLabel(ten, SwingConstants.CENTER);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12)); l.setForeground(new Color(0xA9B4CC)); l.setPreferredSize(new Dimension(22, 34));
        return l;
    }

    private void resetSoDoGhe() { pnlSoDoGhe.removeAll(); pnlSoDoGhe.revalidate(); pnlSoDoGhe.repaint(); updateThongTinHoaDon(); }
    private List<JButton> gheDangChon(){List<JButton> selected=new ArrayList<>();thuThapGhe(pnlSoDoGhe,selected);return selected;}
    private void thuThapGhe(Component root,List<JButton> result){if(root instanceof JButton){JButton b=(JButton)root;if(b.getBackground().equals(GHE_DANG_CHON))result.add(b);}else if(root instanceof Container){for(Component child:((Container)root).getComponents())thuThapGhe(child,result);}}
    private double layGiaVeSuatChieu() {
        if (cbSuatChieu == null || cbSuatChieu.getSelectedItem() == null) return 100000;
        String id=cbSuatChieu.getSelectedItem().toString().split(" - ")[0];
        try (Connection c=ConnectDB.getConnection(); PreparedStatement p=c.prepareStatement("SELECT GiaVe FROM SuatChieu WHERE MaSuatChieu=?")) {
            p.setString(1,id); try(ResultSet r=p.executeQuery()){if(r.next())return r.getDouble(1);}
        } catch(SQLException e) { e.printStackTrace(); }
        return 100000;
    }

    private static String tien(double v) { return String.format("%,.0fđ", v); }

    // Cập nhật khung "Thông tin đặt vé" và tổng tiền theo ghế, combo, khuyến mãi đang chọn
    private void updateThongTinHoaDon() {
        double tongVe = 0;
        List<String> ten = new ArrayList<>();
        java.util.TreeSet<Double> gia = new java.util.TreeSet<>();
        for (JButton seat : gheDangChon()) {
            ten.add(seat.getText());
            Object price = seat.getClientProperty("giaVe");
            if (price instanceof Double) { tongVe += (Double) price; gia.add((Double) price); }
        }
        double tienCombo = 0, giaComboReNhat = 0; // giaComboReNhat = 0: chưa có combo
        int soCombo = 0;
        StringBuilder tomTatCombo = new StringBuilder();
        for (Map.Entry<String, Integer> e : comboSoLuong.entrySet()) {
            ComboBapNuoc c = comboDuLieu.get(e.getKey());
            if (c == null) continue;
            tienCombo += c.getGiaBan() * e.getValue();
            soCombo += e.getValue();
            if (giaComboReNhat == 0 || c.getGiaBan() < giaComboReNhat) giaComboReNhat = c.getGiaBan();
            if (tomTatCombo.length() > 0) tomTatCombo.append(", ");
            tomTatCombo.append(c.getTenCombo()).append(" ×").append(e.getValue());
        }
        double goc = tongVe + tienCombo;
        KhuyenMai km = cbKhuyenMai.getSelectedItem() == null ? null : kmDuLieu.get(cbKhuyenMai.getSelectedItem().toString().split(" - ")[0]);
        double sau = km == null ? GiaVe.lamTron(goc) : km.tongSauGiam(tongVe, tienCombo, giaComboReNhat, soCombo);
        tongTien = sau;
        double giam = goc - sau;
        if (km != null && km.laTangCombo() && soCombo == 0) lblGiam.setText("Cần chọn combo");
        else lblGiam.setText(giam > 0.004 ? "-" + tien(giam) : "—");

        lblGhe.setText(ten.isEmpty() ? "—" : String.join(", ", ten));
        lblGhe.setToolTipText(lblGhe.getText());
        lblSoLuong.setText(ten.size() + " ghế");
        lblDonGia.setText(gia.isEmpty() ? tien(giaCoBan) : gia.size() == 1 ? tien(gia.first()) : tien(gia.first()) + " – " + tien(gia.last()));
        lblTongVe.setText(tien(tongVe));
        btnCombo.setText(tomTatCombo.length() == 0 ? "Không chọn" : tomTatCombo.toString());
        btnCombo.setToolTipText(tomTatCombo.length() == 0 ? "Chọn combo bắp nước" : tomTatCombo.toString());
        lblThanhTien.setText(tien(tongTien));
    }

    /**
     * Dữ liệu in vé/hóa đơn lấy thẳng từ hóa đơn VỪA LƯU trong CSDL (đúng từng dòng vé, combo, tiền giảm và tổng tiền),
     * nên tờ in luôn khớp số tiền đã thu. Chỉ khi đọc CSDL lỗi mới dùng dữ liệu trên màn hình.
     */
    private void luuDuLieuIn(String maHD, Double tienKhachDua, LocalDateTime ngayLap){
        try(Connection c=ConnectDB.getConnection()){
            String phim=cbPhim.getSelectedItem().toString().split(" - ",2)[1];
            String gio=cbSuatChieu.getSelectedItem().toString().split(" - ")[1].substring(0,16);
            String phong=String.valueOf(cbPhongChieu.getSelectedItem());
            String tien="%,.0f";
            List<String[]> ve=new ArrayList<>(); StringBuilder dong=new StringBuilder();
            try(PreparedStatement ps=c.prepareStatement("SELECT g.ViTri, ct.DonGia FROM ChiTietHoaDon ct JOIN Ve v ON v.MaVe=ct.MaVe JOIN Ghe g ON g.MaGhe=v.MaGhe WHERE ct.MaHoaDon=? ORDER BY g.ViTri")){
                ps.setString(1,maHD);
                try(ResultSet rs=ps.executeQuery()){while(rs.next()){String vt=rs.getString(1);double gia=rs.getDouble(2);
                    ve.add(new String[]{phim,gio,phong,vt,String.format("%,.0f VNĐ",gia)});dong.append("Vé ").append(vt).append("\t").append(String.format(tien,gia)).append("\n");}}
            }
            try(PreparedStatement ps=c.prepareStatement("SELECT cb.TenCombo, ct.SoLuong, ct.DonGia FROM ChiTietHoaDon ct JOIN ComboBapNuoc cb ON cb.MaCombo=ct.MaCombo WHERE ct.MaHoaDon=? ORDER BY ct.MaChiTietHoaDon")){
                ps.setString(1,maHD);
                try(ResultSet rs=ps.executeQuery()){while(rs.next())dong.append(rs.getString(1)).append(" × ").append(rs.getInt(2)).append("\t").append(String.format(tien,rs.getDouble(3)*rs.getInt(2))).append("\n");}
            }
            double goc,tong;
            try(PreparedStatement ps=c.prepareStatement("SELECT TongTienGoc, TongTien FROM vw_HoaDon_TongTien WHERE MaHoaDon=?")){
                ps.setString(1,maHD);
                try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("Không thấy hóa đơn vừa lưu");goc=rs.getDouble(1);tong=rs.getDouble(2);}
            }
            if(ve.isEmpty())throw new SQLException("Hóa đơn không có vé");
            StringBuilder sb=new StringBuilder("Phim: "+phim+"\nSuất: "+gio+" - Phòng "+phong+"\n");
            if(cbNhanVien.getSelectedItem()!=null)sb.append("Nhân viên: ").append(cbNhanVien.getSelectedItem()).append("\n");
            if(cbKhachHang.getSelectedItem()!=null)sb.append("Khách: ").append(cbKhachHang.getSelectedItem()).append("\n");
            sb.append("---\n").append(dong).append("---\n");
            double giam=goc-tong;
            if(giam>0.004){
                String ma=cbKhuyenMai.getSelectedItem()==null?"":cbKhuyenMai.getSelectedItem().toString().split(" - ")[0];
                KhuyenMai k=kmDuLieu.get(ma);
                sb.append("Tạm tính\t").append(String.format(tien,goc)).append("\n");
                sb.append("Khuyến mãi ").append(k==null?ma:k.getTenKhuyenMai()).append("\t-").append(String.format(tien,giam)).append("\n");
            }
            sb.append("#TỔNG CỘNG\t").append(String.format(tien,tong)).append(" VNĐ\n---\n");
            sb.append("Thanh toán: ").append(cbPhuongThuc.getSelectedItem()).append("\n");
            if (tienKhachDua != null) {
            sb.append("Khách đưa\t").append(String.format("%,.0f VNĐ", tienKhachDua)).append("\n");
            sb.append("Tiền thừa\t").append(String.format("%,.0f VNĐ", tienKhachDua - tongTien)).append("\n");
        }
        sb.append("Ngày lập: ").append((ngayLap == null ? LocalDateTime.now() : ngayLap).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            maHoaDonVuaThanhToan=maHD; veVuaThanhToan.clear(); veVuaThanhToan.addAll(ve); noiDungHoaDon=sb.toString();
        }catch(Exception ex){ex.printStackTrace();luuDuLieuInTuGiaoDien(maHD, tienKhachDua, ngayLap);}
    }

    // (Dự phòng) Dữ liệu in lấy từ màn hình; gọi trước khi sơ đồ ghế bị reset
    private void luuDuLieuInTuGiaoDien(String maHD, Double tienKhachDua, LocalDateTime ngayLap){
        maHoaDonVuaThanhToan=maHD;
        String phim=cbPhim.getSelectedItem().toString().split(" - ",2)[1];
        String gio=cbSuatChieu.getSelectedItem().toString().split(" - ")[1].substring(0,16); // yyyy-MM-dd HH:mm
        String phong=String.valueOf(cbPhongChieu.getSelectedItem());
        veVuaThanhToan.clear();
        for(JButton b:gheDangChon())veVuaThanhToan.add(new String[]{phim,gio,phong,b.getText(),String.format("%,.0f VNĐ",(Double)b.getClientProperty("giaVe"))});
        String tien="%,.0f";
        StringBuilder sb=new StringBuilder("Phim: "+phim+"\nSuất: "+gio+" - Phòng "+phong+"\n");
        if(cbNhanVien.getSelectedItem()!=null)sb.append("Nhân viên: ").append(cbNhanVien.getSelectedItem()).append("\n");
        if(cbKhachHang.getSelectedItem()!=null)sb.append("Khách: ").append(cbKhachHang.getSelectedItem()).append("\n");
        sb.append("---\n");
        double tamTinh=0;
        for(JButton b:gheDangChon()){double gia=(Double)b.getClientProperty("giaVe");tamTinh+=gia;sb.append("Vé ").append(b.getText()).append("\t").append(String.format(tien,gia)).append("\n");}
        for(Map.Entry<String,Integer> e:comboSoLuong.entrySet()){ComboBapNuoc c=comboDuLieu.get(e.getKey());if(c==null)continue;double t=c.getGiaBan()*e.getValue();tamTinh+=t;sb.append(c.getTenCombo()).append(" × ").append(e.getValue()).append("\t").append(String.format(tien,t)).append("\n");}
        double tong=tongTien,giam=tamTinh-tong;
        sb.append("---\n");
        if(giam>0.004){
            String km=cbKhuyenMai.getSelectedItem()==null?"":cbKhuyenMai.getSelectedItem().toString().split(" - ")[0];
            sb.append("Tạm tính\t").append(String.format(tien,tamTinh)).append("\n");
            sb.append("Khuyến mãi ").append(km).append("\t-").append(String.format(tien,giam)).append("\n");
        }
        sb.append("#TỔNG CỘNG\t").append(String.format(tien,tongTien)).append(" VNĐ\n---\n");
        sb.append("Thanh toán: ").append(cbPhuongThuc.getSelectedItem()).append("\n");
        if (tienKhachDua != null) {
            sb.append("Khách đưa\t").append(String.format("%,.0f VNĐ", tienKhachDua)).append("\n");
            sb.append("Tiền thừa\t").append(String.format("%,.0f VNĐ", tienKhachDua - tongTien)).append("\n");
        }
        sb.append("Ngày lập: ").append((ngayLap == null ? LocalDateTime.now() : ngayLap).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        noiDungHoaDon=sb.toString();
    }

}
