package edu.mayo.mprc.swift.db;

import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.database.FileTokenToDatabaseTranslator;

import java.io.File;

/**
 * A wrapper around {@link FileTokenFactory} that exposes the {@link edu.mayo.mprc.database.FileTokenToDatabaseTranslator}.
 * This interface is not implemented directly by FileTokenFactory because it would introduce a dependency between
 * filesharing and database packages.
 */
public final class FileTokenFactoryWrapper implements FileTokenToDatabaseTranslator {
	private FileTokenFactory factory;

	public FileTokenFactoryWrapper(FileTokenFactory factory) {
		this.factory = factory;
	}

	@Override
	public String fileToDatabaseToken(File file) {
		return factory.fileToDatabaseToken(file);
	}

	@Override
	public File databaseTokenToFile(String tokenPath) {
		return factory.databaseTokenToFile(tokenPath);
	}
}
