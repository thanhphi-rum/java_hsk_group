package util;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Xác định thư mục dự án khi chạy bằng Eclipse, run.cmd hoặc từ thư mục khác. */
public final class ProjectPaths {
    private ProjectPaths() {}

    public static Path root() {
        // Cho phép chỉ định rõ đường dẫn khi triển khai ở máy khác.
        String override = System.getProperty("cinema.home");
        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath().normalize();
        }

        // Eclipse đặt file .class vào bin/: đi lên để tìm đúng dự án chứa mã đang chạy.
        try {
            Path location =
                    Path.of(
                            ProjectPaths.class
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI());
            Path folder = Files.isDirectory(location) ? location : location.getParent();
            for (int level = 0; folder != null && level < 5; level++, folder = folder.getParent()) {
                if (Files.isRegularFile(folder.resolve(".project"))
                        || (Files.isDirectory(folder.resolve("src"))
                                && Files.isDirectory(folder.resolve("lib")))) {
                    return folder.toAbsolutePath().normalize();
                }
            }
        } catch (URISyntaxException | SecurityException ex) {
            // Khi môi trường không cung cấp CodeSource, dùng thư mục làm việc.
        }
        return Path.of("").toAbsolutePath().normalize();
    }

    public static Path resolve(String path) {
        Path value = Path.of(path);
        return value.isAbsolute() ? value.normalize() : root().resolve(value).normalize();
    }
}
