package edu.mayo.mprc.sequest.core;

interface IonsModellerInterface {
	void processLine(String line);

	void setSequestSubmitter(SequestSubmitterInterface submitter);

	void setWorkingDir(String name);

	void setMgfFileName(String name);

	void setOutFilePrefix(String name);

	void forceSubmit();

	void processLine(char[] buffer, int pos, int lineLen);
}
