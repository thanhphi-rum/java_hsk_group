package gui;

import connect.ConnectDB;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ThongKe_GUI extends JFrame {
    private JComboBox<String> cbThoiGian;
    private JTable tableChiTiet;
    private JButton btnXuatBaoCao;
    private JPanel chartPanel;

    public ThongKe_GUI() {
        setTitle("Thống Kê - Rạp Chiếu Phim");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        // Thêm menu từ Menu_GUI
        JPanel menuPanel = new Menu_GUI(this);
        add(menuPanel, BorderLayout.NORTH);

        // Panel tùy chọn thời gian thống kê
        JPanel pnlThoiGian = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlThoiGian.setBorder(BorderFactory.createTitledBorder("Tùy Chọn Thời Gian Thống Kê"));

        cbThoiGian = new JComboBox<>(new String[]{"Ngày", "Tuần", "Tháng", "Năm"});
        pnlThoiGian.add(new JLabel("Chọn Thời Gian: "));
        pnlThoiGian.add(cbThoiGian);

        JButton btnThongKe = new JButton("Thống Kê");
        pnlThoiGian.add(btnThongKe);

        add(pnlThoiGian, BorderLayout.WEST);

        // Panel biểu đồ
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Biểu Đồ Thống Kê"));
        add(chartPanel, BorderLayout.CENTER);

        // Bảng dữ liệu chi tiết
        JPanel pnlChiTiet = new JPanel(new BorderLayout());
        pnlChiTiet.setBorder(BorderFactory.createTitledBorder("Dữ Liệu Chi Tiết"));

        tableChiTiet = new JTable(new DefaultTableModel(new String[]{"Thời Gian", "Tên Phim/Suất Chiếu", "Doanh Thu/Số Khách"}, 0));
        pnlChiTiet.add(new JScrollPane(tableChiTiet), BorderLayout.CENTER);

        add(pnlChiTiet, BorderLayout.EAST);

        // Nút Xuất Báo Cáo
        btnXuatBaoCao = new JButton("Xuất Báo Cáo");
        add(btnXuatBaoCao, BorderLayout.SOUTH);

        // Thêm sự kiện
        btnThongKe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String thoiGian = (String) cbThoiGian.getSelectedItem();
                if (thoiGian != null) {
                    loadThongKe(thoiGian);
                }
            }
        });

        btnXuatBaoCao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(ThongKe_GUI.this, "Báo cáo đã được xuất!");
            }
        });

        setVisible(true);
    }

    private void loadThongKe(String thoiGian) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        DefaultTableModel model = (DefaultTableModel) tableChiTiet.getModel();
        model.setRowCount(0);

        String sql = "";
        switch (thoiGian) {
            case "Ngày":
                sql = "SELECT NgayLap, SUM(TongTien) AS DoanhThu FROM HoaDon GROUP BY NgayLap ORDER BY NgayLap";
                break;
            case "Tuần":
                sql = "SELECT WEEK(NgayLap) AS Tuan, SUM(TongTien) AS DoanhThu FROM HoaDon GROUP BY Tuan ORDER BY Tuan";
                break;
            case "Tháng":
                sql = "SELECT MONTH(NgayLap) AS Thang, SUM(TongTien) AS DoanhThu FROM HoaDon GROUP BY Thang ORDER BY Thang";
                break;
            case "Năm":
                sql = "SELECT YEAR(NgayLap) AS Nam, SUM(TongTien) AS DoanhThu FROM HoaDon GROUP BY Nam ORDER BY Nam";
                break;
        }

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString(1);
                double doanhThu = rs.getDouble(2);
                String doanhThuFormatted = String.format("%,.0f VNĐ", doanhThu); 
                dataset.addValue(doanhThu, "Doanh Thu", label);
                model.addRow(new Object[]{label, "", doanhThuFormatted});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart barChart = ChartFactory.createBarChart("Thống Kê Doanh Thu", "Thời Gian", "Doanh Thu", dataset);
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 153, 255)); // Màu xanh dương
        ChartPanel chart = new ChartPanel(barChart);
        chartPanel.removeAll();
        chartPanel.add(chart, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ThongKe_GUI::new);
    }
}
