package edu.mayo.mprc.io.data;

import edu.mayo.mprc.utilities.FileUtilities;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class creating MPRC file access objects from given URI.
 */
public final class MprcFileFactory {

	private ConcurrentHashMap<URI, MprcFile> daoMap = new ConcurrentHashMap<URI, MprcFile>();

	public MprcFileFactory() {
	}

	public MprcFile getFile(URI uri) {
		MprcFile file = new MprcFile(FileUtilities.fileFromUri(uri));
		MprcFile resultingFile = daoMap.putIfAbsent(uri, file);
		if (resultingFile == null) {
			resultingFile = file;
		}
		resultingFile.open();
		return resultingFile;
	}
}
