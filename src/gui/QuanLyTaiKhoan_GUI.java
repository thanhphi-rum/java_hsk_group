package gui;

import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Quản lý tài khoản đăng nhập (chỉ quản lý được vào). */
public class QuanLyTaiKhoan_GUI extends JPanel {
    private static final int MK_TOI_THIEU = 6;

    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<TaiKhoan> danhSach = new ArrayList<>();

    public QuanLyTaiKhoan_GUI() {
        setLayout(new BorderLayout());

        String[] cot = {"Tên Đăng Nhập", "Mã Nhân Viên", "Tên Nhân Viên", "Chức Vụ", "Trạng Thái"};
        tableModel = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Tài khoản bị khóa hiện chữ đỏ để dễ nhận biết
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setForeground(TaiKhoanDAO.KHOA.equals(v) ? AppTheme.DANGER : new Color(0x15803D));
                }
                return comp;
            }
        });

        JPanel dieuKhien = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        dieuKhien.add(nut("Thêm Tài Khoản", e -> themTaiKhoan()));
        dieuKhien.add(nut("Đặt Lại Mật Khẩu", e -> datLaiMatKhau()));
        dieuKhien.add(nut("Khóa / Mở Khóa", e -> khoaMoKhoa()));
        dieuKhien.add(nut("Xóa", e -> xoaTaiKhoan()));
        dieuKhien.add(nut("Làm Mới", e -> taiDanhSach()));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(dieuKhien, BorderLayout.SOUTH);

        taiDanhSach();
    }

    private JButton nut(String text, java.awt.event.ActionListener l) {
        JButton b = new JButton(text);
        b.addActionListener(l);
        return b;
    }

    // ================= Dữ liệu =================

    private void taiDanhSach() {
        try {
            danhSach = taiKhoanDAO.layTatCa();
        } catch (SQLException e) {
            baoLoiCSDL(e);
            return;
        }
        tableModel.setRowCount(0);
        for (TaiKhoan tk : danhSach) {
            tableModel.addRow(new Object[]{tk.getTenDangNhap(), tk.getNhanVien().getMaNhanVien(),
                    tk.getNhanVien().getTenNhanVien(), tk.getNhanVien().getChucVu(), tk.getTrangThai()});
        }
    }

    private TaiKhoan dongDangChon() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản trong bảng.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return danhSach.get(r);
    }

    private boolean laChinhMinh(TaiKhoan tk) {
        TaiKhoan toi = MainFrame.getInstance().getTaiKhoan();
        return toi != null && toi.getTenDangNhap().equalsIgnoreCase(tk.getTenDangNhap());
    }

    private void baoLoiCSDL(SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /** Không cho khóa/xóa người quản lý đang hoạt động cuối cùng (tránh tự khóa hệ thống). */
    private boolean laQuanLyHoatDongCuoiCung(TaiKhoan tk) throws SQLException {
        return tk.isQuanLy() && tk.isHoatDong() && taiKhoanDAO.demQuanLyHoatDong() <= 1;
    }

    // ================= Chức năng =================

    private void themTaiKhoan() {
        List<NhanVien> chuaCo;
        try {
            chuaCo = taiKhoanDAO.layNhanVienChuaCoTaiKhoan();
        } catch (SQLException e) {
            baoLoiCSDL(e);
            return;
        }
        if (chuaCo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tất cả nhân viên đều đã có tài khoản.\n"
                    + "Hãy thêm nhân viên mới ở mục Quản lý Nhân Viên trước.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> cbNhanVien = new JComboBox<>();
        for (NhanVien nv : chuaCo) {
            cbNhanVien.addItem(nv.getMaNhanVien() + " - " + nv.getTenNhanVien() + " (" + nv.getChucVu() + ")");
        }
        JTextField tfTen = new JTextField(chuaCo.get(0).getMaNhanVien().toLowerCase());
        cbNhanVien.addActionListener(e -> {
            int i = cbNhanVien.getSelectedIndex();
            if (i >= 0) tfTen.setText(chuaCo.get(i).getMaNhanVien().toLowerCase());
        });
        JPasswordField pfMk = new JPasswordField();
        JPasswordField pfNhapLai = new JPasswordField();

        Object[] form = {"Nhân viên:", cbNhanVien, "Tên đăng nhập:", tfTen,
                "Mật khẩu (tối thiểu " + MK_TOI_THIEU + " ký tự):", pfMk, "Nhập lại mật khẩu:", pfNhapLai};

        while (true) {
            int kq = JOptionPane.showConfirmDialog(this, form, "Thêm Tài Khoản", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;

            String ten = tfTen.getText().trim();
            String mk = new String(pfMk.getPassword());
            String loi = null;
            if (!ten.matches("[A-Za-z0-9_.]{3,30}")) {
                loi = "Tên đăng nhập gồm 3-30 ký tự: chữ không dấu, số, dấu _ hoặc dấu chấm.";
            } else if (mk.length() < MK_TOI_THIEU) {
                loi = "Mật khẩu phải có ít nhất " + MK_TOI_THIEU + " ký tự.";
            } else if (!mk.equals(new String(pfNhapLai.getPassword()))) {
                loi = "Mật khẩu nhập lại không khớp.";
            }
            try {
                if (loi == null && taiKhoanDAO.tonTai(ten)) {
                    loi = "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.";
                }
                if (loi != null) {
                    JOptionPane.showMessageDialog(this, loi, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                taiKhoanDAO.them(ten, mk, chuaCo.get(cbNhanVien.getSelectedIndex()).getMaNhanVien());
                JOptionPane.showMessageDialog(this, "Đã tạo tài khoản \"" + ten + "\".");
                taiDanhSach();
                return;
            } catch (SQLException e) {
                baoLoiCSDL(e);
                return;
            }
        }
    }

    private void datLaiMatKhau() {
        TaiKhoan tk = dongDangChon();
        if (tk == null) return;

        JPasswordField pfMk = new JPasswordField();
        JPasswordField pfNhapLai = new JPasswordField();
        Object[] form = {"Mật khẩu mới cho \"" + tk.getTenDangNhap() + "\" (tối thiểu " + MK_TOI_THIEU + " ký tự):",
                pfMk, "Nhập lại mật khẩu:", pfNhapLai};

        while (true) {
            int kq = JOptionPane.showConfirmDialog(this, form, "Đặt Lại Mật Khẩu", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;

            String mk = new String(pfMk.getPassword());
            if (mk.length() < MK_TOI_THIEU) {
                JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất " + MK_TOI_THIEU + " ký tự.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!mk.equals(new String(pfNhapLai.getPassword()))) {
                JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            try {
                taiKhoanDAO.doiMatKhau(tk.getTenDangNhap(), mk);
                JOptionPane.showMessageDialog(this, "Đã đặt lại mật khẩu cho \"" + tk.getTenDangNhap() + "\".");
            } catch (SQLException e) {
                baoLoiCSDL(e);
            }
            return;
        }
    }

    private void khoaMoKhoa() {
        TaiKhoan tk = dongDangChon();
        if (tk == null) return;
        try {
            if (tk.isHoatDong()) {
                if (laChinhMinh(tk)) {
                    JOptionPane.showMessageDialog(this, "Bạn không thể tự khóa tài khoản của mình.", "Không thực hiện được",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (laQuanLyHoatDongCuoiCung(tk)) {
                    JOptionPane.showMessageDialog(this, "Đây là tài khoản quản lý đang hoạt động cuối cùng, không thể khóa.",
                            "Không thực hiện được", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int ok = JOptionPane.showConfirmDialog(this, "Khóa tài khoản \"" + tk.getTenDangNhap() + "\"?\n"
                        + "Người này sẽ không đăng nhập được cho đến khi mở khóa.", "Khóa tài khoản",
                        JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) return;
                taiKhoanDAO.datTrangThai(tk.getTenDangNhap(), TaiKhoanDAO.KHOA);
            } else {
                taiKhoanDAO.datTrangThai(tk.getTenDangNhap(), TaiKhoanDAO.HOAT_DONG);
            }
            taiDanhSach();
        } catch (SQLException e) {
            baoLoiCSDL(e);
        }
    }

    private void xoaTaiKhoan() {
        TaiKhoan tk = dongDangChon();
        if (tk == null) return;
        try {
            if (laChinhMinh(tk)) {
                JOptionPane.showMessageDialog(this, "Bạn không thể xóa tài khoản đang đăng nhập.", "Không thực hiện được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (laQuanLyHoatDongCuoiCung(tk)) {
                JOptionPane.showMessageDialog(this, "Đây là tài khoản quản lý đang hoạt động cuối cùng, không thể xóa.",
                        "Không thực hiện được", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this, "Xóa tài khoản \"" + tk.getTenDangNhap() + "\"?\n"
                    + "Nhân viên " + tk.getNhanVien().getTenNhanVien() + " sẽ không còn đăng nhập được.",
                    "Xóa tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;
            taiKhoanDAO.xoa(tk.getTenDangNhap());
            taiDanhSach();
        } catch (SQLException e) {
            baoLoiCSDL(e);
        }
    }
}

