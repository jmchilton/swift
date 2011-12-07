package edu.mayo.mprc.sequest.core;

interface SequestSubmitterInterface {

	void addDtaFile(String fileName, boolean forced);

	int getHowManyFiles();

	void setExceptionThrown(Throwable m);

	void forceSubmit();


	SequestCallerInterface getSequestCaller();

	void setSequestCaller(SequestCallerInterface sequestCaller);
}