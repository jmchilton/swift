package edu.mayo.mprc.dbcurator.server;

import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;

import java.io.File;

/**
 * Static configuration for the db-curator. Has to be initialized prior to usage.
 * <p/>
 * TODO: Ideally this would be done via dependency injection.
 */
public final class CurationWebContext {
	private static CurationDao curationDao;
	private static File fastaFolder;
	private static File fastaUploadFolder;
	private static File fastaArchiveFolder;
	private static File localTempFolder;

	private CurationWebContext() {
	}

	public static void initialize(final CurationDao curationDao, final File fastaFolder, final File fastaUploadFolder, final File fastaArchiveFolder, final File localTempFolder) {
		CurationWebContext.curationDao = curationDao;
		CurationWebContext.fastaFolder = fastaFolder;
		CurationWebContext.fastaUploadFolder = fastaUploadFolder;
		CurationWebContext.fastaArchiveFolder = fastaArchiveFolder;
		CurationWebContext.localTempFolder = localTempFolder;
	}

	public static CurationDao getCurationDAO() {
		return curationDao;
	}

	public static File getFastaFolder() {
		return fastaFolder;
	}

	public static File getFastaUploadFolder() {
		return fastaUploadFolder;
	}

	public static File getFastaArchiveFolder() {
		return fastaArchiveFolder;
	}

	public static File getLocalTempFolder() {
		return localTempFolder;
	}
}
