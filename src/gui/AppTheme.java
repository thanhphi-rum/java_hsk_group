package gui;


import javax.swing.*;

import com.formdev.flatlaf.FlatLightLaf;

import java.awt.Color;
import java.awt.Font;

/**
 * Cài giao diện hiện đại (FlatLaf) cho toàn bộ ứng dụng.
 * Gọi AppTheme.install() ở dòng đầu tiên của main(), trước khi tạo bất kỳ JFrame nào.
 */
public class AppTheme {
    public static final Color NAV_BG      = new Color(0x1F2A44); // nền thanh menu
    public static final Color NAV_HOVER   = new Color(0x2D3B5E);
    public static final Color ACCENT      = new Color(0xE50914); // màu nhấn (trang đang mở, nút chính)
    public static final Color ACCENT_DARK = new Color(0xB8070F);
    public static final Color PAGE_BG     = new Color(0xF3F4F6); // nền trang
    public static final Color CARD_BORDER = new Color(0xE5E7EB);

    public static void install() {
        try {
            // Các thuộc tính này phải đặt TRƯỚC khi setup()
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.alternateRowColor", new Color(0xF9FAFB));
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Component.focusColor", ACCENT);
            UIManager.put("Button.default.background", ACCENT);
            UIManager.put("Button.default.foreground", Color.WHITE);
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
