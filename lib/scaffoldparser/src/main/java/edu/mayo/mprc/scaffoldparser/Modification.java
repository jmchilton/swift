package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("Modification")
public final class Modification {

	@XStreamAlias("name")
	@XStreamAsAttribute
	private String name;

	@XStreamAlias("location")
	@XStreamAsAttribute
	private int location;

	public Modification() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}
}
