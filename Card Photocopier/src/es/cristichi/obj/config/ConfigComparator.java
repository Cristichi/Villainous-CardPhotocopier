package es.cristichi.obj.config;

import java.util.Comparator;

public class ConfigComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		int val1 = Integer.MAX_VALUE;
		int val2 = Integer.MAX_VALUE;
		try {
			val1 = ConfigValue.getValueOfKey(o1).ordinal();
		} catch (Exception e) {
		}
		try {
			val2 = ConfigValue.getValueOfKey(o2).ordinal();
		} catch (Exception e) {
		}
		return Integer.compare(val1, val2);
	}

}
