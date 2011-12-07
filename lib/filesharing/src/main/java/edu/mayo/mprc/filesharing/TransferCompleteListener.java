package edu.mayo.mprc.filesharing;

import java.util.EventListener;

public interface TransferCompleteListener extends EventListener {
	void transferCompleted(TransferCompleteEvent event);
}
