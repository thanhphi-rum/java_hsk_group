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
	
	
	
	
	public class QuanLySuatChieu_GUI extends JFrame {
	    private JTable table;
	    private DefaultTableModel tableModel;
	    private JTextField tfTimKiem;
	    private SuatChieuDAO suatChieuDAO = new SuatChieuDAO();
	    private GheDAO gheDao = new GheDAO();
	
	
	    public QuanLySuatChieu_GUI() {
	        setTitle("Quản Lý Suất Chiếu - Rạp Chiếu Phim");
	        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng màn hình full
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setLocationRelativeTo(null);
	        setResizable(false);
	
	        JPanel mainPanel = new JPanel(new BorderLayout());
	
	        // Tạo menu điều hướng từ Menu_GUI
	        JPanel menuPanel = new Menu_GUI(this);
	        mainPanel.add(menuPanel, BorderLayout.NORTH);
	
	        // Tạo bảng danh sách suất chiếu
	        String[] columnNames = {"Mã Suất Chiếu", "Mã Phim", "Mã Phòng Chiếu", "Giờ Bắt Đầu", "Giờ Kết Thúc"};
	        tableModel = new DefaultTableModel(columnNames, 0);
	        table = new JTable(tableModel);
	        table.setRowHeight(30); // Tăng chiều cao hàng để dễ nhìn hơn
	        table.setFont(new Font("Arial", Font.PLAIN, 16)); // Tăng kích thước font để dễ nhìn hơn
	        JScrollPane scrollPane = new JScrollPane(table);
	        scrollPane.setPreferredSize(new Dimension(1200, 600)); // Tăng kích thước bảng
	
	        // Khu vực tìm kiếm và các nút chức năng
	        JPanel controlPanel = new JPanel();
	        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
	
	        tfTimKiem = new HintTextField("Nhập mã phim");
	        tfTimKiem.setPreferredSize(new Dimension(200, 30));
	        controlPanel.add(new JLabel("Tìm Kiếm sc theo mã phim:"));
	        controlPanel.add(tfTimKiem);
	
	        JButton btnTimKiem = new JButton("Tìm Kiếm");
	        btnTimKiem.addActionListener(e -> timKiemSuatChieu());
	        controlPanel.add(btnTimKiem);
	
	        JButton btnThemMoi = new JButton("Thêm Suất Chiếu");
	        btnThemMoi.addActionListener(e -> themMoiSuatChieu());
	        controlPanel.add(btnThemMoi);
	        
	
	        JButton btnChinhSua = new JButton("Chỉnh Sửa");
	        btnChinhSua.addActionListener(e -> chinhSuaSuatChieu());
	        controlPanel.add(btnChinhSua);
	        
	        JButton btnXoa = new JButton("Xóa");
	        btnXoa.addActionListener(e -> xoaSuatChieu());
	        controlPanel.add(btnXoa);

	
	        
	        JButton btnLamMoi = new JButton("Làm Mới");
	        btnLamMoi.addActionListener(e -> lamMoiDanhSach());
	        controlPanel.add(btnLamMoi);

	
	        mainPanel.add(controlPanel, BorderLayout.SOUTH);
	        mainPanel.add(scrollPane, BorderLayout.CENTER);
	
	        add(mainPanel);
	
	        // Load danh sách suất chiếu khi khởi chạy
	        lamMoiDanhSach();
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
		
		        String maSuatChieu = tableModel.getValueAt(selectedRow, 0).toString();
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
		                    JOptionPane.showMessageDialog(this, "Không thể chỉnh sửa suất chiếu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
		                }
		
		            } catch (Exception e) {
		                e.printStackTrace();
		                JOptionPane.showMessageDialog(this, "Lỗi xử lý dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
		            }
		        }
		    }
		
	
	
	    
	
	    private void lamMoiDanhSach() {
	        tableModel.setRowCount(0);
	        List<SuatChieu> danhSach = suatChieuDAO.layTatCaSuatChieu();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        for (SuatChieu sc : danhSach) {
	            tableModel.addRow(new Object[]{
	                sc.getMaSuatChieu(),
	                sc.getPhim().getMaPhim(),
	                sc.getPhongChieu().getMaPhongChieu(),
	                sc.getGioBatDau().format(formatter),
	                sc.getGioKetThuc().format(formatter)
	            });
	        }
	    }
	
	    private void xoaSuatChieu() {
	        int selectedRow = table.getSelectedRow();
	        if (selectedRow == -1) {
	            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu cần xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
	            return;
	        }

	        String maSuatChieu = tableModel.getValueAt(selectedRow, 0).toString();

	        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa suất chiếu này, hãy đãm bảo các hóa đơn tương ứng với suất chiếu này đã được xóa?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
	        if (confirm == JOptionPane.YES_OPTION) {
	            if (suatChieuDAO.xoaSuatChieu(maSuatChieu)) {
	                JOptionPane.showMessageDialog(this, "Xóa suất chiếu thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
	                lamMoiDanhSach();
	            } else {
	                JOptionPane.showMessageDialog(this, "Xóa suất chiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
	            }
	        }
	    }

	
	    private void timKiemSuatChieu() {
	        String input = tfTimKiem.getText().trim();
	        tableModel.setRowCount(0); // Xóa dữ liệu cũ

	        try {
	            String maPhim = input; // Tìm theo mã phim dạng String
	            List<SuatChieu> danhSach = suatChieuDAO.layTatCaSuatChieu();
	            DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	            for (SuatChieu sc : danhSach) {
	                if (sc.getPhim().getMaPhim().equals(maPhim)) {
	                    tableModel.addRow(new Object[]{
	                        sc.getMaSuatChieu(),
	                        sc.getPhim().getMaPhim(),
	                        sc.getPhongChieu().getMaPhongChieu(),
	                        sc.getGioBatDau().format(sdf),
	                        sc.getGioKetThuc().format(sdf)
	                    });
	                }
	            }
	        } catch (Exception e) {
	            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
	        }
	    }


	
	
	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            new QuanLySuatChieu_GUI().setVisible(true);
	        });
	    }
	}