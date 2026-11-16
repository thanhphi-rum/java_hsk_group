package gui;

import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;

import javax.swing.*;
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
    private final HintTextField tfTimKiem = new HintTextField("Nhập tên đăng nhập hoặc tên nhân viên");
    private List<TaiKhoan> danhSach = new ArrayList<>();

    public QuanLyTaiKhoan_GUI() {
        String[] cot = {"Tên Đăng Nhập", "Mã Nhân Viên", "Tên Nhân Viên", "Chức Vụ", "Trạng Thái"};
        tableModel = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(4).setCellRenderer(new AppTheme.PillRenderer());   // xanh: hoạt động, đỏ: khóa

        // Ô tìm kiếm + nút tìm sẽ lên đầu thẻ, các nút còn lại xuống cuối thẻ (do Khung.dung tách)
        JPanel dieuKhien = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        dieuKhien.add(tfTimKiem);
        dieuKhien.add(nut("Tìm Kiếm", e -> taiDanhSach()));
        dieuKhien.add(nut("Thêm", e -> themTaiKhoan()));
        dieuKhien.add(nut("Sửa", e -> suaTaiKhoan()));
        dieuKhien.add(nut("Đặt Lại Mật Khẩu", e -> datLaiMatKhau()));
        dieuKhien.add(nut("Khóa / Mở Khóa", e -> khoaMoKhoa()));
        dieuKhien.add(nut("Xóa", e -> xoaTaiKhoan()));
        dieuKhien.add(nut("Làm mới", e -> {
            tfTimKiem.setText("");
            taiDanhSach();
        }));

        String[] nhan = {"Tên đăng nhập:", "Mã NV:", "Họ tên:", "Chức vụ:", "Trạng thái:"};
        JPanel phai = Khung.chiTiet(table, tableModel, new Khung.Avatar(110), nhan, r -> new String[]{
                giaTri(r, 0), giaTri(r, 1), giaTri(r, 2), giaTri(r, 3), giaTri(r, 4)});
        Khung.dung(this, "Quản Lý Tài Khoản", dieuKhien, new JScrollPane(table), phai);

        taiDanhSach();
    }

    private String giaTri(int dong, int cot) {
        return String.valueOf(tableModel.getValueAt(dong, cot));
    }

    private JButton nut(String text, java.awt.event.ActionListener l) {
        JButton b = new JButton(text);
        b.addActionListener(l);
        return b;
    }

    // ================= Dữ liệu =================

    private void taiDanhSach() {
        List<TaiKhoan> tatCa;
        try {
            tatCa = taiKhoanDAO.layTatCa();
        } catch (SQLException e) {
            baoLoiCSDL(e);
            return;
        }
        String tuKhoa = tfTimKiem.getText().trim().toLowerCase();
        danhSach = new ArrayList<>();
        for (TaiKhoan tk : tatCa) {
            String dong = (tk.getTenDangNhap() + " " + tk.getNhanVien().getMaNhanVien() + " " + tk.getNhanVien().getTenNhanVien()).toLowerCase();
            if (dong.contains(tuKhoa)) danhSach.add(tk);
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
        return danhSach.get(table.convertRowIndexToModel(r));
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

    /** Sửa tài khoản đang chọn: nhân viên được cấp, trạng thái và (tùy chọn) mật khẩu. Tên đăng nhập không đổi. */
    private void suaTaiKhoan() {
        TaiKhoan tk = dongDangChon();
        if (tk == null) return;

        // Nhân viên chọn được: nhân viên hiện tại + các nhân viên chưa có tài khoản
        List<NhanVien> dsNhanVien = new ArrayList<>();
        dsNhanVien.add(tk.getNhanVien());
        try {
            dsNhanVien.addAll(taiKhoanDAO.layNhanVienChuaCoTaiKhoan());
        } catch (SQLException e) {
            baoLoiCSDL(e);
            return;
        }

        JComboBox<String> cbNhanVien = new JComboBox<>();
        for (NhanVien nv : dsNhanVien) {
            cbNhanVien.addItem(nv.getMaNhanVien() + " - " + nv.getTenNhanVien() + " (" + nv.getChucVu() + ")");
        }
        cbNhanVien.setSelectedIndex(0);
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{TaiKhoanDAO.HOAT_DONG, TaiKhoanDAO.KHOA});
        cbTrangThai.setSelectedItem(tk.isHoatDong() ? TaiKhoanDAO.HOAT_DONG : TaiKhoanDAO.KHOA);
        JPasswordField pfMk = new JPasswordField();
        JPasswordField pfNhapLai = new JPasswordField();

        Object[] form = {"Tên đăng nhập:", new JLabel(tk.getTenDangNhap()),
                "Nhân viên:", cbNhanVien,
                "Trạng thái:", cbTrangThai,
                "Mật khẩu mới (bỏ trống = giữ nguyên, tối thiểu " + MK_TOI_THIEU + " ký tự):", pfMk,
                "Nhập lại mật khẩu mới:", pfNhapLai};

        while (true) {
            int kq = JOptionPane.showConfirmDialog(this, form, "Sửa Tài Khoản", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;

            NhanVien nvMoi = dsNhanVien.get(cbNhanVien.getSelectedIndex());
            String ttMoi = (String) cbTrangThai.getSelectedItem();
            String mk = new String(pfMk.getPassword());
            boolean khoa = TaiKhoanDAO.KHOA.equals(ttMoi);
            boolean doiNhanVien = !nvMoi.getMaNhanVien().equals(tk.getNhanVien().getMaNhanVien());

            String loi = null;
            if (!mk.isEmpty() && mk.length() < MK_TOI_THIEU) {
                loi = "Mật khẩu phải có ít nhất " + MK_TOI_THIEU + " ký tự.";
            } else if (!mk.equals(new String(pfNhapLai.getPassword()))) {
                loi = "Mật khẩu nhập lại không khớp.";
            } else if (laChinhMinh(tk) && khoa) {
                loi = "Bạn không thể tự khóa tài khoản của mình.";
            } else if (laChinhMinh(tk) && doiNhanVien) {
                loi = "Bạn không thể đổi nhân viên của tài khoản đang đăng nhập.";
            }
            try {
                if (loi == null && laQuanLyHoatDongCuoiCung(tk)) {
                    boolean conLaQuanLy = !khoa && nvMoi.getChucVu() != null
                            && nvMoi.getChucVu().toLowerCase().contains("quản lý");
                    if (!conLaQuanLy) {
                        loi = "Đây là tài khoản quản lý đang hoạt động cuối cùng, không thể khóa hoặc chuyển sang nhân viên không phải quản lý.";
                    }
                }
                if (loi != null) {
                    JOptionPane.showMessageDialog(this, loi, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                taiKhoanDAO.capNhat(tk.getTenDangNhap(), nvMoi.getMaNhanVien(), ttMoi, mk);
                JOptionPane.showMessageDialog(this, "Đã cập nhật tài khoản \"" + tk.getTenDangNhap() + "\".");
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

