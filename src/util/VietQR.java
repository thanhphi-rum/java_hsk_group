package util;

/**
 * Tạo chuỗi VietQR (chuẩn NAPAS/EMVCo) có sẵn số tiền và nội dung, để khách quét bằng app ngân hàng.
 * >>> ĐỔI 3 HẰNG SỐ DƯỚI ĐÂY THÀNH TÀI KHOẢN NHẬN TIỀN THẬT CỦA RẠP. <<<
 */
public class VietQR {
    public static final String MA_NGAN_HANG_BIN = "970422"; // BIN ngân hàng (970422 = MB, 970436 = Vietcombank, 970415 = VietinBank, 970418 = BIDV, 970407 = Techcombank)
    public static final String TEN_NGAN_HANG    = "MB Bank";    // chỉ để hiển thị cho nhân viên đối chiếu
    public static final String SO_TAI_KHOAN     = "0000000000"; // số tài khoản nhận tiền của rạp
    public static final String TEN_CHU_TK       = "RAP CHIEU PHIM";

    public static String taoChuoi(long soTien, String noiDung) {
        String tk = tlv("00", MA_NGAN_HANG_BIN) + tlv("01", SO_TAI_KHOAN);
        String merchant = tlv("00", "A000000727") + tlv("01", tk) + tlv("02", "QRIBFTTA");
        String s = tlv("00", "01") + tlv("01", "12") + tlv("38", merchant)
                + tlv("53", "704") + tlv("54", String.valueOf(soTien)) + tlv("58", "VN")
                + tlv("62", tlv("08", boDau(noiDung)));
        s += "6304";
        return s + String.format("%04X", crc16(s));
    }

    private static String tlv(String id, String v) { return id + String.format("%02d", v.length()) + v; }

    static int crc16(String s) { // CRC-16/CCITT-FALSE
        int crc = 0xFFFF;
        for (byte b : s.getBytes(java.nio.charset.StandardCharsets.US_ASCII)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) crc = (crc & 0x8000) != 0 ? ((crc << 1) ^ 0x1021) & 0xFFFF : (crc << 1) & 0xFFFF;
        }
        return crc;
    }

    private static String boDau(String s) {
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D');
        return n.replaceAll("[^A-Za-z0-9 ]", "");
    }
}
