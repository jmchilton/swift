package edu.mayo.mprc.sequest.core;

import java.io.File;

interface SequestSubmitterInterface {

	void addDtaFile(File file, boolean forced);

	int getHowManyFiles();

	void setExceptionThrown(Throwable m);

	void forceSubmit();


	SequestCallerInterface getSequestCaller();

	void setSequestCaller(SequestCallerInterface sequestCaller);
}