package gui;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Chọn ảnh rồi sao chép vào dự án để không phụ thuộc thư mục Downloads. */
public class PosterField extends JPanel {
    private final JTextField field = new JTextField(24);

    public PosterField(String current) {
        super(new BorderLayout(6, 0));
        field.setText(current == null ? "" : current);
        JButton browse = new JButton("Chọn ảnh...");
        add(field);
        add(browse, BorderLayout.EAST);
        browse.addActionListener(
                e -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileFilter(
                            new FileNameExtensionFilter("Ảnh JPG, PNG", "jpg", "jpeg", "png"));
                    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                        field.setText(chooser.getSelectedFile().getAbsolutePath());
                });
    }

    public String saveImage() throws IOException {
        String value = field.getText().trim();
        if (value.isEmpty()) return "";
        if (!value.toLowerCase(java.util.Locale.ROOT).matches(".*\\.(jpg|jpeg|png)$"))
            throw new IOException("Poster phải là tệp JPG hoặc PNG.");
        Path source = util.ProjectPaths.resolve(value);
        if (!Files.isRegularFile(source)
                || Files.size(source) > 10 * 1024 * 1024
                || ImageIO.read(source.toFile()) == null)
            throw new IOException("Chọn ảnh JPG/PNG hợp lệ, tối đa 10 MB.");
        Path folder = util.ProjectPaths.resolve("img/posters");
        Files.createDirectories(folder);
        if (source.toAbsolutePath().normalize().startsWith(folder.toAbsolutePath().normalize()))
            return value;
        String ext = value.toLowerCase().endsWith(".png") ? ".png" : ".jpg";
        Path destination = folder.resolve(UUID.randomUUID() + ext);
        Files.copy(source, destination);
        String relative = util.ProjectPaths.root().relativize(destination).toString();
        field.setText(relative);
        return relative;
    }

    public void clear() {
        field.setText("");
    }
}
