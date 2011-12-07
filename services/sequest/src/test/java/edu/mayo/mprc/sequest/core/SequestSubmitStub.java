package edu.mayo.mprc.sequest.core;


import java.util.ArrayList;
import java.util.List;

public final class SequestSubmitStub implements SequestSubmitterInterface {
	private List<String> dtas;
	private SequestCallerInterface sequestCaller;

	SequestSubmitStub(long maxLineLength, long maxChunkSize, int maxDtaFiles, String paramsFileName, String workingDir, String tarName) {
		dtas = new ArrayList<String>();
	}

	public void addDtaFile(String fileName, boolean forced) {
		dtas.add(fileName);
	}

	public int getHowManyFiles() {
		return dtas.size();
	}

	public void setExceptionThrown(Throwable m) {

	}

	public void forceSubmit() {

	}

	public SequestCallerInterface getSequestCaller() {
		return sequestCaller;
	}

	public void setSequestCaller(SequestCallerInterface sequestCaller) {
		this.sequestCaller = sequestCaller;
	}
}
