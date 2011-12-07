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
	public void setCreation(Change creation) {
		this.creation = creation;
	}

	@Override
	public Change getDeletion() {
		return deletion;
	}

	@Override
	public void setDeletion(Change deletion) {
		this.deletion = deletion;
	}
}
