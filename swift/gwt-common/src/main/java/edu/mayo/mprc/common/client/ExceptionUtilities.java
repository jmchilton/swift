package edu.mayo.mprc.common.client;

public final class ExceptionUtilities {

	private ExceptionUtilities() {
	}

	/**
	 * Throw user-readable class cast exception.
	 *
	 * @param object        Object that was supposed to be some class.
	 * @param expectedClass The class the object was expected to be.
	 */
	public static void throwCastException(Object object, Class<?> expectedClass) {
		if (object != null) {
			throw new ClassCastException("Programmer error:\n"
					+ "expected:\t" + expectedClass.getName() + "\n"
					+ "got:\t" + object.getClass());
		}
	}
}
