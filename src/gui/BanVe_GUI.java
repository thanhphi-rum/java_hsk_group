package gui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import connect.ConnectDB;
import dao.GheDAO;
import dao.KhuyenMaiDAO;
import dao.HoaDonDAO;
import dao.ChiTietHoaDonDAO;
import dao.ComboBapNuocDAO;
import dao.ThanhToanBanVeDAO;
import dao.VeDAO;
import entity.Ghe;
import entity.GheSuatChieu;
import entity.KhuyenMai;
import entity.HoaDon;
import entity.Ve;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.KhachHang;
import entity.SuatChieu;
import entity.ComboBapNuoc;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanVe_GUI extends JPanel {
    private JComboBox<String> cbPhim, cbPhongChieu, cbSuatChieu, cbNhanVien, cbKhachHang, cbKhuyenMai, cbPhuongThuc;
    private JPanel pnlSoDoGhe;
    private JTextArea txtThongTinHoaDon;
    private JButton btnInHoaDon, btnLuu, btnLamMoi, btnThemCombo;
    private Runnable capNhatPoster = () -> { };
    private int soGheDaChon = 0;
    private double tongTien = 0.0;
    private static final Color GHE_TRONG = new Color(0x3B82F6);
    private static final Color GHE_DANG_CHON = new Color(0xE0384F);
    private static final Color GHE_DA_DAT = new Color(0xE0A93B);
    private static final Color GHE_DA_BAN = new Color(0x6B7A9E);

    private KhuyenMaiDAO khuyenMaiDAO;
    private GheDAO gheDAO;
    private HoaDonDAO hoaDonDAO;
    private ChiTietHoaDonDAO chiTietHoaDonDAO = new ChiTietHoaDonDAO();
    private VeDAO veDAO;
    private final ComboBapNuocDAO comboDAO=new ComboBapNuocDAO();
    private final ThanhToanBanVeDAO thanhToanDAO=new ThanhToanBanVeDAO();
    private final Map<String,Integer> comboSoLuong=new LinkedHashMap<>();
    private final Map<String,ComboBapNuoc> comboDuLieu=new LinkedHashMap<>();


    public BanVe_GUI(String maPhim) {
        setLayout(new BorderLayout(12, 0)); setOpaque(false);

        khuyenMaiDAO = new KhuyenMaiDAO();
        gheDAO = new GheDAO();
        hoaDonDAO = new HoaDonDAO();
        veDAO = new VeDAO();



        // Panel chọn thông tin phim, phòng chiếu, suất chiếu, nhân viên, khách hàng, khuyến mãi
        JPanel pnlThongTin = new JPanel(new GridLayout(14, 1, 0, 4)); pnlThongTin.setOpaque(false);
        pnlThongTin.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Thông tin vé"));
        pnlThongTin.setPreferredSize(new Dimension(250, 0));

        cbPhim = new JComboBox<>();
        cbPhongChieu = new JComboBox<>();
        cbSuatChieu = new JComboBox<>();
        cbNhanVien = new JComboBox<>();
        cbKhachHang = new JComboBox<>();
        cbKhuyenMai = new JComboBox<>();
        cbPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Thẻ ngân hàng", "Chuyển khoản"});

        pnlThongTin.add(new JLabel("Chọn Phim:"));
        pnlThongTin.add(cbPhim);
        pnlThongTin.add(new JLabel("Chọn Phòng Chiếu:"));
        pnlThongTin.add(cbPhongChieu);
        pnlThongTin.add(new JLabel("Chọn Suất Chiếu:"));
        pnlThongTin.add(cbSuatChieu);
        pnlThongTin.add(new JLabel("Chọn Nhân Viên:"));
        pnlThongTin.add(cbNhanVien);
        pnlThongTin.add(new JLabel("Chọn Khách Hàng:"));
        pnlThongTin.add(cbKhachHang);
        pnlThongTin.add(new JLabel("Chọn Khuyến Mãi:"));
        pnlThongTin.add(cbKhuyenMai);
        pnlThongTin.add(new JLabel("Phương thức thanh toán:"));
        pnlThongTin.add(cbPhuongThuc);

        // Cột trái: poster + tên phim + thông tin vé
        JLabel lblPoster = new JLabel("", SwingConstants.CENTER); lblPoster.setPreferredSize(new Dimension(110, 155));
        JLabel lblTenPhim = AppTheme.label(" ", java.awt.Font.BOLD, 17, Color.WHITE);
        JLabel lblPhut = AppTheme.label(" ", java.awt.Font.PLAIN, 12, AppTheme.MUTED);
        JPanel dauPhim = new JPanel(new BorderLayout(12, 0)); dauPhim.setOpaque(false);
        JPanel tenBox = new JPanel(); tenBox.setOpaque(false); tenBox.setLayout(new BoxLayout(tenBox, BoxLayout.Y_AXIS));
        tenBox.add(Box.createVerticalGlue()); tenBox.add(lblTenPhim); tenBox.add(Box.createVerticalStrut(4)); tenBox.add(lblPhut); tenBox.add(Box.createVerticalGlue());
        dauPhim.add(lblPoster, BorderLayout.WEST); dauPhim.add(tenBox, BorderLayout.CENTER);
        capNhatPoster = () -> {
            Object it = cbPhim.getSelectedItem(); if (it == null) return;
            String[] kt = it.toString().split(" - ", 2);
            lblPoster.setIcon(AppTheme.poster(kt[0], 110, 155)); lblTenPhim.setText("<html>" + (kt.length > 1 ? kt[1] : kt[0]) + "</html>");
            lblPhut.setText(" ");
        };
        cbPhim.addActionListener(e -> capNhatPoster.run());
        AppTheme.Card theTrai = new AppTheme.Card(new BorderLayout(0, 10));
        theTrai.add(dauPhim, BorderLayout.NORTH); theTrai.add(pnlThongTin, BorderLayout.CENTER);
        add(theTrai, BorderLayout.WEST);

        // Sơ đồ ghế theo từng hàng, có thể cuộn khi cửa sổ nhỏ.
        pnlSoDoGhe = new JPanel();
        pnlSoDoGhe.setLayout(new BoxLayout(pnlSoDoGhe, BoxLayout.Y_AXIS));
        pnlSoDoGhe.setBackground(new Color(0x101B40));
        pnlSoDoGhe.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel seatScreen = new JPanel(new BorderLayout(6, 6));
        seatScreen.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Sơ đồ ghế"));
        JLabel stage = new JLabel("MÀN HÌNH", SwingConstants.CENTER);
        stage.setOpaque(true); stage.setBackground(new Color(0x8A94B0)); stage.setForeground(Color.WHITE);
        stage.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12)); stage.setBorder(BorderFactory.createEmptyBorder(7, 4, 7, 4));
        seatScreen.add(stage, BorderLayout.NORTH);
        JScrollPane seatScroll = new JScrollPane(pnlSoDoGhe);
        seatScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        seatScreen.add(seatScroll, BorderLayout.CENTER);
        seatScreen.add(taoChuThichGhe(), BorderLayout.SOUTH);
        seatScreen.setOpaque(false); seatScreen.getComponent(1).setBackground(new Color(0x101B40));
        ((JScrollPane) seatScreen.getComponent(1)).setBorder(BorderFactory.createLineBorder(AppTheme.CARD_BORDER));
        ((JScrollPane) seatScreen.getComponent(1)).getViewport().setBackground(new Color(0x101B40));
        AppTheme.Card theGhe = new AppTheme.Card(new BorderLayout()); theGhe.add(seatScreen, BorderLayout.CENTER);
        add(theGhe, BorderLayout.CENTER);
        // Panel thông tin hóa đơn
        JPanel pnlHoaDon = new JPanel(new BorderLayout());
        pnlHoaDon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Tóm tắt hóa đơn"));
        pnlHoaDon.setPreferredSize(new Dimension(290, 0)); pnlHoaDon.setOpaque(false);

        txtThongTinHoaDon = new JTextArea(10, 30);
        txtThongTinHoaDon.setEditable(false); txtThongTinHoaDon.setOpaque(false); txtThongTinHoaDon.setForeground(Color.WHITE); txtThongTinHoaDon.setFont(AppTheme.font(java.awt.Font.PLAIN, 14));
        JScrollPane spHd = new JScrollPane(txtThongTinHoaDon); spHd.setBorder(null); spHd.setOpaque(false); spHd.getViewport().setOpaque(false);
        pnlHoaDon.add(spHd, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlButtons.setOpaque(false);
        btnInHoaDon = new JButton("In Hóa Đơn");
        btnLuu = new JButton("Thanh toán");
        btnLuu.setToolTipText("Kiểm tra lại tổng tiền và hoàn tất giao dịch bán vé");
        btnLuu.setBackground(new Color(0xE0384F));
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFocusPainted(false);
        btnLamMoi = new JButton("Làm Mới");
        btnThemCombo = new JButton("Thêm combo");

        pnlButtons.add(btnThemCombo);
        pnlButtons.add(btnInHoaDon);
        pnlButtons.add(btnLuu);
        pnlButtons.add(btnLamMoi);
        pnlHoaDon.add(pnlButtons, BorderLayout.SOUTH);

        AppTheme.Card theHd = new AppTheme.Card(new BorderLayout()); theHd.add(pnlHoaDon, BorderLayout.CENTER);
        add(theHd, BorderLayout.EAST);

        // Load dữ liệu từ cơ sở dữ liệu
        loadDanhSachPhim();
        // Chọn sẵn phim ứng với mã phim
        for (int i = 0; i < cbPhim.getItemCount(); i++) {
            if (cbPhim.getItemAt(i).startsWith(maPhim + " -")) {
                cbPhim.setSelectedIndex(i);
                break;
            }
        }
        
        // Gọi thủ công hàm load danh sách phòng chiếu và suất chiếu
        if (cbPhim.getSelectedItem() != null) {
            String maPhimSelected = cbPhim.getSelectedItem().toString().split(" - ")[0];
            loadDanhSachPhongChieu(maPhimSelected);

            if (cbPhongChieu.getItemCount() > 0) {
                String maPhongChieu = cbPhongChieu.getItemAt(0);
                loadDanhSachSuatChieu(maPhongChieu, maPhimSelected);

                if (cbSuatChieu.getItemCount() > 0) {
                    String maSuatChieu = cbSuatChieu.getItemAt(0).split(" - ")[0];
                    loadSoDoGhe(maSuatChieu);
                }
            }
        }
        loadDanhSachNhanVien();
        loadDanhSachKhachHang();
        loadDanhSachKhuyenMai();
        capNhatPoster.run();

        // Thêm sự kiện
        cbPhim.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String maPhim = e.getItem().toString().split(" - ")[0];

                    // Load lại phòng chiếu và suất chiếu
                    loadDanhSachPhongChieu(maPhim);

                    if (cbPhongChieu.getItemCount() > 0) {
                        String maPhong = cbPhongChieu.getItemAt(0);
                        loadDanhSachSuatChieu(maPhong, maPhim);

                        if (cbSuatChieu.getItemCount() > 0) {
                            String maSuatChieu = cbSuatChieu.getItemAt(0).split(" - ")[0];
                            loadSoDoGhe(maSuatChieu);
                        } else {
                            cbSuatChieu.removeAllItems(); //Reset combo suất chiếu
                            resetSoDoGhe();
                        }
                    } else {
                        cbPhongChieu.removeAllItems(); // Reset combo phòng
                        cbSuatChieu.removeAllItems();  // Reset combo suất chiếu
                        resetSoDoGhe();
                    }

                    updateThongTinHoaDon();
                }
            }
        });



        cbPhongChieu.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String maPhongChieu = e.getItem().toString().split(" - ")[0];
                    String maPhim = cbPhim.getSelectedItem().toString().split(" - ")[0];
                    loadDanhSachSuatChieu(maPhongChieu, maPhim);
                    updateThongTinHoaDon();
                }
            }
        });


        cbSuatChieu.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    loadSoDoGhe(e.getItem().toString().split(" - ")[0]);
                    updateThongTinHoaDon();
                }
            }
        });

        cbNhanVien.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateThongTinHoaDon();
                }
            }
        });

        cbKhachHang.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateThongTinHoaDon();
                }
            }
        });

        cbKhuyenMai.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateThongTinHoaDon();
                }
            }
        });

        btnThemCombo.addActionListener(e -> themCombo());
        btnLuu.addActionListener(e -> xacNhanThanhToan());
        btnLamMoi.addActionListener(e -> {
            soGheDaChon=0; comboSoLuong.clear(); comboDuLieu.clear();
            if(cbSuatChieu.getSelectedItem()!=null)loadSoDoGhe(cbSuatChieu.getSelectedItem().toString().split(" - ")[0]);
            if(cbKhuyenMai.getItemCount()>0)cbKhuyenMai.setSelectedIndex(0);
            updateThongTinHoaDon();
        });
        btnInHoaDon.addActionListener(e -> {
            try { inHoaDonPDF(); JOptionPane.showMessageDialog(BanVe_GUI.this,"Hóa đơn đã được in thành công!"); }
            catch(DocumentException|IOException ex){JOptionPane.showMessageDialog(BanVe_GUI.this,"Lỗi khi in PDF: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
        });
    }

    private void themCombo(){
        List<ComboBapNuoc> ds=comboDAO.layComboDangBan();if(ds.isEmpty()){JOptionPane.showMessageDialog(this,"Hiện không có combo đang bán.");return;}
        JComboBox<ComboBapNuoc> choice=new JComboBox<>(ds.toArray(new ComboBapNuoc[0]));JSpinner qty=new JSpinner(new SpinnerNumberModel(1,1,50,1));
        Object[] form={"Combo:",choice,"Số lượng:",qty};if(JOptionPane.showConfirmDialog(this,form,"Thêm combo vào hóa đơn",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        ComboBapNuoc c=(ComboBapNuoc)choice.getSelectedItem();if(c==null)return;comboDuLieu.put(c.getMaCombo(),c);comboSoLuong.merge(c.getMaCombo(),(Integer)qty.getValue(),Integer::sum);updateThongTinHoaDon();
    }

    private void xacNhanThanhToan(){
        if(cbSuatChieu.getSelectedItem()==null||gheDangChon().isEmpty()){JOptionPane.showMessageDialog(this,"Chọn suất chiếu và ít nhất một ghế.","Thiếu thông tin",JOptionPane.WARNING_MESSAGE);return;}
        String maSuat=cbSuatChieu.getSelectedItem().toString().split(" - ")[0];
        String maNV=MainFrame.getInstance().getTaiKhoan().getNhanVien().getMaNhanVien();
        String maKH=cbKhachHang.getSelectedItem()==null||cbKhachHang.getSelectedItem().toString().equals("Khách vãng lai")?null:cbKhachHang.getSelectedItem().toString().split(" - ")[0];
        String maKM=cbKhuyenMai.getSelectedItem()==null||cbKhuyenMai.getSelectedItem().toString().equals("Không")?null:cbKhuyenMai.getSelectedItem().toString().split(" - ")[0];
        BigDecimal attendu=BigDecimal.valueOf(tongTien).setScale(2,RoundingMode.HALF_UP);
        int ok=JOptionPane.showConfirmDialog(this,"Xác nhận thanh toán "+String.format("%,.0f VNĐ",attendu.doubleValue())+" bằng "+cbPhuongThuc.getSelectedItem()+"?","Xác nhận thanh toán",JOptionPane.YES_NO_OPTION);if(ok!=JOptionPane.YES_OPTION)return;
        String id="HD"+System.currentTimeMillis();List<String> seatIds=new ArrayList<>();for(JButton b:gheDangChon())seatIds.add(b.getActionCommand());
        try(Connection conn=ConnectDB.getConnection()){
            conn.setAutoCommit(false);
            try{BigDecimal total=thanhToanDAO.thanhToan(conn,id,maNV,maKH,maKM,maSuat,String.valueOf(cbPhuongThuc.getSelectedItem()),seatIds,new LinkedHashMap<>(comboSoLuong),attendu);conn.commit();JOptionPane.showMessageDialog(this,"Thanh toán thành công. Tổng tiền: "+String.format("%,.0f VNĐ",total.doubleValue())+". Điểm khách hàng đã được cập nhật.");comboSoLuong.clear();comboDuLieu.clear();if(cbSuatChieu.getSelectedItem()!=null)loadSoDoGhe(maSuat);}catch(SQLException ex){conn.rollback();JOptionPane.showMessageDialog(this,"Thanh toán thất bại, giao dịch đã rollback: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
        }catch(SQLException ex){JOptionPane.showMessageDialog(this,"Không thể mở giao dịch: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
    }
    private void loadDanhSachPhim() {
        String sql = "SELECT MaPhim, TenPhim FROM Phim WHERE TrangThai = N'Đang Chiếu'";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maPhim = rs.getString("MaPhim");
                String tenPhim = rs.getString("TenPhim");
                cbPhim.addItem(maPhim + " - " + tenPhim);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDanhSachPhongChieu(String maPhim) {
        cbPhongChieu.removeAllItems();
        String sql = "SELECT DISTINCT MaPhongChieu FROM SuatChieu WHERE MaPhim = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maPhongChieu = rs.getString("MaPhongChieu");
                    cbPhongChieu.addItem(maPhongChieu);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDanhSachSuatChieu(String maPhongChieu, String maPhim) {
        cbSuatChieu.removeAllItems();
        String sql = "SELECT MaSuatChieu, GioBatDau, GioKetThuc FROM SuatChieu WHERE MaPhongChieu = ? AND MaPhim = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhongChieu);
            ps.setString(2, maPhim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maSuatChieu = rs.getString("MaSuatChieu");
                    String gioBatDau = rs.getString("GioBatDau");
                    String gioKetThuc = rs.getString("GioKetThuc");
                    cbSuatChieu.addItem(maSuatChieu + " - " + gioBatDau + " - " + gioKetThuc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        cbKhuyenMai.removeAllItems();
        cbKhuyenMai.addItem("Không"); // Cho phép không chọn KM
        for (KhuyenMai khuyenMai : khuyenMaiDAO.layKhuyenMaiHieuLuc(LocalDate.now())) {
            cbKhuyenMai.addItem(khuyenMai.getMaKhuyenMai() + " - " + khuyenMai.getMoTa() + " - " + khuyenMai.getPhanTramGiam() + "%");
        }
    }

    private JPanel taoChuThichGhe() {
        JPanel legend=new JPanel(new FlowLayout(FlowLayout.CENTER,14,4));
        legend.add(chuThich("Trống",GHE_TRONG)); legend.add(chuThich("Đang chọn",GHE_DANG_CHON)); legend.add(chuThich("Đã đặt",GHE_DA_DAT)); legend.add(chuThich("Đã bán",GHE_DA_BAN));
        return legend;
    }
    private JPanel chuThich(String text,Color color){JPanel item=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));JLabel swatch=new JLabel("  ");swatch.setOpaque(true);swatch.setBackground(color);swatch.setBorder(BorderFactory.createEmptyBorder());item.add(swatch);item.add(new JLabel(text));return item;}
    private void loadSoDoGhe(String maSuatChieu) {
        pnlSoDoGhe.removeAll(); soGheDaChon=0;
        List<GheSuatChieu> all=gheDAO.layGheTheoMaSuatChieu(maSuatChieu);
        Map<String,List<GheSuatChieu>> rows=new java.util.TreeMap<>();
        for(GheSuatChieu gsc:all){String pos=gsc.getGhe().getViTri()==null?"?":gsc.getGhe().getViTri().trim();String row=pos.replaceAll("[0-9].*$","");if(row.isEmpty())row="Khác";rows.computeIfAbsent(row,k->new ArrayList<>()).add(gsc);}
        java.util.Comparator<GheSuatChieu> order=java.util.Comparator.comparingInt((GheSuatChieu g)->{String n=g.getGhe().getViTri().replaceAll("\\D+","");try{return Integer.parseInt(n);}catch(Exception ex){return Integer.MAX_VALUE;}}).thenComparing(g->g.getGhe().getViTri());
        BigDecimal baseFare=BigDecimal.valueOf(layGiaVeSuatChieu());
        for(Map.Entry<String,List<GheSuatChieu>> entry:rows.entrySet()){
            entry.getValue().sort(order);JPanel rowPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,5,4));rowPanel.setOpaque(false);
            JLabel rowName=new JLabel(entry.getKey(),SwingConstants.CENTER);rowName.setFont(new java.awt.Font("Segoe UI",java.awt.Font.BOLD,12));rowName.setForeground(new Color(0xA9B4CC));rowName.setPreferredSize(new Dimension(26,34));rowPanel.add(rowName);
            for(GheSuatChieu gsc:entry.getValue()){
                Ghe seat=gsc.getGhe();String position=seat.getViTri()==null?seat.getMaGhe():seat.getViTri();
                BigDecimal unit=baseFare;boolean vip=seat.getLoaiGhe()!=null&&seat.getLoaiGhe().toLowerCase(java.util.Locale.ROOT).contains("vip");if(vip)unit=unit.multiply(new BigDecimal("1.20")).setScale(2,RoundingMode.HALF_UP);
                JButton button=new JButton(position);button.setActionCommand(seat.getMaGhe());button.putClientProperty("giaVe",unit);button.setToolTipText("Ghế "+position+" · "+(vip?"VIP":"Thường")+" · "+String.format("%,.0f VNĐ",unit.doubleValue()));button.setFont(new java.awt.Font("Segoe UI",java.awt.Font.BOLD,11));button.setFocusPainted(false);button.setOpaque(true);button.setBorder(BorderFactory.createEmptyBorder());button.setPreferredSize(new Dimension(50,34));
                String status=gsc.getTrangThai();if("Đã bán".equalsIgnoreCase(status)){button.setBackground(GHE_DA_BAN);button.setForeground(Color.WHITE);button.setEnabled(false);}else if("Đã đặt".equalsIgnoreCase(status)){button.setBackground(GHE_DA_DAT);button.setForeground(Color.WHITE);button.setEnabled(false);}else{button.setBackground(GHE_TRONG);button.setForeground(Color.WHITE);button.addActionListener(e->{if(button.getBackground().equals(GHE_TRONG)){button.setBackground(GHE_DANG_CHON);button.setForeground(Color.WHITE);soGheDaChon++;}else if(button.getBackground().equals(GHE_DANG_CHON)){button.setBackground(GHE_TRONG);button.setForeground(Color.WHITE);soGheDaChon--;}updateThongTinHoaDon();});}
                rowPanel.add(button);
            }
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);pnlSoDoGhe.add(rowPanel);
        }
        pnlSoDoGhe.revalidate();pnlSoDoGhe.repaint();updateThongTinHoaDon();
    }    private void resetSoDoGhe(){pnlSoDoGhe.removeAll();soGheDaChon=0;pnlSoDoGhe.revalidate();pnlSoDoGhe.repaint();}
    private List<JButton> gheDangChon(){List<JButton> selected=new ArrayList<>();thuThapGhe(pnlSoDoGhe,selected);return selected;}
    private void thuThapGhe(Component root,List<JButton> result){if(root instanceof JButton){JButton b=(JButton)root;if(b.getBackground().equals(GHE_DANG_CHON))result.add(b);}else if(root instanceof Container){for(Component child:((Container)root).getComponents())thuThapGhe(child,result);}}
    private double layGiaVeSuatChieu() {
        if (cbSuatChieu == null || cbSuatChieu.getSelectedItem() == null) return 100000;
        String id=cbSuatChieu.getSelectedItem().toString().split(" - ")[0];
        try (Connection c=ConnectDB.getConnection(); PreparedStatement p=c.prepareStatement("SELECT GiaVe FROM SuatChieu WHERE MaSuatChieu=?")) {
            p.setString(1,id); try(ResultSet r=p.executeQuery()){if(r.next())return r.getBigDecimal(1).doubleValue();}
        } catch(SQLException e) { e.printStackTrace(); }
        return 100000;
    }
    private void updateThongTinHoaDon() {
        StringBuilder text=new StringBuilder("TÓM TẮT HÓA ĐƠN\n");
        if(cbPhim.getSelectedItem()!=null)text.append("Phim: ").append(cbPhim.getSelectedItem()).append("\n");
        if(cbSuatChieu.getSelectedItem()!=null)text.append("Suất: ").append(cbSuatChieu.getSelectedItem()).append("\n");
        BigDecimal subtotal=BigDecimal.ZERO;StringBuilder seatNames=new StringBuilder();
        for(JButton seat:gheDangChon()){if(seatNames.length()>0)seatNames.append(", ");seatNames.append(seat.getText());Object price=seat.getClientProperty("giaVe");if(price instanceof BigDecimal)subtotal=subtotal.add((BigDecimal)price);}
        text.append("Ghế: ").append(seatNames.length()==0?"—":seatNames).append("\n");
        if(cbNhanVien.getSelectedItem()!=null)text.append("Nhân viên: ").append(cbNhanVien.getSelectedItem()).append("\n");
        if(cbKhachHang.getSelectedItem()!=null)text.append("Khách: ").append(cbKhachHang.getSelectedItem()).append("\n");
        if(!comboSoLuong.isEmpty()){
            text.append("Combo:\n");
            for(Map.Entry<String,Integer> e:comboSoLuong.entrySet()){ComboBapNuoc c=comboDuLieu.get(e.getKey());if(c!=null){subtotal=subtotal.add(c.getGiaBan().multiply(BigDecimal.valueOf(e.getValue())));text.append(" • ").append(c.getTenCombo()).append(" × ").append(e.getValue()).append("\n");}}
        }
        BigDecimal discount=BigDecimal.ZERO;if(cbKhuyenMai.getSelectedItem()!=null&&!"Không".equals(cbKhuyenMai.getSelectedItem().toString())){String[] parts=cbKhuyenMai.getSelectedItem().toString().split(" - ");try{discount=new BigDecimal(parts[parts.length-1].replace("%","").trim());}catch(Exception ignored){discount=BigDecimal.ZERO;}text.append("Khuyến mãi: ").append(cbKhuyenMai.getSelectedItem()).append("\n");}
        tongTien=subtotal.multiply(BigDecimal.ONE.subtract(discount.movePointLeft(2))).setScale(2,RoundingMode.HALF_UP).doubleValue();
        text.append("Thanh toán: ").append(cbPhuongThuc.getSelectedItem()).append("\n");text.append("TỔNG: ").append(String.format("%,.0f VNĐ",tongTien));txtThongTinHoaDon.setText(text.toString());
    }
    private void inHoaDonPDF() throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);

        int count = 0;
        String fileName;
        do {
            fileName = String.format("HoaDon/HoaDon%s.pdf", count == 0 ? "" : String.format("_%02d", count));
            count++;
        } while (new File(fileName).exists());

        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        BaseFont baseFont = BaseFont.createFont("fonts/ARIAL.TTF", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontTitle = new Font(baseFont, 18, Font.BOLD);
        Font fontContent = new Font(baseFont, 12, Font.NORMAL);

        Paragraph title = new Paragraph("HÓA ĐƠN BÁN VÉ RẠP CHIẾU PHIM", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(""));

        String[] lines = txtThongTinHoaDon.getText().split("");
        for (String line : lines) {
            document.add(new Paragraph(line, fontContent));
        }

        document.close();
    }


}















