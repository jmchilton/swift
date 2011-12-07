package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("score")
public final class Score {
	@XStreamAlias("type")
	@XStreamAsAttribute
	private String type;

	@XStreamAlias("value")
	@XStreamAsAttribute
	private double value;

	public Score() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
