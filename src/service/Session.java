package service;

public final class Session {
    private Session() {}

    public static String maNhanVien;
    public static String tenNhanVien;

    public static boolean isStaff() {
        return maNhanVien != null;
    }

    public static void requireStaff() {
        if (!isStaff())
            throw new SecurityException("Chức năng này dành cho nhân viên đã đăng nhập.");
    }

    public static void clear() {
        maNhanVien = null;
        tenNhanVien = null;
    }
}
