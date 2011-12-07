package edu.mayo.mprc.chem;

public final class ElementCountPair {
	private Element element;
	private double count;

	public ElementCountPair(Element element, double count) {
		this.element = element;
		this.count = count;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public void setCount(double count) {
		this.count = count;
	}

	public Element getElement() {
		return element;
	}

	public double getCount() {
		return count;
	}
}
