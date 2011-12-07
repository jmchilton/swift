package edu.mayo.mprc.database;

import org.hibernate.AssertionFailure;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.util.StringHelper;

public final class SwiftDatabaseNamingStrategy extends ImprovedNamingStrategy {
	private static final long serialVersionUID = 20101221L;

	public SwiftDatabaseNamingStrategy() {
	}

	@Override
	public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
		String header = propertyName != null ? StringHelper.unqualify(propertyName) : propertyTableName;
		if (header == null) {
			throw new AssertionFailure("NamingStrategy not properly filled");
		}
		return "fk_" + columnName(header);
	}
}
