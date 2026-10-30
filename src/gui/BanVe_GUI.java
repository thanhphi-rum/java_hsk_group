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
import dao.VeDAO;
import entity.Ghe;
import entity.GheSuatChieu;
import entity.KhuyenMai;
import entity.HoaDon;
import entity.Ve;
import entity.NhanVien;
import entity.KhachHang;
import entity.SuatChieu;

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

public class BanVe_GUI extends JFrame {
    private JComboBox<String> cbPhim, cbPhongChieu, cbSuatChieu, cbNhanVien, cbKhachHang, cbKhuyenMai;
    private JPanel pnlSoDoGhe;
    private JTextArea txtThongTinHoaDon;
    private JButton btnInHoaDon, btnLuu, btnLamMoi, btnThemKhuyenMai;
    private int soGheDaChon = 0;
    private double tongTien = 0.0;

    private KhuyenMaiDAO khuyenMaiDAO;
    private GheDAO gheDAO;
    private HoaDonDAO hoaDonDAO;
    private VeDAO veDAO;


    public BanVe_GUI(String maPhim) {
        setTitle("Bán Vé - Rạp Chiếu Phim");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng màn hình full
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        khuyenMaiDAO = new KhuyenMaiDAO();
        gheDAO = new GheDAO();
        hoaDonDAO = new HoaDonDAO();
        veDAO = new VeDAO();

        setLayout(new BorderLayout());

        // Thêm menu từ Menu_GUI
        JPanel menuPanel = new Menu_GUI(this);
        add(menuPanel, BorderLayout.NORTH);

        // Panel chọn thông tin phim, phòng chiếu, suất chiếu, nhân viên, khách hàng, khuyến mãi
        JPanel pnlThongTin = new JPanel(new GridLayout(6, 2, 10, 10));
        pnlThongTin.setBorder(BorderFactory.createTitledBorder("Chọn Thông Tin"));

        cbPhim = new JComboBox<>();
        cbPhongChieu = new JComboBox<>();
        cbSuatChieu = new JComboBox<>();
        cbNhanVien = new JComboBox<>();
        cbKhachHang = new JComboBox<>();
        cbKhuyenMai = new JComboBox<>();

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

        add(pnlThongTin, BorderLayout.WEST);

        // Panel sơ đồ ghế
        pnlSoDoGhe = new JPanel(new GridLayout(10, 10, 5, 5));
        pnlSoDoGhe.setBorder(BorderFactory.createTitledBorder("Sơ Đồ Phòng Chiếu"));
        add(pnlSoDoGhe, BorderLayout.CENTER);

        // Panel thông tin hóa đơn
        JPanel pnlHoaDon = new JPanel(new BorderLayout());
        pnlHoaDon.setBorder(BorderFactory.createTitledBorder("Thông Tin Hóa Đơn"));

        txtThongTinHoaDon = new JTextArea(10, 30);
        txtThongTinHoaDon.setEditable(false);
        pnlHoaDon.add(new JScrollPane(txtThongTinHoaDon), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnInHoaDon = new JButton("In Hóa Đơn");
        btnLuu = new JButton("Lưu");
        btnLamMoi = new JButton("Làm Mới");
        btnThemKhuyenMai = new JButton("Thêm Khuyến Mãi");

        pnlButtons.add(btnThemKhuyenMai);
        pnlButtons.add(btnInHoaDon);
        pnlButtons.add(btnLuu);
        pnlButtons.add(btnLamMoi);
        pnlHoaDon.add(pnlButtons, BorderLayout.SOUTH);

        add(pnlHoaDon, BorderLayout.EAST);

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

        btnLamMoi.addActionListener(e -> {
            soGheDaChon = 0;
            tongTien = 0;
            txtThongTinHoaDon.setText("");

            if (cbSuatChieu.getSelectedItem() != null) {
                String maSuatChieu = cbSuatChieu.getSelectedItem().toString().split(" - ")[0];
                loadSoDoGhe(maSuatChieu);
            }

            cbKhuyenMai.setSelectedIndex(0); 
        });
        
        btnInHoaDon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    inHoaDonPDF(); 
                    JOptionPane.showMessageDialog(BanVe_GUI.this, "Hóa đơn đã được in thành công!");
                } catch (DocumentException | IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(BanVe_GUI.this, "Lỗi khi in hóa đơn PDF!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        btnThemKhuyenMai.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel pnlForm = new JPanel(new GridLayout(3, 2, 10, 10));
                JTextField txtMaKhuyenMai = new JTextField();
                JTextField txtMoTa = new JTextField();
                JTextField txtPhanTramGiam = new JTextField();
                pnlForm.add(new JLabel("Mã Khuyến Mãi:"));
                pnlForm.add(txtMaKhuyenMai);
                pnlForm.add(new JLabel("Mô Tả Khuyến Mãi:"));
                pnlForm.add(txtMoTa);
                pnlForm.add(new JLabel("Phần Trăm Giảm (%):"));
                pnlForm.add(txtPhanTramGiam);

                int result = JOptionPane.showConfirmDialog(BanVe_GUI.this, pnlForm, "Thêm Khuyến Mãi", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String maKhuyenMai = txtMaKhuyenMai.getText().trim();
                        String moTa = txtMoTa.getText().trim();
                        double phanTramGiam = Double.parseDouble(txtPhanTramGiam.getText().trim());

                        if (!moTa.isEmpty() && phanTramGiam >= 0) {
                            if (khuyenMaiDAO.kiemTraMaKhuyenMai(maKhuyenMai)) {
                                JOptionPane.showMessageDialog(BanVe_GUI.this, "Mã khuyến mãi đã tồn tại, vui lòng nhập mã khác.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            } else {
                                khuyenMaiDAO.themKhuyenMai(new KhuyenMai(maKhuyenMai, moTa, moTa, phanTramGiam, LocalDate.now(), LocalDate.now().plusDays(30)));
                                loadDanhSachKhuyenMai();
                                JOptionPane.showMessageDialog(BanVe_GUI.this, "Thêm khuyến mãi thành công!");
                            }
                        } else {
                            JOptionPane.showMessageDialog(BanVe_GUI.this, "Vui lòng nhập đầy đủ thông tin và phần trăm giảm phải lớn hơn hoặc bằng 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(BanVe_GUI.this, "Phần trăm giảm phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnLuu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	// Kiểm tra điều kiện trước khi lưu
                if (cbPhongChieu.getItemCount() == 0 || cbSuatChieu.getItemCount() == 0 || soGheDaChon == 0) {
                    JOptionPane.showMessageDialog(BanVe_GUI.this, "Không thể lưu hóa đơn vì chưa có suất chiếu hoặc chưa chọn ghế", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Thu thập thông tin từ combobox
                String maSuatChieu = cbSuatChieu.getSelectedItem().toString().split(" - ")[0];
                String maNhanVien = cbNhanVien.getSelectedItem().toString().split(" - ")[0];
                
                String maKhachHang = "";
                if(cbKhachHang.getSelectedItem() != null && !cbKhachHang.getSelectedItem().toString().equals("Khách vãng lai")){
                    maKhachHang = cbKhachHang.getSelectedItem().toString().split(" - ")[0];
                }
                
                String maKhuyenMai = "";
                if(cbKhuyenMai.getSelectedItem() != null && !cbKhuyenMai.getSelectedItem().toString().equals("Không")){
                    maKhuyenMai = cbKhuyenMai.getSelectedItem().toString().split(" - ")[0];
                }
                
                // Lấy danh sách các mã ghế đã chọn (Màu Vàng)
                List<String> danhSachGheDaChon = new ArrayList<>();
                for (Component comp : pnlSoDoGhe.getComponents()) {
                    if (comp instanceof JButton) {
                        JButton btnGhe = (JButton) comp;
                        if (btnGhe.getBackground() == Color.YELLOW) {
                            danhSachGheDaChon.add(btnGhe.getText());
                        }
                    }
                }

                // Bắt đầu Transaction
                try (Connection conn = ConnectDB.getConnection()) {
                    conn.setAutoCommit(false); // Bật chế độ Transaction

                    // 1. Tạo mới đối tượng Hóa Đơn và lưu xuống CSDL
                    String maHoaDonMoi = "HD" + System.currentTimeMillis(); // Sinh mã Hóa Đơn ngẫu nhiên theo thời gian
                    
                    HoaDon hoaDonMoi = new HoaDon(
                        maHoaDonMoi, 
                        new NhanVien(maNhanVien), 
                        new KhachHang(maKhachHang), 
                        new KhuyenMai(maKhuyenMai), 
                        LocalDateTime.now(), 
                        tongTien, 
                        "Tiền mặt"
                    );

                    boolean luuHoaDonThanhCong = hoaDonDAO.themHoaDon(conn, hoaDonMoi);

                    if (!luuHoaDonThanhCong) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(BanVe_GUI.this, "Lỗi khi lưu hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 2. Với mỗi ghế đã chọn, lưu 1 record vào bảng Ve
                    double giaMoiVe = tongTien / soGheDaChon;
                    boolean loiTaoVe = false;

                    for (String maGhe : danhSachGheDaChon) {
                        String maVeMoi = "V" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                        
                        Ve veMoi = new Ve(
                            maVeMoi,
                            new SuatChieu(maSuatChieu),
                            new Ghe(maGhe),
                            new HoaDon(maHoaDonMoi),
                            giaMoiVe
                        );

                        if (!veDAO.themVe(conn, veMoi)) {
                            loiTaoVe = true;
                            break;
                        }
                    }

                    if (loiTaoVe) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(BanVe_GUI.this, "Lỗi khi tạo vé chi tiết.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 3. Cập nhật trạng thái các ghế vừa đặt thành "Đã đặt"
                    for (String maGhe : danhSachGheDaChon) {
                        gheDAO.capNhatTrangThaiGhe(maSuatChieu, maGhe, "Đã đặt");
                    }

                    // Nếu tất cả các bước trên đều thành công, Commit!
                    conn.commit();
                    JOptionPane.showMessageDialog(BanVe_GUI.this, "Thanh toán và lưu hóa đơn thành công!");
                    
                    // Reset lại giao diện
                    soGheDaChon = 0;
                    tongTien = 0;
                    txtThongTinHoaDon.setText("");
                    loadSoDoGhe(maSuatChieu);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(BanVe_GUI.this, "Lỗi hệ thống trong quá trình giao dịch!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
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
        for (KhuyenMai khuyenMai : khuyenMaiDAO.layTatCaKhuyenMai()) {
            cbKhuyenMai.addItem(khuyenMai.getMaKhuyenMai() + " - " + khuyenMai.getMoTa() + " - " + khuyenMai.getPhanTramGiam() + "%");
        }
    }

    private void loadSoDoGhe(String maSuatChieu) {
        pnlSoDoGhe.removeAll();

        List<GheSuatChieu> gheList = gheDAO.layGheTheoMaSuatChieu(maSuatChieu);
        for (GheSuatChieu gsc : gheList) {
            Ghe ghe = gsc.getGhe();
            JButton btnGhe = new JButton(String.valueOf(ghe.getMaGhe()));

            if ("Đã đặt".equalsIgnoreCase(gsc.getTrangThai())) {
                btnGhe.setBackground(Color.RED);
                btnGhe.setEnabled(false);
            } else {
                btnGhe.setBackground(Color.GRAY);
                btnGhe.addActionListener(e -> {
                    if (btnGhe.getBackground() == Color.GRAY) {
                        btnGhe.setBackground(Color.YELLOW);
                        soGheDaChon++;
                    } else if (btnGhe.getBackground() == Color.YELLOW) {
                        btnGhe.setBackground(Color.GRAY);
                        soGheDaChon--;
                    }
                    updateThongTinHoaDon();
                });
            }

            pnlSoDoGhe.add(btnGhe);
        }

        pnlSoDoGhe.revalidate();
        pnlSoDoGhe.repaint();	
    }
    
    private void resetSoDoGhe() {
        pnlSoDoGhe.removeAll();
        pnlSoDoGhe.revalidate();
        pnlSoDoGhe.repaint();
    }

    private void updateThongTinHoaDon() {
        StringBuilder sb = new StringBuilder();
        sb.append("Thông Tin Hóa Đơn:");
        
        if (cbPhim.getSelectedItem() != null) {
            sb.append("Phim: ").append(cbPhim.getSelectedItem().toString()).append(""); 
        }

        if (cbSuatChieu.getSelectedItem() != null) {
            sb.append("Mã Suất Chiếu: ").append(cbSuatChieu.getSelectedItem().toString()).append("");
        }

        StringBuilder danhSachGhe = new StringBuilder();
        for (Component comp : pnlSoDoGhe.getComponents()) {
            if (comp instanceof JButton) {
                JButton btnGhe = (JButton) comp;
                if (btnGhe.getBackground() == Color.YELLOW) {
                    if (danhSachGhe.length() > 0) {
                        danhSachGhe.append(", ");
                    }
                    danhSachGhe.append(btnGhe.getText());
                }
            }
        }
        if (danhSachGhe.length() > 0) {
            sb.append("Mã Ghế: ").append(danhSachGhe.toString()).append("");
        }

        sb.append("Ngày Lập: ").append(LocalDate.now()).append("");

        if (cbNhanVien.getSelectedItem() != null) {
            sb.append("Mã Nhân Viên: ").append(cbNhanVien.getSelectedItem().toString()).append("");
        }
        
        if (cbKhachHang.getSelectedItem() != null && !cbKhachHang.getSelectedItem().toString().equals("Khách vãng lai")) {
            sb.append("Mã Khách Hàng: ").append(cbKhachHang.getSelectedItem().toString()).append("");
        }
        
        if (cbKhuyenMai.getSelectedItem() != null && !cbKhuyenMai.getSelectedItem().toString().equals("Không")) {
            String selectedKhuyenMai = cbKhuyenMai.getSelectedItem().toString();
            sb.append("Mã Khuyến Mãi: ").append(selectedKhuyenMai).append("");
            String[] khuyenMaiParts = selectedKhuyenMai.split(" - ");
            if (khuyenMaiParts.length > 2) {
                try {
                    double phanTramGiam = Double.parseDouble(khuyenMaiParts[2].replace("%", ""));
                    tongTien = (soGheDaChon * 100000) * (1 - phanTramGiam / 100);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            tongTien = soGheDaChon * 100000;
        }

        if (tongTien < 0) {
            tongTien = 0;
        }
        sb.append("Tổng Tiền: ").append(String.format("%,.0f VNĐ", tongTien)).append("");

        txtThongTinHoaDon.setText(sb.toString());
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


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BanVe_GUI("1").setVisible(true));
    }
}
