package edu.mayo.mprc.peaks.core;

import edu.mayo.mprc.MprcException;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Class represents a PeaksOnline search parameter. PeaksOnline search parameter may
 * contain multiple values.
 */
public final class PeaksParameter implements Comparable {
	private String parameterName;
	private Object parameterValue;

	/**
	 * @param parameterName
	 * @param parameterValue Paramater value use in the http call.
	 */
	public PeaksParameter(String parameterName, Object parameterValue) {
		this.parameterName = parameterName;

		this.parameterValue = parameterValue;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public Object getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(Object parameterValue) {
		this.parameterValue = parameterValue;
	}

	public Part getHttpHeaderPart() {
		Part part = null;

		if (parameterValue instanceof File) {
			try {
				part = new FilePart(parameterName, File.class.cast(parameterValue));
			} catch (FileNotFoundException e) {
				throw new MprcException("Error creating FilePart object", e);
			}
		} else if (parameterValue instanceof String) {
			part = new StringPart(parameterName, String.class.cast(parameterValue));
		} else {
			throw new MprcException("Part value must be object of the type " + String.class.getName() + " or " + File.class.getName());
		}


		return part;
	}

	public int compareTo(Object o) {

		if (o instanceof PeaksParameter) {
			PeaksParameter peaksOnlineParameter = PeaksParameter.class.cast(o);

			return getParameterName().compareTo(peaksOnlineParameter.getParameterName());
		}

		return 1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PeaksParameter)) {
			return false;
		}

		PeaksParameter that = (PeaksParameter) o;

		if (parameterName != null ? !parameterName.equals(that.parameterName) : that.parameterName != null) {
			return false;
		}
		if (parameterValue != null ? !parameterValue.equals(that.parameterValue) : that.parameterValue != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = parameterName != null ? parameterName.hashCode() : 0;
		result = 31 * result + (parameterValue != null ? parameterValue.hashCode() : 0);
		return result;
	}
}
