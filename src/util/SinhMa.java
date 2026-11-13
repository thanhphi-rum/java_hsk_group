package util;

public class SinhMa {
	/**
	 * Sinh mã kế tiếp từ mã lớn nhất hiện có.
	 * Ví dụ: maTiepTheo("VE000012", "VE", 6) -> "VE000013"
	 *        maTiepTheo(null, "VE", 6)        -> "VE000001"
	 */
	public static String maTiepTheo(String maLonNhat, String tienTo, int soChuSo) {
		int so = 0;
		if (maLonNhat != null && maLonNhat.length() > tienTo.length()) {
			try {
				so = Integer.parseInt(maLonNhat.substring(tienTo.length()));
			} catch (NumberFormatException e) {
				so = 0;
			}
		}
		return tienTo + String.format("%0" + soChuSo + "d", so + 1);
	}
}
