package edu.mayo.mprc.database;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * A class for safely persisting a type safe Java Enum in a database using a VarChar field.
 * This was provided by http://www.hibernate.org/272.html maybe that link still works if you want more information.
 * <p/>
 * This class unfortunately cannot accept inner enums (inner classes) because of how Class.forName() works
 */
public final class EnumUserType implements UserType, ParameterizedType {

	private Class clazz = null;

	public void setParameterValues(Properties params) {
		String enumClassName = params.getProperty("enumClassName");
		if (enumClassName == null) {
			throw new MappingException("enumClassName parameter not specified");
		}

		try {
			this.clazz = Class.forName(enumClassName);
		} catch (java.lang.ClassNotFoundException e) {
			//try to it as if it were an inner class (aka enum) before failing all together


			throw new MappingException("enumClass " + enumClassName + " not found", e);
		}
	}

	private static final int[] SQL_TYPES = {Types.VARCHAR};

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	public Class returnedClass() {
		return clazz;
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
			throws HibernateException, SQLException {
		String name = resultSet.getString(names[0]);
		Object result = null;
		if (!resultSet.wasNull()) {
			result = Enum.valueOf(clazz, name);
		}
		return result;
	}

	public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index)
			throws HibernateException, SQLException {
		if (null == value) {
			preparedStatement.setNull(index, Types.VARCHAR);
		} else {
			preparedStatement.setString(index, ((Enum) value).name());
		}
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public boolean isMutable() {
		return false;
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		}
		if (null == x || null == y) {
			return false;
		}
		return x.equals(y);
	}
}