package gui;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Admin promotion editor with an explicit active/inactive switch and validity dates. */
public class QuanLyKhuyenMai_GUI extends JPanel {
    private final KhuyenMaiDAO dao=new KhuyenMaiDAO();
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"Mã","Tên","Giảm (%)","Bắt đầu","Kết thúc","Hiệu lực","Mô tả"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=new JTable(model);
    public QuanLyKhuyenMai_GUI(){setLayout(new BorderLayout(8,8));table.setRowHeight(30);JScrollPane sp=new JScrollPane(table);JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));JButton add=new JButton("Thêm"),edit=new JButton("Sửa"),del=new JButton("Xóa"),refresh=new JButton("Làm mới");actions.add(add);actions.add(edit);actions.add(del);actions.add(refresh);AppTheme.styleTable(table);table.getColumnModel().getColumn(5).setCellRenderer(new AppTheme.PillRenderer());
        String[] nhan={"Mã KM:","Tên CT:","Phần trăm:","Từ ngày:","Đến ngày:","Trạng thái:","Mô tả:"};
        JPanel phai=Khung.chiTiet(table,model,null,nhan,r->new String[]{String.valueOf(model.getValueAt(r,0)),String.valueOf(model.getValueAt(r,1)),model.getValueAt(r,2)+"%",String.valueOf(model.getValueAt(r,3)),String.valueOf(model.getValueAt(r,4)),String.valueOf(model.getValueAt(r,5)),String.valueOf(model.getValueAt(r,6))});
        Khung.dung(this,"Quản Lý Khuyến Mãi",actions,sp,phai);add.addActionListener(e->edit(null));edit.addActionListener(e->{int r=table.getSelectedRow();if(r>=0)edit((String)model.getValueAt(table.convertRowIndexToModel(r),0));});del.addActionListener(e->xoa());refresh.addActionListener(e->load());load();}
    private void load(){model.setRowCount(0);LocalDate today=LocalDate.now();for(KhuyenMai k:dao.layTatCaKhuyenMai()){String state=!"Hoạt động".equalsIgnoreCase(k.getTrangThai())?"Ngừng hoạt động":today.isBefore(k.getNgayBatDau())?"Chưa bắt đầu":today.isAfter(k.getNgayKetThuc())?"Hết hạn":"Hợp lệ";model.addRow(new Object[]{k.getMaKhuyenMai(),k.getTenKhuyenMai(),k.getPhanTramGiam(),k.getNgayBatDau(),k.getNgayKetThuc(),state,k.getMoTa()});}}
    private void edit(String id){KhuyenMai old=null;if(id!=null)for(KhuyenMai k:dao.layTatCaKhuyenMai())if(k.getMaKhuyenMai().equals(id))old=k;JTextField ma=new JTextField(old==null?"":old.getMaKhuyenMai()),ten=new JTextField(old==null?"":old.getTenKhuyenMai()),pct=new JTextField(old==null?"":String.valueOf(old.getPhanTramGiam())),bd=new JTextField(old==null?LocalDate.now().toString():old.getNgayBatDau().toString()),kt=new JTextField(old==null?LocalDate.now().plusDays(30).toString():old.getNgayKetThuc().toString()),mota=new JTextField(old==null?"":old.getMoTa());JComboBox<String> status=new JComboBox<>(new String[]{"Hoạt động","Ngừng hoạt động"});if(old!=null)status.setSelectedItem(old.getTrangThai());ma.setEditable(old==null);Object[] f={"Mã:",ma,"Tên:",ten,"Giảm (%):",pct,"Bắt đầu (yyyy-MM-dd):",bd,"Kết thúc (yyyy-MM-dd):",kt,"Trạng thái:",status,"Mô tả:",mota};if(JOptionPane.showConfirmDialog(this,f,old==null?"Thêm khuyến mãi":"Sửa khuyến mãi",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;try{double p=Double.parseDouble(pct.getText().trim());LocalDate a=LocalDate.parse(bd.getText().trim()),b=LocalDate.parse(kt.getText().trim());if(ma.getText().isBlank()||ten.getText().isBlank()||p<=0||p>100||b.isBefore(a))throw new IllegalArgumentException("Kiểm tra mã, tên, mức giảm và ngày áp dụng.");KhuyenMai k=new KhuyenMai(ma.getText().trim(),ten.getText().trim(),mota.getText().trim(),p,a,b);k.setTrangThai((String)status.getSelectedItem());boolean ok=old==null?dao.themKhuyenMai(k):dao.suaKhuyenMai(k);if(!ok)throw new IllegalStateException("Không lưu được khuyến mãi.");load();}catch(Exception ex){JOptionPane.showMessageDialog(this,"Dữ liệu không hợp lệ: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}}
    private void xoa(){int r=table.getSelectedRow();if(r<0)return;String id=(String)model.getValueAt(table.convertRowIndexToModel(r),0);if(JOptionPane.showConfirmDialog(this,"Xóa khuyến mãi "+id+"?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;if(!dao.xoaKhuyenMai(id))JOptionPane.showMessageDialog(this,"Không xóa được khuyến mãi đã dùng trong hóa đơn; hãy chuyển sang Ngừng hoạt động.");load();}
}
