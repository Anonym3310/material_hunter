package mirivan;

public class TransparentQ {

	public static String p2c(String defcolor, int percent) {
        String alpha;
        alpha = Integer.toHexString(Math.round(255 * percent / 100));
        alpha = (alpha.length() < 2 ? "0" : "") + alpha;

		return "#" + alpha + defcolor;
	}
}