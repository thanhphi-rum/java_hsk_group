package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import connectDB.ConnectDB;
import dao.KhachHang_DAO;
import entity.KhachHang;

public class KhachHang_GUI extends JFrame implements ActionListener, MouseListener {

	private JTable table;
	private DefaultTableModel tableModel;
	private JTextField txtMaKhachHang;
	private JTextField txtTenKhachHang;
	private JTextField txtEmail;
	private JTextField txtSoDienThoai;
	private JTextField txtDiemTichLuy;
	private JTextField txtTim;
	private JButton btnTim;
	private JButton btnThem;
	private JButton btnCapNhat;
	private JButton btnLamMoi;
	private KhachHang_DAO kh_dao;

	public KhachHang_GUI() {
		// khởi tạo kết nối đến CSDL
		ConnectDB.getInstance().connect();
		kh_dao = new KhachHang_DAO();

		setTitle("Quản lý khách hàng");
		setSize(750, 520);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// ---- Tiêu đề ----
		JPanel pnlNorth;
		add(pnlNorth = new JPanel(), BorderLayout.NORTH);
		JLabel lblTieuDe;
		pnlNorth.add(lblTieuDe = new JLabel("THÔNG TIN KHÁCH HÀNG"));
		lblTieuDe.setFont(new Font("Arial", Font.BOLD, 20));
		lblTieuDe.setForeground(Color.BLUE);

		// ---- Form nhập liệu + bảng ----
		Box b = Box.createVerticalBox();
		Box b1, b2, b3, b4, b5;
		JLabel lblMa, lblTen, lblEmail, lblSdt, lblDiem;

		b.add(b1 = Box.createHorizontalBox());
		b.add(Box.createVerticalStrut(10));
		b1.add(lblMa = new JLabel("Mã khách hàng: "));
		b1.add(txtMaKhachHang = new JTextField());
		txtMaKhachHang.setEditable(false); // mã tự sinh

		b.add(b2 = Box.createHorizontalBox());
		b.add(Box.createVerticalStrut(10));
		b2.add(lblTen = new JLabel("Tên khách hàng: "));
		b2.add(txtTenKhachHang = new JTextField());

		b.add(b3 = Box.createHorizontalBox());
		b.add(Box.createVerticalStrut(10));
		b3.add(lblSdt = new JLabel("Số điện thoại: "));
		b3.add(txtSoDienThoai = new JTextField());

		b.add(b4 = Box.createHorizontalBox());
		b.add(Box.createVerticalStrut(10));
		b4.add(lblEmail = new JLabel("Email: "));
		b4.add(txtEmail = new JTextField());
		b4.add(Box.createHorizontalStrut(10));
		b4.add(lblDiem = new JLabel("Điểm tích lũy: "));
		b4.add(txtDiemTichLuy = new JTextField());
		txtDiemTichLuy.setEditable(false); // điểm chỉ đổi qua hóa đơn

		lblTen.setPreferredSize(lblMa.getPreferredSize());
		lblSdt.setPreferredSize(lblMa.getPreferredSize());
		lblEmail.setPreferredSize(lblMa.getPreferredSize());

		b.add(b5 = Box.createHorizontalBox());
		b.add(Box.createVerticalStrut(10));
		String[] headers = "Mã KH;Tên khách hàng;Số điện thoại;Email;Điểm tích lũy".split(";");
		tableModel = new DefaultTableModel(headers, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(table = new JTable(tableModel));
		table.setRowHeight(25);
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		b5.add(scroll);
		add(b, BorderLayout.CENTER);

		// ---- Tìm kiếm + nút chức năng ----
		JPanel pnlSouth;
		add(pnlSouth = new JPanel(), BorderLayout.SOUTH);
		pnlSouth.add(new JLabel("Tìm theo mã / tên / SĐT: "));
		pnlSouth.add(txtTim = new JTextField(12));
		pnlSouth.add(btnTim = new JButton("Tìm"));
		pnlSouth.add(Box.createHorizontalStrut(20));
		pnlSouth.add(btnThem = new JButton("Thêm"));
		pnlSouth.add(btnCapNhat = new JButton("Cập nhật"));
		pnlSouth.add(btnLamMoi = new JButton("Làm mới"));

		// ---- Sự kiện ----
		btnTim.addActionListener(this);
		btnThem.addActionListener(this);
		btnCapNhat.addActionListener(this);
		btnLamMoi.addActionListener(this);
		txtTim.addActionListener(this); // nhấn Enter trong ô tìm
		table.addMouseListener(this);

		// Đọc dữ liệu từ database vào JTable
		docDuLieuVaoTable(kh_dao.getAllKhachHang());
		lamMoiForm();
	}

	private void docDuLieuVaoTable(ArrayList<KhachHang> ds) {
		tableModel.setRowCount(0);
		for (KhachHang kh : ds) {
			String[] rowData = { kh.getMaKhachHang(), kh.getTenKhachHang(), kh.getSoDienThoai(), kh.getEmail(),
					kh.getDiemTichLuy() + "" };
			tableModel.addRow(rowData);
		}
	}

	private void lamMoiForm() {
		txtMaKhachHang.setText(kh_dao.taoMaKhachHangMoi());
		txtTenKhachHang.setText("");
		txtSoDienThoai.setText("");
		txtEmail.setText("");
		txtDiemTichLuy.setText("0");
		txtTim.setText("");
		table.clearSelection();
		txtTenKhachHang.requestFocus();
	}

	// Kiểm tra dữ liệu nhập; trả về false và báo lỗi nếu không hợp lệ
	private boolean kiemTraDuLieu(boolean laThem) {
		String ten = txtTenKhachHang.getText().trim();
		String sdt = txtSoDienThoai.getText().trim();
		String email = txtEmail.getText().trim();

		if (ten.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Tên khách hàng không được rỗng");
			txtTenKhachHang.requestFocus();
			return false;
		}
		if (!sdt.matches("0\\d{9}")) {
			JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0");
			txtSoDienThoai.requestFocus();
			return false;
		}
		if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
			JOptionPane.showMessageDialog(this, "Email không hợp lệ");
			txtEmail.requestFocus();
			return false;
		}
		String maLoaiTru = laThem ? null : txtMaKhachHang.getText().trim();
		if (kh_dao.tonTaiSoDienThoai(sdt, maLoaiTru)) {
			JOptionPane.showMessageDialog(this, "Số điện thoại này đã thuộc về khách hàng khác");
			txtSoDienThoai.requestFocus();
			return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o.equals(btnTim) || o.equals(txtTim)) {
			String tuKhoa = txtTim.getText().trim();
			docDuLieuVaoTable(kh_dao.timKhachHang(tuKhoa));
		} else if (o.equals(btnLamMoi)) {
			docDuLieuVaoTable(kh_dao.getAllKhachHang());
			lamMoiForm();
		} else if (o.equals(btnThem)) {
			if (!kiemTraDuLieu(true))
				return;
			KhachHang kh = new KhachHang(kh_dao.taoMaKhachHangMoi(), txtTenKhachHang.getText().trim(),
					txtEmail.getText().trim(), txtSoDienThoai.getText().trim(), 0);
			if (kh_dao.themKhachHang(kh)) {
				JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công");
				docDuLieuVaoTable(kh_dao.getAllKhachHang());
				lamMoiForm();
			} else {
				JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại");
			}
		} else if (o.equals(btnCapNhat)) {
			if (table.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(this, "Hãy chọn một khách hàng trong bảng để cập nhật");
				return;
			}
			if (!kiemTraDuLieu(false))
				return;
			KhachHang kh = new KhachHang(txtMaKhachHang.getText().trim(), txtTenKhachHang.getText().trim(),
					txtEmail.getText().trim(), txtSoDienThoai.getText().trim(),
					Integer.parseInt(txtDiemTichLuy.getText().trim()));
			if (kh_dao.capNhatKhachHang(kh)) {
				JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công");
				docDuLieuVaoTable(kh_dao.getAllKhachHang());
				lamMoiForm();
			} else {
				JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thất bại");
			}
		}
	}

	// Chọn một dòng trong bảng thì đưa dữ liệu lên form
	@Override
	public void mouseClicked(MouseEvent e) {
		int viewRow = table.getSelectedRow();
		if (viewRow == -1)
			return;
		int row = table.convertRowIndexToModel(viewRow); // vì bảng có sắp xếp
		txtMaKhachHang.setText(tableModel.getValueAt(row, 0).toString());
		txtTenKhachHang.setText(tableModel.getValueAt(row, 1).toString());
		txtSoDienThoai.setText(tableModel.getValueAt(row, 2).toString());
		txtEmail.setText(tableModel.getValueAt(row, 3) == null ? "" : tableModel.getValueAt(row, 3).toString());
		txtDiemTichLuy.setText(tableModel.getValueAt(row, 4).toString());
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	public static void main(String[] args) {
		new KhachHang_GUI().setVisible(true);
	}
}