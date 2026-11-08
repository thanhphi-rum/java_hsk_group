package gui;
import java.awt.*;
import javax.swing.*;
/** Giao diện Swing mặc định, chỉ thiết lập khả năng đọc và thao tác bảng. */
public final class AppTheme {
 public static final Color PAGE_BG=UIManager.getColor("Panel.background");
 public static final Color ACCENT=UIManager.getColor("Button.background");
 private AppTheme() {}
 public static void install() {
  try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
  catch(Exception e) { throw new IllegalStateException(e); }
 }
 public static void style(Container parent) {
  for(Component c:parent.getComponents()) {
   if(c instanceof AbstractButton b) {
    b.setBackground(UIManager.getColor("Button.background"));
    b.setForeground(UIManager.getColor("Button.foreground"));
    b.setFont(UIManager.getFont("Button.font")); b.setFocusPainted(true);
   }
   if(c instanceof JTable t) {
    t.setRowHeight(25); t.setFont(UIManager.getFont("Table.font"));
    t.setDefaultEditor(Object.class,null); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    t.getTableHeader().setReorderingAllowed(false);
   }
   if(c instanceof Container child) style(child);
  }
 }
}
