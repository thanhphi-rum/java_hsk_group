package gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Chọn ảnh cục bộ; chỉ sao chép vào project khi người dùng lưu phim. */
public class PosterPicker extends JPanel {
    private final JTextField path = new JTextField(20);
    private final JLabel preview = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
    private BufferedImage selectedImage;

    public PosterPicker(String oldPath, boolean readOnly) {
        super(new BorderLayout(8, 8));
        path.setText(oldPath == null ? "" : oldPath);
        path.setEditable(false);
        preview.setPreferredSize(new Dimension(160, 220));
        add(path, BorderLayout.NORTH);
        add(preview, BorderLayout.CENTER);
        if (!path.getText().isBlank()) {
            try { showImage(ImageIO.read(util.ProjectPaths.resolve(path.getText()).toFile())); }
            catch (IOException | RuntimeException ex) { preview.setText("Không đọc được ảnh"); }
        }
        if (!readOnly) {
            JPanel buttons = new JPanel();
            JButton choose = new JButton("Chọn ảnh...");
            choose.addActionListener(e -> chooseImage());
            JButton clear = new JButton("Bỏ ảnh");
            clear.addActionListener(e -> {
                selectedImage = null; path.setText("");
                preview.setIcon(null); preview.setText("Chưa có ảnh");
            });
            buttons.add(choose); buttons.add(clear); add(buttons, BorderLayout.SOUTH);
        }
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh JPG, PNG, GIF", "jpg", "jpeg", "png", "gif"));
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            if (Files.size(chooser.getSelectedFile().toPath()) > 10 * 1024 * 1024)
                throw new IOException("Ảnh không được lớn hơn 10 MB.");
            BufferedImage image = ImageIO.read(chooser.getSelectedFile());
            if (image == null) throw new IOException("Tệp không phải ảnh hợp lệ.");
            selectedImage = image;
            path.setText(chooser.getSelectedFile().getAbsolutePath());
            showImage(image);
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không mở được ảnh", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showImage(BufferedImage image) {
        if (image == null) { preview.setText("Không đọc được ảnh"); return; }
        double scale = Math.min(160.0 / image.getWidth(), 220.0 / image.getHeight());
        preview.setIcon(new ImageIcon(image.getScaledInstance(Math.max(1, (int)(image.getWidth()*scale)),
                Math.max(1, (int)(image.getHeight()*scale)), Image.SCALE_SMOOTH)));
        preview.setText(null);
    }

    public String value() { return path.getText().trim(); }

    /** Trả đường dẫn tương đối; giữ nguyên ảnh cũ nếu không chọn ảnh mới. */
    public String saveImage() throws IOException {
        if (selectedImage == null) return value().isEmpty() ? null : value();
        String relative = "img/posters/" + UUID.randomUUID() + ".png";
        Path destination = util.ProjectPaths.resolve(relative);
        Files.createDirectories(destination.getParent());
        if (!ImageIO.write(selectedImage, "png", destination.toFile()))
            throw new IOException("Không lưu được ảnh PNG.");
        return relative;
    }

    public boolean hasNewImage() { return selectedImage != null; }
}
