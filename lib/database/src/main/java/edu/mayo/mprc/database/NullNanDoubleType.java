package edu.mayo.mprc.database;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Will map Double.NaN as NULL, and NULL as Double.NaN.
 * <p/>
 * This is necessary as databases like MySQL do not handle NaN properly.
 * <p/>
 * After http://stackoverflow.com/questions/7046705/can-hibernate-map-null-to-the-not-a-number-float
 *
 * @author Roman Zenka
 */
public class NullNanDoubleType implements UserType {

	@Override
	public int[] sqlTypes() {
		return new int[]{Types.DOUBLE};
	}

	@Override
	public Class returnedClass() {
		return Float.class;
	}

	@Override
	public boolean equals(final Object x, final Object y) throws HibernateException {
		return (x == y) || (x != null && x.equals(y));
	}

	@Override
	public int hashCode(final Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException, SQLException {
		double value = rs.getDouble(names[0]);
		if (rs.wasNull()) {
			value = Double.NaN;
		}
		return value;
	}

	@Override
	public void nullSafeSet(final PreparedStatement ps, final Object value, final int index) throws HibernateException, SQLException {
		if (value == null || Double.isNaN((Double) value)) {
			ps.setNull(index, Types.DOUBLE);
		} else {
			ps.setDouble(index, (Double) value);
		}
	}

	@Override
	public Object deepCopy(final Object value) throws HibernateException {
		//returning value should be OK since doubles are immutable
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(final Object value) throws HibernateException {
		return (Serializable) value;
	}

	@Override
	public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
		return cached;
	}

	@Override
	public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
		return original;
	}
}
