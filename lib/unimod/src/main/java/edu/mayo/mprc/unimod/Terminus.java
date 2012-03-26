package edu.mayo.mprc.unimod;

import edu.mayo.mprc.MprcException;

/**
 * Which terminus is the modification specific to.
 */
public enum Terminus {
	Cterm('C'),
	Nterm('N'),
	/**
	 * Anywhere, not only at the terminus.
	 */
	Anywhere('*');

	private char code;

	Terminus(final char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}

	public static Terminus getForCode(final String code) {
		for (final Terminus t : values()) {
			if (String.valueOf(t.getCode()).equals(code)) {
				return t;
			}
		}
		throw new MprcException("Unsupported terminus code: " + code);
	}
}
