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
	import java.time.ZoneId;
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

	    public QuanLySuatChieu_GUI() {
	        setLayout(new BorderLayout());
	        String[] columnNames = {"Mã SC", "Phim", "Phòng", "Giờ bắt đầu", "Giờ kết thúc", "Trạng thái"};
	        tableModel = new DefaultTableModel(columnNames, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
	        table = new JTable(tableModel);
	        sorter = new javax.swing.table.TableRowSorter<>(tableModel);
	        table.setRowSorter(sorter);
	        AppTheme.styleTable(table);
	        table.getColumnModel().getColumn(5).setCellRenderer(new AppTheme.PillRenderer());
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
	        JButton btnThemMoi = new JButton("Thêm Suất Chiếu"); btnThemMoi.addActionListener(e -> themMoiSuatChieu()); controlPanel.add(btnThemMoi);
	        JButton btnChinhSua = new JButton("Chỉnh Sửa"); btnChinhSua.addActionListener(e -> chinhSuaSuatChieu()); controlPanel.add(btnChinhSua);
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
	            fs.add(RowFilter.regexFilter("^" + d, 3));
	        }
	        Object ph = cbPhong.getSelectedItem();
	        if (ph != null && !"Tất cả".equals(ph.toString())) fs.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(ph.toString()) + "$", 2));
	        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.andFilter(fs));
	        if (timeline != null) timeline.repaint();
	    }

	    /** Thanh thời gian theo phòng (08:00 – 24:00) cho các suất đang hiển thị. */
	    private void veTimeline(Graphics2D g, int w, int h) {
	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        int left = 70, top = 24, startH = 8, endH = 24;
	        double per = (w - left - 10) / (double) (endH - startH);
	        java.util.TreeSet<String> rooms = new java.util.TreeSet<>();
	        for (int i = 0; i < tableModel.getRowCount(); i++) rooms.add(String.valueOf(tableModel.getValueAt(i, 2)));
	        if (rooms.isEmpty()) return;
	        int rowH = Math.min(30, (h - top - 4) / rooms.size());
	        g.setFont(AppTheme.font(Font.PLAIN, 11));
	        for (int hr = startH; hr <= endH; hr += 2) {
	            int x = left + (int) ((hr - startH) * per);
	            g.setColor(new Color(0x34478A)); g.drawLine(x, top - 4, x, top + rooms.size() * rowH);
	            g.setColor(AppTheme.MUTED); g.drawString(String.format("%02d:00", hr % 24), x - 14, top - 8);
	        }
	        int idx = 0; java.util.Map<String, Integer> rowOf = new java.util.HashMap<>();
	        for (String r : rooms) {
	            int y = top + idx * rowH; rowOf.put(r, y);
	            g.setColor(Color.WHITE); g.setFont(AppTheme.font(Font.BOLD, 12)); g.drawString("Phòng " + r.replaceAll("^P", ""), 6, y + rowH / 2 + 4);
	            idx++;
	        }
	        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	        g.setFont(AppTheme.font(Font.BOLD, 11));
	        for (int v = 0; v < table.getRowCount(); v++) {
	            int m = table.convertRowIndexToModel(v);
	            try {
	                LocalDateTime a = LocalDateTime.parse(String.valueOf(tableModel.getValueAt(m, 3)), f), b = LocalDateTime.parse(String.valueOf(tableModel.getValueAt(m, 4)), f);
	                double s0 = a.getHour() + a.getMinute() / 60.0, e0 = b.toLocalDate().isAfter(a.toLocalDate()) ? endH : b.getHour() + b.getMinute() / 60.0;
	                s0 = Math.max(startH, s0); e0 = Math.min(endH, e0);
	                if (e0 <= s0) continue;
	                int x = left + (int) ((s0 - startH) * per), bw = Math.max(6, (int) ((e0 - s0) * per)), y = rowOf.get(String.valueOf(tableModel.getValueAt(m, 2))) + 3;
	                boolean chon = table.getSelectedRow() == v;
	                g.setColor(chon ? new Color(0xE0384F) : AppTheme.ACCENT); g.fillRoundRect(x, y, bw, rowH - 6, 8, 8);
	                g.setColor(Color.WHITE); Shape old = g.getClip(); g.setClip(x, y, bw, rowH - 6);
	                g.drawString(String.valueOf(tableModel.getValueAt(m, 1)), x + 6, y + (rowH - 6) / 2 + 4); g.setClip(old);
	            } catch (Exception ignore) { }
	        }
	    }

	    private void themMoiSuatChieu() {
	        JTextField tfMaSuatChieu = new JTextField();
	        JComboBox<String> cbMaPhim = new JComboBox<>();
	        JComboBox<String> cbMaPhongChieu = new JComboBox<>();
	        JDateChooser dateChooserGioBatDau = new JDateChooser();
	        dateChooserGioBatDau.setDateFormatString("yyyy-MM-dd HH:mm:ss");
	        JTextField tfGioKetThuc = new JTextField();
	        tfGioKetThuc.setEditable(false);
	
	        // Load mã phim
	        try (Connection conn = ConnectDB.getConnection();
	             Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery("SELECT MaPhim FROM Phim WHERE TrangThai = N'Đang Chiếu'")) {
	            while (rs.next()) cbMaPhim.addItem(rs.getString("MaPhim"));
	        } catch (SQLException e) { e.printStackTrace(); }
	
	        // Load mã phòng
	        try (Connection conn = ConnectDB.getConnection();
	             Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery("SELECT MaPhongChieu FROM PhongChieu")) {
	            while (rs.next()) cbMaPhongChieu.addItem(rs.getString("MaPhongChieu"));
	        } catch (SQLException e) { e.printStackTrace(); }
	
	        // Sự kiện cập nhật giờ kết thúc
	        cbMaPhim.addActionListener(e -> updateGioKetThuc(cbMaPhim, dateChooserGioBatDau, tfGioKetThuc));
	        dateChooserGioBatDau.getDateEditor().addPropertyChangeListener("date", evt -> updateGioKetThuc(cbMaPhim, dateChooserGioBatDau, tfGioKetThuc));
	
	        Object[] message = {
	            "Mã Suất Chiếu:", tfMaSuatChieu,
	            "Mã Phim:", cbMaPhim,
	            "Mã Phòng Chiếu:", cbMaPhongChieu,
	            "Giờ Bắt Đầu:", dateChooserGioBatDau,
	            "Giờ Kết Thúc:", tfGioKetThuc
	        };
	
	        while (true) {
	            String[] options = {"Lưu", "Clear"};
	            int option = JOptionPane.showOptionDialog(this, message, "Thêm Suất Chiếu Mới",
	                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
	                    null, options, options[0]);
	
	            if (option == 1) {
	                tfMaSuatChieu.setText("");
	                dateChooserGioBatDau.setDate(null);
	                tfGioKetThuc.setText("");
	                if(cbMaPhim.getItemCount() > 0) cbMaPhim.setSelectedIndex(0);
	                if(cbMaPhongChieu.getItemCount() > 0) cbMaPhongChieu.setSelectedIndex(0);
	                continue;
	            } else if (option != 0) break;
	
	            try {
	                String maSuatChieu = tfMaSuatChieu.getText().trim();
	                String maPhim = (String) cbMaPhim.getSelectedItem();
	                String maPhongChieu = (String) cbMaPhongChieu.getSelectedItem();
	                Date gioBatDauDate = dateChooserGioBatDau.getDate();
	
	                if (maSuatChieu.isEmpty() || gioBatDauDate == null || maPhim == null || maPhongChieu == null) {
	                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                    continue;
	                }
	
	                String gioBatDau = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gioBatDauDate);
	                String gioKetThuc = tfGioKetThuc.getText().trim();
	
	                if (gioBatDauDate.before(new Date())) {
	                    JOptionPane.showMessageDialog(this, "Giờ bắt đầu phải lớn hơn giờ hiện tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                    continue;
	                }
	
	                try (Connection conn = ConnectDB.getConnection()) {
	                    // Kiểm tra mã suất chiếu trùng
	                    String sqlCheck = "SELECT MaSuatChieu FROM SuatChieu WHERE MaSuatChieu = ?";
	                    try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
	                        ps.setString(1, maSuatChieu);
	                        try (ResultSet rs = ps.executeQuery()) {
	                            if (rs.next()) {
	                                JOptionPane.showMessageDialog(this, "Mã suất chiếu đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                                continue;
	                            }
	                        }
	                    }
	
	                    // Kiểm tra suất chiếu trùng giờ/phòng
	                    String sqlTimeCheck = "SELECT * FROM SuatChieu WHERE MaPhongChieu = ? AND ((GioBatDau <= ? AND GioKetThuc > ?) OR (GioBatDau < ? AND GioBatDau >= ?))";
	                    try (PreparedStatement ps = conn.prepareStatement(sqlTimeCheck)) {
	                        ps.setString(1, maPhongChieu);
	                        ps.setString(2, gioBatDau);
	                        ps.setString(3, gioBatDau);
	                        ps.setString(4, gioKetThuc);
	                        ps.setString(5, gioBatDau);
	                        try (ResultSet rs = ps.executeQuery()) {
	                            if (rs.next()) {
	                                JOptionPane.showMessageDialog(this, "Trùng suất chiếu trong phòng này.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	                                continue;
	                            }
	                        }
	                    }
	
	                    LocalDateTime gioBD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(gioBatDau)
	                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	                    LocalDateTime gioKT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(gioKetThuc)
	                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	
	
	                 // Tạo mới một suất chiếu
	                    SuatChieu suatChieu = new SuatChieu(maSuatChieu, new Phim(maPhim), new PhongChieu(maPhongChieu), gioBD.toLocalDate(), gioBD, gioKT, 100000.0, "Đang Mở Bán");
	
	                    // Gọi thêm suất chiếu
	                    boolean thanhCong = suatChieuDAO.themSuatChieu(suatChieu);
	
	                    if (thanhCong) {
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
	
	
	
	    private void updateGioKetThuc(JComboBox<String> cbMaPhim, JDateChooser dateChooserGioBatDau, JTextField tfGioKetThuc) {
	        String selectedMaPhim = (String) cbMaPhim.getSelectedItem();
	        if (selectedMaPhim != null) {
	            try (Connection conn = ConnectDB.getConnection();
	                 PreparedStatement ps = conn.prepareStatement("SELECT ThoiLuong FROM Phim WHERE MaPhim = ?")) {
	                ps.setString(1, selectedMaPhim);
	                try (ResultSet rs = ps.executeQuery()) {
	                    if (rs.next()) {
	                        int thoiLuong = rs.getInt("ThoiLuong");
	                        Date gioBatDau = dateChooserGioBatDau.getDate();
	                        if (gioBatDau != null) {
	                            long gioKetThucMillis = gioBatDau.getTime() + thoiLuong * 60000;
	                            tfGioKetThuc.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(gioKetThucMillis)));
	                        }
	                    }
	                }
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	        }
	    }
	    
		    
	
		    private void chinhSuaSuatChieu() {
		        int selectedRow = table.getSelectedRow();
		        if (selectedRow == -1) {
		            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu cần chỉnh sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
		            return;
		        }
		
		        String maSuatChieu = tableModel.getValueAt(table.convertRowIndexToModel(selectedRow), 0).toString();
		        JComboBox<String> cbMaPhim = new JComboBox<>();
		        JComboBox<String> cbMaPhongChieu = new JComboBox<>();
		        JDateChooser dateChooserGioBatDau = new JDateChooser();
		        dateChooserGioBatDau.setDateFormatString("yyyy-MM-dd HH:mm:ss");
		        JTextField tfGioKetThuc = new JTextField();
		        tfGioKetThuc.setEditable(false);
		
		        // Load dữ liệu
		        try (Connection conn = ConnectDB.getConnection();
		             Statement stmt = conn.createStatement()) {
		
		            ResultSet rsPhim = stmt.executeQuery("SELECT MaPhim FROM Phim WHERE TrangThai = N'Đang Chiếu'");
		            while (rsPhim.next()) cbMaPhim.addItem(rsPhim.getString("MaPhim"));
		
		            rsPhim.close();
		            ResultSet rsPhong = stmt.executeQuery("SELECT MaPhongChieu FROM PhongChieu");
		            while (rsPhong.next()) cbMaPhongChieu.addItem(rsPhong.getString("MaPhongChieu"));
		            rsPhong.close();
		
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		
		        // Load dữ liệu hiện tại
		        SuatChieu suatChieu = suatChieuDAO.laySuatChieuTheoMa(maSuatChieu);
		        if (suatChieu != null) {
		            cbMaPhim.setSelectedItem(suatChieu.getPhim().getMaPhim());
		            cbMaPhongChieu.setSelectedItem(suatChieu.getPhongChieu().getMaPhongChieu());
		            
		         //Khóa comboBox lại không cho sửa
		            cbMaPhim.setEnabled(false);
		            cbMaPhongChieu.setEnabled(false);
		
		            Date gioBatDau = Date.from(suatChieu.getGioBatDau().atZone(ZoneId.systemDefault()).toInstant());
		            dateChooserGioBatDau.setDate(gioBatDau);
		
		            tfGioKetThuc.setText(suatChieu.getGioKetThuc().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		        }
		
		        cbMaPhim.addActionListener(e -> updateGioKetThuc(cbMaPhim, dateChooserGioBatDau, tfGioKetThuc));
		        dateChooserGioBatDau.getDateEditor().addPropertyChangeListener("date", evt -> updateGioKetThuc(cbMaPhim, dateChooserGioBatDau, tfGioKetThuc));
		
		        Object[] message = {
		            "Mã Suất Chiếu:", maSuatChieu,
		            "Mã Phim:", cbMaPhim,
		            "Mã Phòng Chiếu:", cbMaPhongChieu,
		            "Giờ Bắt Đầu:", dateChooserGioBatDau,
		            "Giờ Kết Thúc:", tfGioKetThuc
		        };
		
		        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh Sửa Suất Chiếu", JOptionPane.OK_CANCEL_OPTION);
		        if (option == JOptionPane.OK_OPTION) {
		            try {
		                String maPhim = (String) cbMaPhim.getSelectedItem();
		                String maPhong = (String) cbMaPhongChieu.getSelectedItem();
		                Date gioBDDate = dateChooserGioBatDau.getDate();
		                Date gioKTDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tfGioKetThuc.getText());
		                
		                LocalDateTime gioBD = gioBDDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		                LocalDateTime gioKT = gioKTDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		
		                SuatChieu updated = new SuatChieu(maSuatChieu, new Phim(maPhim), new PhongChieu(maPhong), gioBD.toLocalDate(), gioBD, gioKT, 100000.0, "Đang Mở Bán");
		                
		             // Kiểm tra trùng suất chiếu trong cùng phòng
		                try (Connection conn = ConnectDB.getConnection()) {
		                    String sqlTimeCheck = "SELECT * FROM SuatChieu WHERE MaPhongChieu = ? AND MaSuatChieu != ? " +
		                                          "AND ((GioBatDau <= ? AND GioKetThuc > ?) OR (GioBatDau < ? AND GioBatDau >= ?))";
		                    try (PreparedStatement ps = conn.prepareStatement(sqlTimeCheck)) {
		                        ps.setString(1, maPhong);
		                        ps.setString(2, maSuatChieu); // KHÁC mã suất chiếu hiện tại
		                        ps.setString(3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gioBDDate));
		                        ps.setString(4, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gioBDDate));
		                        ps.setString(5, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gioKTDate));
		                        ps.setString(6, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gioBDDate));
	
		                        try (ResultSet rs = ps.executeQuery()) {
		                            if (rs.next()) {
		                                JOptionPane.showMessageDialog(this, "Suất chiếu bị trùng giờ trong phòng chiếu này.", "Lỗi", JOptionPane.ERROR_MESSAGE);
		                                return; // Ngưng, không cho sửa
		                            }
		                        }
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
		    }
		
	
	
	    
	
	    private void lamMoiDanhSach() {
	        tableModel.setRowCount(0);
	        String sql = "SELECT sc.MaSuatChieu, p.TenPhim, sc.MaPhongChieu, sc.GioBatDau, sc.GioKetThuc, sc.TrangThai FROM SuatChieu sc JOIN Phim p ON p.MaPhim = sc.MaPhim ORDER BY sc.GioBatDau";
	        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	        java.util.TreeSet<String> rooms = new java.util.TreeSet<>();
	        try (Connection c = ConnectDB.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
	            while (rs.next()) {
	                rooms.add(rs.getString("MaPhongChieu"));
	                tableModel.addRow(new Object[]{rs.getString("MaSuatChieu"), rs.getString("TenPhim"), rs.getString("MaPhongChieu"),
	                        rs.getTimestamp("GioBatDau").toLocalDateTime().format(f), rs.getTimestamp("GioKetThuc").toLocalDateTime().format(f), rs.getString("TrangThai")});
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

	        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa suất chiếu này, hãy đãm bảo các hóa đơn tương ứng với suất chiếu này đã được xóa?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
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
