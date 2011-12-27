package edu.mayo.mprc.database;

import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Storing {@link File} into database as URI of its absolute path.
 */
public final class FileType implements UserType {

	private static FileTokenToDatabaseTranslator translator;

	public FileType() {
	}

	public static void initialize(FileTokenToDatabaseTranslator translator) {
		FileType.translator = translator;
	}

	public int[] sqlTypes() {
		return new int[]{Types.VARCHAR};
	}

	public Class returnedClass() {
		return File.class;
	}

	public boolean equals(Object o, Object o1) throws HibernateException {
		if (o == o1) {
			return true;
		}
		if (o == null || o1 == null) {
			return false;
		}
		return o.equals(o1);
	}

	public int hashCode(Object o) throws HibernateException {
		return o.hashCode();
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
		String uriString = resultSet.getString(names[0]);

		if (resultSet.wasNull()) {
			return null;
		}
		if (uriString == null) {
			return null;
		}

		try {
			return assemble(uriString, null);
		} catch (Exception t) {
			throw new HibernateException(t);
		}
	}

	public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index) throws HibernateException, SQLException {
		if (null == value) {
			preparedStatement.setNull(index, Types.VARCHAR);
		} else {
			preparedStatement.setString(index, translator.fileToDatabaseToken((File) value));
		}
	}

	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
			return null;
		}
		return new File(((File) o).getAbsoluteFile().toURI());
	}

	public boolean isMutable() {
		return false;
	}

	public Serializable disassemble(Object o) throws HibernateException {
		try {
			return translator.fileToDatabaseToken((File) o);
		} catch (Exception t) {
			throw new HibernateException(t);
		}
	}

	public Object assemble(Serializable serializable, Object o) throws HibernateException {
		try {
			if (!(serializable instanceof String)) {
				ExceptionUtilities.throwCastException(serializable, String.class);
				return null;
			}
			return translator.databaseTokenToFile((String) serializable);
		} catch (Exception t) {
			throw new HibernateException(t);
		}
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}
}
