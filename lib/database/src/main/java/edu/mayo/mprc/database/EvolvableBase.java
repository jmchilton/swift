package edu.mayo.mprc.database;

/**
 * Base class for easier implementation of Evolvable interface.
 */
public abstract class EvolvableBase extends PersistableBase implements Evolvable {
	private Change creation;
	private Change deletion;

	@Override
	public Change getCreation() {
		return creation;
	}

	@Override
	public void setCreation(final Change creation) {
		this.creation = creation;
	}

	@Override
	public Change getDeletion() {
		return deletion;
	}

	@Override
	public void setDeletion(final Change deletion) {
		this.deletion = deletion;
	}
}
