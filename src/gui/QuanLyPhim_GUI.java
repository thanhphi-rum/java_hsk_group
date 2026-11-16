package gui;

import dao.PhimDAO;
import dao.TheLoaiDAO;
import entity.Phim;
import entity.TheLoai;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.toedter.calendar.JDateChooser;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;

public class QuanLyPhim_GUI extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfTimKiem;
    private PhimDAO phimDAO;
    private final TheLoaiDAO theLoaiDAO = new TheLoaiDAO();
    /** Phim đầy đủ (kể cả mô tả, đạo diễn, diễn viên) theo mã, để khung chi tiết bên phải hiển thị. */
    private final java.util.Map<String, Phim> phimTheoMa = new java.util.HashMap<>();
    private boolean dangNap = false; // đang nạp lại bảng -> không lưu chỉnh sửa tại ô
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
    private static final String[] DS_TUOI = {"P", "K", "T13", "T16", "T18"};
    private static final String[] DS_TT = {"Sắp Chiếu", "Đang Chiếu", "Ngừng Chiếu"};

    public QuanLyPhim_GUI() {
        phimDAO = new PhimDAO();
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());


        // Tạo bảng danh sách phim
        String[] columnNames = {"Mã Phim", "Tên Phim", "Thể Loại", "Thời Lượng", "Giới Hạn Tuổi", "Ngày Khởi Chiếu", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c >= 4; } // nhấn đúp vào ô để chọn
        };
        table = new JTable(tableModel);
        table.setRowHeight(30); // Tăng chiều cao hàng để dễ nhìn hơn
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(table);

        // Khu vực tìm kiếm và các nút chức năng
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        tfTimKiem = new HintTextField("Tìm theo mã, tên hoặc thể loại...");
        tfTimKiem.setPreferredSize(new Dimension(200, 30));
        controlPanel.add(new JLabel("Tìm Kiếm:"));
        controlPanel.add(tfTimKiem);

        JButton btnTimKiem = new JButton("Tìm Kiếm");
        btnTimKiem.addActionListener(e -> timKiemPhim());
        controlPanel.add(btnTimKiem);

        JButton btnThemMoi = new JButton("Thêm");
        btnThemMoi.addActionListener(e -> themMoiPhim());
        controlPanel.add(btnThemMoi);

        JButton btnChinhSua = new JButton("Sửa");
        btnChinhSua.addActionListener(e -> chinhSuaPhim());
        controlPanel.add(btnChinhSua);

        JButton btnXoa = new JButton("Xóa");
        btnXoa.addActionListener(e -> xoaPhim());
        controlPanel.add(btnXoa);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> lamMoiDanhSach());
        controlPanel.add(btnLamMoi);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        AppTheme.styleTable(table); table.setRowHeight(40);
        table.getColumnModel().getColumn(6).setCellRenderer(new AppTheme.PillRenderer());
        // Nhấn đúp vào ô: Giới hạn tuổi / Trạng thái hiện hộp chọn, Ngày khởi chiếu hiện lịch
        DefaultCellEditor eTuoi = new DefaultCellEditor(new JComboBox<>(DS_TUOI)); eTuoi.setClickCountToStart(2);
        DefaultCellEditor eTrangThai = new DefaultCellEditor(new JComboBox<>(DS_TT)); eTrangThai.setClickCountToStart(2);
        table.getColumnModel().getColumn(4).setCellEditor(eTuoi);
        table.getColumnModel().getColumn(5).setCellEditor(new NgayEditor());
        table.getColumnModel().getColumn(6).setCellEditor(eTrangThai);
        table.setSurrendersFocusOnKeystroke(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        tableModel.addTableModelListener(ev -> {
            if (dangNap || ev.getType() != TableModelEvent.UPDATE || ev.getColumn() < 4 || ev.getFirstRow() < 0) return;
            luuTaiO(ev.getFirstRow(), ev.getColumn());
        });
        JLabel poster = new JLabel("", SwingConstants.CENTER);
        poster.setPreferredSize(new Dimension(170, 240));
        String[] nhan = {"Tên phim:", "Thể loại:", "Thời lượng:", "Giới hạn tuổi:", "Ngày khởi chiếu:", "Trạng thái:", "Đạo diễn:", "Diễn viên:", "Mô tả:"};
        JPanel phai = Khung.chiTiet(table, tableModel, poster, nhan, r -> {
            String ma = String.valueOf(tableModel.getValueAt(r, 0));
            poster.setIcon(AppTheme.poster(ma, 170, 240));
            Phim ct = phimTheoMa.get(ma);
            return new String[]{wrap(tableModel.getValueAt(r, 1)), wrap(tableModel.getValueAt(r, 2)),
                    tableModel.getValueAt(r, 3) + " phút", String.valueOf(tableModel.getValueAt(r, 4)), String.valueOf(tableModel.getValueAt(r, 5)),
                    String.valueOf(tableModel.getValueAt(r, 6)),
                    wrap(ct == null ? null : ct.getDaoDien()), wrap(ct == null ? null : ct.getDienVien()), wrap(ct == null ? null : ct.getMoTa())};
        });
        Khung.dung(this, "Quản Lý Phim", controlPanel, scrollPane, phai);

        // Load danh sách phim khi khởi chạy
        lamMoiDanhSach();
    }

    /** Bọc chữ trong HTML để nhãn tự xuống dòng khi văn bản dài. */
    private static String wrap(Object o) {
        if (o == null || o.toString().trim().isEmpty()) return "";
        return "<html><div style='width:170px'>" + o + "</div></html>";
    }

    /** Tạo mỗi thể loại một ô tick; ô nào có tên trong dangChon ("Hành động, Hài") thì tick sẵn. */
    private List<JCheckBox> taoOTheLoai(String dangChon) {
        List<JCheckBox> ds = new java.util.ArrayList<>();
        for (TheLoai tl : theLoaiDAO.layTatCa()) {
            JCheckBox cb = new JCheckBox(tl.getTenTheLoai());
            if (dangChon != null && dangChon.contains(tl.getTenTheLoai())) cb.setSelected(true);
            ds.add(cb);
        }
        return ds;
    }

    /** Đặt các ô tick vào một khung có thanh cuộn. */
    private static JScrollPane khungTheLoai(List<JCheckBox> ds) {
        JPanel p = new JPanel(new GridLayout(0, 3));
        for (JCheckBox cb : ds) p.add(cb);
        JScrollPane sp = new JScrollPane(p);
        sp.setPreferredSize(new Dimension(380, 110));
        return sp;
    }

    /** Ghép tên các thể loại đã tick thành chuỗi "A, B, C". */
    private static String theLoaiDaChon(List<JCheckBox> ds) {
        String kq = "";
        for (JCheckBox cb : ds) {
            if (cb.isSelected()) kq = kq.isEmpty() ? cb.getText() : kq + ", " + cb.getText();
        }
        return kq;
    }

    /** Ô nhập mô tả nhiều dòng. */
    private static JScrollPane khungMoTa(JTextArea ta) {
        ta.setLineWrap(true);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(380, 70));
        return sp;
    }

    private void themMoiPhim() {
        JTextField tfMaPhim = new JTextField();
        JTextField tfTenPhim = new JTextField();
        List<JCheckBox> dsTheLoai = taoOTheLoai(null);
        if (dsTheLoai.isEmpty()) { thongBaoThieuTheLoai(); return; }
        JTextField tfDaoDien = new JTextField();
        JTextField tfDienVien = new JTextField();
        JTextArea taMoTa = new JTextArea(3, 30);
        JTextField tfThoiLuong = new JTextField();
        JTextField tfPoster = new JTextField();
        JComboBox<String> cbTrangThai = new JComboBox<>(DS_TT);
        JComboBox<String> cbTuoi = new JComboBox<>(DS_TUOI);
        JDateChooser dcNgay = new JDateChooser(); dcNgay.setDateFormatString("dd/MM/yyyy");

        Object[] message = {
            "Mã Phim:", tfMaPhim,
            "Tên Phim:", tfTenPhim,
            "Thể Loại (chọn được nhiều):", khungTheLoai(dsTheLoai),
            "Đạo Diễn:", tfDaoDien,
            "Diễn Viên (cách nhau dấu phẩy):", tfDienVien,
            "Mô Tả:", khungMoTa(taMoTa),
            "Thời Lượng (phút):", tfThoiLuong,
            "Ảnh poster (không bắt buộc):", oChonAnh(tfPoster),
            "Giới Hạn Tuổi:", cbTuoi,
            "Ngày Khởi Chiếu:", dcNgay,
            "Trạng Thái:", cbTrangThai
        };

        while (true) {
            String[] options = {"Lưu", "Xóa trắng"};
            int option = JOptionPane.showOptionDialog(this, message, "Thêm Phim Mới",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);

            if (option == 1) { // Xóa trắng
                tfMaPhim.setText("");
                tfTenPhim.setText("");
                for (JCheckBox cb : dsTheLoai) cb.setSelected(false);
                tfDaoDien.setText(""); tfDienVien.setText(""); taMoTa.setText("");
                tfThoiLuong.setText("");
                tfPoster.setText("");
                cbTuoi.setSelectedIndex(0); dcNgay.setDate(null);
                cbTrangThai.setSelectedIndex(0);
                continue;
            } else if (option != 0) {
                break;
            }

            try {
                String maPhim = tfMaPhim.getText().trim();
                String tenPhim = tfTenPhim.getText().trim();
                String theLoai = theLoaiDaChon(dsTheLoai);
                String thoiLuongStr = tfThoiLuong.getText().trim();
                String trangThai = (String) cbTrangThai.getSelectedItem();
                String tuoi = (String) cbTuoi.getSelectedItem();
                LocalDate ngayChieu = dcNgay.getDate() == null ? null : dcNgay.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (ngayChieu == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày khởi chiếu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Kiểm tra rỗng
                if (maPhim.isEmpty() || tenPhim.isEmpty() || thoiLuongStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Kiểm tra định dạng
                if (!tenPhim.matches("^[\\p{L}0-9 ,\\.]+$")) {
                    JOptionPane.showMessageDialog(this, "Tên phim chỉ được chứa chữ cái, số, dấu cách, dấu phẩy và dấu chấm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (theLoai.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một thể loại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!thoiLuongStr.matches("^\\d{2,3}$")) {
                    JOptionPane.showMessageDialog(this, "Thời lượng phải là số nguyên có 2 hoặc 3 chữ số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                int thoiLuong = Integer.parseInt(thoiLuongStr);
                if (thoiLuong <= 0) { JOptionPane.showMessageDialog(this, "Thời lượng phải lớn hơn 0.", "Lỗi", JOptionPane.ERROR_MESSAGE); continue; }

                String loiAnh = kiemTraAnh(tfPoster.getText().trim());
                if (loiAnh != null) {
                    JOptionPane.showMessageDialog(this, loiAnh, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Kiểm tra trùng mã phim
                if (phimDAO.layPhimTheoMa(maPhim) != null) {
                    JOptionPane.showMessageDialog(this, "Mã phim đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                Phim phim = new Phim(maPhim, tenPhim, theLoai, thoiLuong, trangThai, taMoTa.getText().trim(),
                        tfDaoDien.getText().trim(), tfDienVien.getText().trim(), tuoi, ngayChieu);
                boolean success = phimDAO.themPhim(phim);
                if (success) {
                    luuAnh(maPhim, tfPoster.getText().trim());
                    JOptionPane.showMessageDialog(this, "Thêm phim mới thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    lamMoiDanhSach();
                    chonLaiDong(maPhim);
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm phim.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Thời lượng phải là số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void chinhSuaPhim() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phim cần chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedRow = table.convertRowIndexToModel(selectedRow);
        String maPhim = tableModel.getValueAt(selectedRow, 0).toString();
        JTextField tfTenPhim = new JTextField(tableModel.getValueAt(selectedRow, 1).toString());
        List<JCheckBox> dsTheLoai = taoOTheLoai(String.valueOf(tableModel.getValueAt(selectedRow, 2)));
        if (dsTheLoai.isEmpty()) { thongBaoThieuTheLoai(); return; }
        JTextField tfThoiLuong = new JTextField(tableModel.getValueAt(selectedRow, 3).toString());
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Sắp Chiếu", "Đang Chiếu", "Ngừng Chiếu"});
        JTextField tfPoster = new JTextField();
        Phim cu = phimDAO.layPhimTheoMa(maPhim);
        JTextField tfDaoDien = new JTextField(cu == null || cu.getDaoDien() == null ? "" : cu.getDaoDien());
        JTextField tfDienVien = new JTextField(cu == null || cu.getDienVien() == null ? "" : cu.getDienVien());
        JTextArea taMoTa = new JTextArea(cu == null || cu.getMoTa() == null ? "" : cu.getMoTa(), 3, 30);
        cbTrangThai.setSelectedItem(tableModel.getValueAt(selectedRow, 6).toString());
        JComboBox<String> cbTuoi = new JComboBox<>(DS_TUOI);
        if (cu != null && cu.getGioiHanTuoi() != null) cbTuoi.setSelectedItem(cu.getGioiHanTuoi());
        JDateChooser dcNgay = new JDateChooser(); dcNgay.setDateFormatString("dd/MM/yyyy");
        if (cu != null && cu.getNgayKhoiChieu() != null) dcNgay.setDate(java.util.Date.from(cu.getNgayKhoiChieu().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));

        Object[] message = {
            "Tên Phim:", tfTenPhim,
            "Thể Loại (chọn được nhiều):", khungTheLoai(dsTheLoai),
            "Đạo Diễn:", tfDaoDien,
            "Diễn Viên (cách nhau dấu phẩy):", tfDienVien,
            "Mô Tả:", khungMoTa(taMoTa),
            "Thời Lượng:", tfThoiLuong,
            "Ảnh poster (bỏ trống = giữ nguyên):", oChonAnh(tfPoster),
            "Giới Hạn Tuổi:", cbTuoi,
            "Ngày Khởi Chiếu:", dcNgay,
            "Trạng Thái:", cbTrangThai
        };

        String[] nut = {"Lưu", "Hủy"};
        int option = JOptionPane.showOptionDialog(this, message, "Chỉnh Sửa Phim", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, nut, nut[0]);
        if (option != 0) {
            return;
        }

        String tenPhim = tfTenPhim.getText().trim();
        String theLoai = theLoaiDaChon(dsTheLoai);
        String thoiLuongStr = tfThoiLuong.getText().trim();

        // === RÀNG BUỘC ===
        if (!tenPhim.matches("^[\\p{L}0-9 ,\\.]+$")) {
            JOptionPane.showMessageDialog(this, "Tên phim chỉ được chứa chữ cái, số, dấu cách, dấu phẩy và dấu chấm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            chinhSuaPhim();
            return;
        }

        if (theLoai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một thể loại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            chinhSuaPhim();
            return;
        }

        if (!thoiLuongStr.matches("^\\d{2,3}$")) {
            JOptionPane.showMessageDialog(this, "Thời lượng phải là số nguyên có 2 hoặc 3 chữ số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            chinhSuaPhim();
            return;
        }

        String loiAnh = kiemTraAnh(tfPoster.getText().trim());
        if (loiAnh != null) {
            JOptionPane.showMessageDialog(this, loiAnh, "Lỗi", JOptionPane.ERROR_MESSAGE);
            chinhSuaPhim();
            return;
        }

        try {
            int thoiLuong = Integer.parseInt(thoiLuongStr);
            if (thoiLuong <= 0) { JOptionPane.showMessageDialog(this, "Thời lượng phải lớn hơn 0.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            String trangThai = (String) cbTrangThai.getSelectedItem();

            LocalDate ngayChieu = dcNgay.getDate() == null ? null : dcNgay.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            if (ngayChieu == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày khởi chiếu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                chinhSuaPhim();
                return;
            }
            Phim phim = new Phim(maPhim, tenPhim, theLoai, thoiLuong, trangThai,
                    taMoTa.getText().trim(), tfDaoDien.getText().trim(), tfDienVien.getText().trim(),
                    (String) cbTuoi.getSelectedItem(), ngayChieu);
            boolean success = phimDAO.suaPhimVaTheLoai(phim);
            if (success) {
                luuAnh(maPhim, tfPoster.getText().trim());
                JOptionPane.showMessageDialog(this, "Chỉnh sửa phim thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                lamMoiDanhSach();
                chonLaiDong(maPhim);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi chỉnh sửa phim.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Thời lượng phải là số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void thongBaoThieuTheLoai() {
        JOptionPane.showMessageDialog(this, "Không đọc được danh sách thể loại.\n"
                + "Hãy chạy file data/migration_merged.sql (hoặc data/QuanLyBanVePhimMoi.sql) để tạo bảng TheLoai.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /** Ô nhập đường dẫn ảnh kèm nút mở hộp chọn file. */
    private JPanel oChonAnh(JTextField tf) {
        JButton nut = new JButton("Chọn ảnh...");
        nut.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Chọn ảnh poster");
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh (jpg, jpeg, png)", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) tf.setText(fc.getSelectedFile().getAbsolutePath());
        });
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(tf, BorderLayout.CENTER);
        p.add(nut, BorderLayout.EAST);
        return p;
    }

    /** Trả về thông báo lỗi nếu đường dẫn ảnh không hợp lệ; null nếu ổn hoặc để trống. */
    private String kiemTraAnh(String duongDan) {
        if (duongDan.isEmpty()) return null;
        java.io.File f = new java.io.File(duongDan);
        if (!f.isFile()) return "Không tìm thấy file ảnh poster.";
        String t = f.getName().toLowerCase();
        if (!(t.endsWith(".jpg") || t.endsWith(".jpeg") || t.endsWith(".png"))) return "Ảnh poster phải là file .jpg, .jpeg hoặc .png.";
        try {
            if (javax.imageio.ImageIO.read(f) == null) return "Không đọc được file ảnh poster.";
        } catch (java.io.IOException ex) {
            return "Không đọc được file ảnh poster.";
        }
        return null;
    }

    private void luuAnh(String maPhim, String duongDan) {
        if (duongDan.isEmpty()) return;
        try {
            AppTheme.luuPoster(maPhim, new java.io.File(duongDan));
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Đã lưu phim nhưng không chép được ảnh poster: " + ex.getMessage(), "Thông Báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Chọn lại dòng của phim vừa thêm/sửa để khung bên phải hiện poster mới. */
    private void chonLaiDong(String maPhim) {
        for (int r = 0; r < tableModel.getRowCount(); r++)
            if (maPhim.equals(String.valueOf(tableModel.getValueAt(r, 0)))) { table.setRowSelectionInterval(r, r); return; }
    }

    private void xoaPhim() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phim cần xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedRow = table.convertRowIndexToModel(selectedRow);
        String maPhim = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phim này, hãy đảm bảo không còn suất chiếu nào cho phim này?", "Xóa Phim", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = phimDAO.xoaPhim(maPhim);
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa phim thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                lamMoiDanhSach();
            } else {
                JOptionPane.showMessageDialog(this, "Không xóa được phim đang có suất chiếu hoặc vé liên quan. Có thể chuyển trạng thái sang Ngừng Chiếu.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void lamMoiDanhSach() {
        napBang(phimDAO.layTatCaPhim());
    }

    private void timKiemPhim() {
        String keyword = tfTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa để tìm kiếm.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        napBang(phimDAO.timKiemPhim(keyword));
    }

    private void napBang(List<Phim> ds) {
        dangNap = true;
        try {
            tableModel.setRowCount(0);
            phimTheoMa.clear();
            for (Phim p : ds) {
                phimTheoMa.put(p.getMaPhim(), p);
                tableModel.addRow(new Object[]{p.getMaPhim(), p.getTenPhim(), p.getTheLoai(), p.getThoiLuong(),
                        p.getGioiHanTuoi() == null ? "" : p.getGioiHanTuoi(), p.getNgayKhoiChieu() == null ? "" : p.getNgayKhoiChieu().format(NGAY), p.getTrangThai()});
            }
        } finally { dangNap = false; }
    }

    /** Lưu ngay giá trị vừa chọn tại ô (giới hạn tuổi, ngày khởi chiếu, trạng thái) vào CSDL. */
    private void luuTaiO(int row, int col) {
        String ma = String.valueOf(tableModel.getValueAt(row, 0));
        String v = String.valueOf(tableModel.getValueAt(row, col));
        Phim p = phimDAO.layPhimTheoMa(ma);
        boolean ok = p != null;
        if (ok) {
            if (col == 4) p.setGioiHanTuoi(v);
            else if (col == 6) p.setTrangThai(v);
            else {
                try { p.setNgayKhoiChieu(v.isEmpty() ? null : LocalDate.parse(v, NGAY)); } catch (Exception ex) { ok = false; }
            }
            if (ok) ok = phimDAO.suaPhim(p);
        }
        if (!ok) JOptionPane.showMessageDialog(this, "Không lưu được thay đổi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        SwingUtilities.invokeLater(() -> { lamMoiDanhSach(); chonLaiDong(ma); });
    }

    /** Ô chọn ngày (lịch) cho cột Ngày khởi chiếu: nhấn đúp để mở. */
    private static class NgayEditor extends AbstractCellEditor implements TableCellEditor {
        private final JDateChooser dc = new JDateChooser();
        private boolean nap = false;
        NgayEditor() {
            dc.setDateFormatString("dd/MM/yyyy");
            dc.addPropertyChangeListener("date", e -> { if (!nap) stopCellEditing(); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            nap = true;
            try {
                String s = v == null ? "" : v.toString();
                dc.setDate(s.isEmpty() ? null : java.util.Date.from(LocalDate.parse(s, NGAY).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
            } catch (Exception ex) { dc.setDate(null); }
            nap = false;
            return dc;
        }
        @Override public Object getCellEditorValue() {
            return dc.getDate() == null ? "" : dc.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(NGAY);
        }
        @Override public boolean isCellEditable(java.util.EventObject e) {
            return !(e instanceof java.awt.event.MouseEvent) || ((java.awt.event.MouseEvent) e).getClickCount() >= 2;
        }
    }

}

