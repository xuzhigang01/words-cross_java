package code;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	/**
	 * Splits the string, and trim sub strings
	 */
	public static List<String> splitStr(String str, String regex) {
		if (str == null) {
			return null;
		}
		String[] array = str.split(regex);
		
		List<String> list = new ArrayList<>();
		for (String s : array) {
			s = s.trim();
			if (s.length() > 0) {
				list.add(s);
			}
		}
		return list;
	}
	
	public static class Pair<L, R> implements Serializable {
		private static final long serialVersionUID = -8631342233963197956L;
		
		private L left;
		private R right;
		
		public static <L, R> Pair<L, R> of(L left, R right) {
			Pair<L, R> pair = new Pair<>();
			pair.left = left;
			pair.right = right;
			return pair;
		}
		
		public L getLeft() {
			return left;
		}
		
		public R getRight() {
			return right;
		}
	}
}
