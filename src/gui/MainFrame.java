package gui;

import javax.swing.*;
import java.awt.*;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;

import java.sql.SQLException;

/**
 * Cửa sổ duy nhất của ứng dụng.
 * - Màn hình đăng nhập hiện đầu tiên.
 * - Sau khi đăng nhập: menu dọc bên trái + nội dung bên phải; chuyển tab chỉ thay nội dung, không mở cửa sổ mới.
 */
public class MainFrame extends JFrame {
    private static MainFrame instance;

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_APP = "APP";

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel root = new JPanel(rootLayout);
    private final JPanel content = new JPanel(new BorderLayout());
    private final Menu_GUI menu;
    private final DangNhap_GUI dangNhap;

    private TaiKhoan taiKhoan;
    private String mucHienTai;

    public MainFrame() {
        instance = this;
        setTitle("Rạp Chiếu Phim");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        menu = new Menu_GUI(this);
        dangNhap = new DangNhap_GUI(this);

        content.setBackground(AppTheme.PAGE_BG);
        JPanel app = new JPanel(new BorderLayout());
        app.add(menu, BorderLayout.WEST);
        app.add(content, BorderLayout.CENTER);

        root.add(dangNhap, CARD_LOGIN);
        root.add(app, CARD_APP);
        setContentPane(root);

        rootLayout.show(root, CARD_LOGIN);
        dangNhap.datLai();
    }

    public static MainFrame getInstance() {
        return instance;
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    /** Người đang đăng nhập có phải quản lý không. */
    public static boolean laQuanLy() {
        return instance != null && instance.taiKhoan != null && instance.taiKhoan.isQuanLy();
    }

    // ================= Đăng nhập / đăng xuất =================

    public void dangNhapThanhCong(TaiKhoan tk) {
        this.taiKhoan = tk;
        menu.setNguoiDung(tk);
        rootLayout.show(root, CARD_APP);
        mucHienTai = null;
        dieuHuong(Menu_GUI.TRANG_CHU);
    }

    public void dangXuat() {
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Đăng xuất",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        taiKhoan = null;
        mucHienTai = null;
        content.removeAll(); // bỏ dữ liệu của phiên cũ
        menu.setNguoiDung(null);
        dangNhap.datLai();
        rootLayout.show(root, CARD_LOGIN);
    }

    /** Người đang đăng nhập tự đổi mật khẩu của mình (phải nhập đúng mật khẩu cũ). */
    public void doiMatKhau() {
        if (taiKhoan == null) return;
        JPasswordField pfCu = new JPasswordField();
        JPasswordField pfMoi = new JPasswordField();
        JPasswordField pfNhapLai = new JPasswordField();
        Object[] form = {"Mật khẩu hiện tại:", pfCu, "Mật khẩu mới (tối thiểu 6 ký tự):", pfMoi,
                "Nhập lại mật khẩu mới:", pfNhapLai};
        TaiKhoanDAO dao = new TaiKhoanDAO();

        while (true) {
            int kq = JOptionPane.showConfirmDialog(this, form, "Đổi mật khẩu", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;

            String cu = new String(pfCu.getPassword());
            String moi = new String(pfMoi.getPassword());
            String loi = null;
            if (moi.length() < 6) {
                loi = "Mật khẩu mới phải có ít nhất 6 ký tự.";
            } else if (!moi.equals(new String(pfNhapLai.getPassword()))) {
                loi = "Mật khẩu nhập lại không khớp.";
            } else if (moi.equals(cu)) {
                loi = "Mật khẩu mới phải khác mật khẩu hiện tại.";
            }
            try {
                if (loi == null && dao.dangNhap(taiKhoan.getTenDangNhap(), cu) == null) {
                    loi = "Mật khẩu hiện tại không đúng.";
                }
                if (loi != null) {
                    JOptionPane.showMessageDialog(this, loi, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    pfCu.setText("");
                    continue;
                }
                dao.doiMatKhau(taiKhoan.getTenDangNhap(), moi);
                JOptionPane.showMessageDialog(this, "Đã đổi mật khẩu thành công.");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
    }

    // ================= Chuyển tab =================

    /** Chuyển sang màn hình theo tên mục ở menu. Dữ liệu được tải mới mỗi lần mở. */
    public void dieuHuong(String muc) {
        if (taiKhoan == null || muc.equals(mucHienTai)) return;
        if (Menu_GUI.laMucQuanTri(muc) && !laQuanLy()) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền truy cập chức năng này.",
                    "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
        hienThi(muc, muc, null);
    }

    /** Từ danh sách phim bấm "Đặt Vé": mở màn hình bán vé (vẫn tính là mục "Phim"). */
    public void showBanVe(String maPhim) {
        hienThi("BanVe", Menu_GUI.PHIM, maPhim);
    }

    private void hienThi(String khoa, String mucSang, String thamSo) {
        mucHienTai = khoa;
        menu.setActive(mucSang); // đổi màu menu ngay lập tức

        Cursor cu = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Để menu vẽ xong rồi mới dựng màn hình (tải dữ liệu) -> bấm là thấy phản hồi liền
        SwingUtilities.invokeLater(() -> {
            try {
                JComponent man;
                try {
                    man = taoManHinh(khoa, thamSo);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    man = manHinhLoi();
                }
                content.removeAll();
                content.add(man, BorderLayout.CENTER);
                content.revalidate();
                content.repaint();
            } finally {
                setCursor(cu);
            }
        });
    }

    /** Hiện khi không dựng được màn hình (thường do mất kết nối cơ sở dữ liệu). */
    private JComponent manHinhLoi() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AppTheme.PAGE_BG);
        JLabel l = new JLabel("<html><div style='text-align:center'>Không thể tải màn hình này.<br>"
                + "Vui lòng kiểm tra kết nối cơ sở dữ liệu rồi chọn lại mục ở menu.</div></html>");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        l.setForeground(AppTheme.ACCENT);
        p.add(l);
        return p;
    }

    private JComponent taoManHinh(String khoa, String thamSo) {
        switch (khoa) {
            case Menu_GUI.TRANG_CHU:      return new TrangChu_GUI();
            case Menu_GUI.PHIM:           return new Phim_GUI();
            case "BanVe":                 return new BanVe_GUI(thamSo);
            case Menu_GUI.HOA_DON:        return new HoaDon_GUI();
            case Menu_GUI.QL_PHIM:        return new QuanLyPhim_GUI();
            case Menu_GUI.QL_SUAT_CHIEU:  return new QuanLySuatChieu_GUI();
            case Menu_GUI.QL_KHACH_HANG:  return new QuanLyKhachHang_GUI();
            case Menu_GUI.QL_NHAN_VIEN:   return new QuanLyNhanVien_GUI();
            case Menu_GUI.QL_TAI_KHOAN:   return new QuanLyTaiKhoan_GUI();
            case Menu_GUI.THONG_KE:       return new ThongKe_GUI();
            default:                      return new TrangChu_GUI();
        }
    }

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
