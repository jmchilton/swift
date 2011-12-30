package edu.mayo.mprc.dbcurator.model.curationsteps;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.dbcurator.model.CurationStep;

/**
 * To remove repetition in curation steps. Feel free to embed the class directly instead
 * inheriting.
 *
 * @author Roman Zenka
 */
public abstract class CurationStepBase extends PersistableBase implements CurationStep {
	private static final long serialVersionUID = -3173831989144837494L;

	/**
	 * the number of sequences present when this step was last run
	 */
	private Integer lastRunCompletionCount;

	public Integer getLastRunCompletionCount() {
		return lastRunCompletionCount;
	}

	public void setLastRunCompletionCount(Integer count) {
		lastRunCompletionCount = count;
	}
}
