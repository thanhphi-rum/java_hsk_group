package gui;

import entity.GheBanVe;
import entity.LuaChonBanVe;

import service.BanVeService;
import service.Session;
import service.ThanhToanService;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;

/** Màn hình chỉ dựng giao diện và nhận thao tác; nghiệp vụ được gọi qua BanVeService. */
public class BanVe_GUI extends JPanel {
    private final BanVeService service = new BanVeService();
    private final boolean staff = Session.isStaff();
    private final JComboBox<LuaChonBanVe> movies = new JComboBox<>();
    private final JComboBox<LuaChonBanVe> shows = new JComboBox<>();
    private final JComboBox<LuaChonBanVe> customers = new JComboBox<>();
    private final JComboBox<LuaChonBanVe> promotions = new JComboBox<>();
    private final JPanel seatsPanel = new JPanel();
    private final JTextArea summary = new JTextArea(12, 24);
    private final JButton print = new JButton("Xuất PDF hóa đơn vừa lưu");
    // Lưu mã ghế riêng với tên hiển thị; không suy ra lựa chọn từ màu nút.
    private final Map<String, String> selected = new LinkedHashMap<>();
    private boolean loading;
    private entity.DatCho lastBooking;
    private String lastInvoice;

    public BanVe_GUI(String movieId) {
        super(new BorderLayout(12, 12));
        Session.requireStaff();
        add(createFilters(), BorderLayout.NORTH);
        add(createSeatArea(), BorderLayout.CENTER);
        add(createPaymentArea(), BorderLayout.EAST);
        loadFilters(movieId);
        registerEvents();
        loadShows();
    }

    // ===== 1. DỰNG GIAO DIỆN: không truy vấn SQL tại đây =====
    private JPanel createFilters() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(
                BorderFactory.createTitledBorder(
                        staff ? "Thông tin bán vé" : "Chọn phim và suất chiếu"));
        addFilter(panel, "Phim", movies, 0);
        addFilter(panel, "Suất chiếu / phòng", shows, 1);
        if (staff) {
            addFilter(panel, "Khách hàng", customers, 2);
            addFilter(panel, "Khuyến mãi", promotions, 3);
        }
        return panel;
    }

    private JPanel createSeatArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        JLabel screen = new JLabel("MÀN HÌNH", SwingConstants.CENTER);
        screen.setOpaque(true);

        screen.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(screen, BorderLayout.NORTH);
        seatsPanel.setLayout(new BoxLayout(seatsPanel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(seatsPanel), BorderLayout.CENTER);
        panel.add(
                new JLabel("Nhấn ghế để chọn hoặc bỏ chọn. Ghế bị khóa không thể bán."),
                BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPaymentArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        panel.setPreferredSize(new Dimension(285, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Thông tin hóa đơn"));
        summary.setEditable(false);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setMargin(new Insets(12, 12, 12, 12));
        panel.add(new JScrollPane(summary), BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(0, 1, 0, 8));
        JButton save = new JButton(staff ? "Thanh toán / Lưu hóa đơn" : "Đặt vé / Giữ chỗ");
        JButton refresh = new JButton("Bỏ chọn / Làm mới ghế");
        actions.add(save);
        if (staff) actions.add(print);
        actions.add(refresh);
        print.setEnabled(false);
        print.addActionListener(
                e -> {
                    if (lastInvoice != null) HoaDonPDF.chonNoiLuu(this, lastInvoice);
                });
        refresh.addActionListener(e -> loadSeats());
        save.addActionListener(e -> save());
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private static void addFilter(JPanel panel, String label, JComponent field, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.insets = new Insets(4, 8, 4, 12);
        constraints.anchor = GridBagConstraints.WEST;
        JLabel text = new JLabel(label);
        text.setPreferredSize(new Dimension(145, 24));
        panel.add(text, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, constraints);
    }

    private void registerEvents() {
        movies.addActionListener(
                e -> {
                    if (!loading) loadShows();
                });
        shows.addActionListener(
                e -> {
                    if (!loading) loadSeats();
                });
        promotions.addActionListener(e -> updateSummary());
        customers.addActionListener(e -> updateSummary());
    }

    // ===== 2. TẢI DỮ LIỆU: lấy DTO qua service, GUI không biết câu lệnh SQL =====
    private void loadFilters(String movieId) {
        loading = true;
        customers.addItem(new LuaChonBanVe(null, "Khách vãng lai", BigDecimal.ZERO));
        promotions.addItem(new LuaChonBanVe(null, "Không áp dụng", BigDecimal.ZERO));
        try {
            for (LuaChonBanVe movie : service.phimDangChieu()) {
                movies.addItem(movie);
                if (movie.id().equals(movieId)) movies.setSelectedItem(movie);
            }
            if (staff) {
                for (LuaChonBanVe customer : service.khachHang()) customers.addItem(customer);
                for (LuaChonBanVe promotion : service.khuyenMai()) promotions.addItem(promotion);
            }
        } catch (Exception ex) {
            error(ex);
        } finally {
            loading = false;
        }
    }

    /** Đổi phim: tải lại suất, sau đó xóa lựa chọn ghế của suất cũ. */
    private void loadShows() {
        loading = true;
        shows.removeAllItems();
        LuaChonBanVe movie = choice(movies);
        try {
            if (movie != null) {
                for (LuaChonBanVe show : service.suatChieu(movie.id())) shows.addItem(show);
            }
        } catch (Exception ex) {
            error(ex);
        } finally {
            loading = false;
        }
        loadSeats();
    }

    /** Đổi suất/làm mới: không giữ lại số ghế hoặc tổng tiền của lần chọn trước. */
    private void loadSeats() {
        selected.clear();
        seatsPanel.removeAll();
        LuaChonBanVe show = choice(shows);
        Map<String, JPanel> rows = new LinkedHashMap<>();
        try {
            if (show != null) {
                for (GheBanVe seat : service.gheTheoSuat(show.id())) {
                    JPanel row = rows.get(seat.dayGhe());
                    if (row == null) {
                        row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
                        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                        row.setAlignmentX(Component.LEFT_ALIGNMENT);
                        row.add(new JLabel(seat.dayGhe() + "  "));
                        rows.put(seat.dayGhe(), row);
                        seatsPanel.add(row);
                    }
                    row.add(createSeatButton(seat));
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
        if (seatsPanel.getComponentCount() == 0) {
            seatsPanel.add(
                    new JLabel(
                            show == null
                                    ? "Chưa có suất chiếu đang mở bán."
                                    : "Chưa có ghế khả dụng cho suất này."));
        }
        seatsPanel.revalidate();
        seatsPanel.repaint();
        updateSummary();
    }

    private JToggleButton createSeatButton(GheBanVe seat) {
        JToggleButton button = new JToggleButton(seat.location());
        button.setPreferredSize(new Dimension(55, 38));
        button.setMargin(new Insets(2, 2, 2, 2));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setToolTipText(seat.location() + " - " + seat.status());
        button.setEnabled(seat.conTrong());

        button.addActionListener(
                e -> {
                    if (button.isSelected()) selected.put(seat.id(), seat.location());
                    else selected.remove(seat.id());

                    updateSummary();
                });
        return button;
    }

    // ===== 3. TẠM TÍNH: chỉ hiển thị, chưa ghi hóa đơn hay doanh thu =====
    private BigDecimal total() {
        LuaChonBanVe show = choice(shows);
        LuaChonBanVe promotion = choice(promotions);
        if (show == null || selected.isEmpty()) return BigDecimal.ZERO;
        return ThanhToanService.tinhTongTien(
                show.amount(),
                selected.size(),
                promotion == null ? BigDecimal.ZERO : promotion.amount());
    }

    private void updateSummary() {
        LuaChonBanVe movie = choice(movies);
        LuaChonBanVe show = choice(shows);
        StringBuilder text = new StringBuilder();
        text.append("Phim: ").append(movie == null ? "—" : movie.label());
        text.append("\n\nSuất: ").append(show == null ? "—" : show.label());
        text.append("\n\nGhế: ").append(String.join(", ", selected.values()));
        text.append("\nSố vé: ").append(selected.size());
        text.append("\n\nĐơn giá: ")
                .append(show == null ? "—" : String.format("%,.0f đ", show.amount()));
        text.append("\n\nTổng tiền: ").append(String.format("%,.0f đ", total()));
        if (staff) text.append("\n\nThanh toán tiền mặt\nNhân viên: ").append(Session.tenNhanVien);
        else
            text.append(
                    "\n\n"
                        + "Không cần tài khoản. Giữ chỗ tối đa 10 phút; mang mã đến quầy thanh toán"
                        + " trước hạn.");
        if (lastBooking != null)
            text.append("\n\nMã vừa đặt: ")
                    .append(lastBooking.id())
                    .append("\nGiữ đến: ")
                    .append(lastBooking.expires());
        if (lastInvoice != null) text.append("\n\nHóa đơn vừa lưu: ").append(lastInvoice);
        summary.setText(text.toString());
    }

    // ===== 4. HÀNH ĐỘNG: tách đặt chỗ của khách và thu tiền của nhân viên =====
    private void save() {
        LuaChonBanVe show = choice(shows);
        if (show == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu và ít nhất một ghế.");
            return;
        }
        if (staff) payAtCounter(show);
        else reserveForGuest(show);
    }

    private void reserveForGuest(LuaChonBanVe show) {
        if (JOptionPane.showConfirmDialog(
                        this,
                        "Giữ "
                                + selected.size()
                                + " ghế tối đa 10 phút và thanh toán tại quầy trước hạn?",
                        "Đặt vé không cần tài khoản",
                        JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;
        try {
            lastBooking = service.datCho(show.id(), new ArrayList<>(selected.keySet()));
            loadSeats();
            JTextArea receipt =
                    new JTextArea(
                            lastBooking.description()
                                    + "\n\n"
                                    + "Vui lòng lưu mã và đến quầy trước hạn.\n"
                                    + "Đây là phiếu giữ chỗ, chưa phải vé đã thanh toán.");
            receipt.setEditable(false);
            receipt.setRows(14);
            receipt.setColumns(42);
            receipt.setCaretPosition(0);
            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(receipt),
                    "Đặt vé thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            error(ex);
            loadSeats();
        }
    }

    /** Hủy hộp thoại/thiếu tiền: dừng trước khi gọi service ghi dữ liệu. */
    private void payAtCounter(LuaChonBanVe show) {
        BigDecimal approvedTotal = total();
        if (!ThanhToanDialog.xacNhan(this, approvedTotal)) return;
        LuaChonBanVe customer = choice(customers);
        LuaChonBanVe promotion = choice(promotions);
        try {
            lastInvoice =
                    service.banTaiQuay(
                            show.id(),
                            new ArrayList<>(selected.keySet()),
                            customer == null ? null : customer.id(),
                            promotion == null ? null : promotion.id(),
                            approvedTotal);
            print.setEnabled(true);
            loadSeats();
            if (JOptionPane.showConfirmDialog(
                            this,
                            "Đã lưu hóa đơn " + lastInvoice + ". Xuất PDF ngay?",
                            "Thành công",
                            JOptionPane.YES_NO_OPTION)
                    == JOptionPane.YES_OPTION) HoaDonPDF.chonNoiLuu(this, lastInvoice);
        } catch (Exception ex) {
            error(ex);
            loadSeats();
        }
    }

    private static LuaChonBanVe choice(JComboBox<LuaChonBanVe> box) {
        return (LuaChonBanVe) box.getSelectedItem();
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(
                this, ex.getMessage(), "Không thể xử lý", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        Main_GUI.main(args);
    }
}