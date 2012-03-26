package edu.mayo.mprc.sequest.core;


import java.util.ArrayList;
import java.util.List;

public final class SequestSubmitStub implements SequestSubmitterInterface {
	private List<String> dtas;
	private SequestCallerInterface sequestCaller;

	SequestSubmitStub(final long maxLineLength, final long maxChunkSize, final int maxDtaFiles, final String paramsFileName, final String workingDir, final String tarName) {
		dtas = new ArrayList<String>();
	}

	public void addDtaFile(final String fileName, final boolean forced) {
		dtas.add(fileName);
	}

	public int getHowManyFiles() {
		return dtas.size();
	}

	public void setExceptionThrown(final Throwable m) {

	}

	public void forceSubmit() {

	}

	public SequestCallerInterface getSequestCaller() {
		return sequestCaller;
	}

	public void setSequestCaller(final SequestCallerInterface sequestCaller) {
		this.sequestCaller = sequestCaller;
	}
}
