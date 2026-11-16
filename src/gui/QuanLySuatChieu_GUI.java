package gui;
	
	import javax.swing.*;
	import javax.swing.table.DefaultTableModel;
	import java.awt.*;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.beans.PropertyChangeEvent;
	import java.beans.PropertyChangeListener;
	import java.sql.Connection;
	import java.sql.PreparedStatement;
	import java.sql.ResultSet;
	import java.sql.SQLException;
	import java.sql.Statement;
	import java.sql.Timestamp;
	import java.text.SimpleDateFormat;
	import java.time.LocalDateTime;
	import java.time.LocalDate;
	import java.time.LocalTime;
	import java.time.ZoneId;
	import java.time.temporal.ChronoUnit;
	import java.time.format.DateTimeFormatter;
	
	import connect.ConnectDB;
	import dao.GheDAO;
import dao.HoaDonDAO;
import dao.SuatChieuDAO;
	import entity.SuatChieu;
    import entity.PhongChieu;
    import entity.Phim;
	
	import com.toedter.calendar.JDateChooser;
	import java.util.Date;
	import java.util.List;
	
	
	
	
	public class QuanLySuatChieu_GUI extends JPanel {
	    private JTable table;
	    private DefaultTableModel tableModel;
	    private JTextField tfTimKiem;
	    private SuatChieuDAO suatChieuDAO = new SuatChieuDAO();
	    private GheDAO gheDao = new GheDAO();
	
	
	    private final JDateChooser dcNgay = new JDateChooser();
	    private final JComboBox<String> cbPhong = new JComboBox<>();
	    private javax.swing.table.TableRowSorter<DefaultTableModel> sorter;
	    private JPanel timeline;
	    /** Giờ bắt đầu và kết thúc đầy đủ (ngày + giờ) của từng dòng trong tableModel, cùng thứ tự dòng. */
	    private final List<LocalDateTime[]> thoiGian = new java.util.ArrayList<>();
	    private static final DateTimeFormatter NGAY_F = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    private static final DateTimeFormatter GIO_F = DateTimeFormatter.ofPattern("HH:mm");
	    private static final DateTimeFormatter NGAY_GIO_F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	    public QuanLySuatChieu_GUI() {
	        setLayout(new BorderLayout());
	        String[] columnNames = {"Mã SC", "Phim", "Phòng", "Ngày bắt đầu", "Giờ bắt đầu", "Giờ kết thúc", "Trạng thái"};
	        tableModel = new DefaultTableModel(columnNames, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
	        table = new JTable(tableModel);
	        sorter = new javax.swing.table.TableRowSorter<>(tableModel);
	        table.setRowSorter(sorter);
	        AppTheme.styleTable(table);
	        table.getColumnModel().getColumn(6).setCellRenderer(new AppTheme.PillRenderer());
	        JScrollPane scrollPane = new JScrollPane(table);

	        // Thanh lọc: ngày chiếu + phòng + làm mới
	        dcNgay.setDateFormatString("dd/MM/yyyy"); dcNgay.setDate(new Date());
	        dcNgay.setPreferredSize(new Dimension(140, 36)); cbPhong.setPreferredSize(new Dimension(120, 36));
	        JButton btnLamMoi = new JButton("Làm mới"); btnLamMoi.setPreferredSize(new Dimension(100, 36));
	        btnLamMoi.addActionListener(e -> lamMoiDanhSach());
	        JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); loc.setOpaque(false);
	        loc.add(AppTheme.label("Ngày chiếu:", Font.PLAIN, 13, AppTheme.MUTED)); loc.add(dcNgay);
	        loc.add(AppTheme.label("Phòng:", Font.PLAIN, 13, AppTheme.MUTED)); loc.add(cbPhong); loc.add(btnLamMoi);
	        dcNgay.addPropertyChangeListener("date", e -> apDungLoc());
	        cbPhong.addActionListener(e -> apDungLoc());

	        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
	        JButton btnThemMoi = new JButton("Thêm"); btnThemMoi.addActionListener(e -> themMoiSuatChieu()); controlPanel.add(btnThemMoi);
	        JButton btnChinhSua = new JButton("Sửa"); btnChinhSua.addActionListener(e -> chinhSuaSuatChieu()); controlPanel.add(btnChinhSua);
	        JButton btnXoa = new JButton("Xóa"); btnXoa.addActionListener(e -> xoaSuatChieu()); controlPanel.add(btnXoa);

	        timeline = new JPanel() {
	            @Override protected void paintComponent(Graphics g) { super.paintComponent(g); veTimeline((Graphics2D) g, getWidth(), getHeight()); }
	        };
	        timeline.setOpaque(false); timeline.setPreferredSize(new Dimension(0, 150));
	        table.getSelectionModel().addListSelectionListener(e -> timeline.repaint());

	        Khung.dung(this, "Quản Lý Suất Chiếu", controlPanel, scrollPane, null, timeline, loc);
	        lamMoiDanhSach();
	    }

	    /** Lọc bảng theo ngày + phòng đang chọn rồi vẽ lại thanh thời gian. */
	    private void apDungLoc() {
	        java.util.List<RowFilter<DefaultTableModel, Integer>> fs = new java.util.ArrayList<>();
	        if (dcNgay.getDate() != null) {
	            String d = new SimpleDateFormat("yyyy-MM-dd").format(dcNgay.getDate());
	            fs.add(RowFilter.regexFilter("^" + d + "$", 3));
	        }
	        Object ph = cbPhong.getSelectedItem();
	        if (ph != null && !"Tất cả".equals(ph.toString())) fs.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(ph.toString()) + "$", 2));
	        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.andFilter(fs));
	        if (timeline != null) timeline.repaint();
	    }

	    /** Thanh khung giờ theo phòng: tự co giãn theo các suất đang hiển thị, suất qua nửa đêm kéo dài sang giờ 24+ (vd 23:39 - 02:40). */
	    private void veTimeline(Graphics2D g, int w, int h) {
	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        java.util.TreeSet<String> rooms = new java.util.TreeSet<>();
	        for (int i = 0; i < tableModel.getRowCount(); i++) rooms.add(String.valueOf(tableModel.getValueAt(i, 2)));
	        if (rooms.isEmpty()) return;
	        // Khoảng giờ hiển thị: mặc định 08:00-24:00, mở rộng để chứa mọi suất đang hiển thị
	        int startH = 8, endH = 24;
	        java.util.List<double[]> khoang = new java.util.ArrayList<>(); java.util.List<Integer> dongView = new java.util.ArrayList<>();
	        for (int v = 0; v < table.getRowCount(); v++) {
	            int m = table.convertRowIndexToModel(v);
	            try {
	                LocalDateTime a = thoiGian.get(m)[0], b = thoiGian.get(m)[1];
	                double s0 = a.getHour() + a.getMinute() / 60.0, e0 = s0 + java.time.Duration.between(a, b).toMinutes() / 60.0;
	                startH = Math.min(startH, (int) Math.floor(s0)); endH = Math.max(endH, (int) Math.ceil(e0));
	                khoang.add(new double[]{s0, e0, m}); dongView.add(v);
	            } catch (Exception ignore) { }
	        }
	        endH = Math.min(endH, startH + 30);
	        int left = 70, top = 24;
	        double per = (w - left - 14) / (double) (endH - startH);
	        int rowH = Math.max(18, Math.min(30, (h - top - 4) / rooms.size()));
	        int buoc = (endH - startH) > 16 ? 3 : 2;
	        g.setFont(AppTheme.font(Font.PLAIN, 11));
	        for (int hr = startH; hr <= endH; hr += buoc) {
	            int x = left + (int) ((hr - startH) * per);
	            g.setColor(new Color(0x34478A)); g.drawLine(x, top - 4, x, top + rooms.size() * rowH);
	            g.setColor(AppTheme.MUTED); g.drawString(String.format("%02d:00", hr % 24), x - 14, top - 8);
	        }
	        int idx = 0; java.util.Map<String, Integer> rowOf = new java.util.HashMap<>();
	        for (String r : rooms) {
	            int y = top + idx * rowH; rowOf.put(r, y);
	            g.setColor(Color.WHITE); g.setFont(AppTheme.font(Font.BOLD, 12)); g.drawString("Phòng " + r.replaceAll("^P0*", ""), 6, y + rowH / 2 + 4);
	            idx++;
	        }
	        g.setFont(AppTheme.font(Font.BOLD, 11));
	        for (int k = 0; k < khoang.size(); k++) {
	            double[] kh = khoang.get(k); int m = (int) kh[2];
	            double s0 = Math.max(startH, kh[0]), e0 = Math.min(endH, kh[1]);
	            if (e0 <= s0) continue;
	            int x = left + (int) ((s0 - startH) * per), bw = Math.max(8, (int) ((e0 - s0) * per)), y = rowOf.get(String.valueOf(tableModel.getValueAt(m, 2))) + 3;
	            boolean chon = table.getSelectedRow() == dongView.get(k);
	            g.setColor(chon ? new Color(0xE0384F) : AppTheme.ACCENT); g.fillRoundRect(x, y, bw, rowH - 6, 8, 8);
	            g.setColor(Color.WHITE); Shape old = g.getClip(); g.setClip(x, y, bw, rowH - 6);
	            g.drawString(String.valueOf(tableModel.getValueAt(m, 1)), x + 6, y + (rowH - 6) / 2 + 4); g.setClip(old);
	        }
	    }

	    // ================= Hàm dùng chung cho hộp thoại thêm / sửa =================

	    /** Mục trong combobox chọn phim: hiện "mã phim - tên phim". */
	    private static class PhimItem {
	        final String ma, ten;
	        final int thoiLuong;
	        PhimItem(String ma, String ten, int thoiLuong) { this.ma = ma; this.ten = ten; this.thoiLuong = thoiLuong; }
	        @Override public String toString() { return ma + " - " + ten; }
	    }

	    /** Nạp các phim đang chiếu (và phim maGiu nếu có, dù không còn chiếu) vào combobox. */
	    private void napPhim(JComboBox<PhimItem> cb, String maGiu) {
	        String sql = "SELECT MaPhim, TenPhim, ThoiLuong FROM Phim WHERE TrangThai = N'Đang Chiếu'"
	                + (maGiu == null ? "" : " OR MaPhim = ?") + " ORDER BY MaPhim";
	        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
	            if (maGiu != null) ps.setString(1, maGiu);
	            try (ResultSet rs = ps.executeQuery()) {
	                while (rs.next()) cb.addItem(new PhimItem(rs.getString("MaPhim"), rs.getString("TenPhim"), rs.getInt("ThoiLuong")));
	            }
	        } catch (SQLException e) { e.printStackTrace(); }
	    }

	    private void napPhong(JComboBox<String> cb) {
	        try (Connection conn = ConnectDB.getConnection(); Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery("SELECT MaPhongChieu FROM PhongChieu")) {
	            while (rs.next()) cb.addItem(rs.getString("MaPhongChieu"));
	        } catch (SQLException e) { e.printStackTrace(); }
	    }

	    private static JDateChooser taoNgayChooser() {
	        JDateChooser dc = new JDateChooser();
	        dc.setDateFormatString("dd/MM/yyyy");
	        return dc;
	    }

	    /** Ô chọn giờ:phút (bấm mũi tên để tăng/giảm phần giờ hoặc phút đang đặt con trỏ). */
	    private static JSpinner taoGioSpinner(LocalTime gio) {
	        JSpinner sp = new JSpinner(new SpinnerDateModel());
	        sp.setEditor(new JSpinner.DateEditor(sp, "HH:mm"));
	        datGio(sp, gio);
	        return sp;
	    }

	    private static void datGio(JSpinner sp, LocalTime gio) {
	        sp.setValue(Date.from(gio.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant()));
	    }

	    private static LocalTime layGio(JSpinner sp) {

        return ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
	    }

	    /** Ghép ngày (từ ô chọn ngày) với giờ (từ ô chọn giờ); null nếu chưa chọn ngày. */
	    private static LocalDateTime ghepNgayGio(Date ngay, JSpinner spGio) {
	        if (ngay == null) return null;
	        return LocalDateTime.of(ngay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), layGio(spGio));
	    }

	    /** Giờ kết thúc = giờ bắt đầu + thời lượng phim; hiện vào ô tfGioKetThuc. Trả về null nếu chưa đủ dữ liệu. */
	    private static LocalDateTime tinhGioKetThuc(PhimItem phim, LocalDateTime gioBD, JTextField tfGioKetThuc) {
	        tfGioKetThuc.setText("");
	        if (phim == null || gioBD == null) return null;
	        if (phim.thoiLuong <= 0) throw new IllegalArgumentException("Thời lượng phim phải lớn hơn 0.");
        LocalDateTime kt = gioBD.plusMinutes(phim.thoiLuong);
	        tfGioKetThuc.setText(kt.format(NGAY_GIO_F));
	        return kt;
	    }

	    /** Có suất khác trong cùng phòng chồng giờ với khoảng [bd, kt) không (bỏ qua suất maBoQua nếu có). */
	    private boolean biTrungGio(Connection conn, String maPhong, String maBoQua, LocalDateTime bd, LocalDateTime kt) throws SQLException {
	        String sql = "SELECT 1 FROM SuatChieu WHERE MaPhongChieu = ? " + (maBoQua == null ? "" : "AND MaSuatChieu <> ? ")
	                + "AND ((GioBatDau <= ? AND GioKetThuc > ?) OR (GioBatDau < ? AND GioBatDau >= ?))";
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            int i = 1;
	            ps.setString(i++, maPhong);
	            if (maBoQua != null) ps.setString(i++, maBoQua);
	            ps.setTimestamp(i++, Timestamp.valueOf(bd));
	            ps.setTimestamp(i++, Timestamp.valueOf(bd));
	            ps.setTimestamp(i++, Timestamp.valueOf(kt));
	            ps.setTimestamp(i, Timestamp.valueOf(bd));
	            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
	        }
	    }

	    // ================= Thêm suất chiếu =================

	    private void themMoiSuatChieu() {
	        JTextField tfMaSuatChieu = new JTextField();
	        JComboBox<PhimItem> cbMaPhim = new JComboBox<>();
	        JComboBox<String> cbMaPhongChieu = new JComboBox<>();
	        JDateChooser dcNgayBatDau = taoNgayChooser();
	        JSpinner spGioBatDau = taoGioSpinner(LocalTime.of(8, 0));
	        JTextField tfGioKetThuc = new JTextField();
	        tfGioKetThuc.setEditable(false);

	        napPhim(cbMaPhim, null);
	        napPhong(cbMaPhongChieu);

	        // Giờ kết thúc tự cập nhật khi đổi phim, ngày hoặc giờ bắt đầu
	        Runnable capNhatKetThuc = () -> tinhGioKetThuc((PhimItem) cbMaPhim.getSelectedItem(),
	                ghepNgayGio(dcNgayBatDau.getDate(), spGioBatDau), tfGioKetThuc);
	        cbMaPhim.addActionListener(e -> capNhatKetThuc.run());
	        dcNgayBatDau.getDateEditor().addPropertyChangeListener("date", evt -> capNhatKetThuc.run());
	        spGioBatDau.addChangeListener(e -> capNhatKetThuc.run());

	        Object[] message = {
	            "Mã Suất Chiếu:", tfMaSuatChieu,
	            "Mã Phim:", cbMaPhim,
	            "Mã Phòng Chiếu:", cbMaPhongChieu,
	            "Ngày Bắt Đầu:", dcNgayBatDau,
	            "Giờ Bắt Đầu (giờ:phút):", spGioBatDau,
	            "Giờ Kết Thúc:", tfGioKetThuc
	        };

	        while (true) {
	            String[] options = {"Lưu", "Xóa trắng"};
	            int option = JOptionPane.showOptionDialog(this, message, "Thêm Suất Chiếu Mới",
	                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
	                    null, options, options[0]);

	            if (option == 1) {
	                tfMaSuatChieu.setText("");
	                dcNgayBatDau.setDate(null);
	                datGio(spGioBatDau, LocalTime.of(8, 0));
	                tfGioKetThuc.setText("");
	                if (cbMaPhim.getItemCount() > 0) cbMaPhim.setSelectedIndex(0);
	                if (cbMaPhongChieu.getItemCount() > 0) cbMaPhongChieu.setSelectedIndex(0);
	                continue;
	            } else if (option != 0) break;

	            try {
	                String maSuatChieu = tfMaSuatChieu.getText().trim();
	                spGioBatDau.commitEdit();
                PhimItem phim = (PhimItem) cbMaPhim.getSelectedItem();
	                String maPhongChieu = (String) cbMaPhongChieu.getSelectedItem();
	                LocalDateTime gioBD = ghepNgayGio(dcNgayBatDau.getDate(), spGioBatDau);

	                if (maSuatChieu.isEmpty() || gioBD == null || phim == null || maPhongChieu == null) {
	                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                    continue;
	                }

	                if (gioBD.isBefore(LocalDateTime.now())) {
	                    JOptionPane.showMessageDialog(this, "Giờ bắt đầu phải lớn hơn giờ hiện tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                    continue;
	                }
	                LocalDateTime gioKT = tinhGioKetThuc(phim, gioBD, tfGioKetThuc);

	                try (Connection conn = ConnectDB.getConnection()) {
	                    // Kiểm tra mã suất chiếu trùng
	                    try (PreparedStatement ps = conn.prepareStatement("SELECT MaSuatChieu FROM SuatChieu WHERE MaSuatChieu = ?")) {
	                        ps.setString(1, maSuatChieu);
	                        try (ResultSet rs = ps.executeQuery()) {
	                            if (rs.next()) {
	                                JOptionPane.showMessageDialog(this, "Mã suất chiếu đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                                continue;
	                            }
	                        }
	                    }

	                    // Kiểm tra suất chiếu trùng giờ/phòng
	                    if (biTrungGio(conn, maPhongChieu, null, gioBD, gioKT)) {
	                        JOptionPane.showMessageDialog(this, "Trùng suất chiếu trong phòng này.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                        continue;
	                    }

	                    // Tạo mới một suất chiếu
	                    SuatChieu suatChieu = new SuatChieu(maSuatChieu, new Phim(phim.ma), new PhongChieu(maPhongChieu), gioBD.toLocalDate(), gioBD, gioKT, 100000.0, "Đang Mở Bán");

	                    if (suatChieuDAO.themSuatChieu(suatChieu)) {
	                        gheDao.khoiTaoTrangThaiGheChoSuatChieu(suatChieu.getMaSuatChieu(), suatChieu.getPhongChieu().getMaPhongChieu());
	                        JOptionPane.showMessageDialog(this, "Thêm suất chiếu thành công!");
	                        lamMoiDanhSach();
	                        break;
	                    } else {
	                        JOptionPane.showMessageDialog(this, "Không thể thêm suất chiếu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                    }

	                } catch (SQLException e) {
	                    e.printStackTrace();
	                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm suất chiếu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                }

	            } catch (Exception ex) {
	                ex.printStackTrace();
	                JOptionPane.showMessageDialog(this, "Lỗi xử lý dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
	            }
	        }
	    }

	    // ================= Sửa suất chiếu =================

	    private void chinhSuaSuatChieu() {
	        int selectedRow = table.getSelectedRow();
	        if (selectedRow == -1) {
	            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu cần chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
	            return;
	        }

	        String maSuatChieu = tableModel.getValueAt(table.convertRowIndexToModel(selectedRow), 0).toString();
	        SuatChieu suatChieu = suatChieuDAO.laySuatChieuTheoMa(maSuatChieu);
	        if (suatChieu == null) {
	            JOptionPane.showMessageDialog(this, "Không tìm thấy suất chiếu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        JComboBox<PhimItem> cbMaPhim = new JComboBox<>();
	        JComboBox<String> cbMaPhongChieu = new JComboBox<>();
	        JDateChooser dcNgayBatDau = taoNgayChooser();
	        JSpinner spGioBatDau = taoGioSpinner(LocalTime.of(8, 0));
	        JTextField tfGioKetThuc = new JTextField();
	        tfGioKetThuc.setEditable(false);

	        // Phim của suất này luôn có trong danh sách, kể cả khi phim không còn "Đang Chiếu"
	        napPhim(cbMaPhim, suatChieu.getPhim().getMaPhim());
	        napPhong(cbMaPhongChieu);

	        // Hiện dữ liệu hiện tại; phim và phòng khóa lại không cho sửa
	        for (int i = 0; i < cbMaPhim.getItemCount(); i++) {
	            if (cbMaPhim.getItemAt(i).ma.equals(suatChieu.getPhim().getMaPhim())) { cbMaPhim.setSelectedIndex(i); break; }
	        }
	        cbMaPhongChieu.setSelectedItem(suatChieu.getPhongChieu().getMaPhongChieu());
	        cbMaPhim.setEnabled(false);
	        cbMaPhongChieu.setEnabled(false);

	        LocalDateTime gioBDCu = suatChieu.getGioBatDau();
	        dcNgayBatDau.setDate(Date.from(gioBDCu.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
	        datGio(spGioBatDau, gioBDCu.toLocalTime());
	        tfGioKetThuc.setText(suatChieu.getGioKetThuc().format(NGAY_GIO_F));

	        // Giờ kết thúc giữ nguyên cho tới khi đổi ngày/giờ bắt đầu, sau đó tính lại = bắt đầu + thời lượng phim
	        LocalDateTime[] gioKetThuc = {suatChieu.getGioKetThuc()};
	        Runnable capNhatKetThuc = () -> gioKetThuc[0] = tinhGioKetThuc((PhimItem) cbMaPhim.getSelectedItem(),
	                ghepNgayGio(dcNgayBatDau.getDate(), spGioBatDau), tfGioKetThuc);
	        dcNgayBatDau.getDateEditor().addPropertyChangeListener("date", evt -> capNhatKetThuc.run());
	        spGioBatDau.addChangeListener(e -> capNhatKetThuc.run());

	        Object[] message = {
	            "Mã Suất Chiếu:", maSuatChieu,
	            "Mã Phim:", cbMaPhim,
	            "Mã Phòng Chiếu:", cbMaPhongChieu,
	            "Ngày Bắt Đầu:", dcNgayBatDau,
	            "Giờ Bắt Đầu (giờ:phút):", spGioBatDau,
	            "Giờ Kết Thúc:", tfGioKetThuc
	        };

	        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh Sửa Suất Chiếu", JOptionPane.OK_CANCEL_OPTION);
	        if (option != JOptionPane.OK_OPTION) return;

	        try {
	            spGioBatDau.commitEdit();
                PhimItem phim = (PhimItem) cbMaPhim.getSelectedItem();
	            String maPhong = (String) cbMaPhongChieu.getSelectedItem();
	            LocalDateTime gioBD = ghepNgayGio(dcNgayBatDau.getDate(), spGioBatDau);
	            LocalDateTime gioKT = gioKetThuc[0];
	            if (phim == null || maPhong == null || gioBD == null || gioKT == null) {
	                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                return;
	            }

	            if (!gioKT.isAfter(gioBD)) {
                JOptionPane.showMessageDialog(this, "Giờ kết thúc phải sau giờ bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!gioBD.equals(suatChieu.getGioBatDau()) && !gioBD.isAfter(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "Giờ bắt đầu mới phải lớn hơn giờ hiện tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Giữ nguyên giá vé và trạng thái của suất chiếu khi sửa
	            SuatChieu updated = new SuatChieu(maSuatChieu, new Phim(phim.ma), new PhongChieu(maPhong), gioBD.toLocalDate(), gioBD, gioKT,
	                    suatChieu.getGiaVe(), suatChieu.getTrangThai());

	            // Kiểm tra trùng suất chiếu trong cùng phòng
	            try (Connection conn = ConnectDB.getConnection()) {
	                if (biTrungGio(conn, maPhong, maSuatChieu, gioBD, gioKT)) {
	                    JOptionPane.showMessageDialog(this, "Suất chiếu bị trùng giờ trong phòng chiếu này.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                    return; // Ngưng, không cho sửa
	                }
	            }

	            if (suatChieuDAO.suaSuatChieu(updated)) {
	                JOptionPane.showMessageDialog(this, "Chỉnh sửa suất chiếu thành công.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
	                lamMoiDanhSach();
	            } else {
	                JOptionPane.showMessageDialog(this, "Không thể sửa suất chiếu đã phát sinh vé. Hãy xử lý vé liên quan trước.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(this, "Lỗi xử lý dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
	        }
	    }

	    // ================= Danh sách =================

	    /** Giờ kết thúc để hiện trong bảng: "HH:mm", thêm "(+1 ngày)" nếu suất kết thúc sau nửa đêm. */
	    private static String gioKetThucHienThi(LocalDateTime bd, LocalDateTime kt) {
	        long ngayLech = ChronoUnit.DAYS.between(bd.toLocalDate(), kt.toLocalDate());
	        return kt.format(GIO_F) + (ngayLech > 0 ? " (+" + ngayLech + " ngày)" : "");
	    }

	    private void lamMoiDanhSach() {
	        tableModel.setRowCount(0);
	        thoiGian.clear();
	        String sql = "SELECT sc.MaSuatChieu, p.TenPhim, sc.MaPhongChieu, sc.GioBatDau, sc.GioKetThuc, sc.TrangThai FROM SuatChieu sc JOIN Phim p ON p.MaPhim = sc.MaPhim ORDER BY sc.GioBatDau";
	        java.util.TreeSet<String> rooms = new java.util.TreeSet<>();
	        try (Connection c = ConnectDB.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
	            while (rs.next()) {
	                rooms.add(rs.getString("MaPhongChieu"));
	                LocalDateTime bd = rs.getTimestamp("GioBatDau").toLocalDateTime();
	                LocalDateTime kt = rs.getTimestamp("GioKetThuc").toLocalDateTime();
	                thoiGian.add(new LocalDateTime[]{bd, kt});
	                tableModel.addRow(new Object[]{rs.getString("MaSuatChieu"), rs.getString("TenPhim"), rs.getString("MaPhongChieu"),
	                        bd.format(NGAY_F), bd.format(GIO_F), gioKetThucHienThi(bd, kt), rs.getString("TrangThai")});
	            }
	        } catch (Exception e) { e.printStackTrace(); }
	        Object chon = cbPhong.getSelectedItem();
	        cbPhong.removeAllItems(); cbPhong.addItem("Tất cả");
	        for (String r : rooms) cbPhong.addItem(r);
	        if (chon != null) cbPhong.setSelectedItem(chon);
	        apDungLoc();
	    }

	    private void xoaSuatChieu() {
	        int selectedRow = table.getSelectedRow();
	        if (selectedRow == -1) {
	            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu cần xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
	            return;
	        }

	        String maSuatChieu = tableModel.getValueAt(table.convertRowIndexToModel(selectedRow), 0).toString();

	        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa suất chiếu này? Suất chiếu đã phát sinh vé sẽ được giữ lại.", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
	        if (confirm == JOptionPane.YES_OPTION) {
	            if (suatChieuDAO.xoaSuatChieu(maSuatChieu)) {
	                JOptionPane.showMessageDialog(this, "Xóa suất chiếu thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
	                lamMoiDanhSach();
	            } else {
	                JOptionPane.showMessageDialog(this, "Không xóa được: suất chiếu đã phát sinh vé hoặc đang được tham chiếu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	            }
	        }
	    }

	
	}
