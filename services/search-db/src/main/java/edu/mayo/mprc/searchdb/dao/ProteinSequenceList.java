package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableListBase;
import edu.mayo.mprc.fastadb.ProteinSequence;

import java.util.Collection;

/**
 * A list of protein sequences.
 *
 * @author Roman Zenka
 */
public final class ProteinSequenceList extends PersistableListBase<ProteinSequence> {
    public ProteinSequenceList() {
    }

    public ProteinSequenceList(int initialCapacity) {
        super(initialCapacity);
    }

    public ProteinSequenceList(Collection<ProteinSequence> items) {
        super(items);
    }
}
