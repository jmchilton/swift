package edu.mayo.mprc.sge;

/**
 * Listens to the change of the work packet state.
 */
public interface GridWorkPacketStateListener {
	void stateChanged(GridWorkPacket w);
}
