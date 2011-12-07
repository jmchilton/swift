package edu.mayo.mprc.utilities.exceptions;

/**
 * Utility classes dealing with exceptions.
 */
public final class ExceptionUtilities {
	private ExceptionUtilities() {

	}

	/**
	 * Return a string representation of a strack trace.  There's probably a much better way of doing this.
	 */
	public static String stringifyStackTrace(Throwable t) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement ste : t.getStackTrace()) {
			sb.append("\t");
			sb.append(ste.toString());
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Throw user-readable class cast exception.
	 *
	 * @param object        Object that was supposed to be some class.
	 * @param expectedClass The class the object was expected to be.
	 */
	public static void throwCastException(Object object, Class<?> expectedClass) {
		if (object != null && !expectedClass.isAssignableFrom(object.getClass())) {
			throw new ClassCastException("Programmer error:\n"
					+ "expected:\t" + expectedClass.getName() + "\n"
					+ "got:\t" + object.getClass());
		}
	}

	/**
	 * Throw a standard "Not implemented" runtime exception. This signifies a programmer error.
	 */
	public static void throwNotImplemented() {
		throw new UnsupportedOperationException("Programmer error: operation not implemented");
	}
}
