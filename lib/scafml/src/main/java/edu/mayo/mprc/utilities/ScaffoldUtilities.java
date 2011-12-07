package edu.mayo.mprc.utilities;

import java.util.Map;

/**
 * @author Eric Winter
 */
public final class ScaffoldUtilities {
	private ScaffoldUtilities() {
	}

	/**
	 * replace any environment variable names with values
	 *
	 * @param rawvalue
	 * @return
	 */
	public static String resolveValue(String rawvalue, Map<String, String> environmentVariables) {
		if (rawvalue == null || rawvalue.equals("")) {
			return "";
		}
		String result = rawvalue;
		String oldresult = null;
		while (!result.equals(oldresult)) {
			oldresult = result;
			for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
				// search for the wrapped key in the string, if found replace it with value
				String wrappedKey = "${" + entry.getKey() + "}";
				int pos = result.indexOf(wrappedKey);
				if (pos != -1) {
					result = result.replace(wrappedKey, entry.getValue());
				}
			}
		}
		// also replace '&lt;' and '&gt;'
		result = result.replace("&lt;", "<");
		result = result.replace("&gt;", ">");
		return result;
	}
}
