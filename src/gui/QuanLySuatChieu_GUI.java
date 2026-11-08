package gui;

import com.toedter.calendar.JDateChooser;

import dao.GheDAO;
import dao.SuatChieuDAO;

import entity.Phim;
import entity.SuatChieu;

import service.Session;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLySuatChieu_GUI extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfTimKiem;
    private SuatChieuDAO suatChieuDAO = new SuatChieuDAO();
    private GheDAO gheDao = new GheDAO();

    public QuanLySuatChieu_GUI() {
        Session.requireStaff();
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Tạo menu điều hướng từ Menu_GUI
        JPanel menuPanel = new Menu_GUI(this);
        mainPanel.add(menuPanel, BorderLayout.NORTH);

        // Tạo bảng danh sách suất chiếu
        String[] columnNames = {
            "Mã Suất Chiếu", "Mã Phim", "Mã Phòng Chiếu", "Giờ Bắt Đầu", "Giờ Kết Thúc"
        };
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

    // Giao diện chỉ thu thập dữ liệu; SuatChieuService kiểm tra và lưu lịch chiếu.
    private void themMoiSuatChieu() {
        moFormLichChieu(null);
    }

    private void chinhSuaSuatChieu() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu cần sửa.");
            return;
        }
        moFormLichChieu(suatChieuDAO.laySuatChieuTheoMa(table.getValueAt(row, 0).toString()));
    }

    /** Dùng chung form thêm/sửa, tránh lặp lại cùng một đoạn xử lý. */
    private void moFormLichChieu(SuatChieu existing) {
        boolean editing = existing != null;
        JTextField id = new JTextField(editing ? existing.getMaSuatChieu() : "");
        JComboBox<String> movies = new JComboBox<>();
        JComboBox<String> rooms = new JComboBox<>();
        JDateChooser starts = new JDateChooser();
        starts.setDateFormatString("yyyy-MM-dd HH:mm:ss");
        JTextField ends = new JTextField();
        ends.setEditable(false);
        for (Phim movie : new dao.PhimDAO().layTatCaPhim()) {
            if ("Đang Chiếu".equalsIgnoreCase(movie.getTrangThai()))
                movies.addItem(movie.getMaPhim());
        }
        try {
            for (String room : new dao.LichChieuDAO().maPhongChieu()) rooms.addItem(room);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Lỗi tải phòng", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (editing) {
            movies.setSelectedItem(existing.getPhim().getMaPhim());
            rooms.setSelectedItem(existing.getPhongChieu().getMaPhongChieu());
            starts.setDate(
                    Date.from(existing.getGioBatDau().atZone(ZoneId.systemDefault()).toInstant()));
            id.setEditable(false);
            movies.setEnabled(false);
            rooms.setEnabled(false);
        }
        Runnable preview =
                () -> {
                    Phim movie = new dao.PhimDAO().layPhimTheoMa((String) movies.getSelectedItem());
                    if (movie != null && starts.getDate() != null) {
                        LocalDateTime time =
                                starts.getDate()
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                        ends.setText(
                                time.plusMinutes(movie.getThoiLuong())
                                        .format(
                                                DateTimeFormatter.ofPattern(
                                                        "yyyy-MM-dd HH:mm:ss")));
                    } else ends.setText("");
                };
        movies.addActionListener(e -> preview.run());
        starts.getDateEditor().addPropertyChangeListener("date", e -> preview.run());
        preview.run();
        Object[] fields = {
            "Mã suất chiếu:",
            id,
            "Mã phim:",
            movies,
            "Mã phòng:",
            rooms,
            "Giờ bắt đầu:",
            starts,
            "Giờ kết thúc (theo thời lượng phim):",
            ends
        };
        while (JOptionPane.showConfirmDialog(
                        this,
                        fields,
                        editing ? "Sửa suất chiếu" : "Thêm suất chiếu",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION) {
            try {
                LocalDateTime time =
                        starts.getDate() == null
                                ? null
                                : starts.getDate()
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                new service.SuatChieuService()
                        .luu(
                                id.getText().trim(),
                                (String) movies.getSelectedItem(),
                                (String) rooms.getSelectedItem(),
                                time,
                                editing ? existing.getGiaVe() : 100000,
                                editing);
                lamMoiDanhSach();
                JOptionPane.showMessageDialog(this, "Đã lưu suất chiếu.");
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this, ex.getMessage(), "Không thể lưu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void lamMoiDanhSach() {
        tableModel.setRowCount(0);
        List<SuatChieu> danhSach = suatChieuDAO.layTatCaSuatChieu();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (SuatChieu sc : danhSach) {
            tableModel.addRow(
                    new Object[] {
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
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn suất chiếu cần xóa.",
                    "Thông Báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maSuatChieu = tableModel.getValueAt(selectedRow, 0).toString();

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Xóa suất chiếu này? Chỉ xóa khi chưa phát sinh đặt vé hoặc bán vé.",
                        "Xác nhận xóa",
                        JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (suatChieuDAO.xoaSuatChieu(maSuatChieu)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Xóa suất chiếu thành công!",
                        "Thông Báo",
                        JOptionPane.INFORMATION_MESSAGE);
                lamMoiDanhSach();
            } else {
                JOptionPane.showMessageDialog(
                        this, "Xóa suất chiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
                    tableModel.addRow(
                            new Object[] {
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
        DangNhap_GUI.main(args);
    }
}
