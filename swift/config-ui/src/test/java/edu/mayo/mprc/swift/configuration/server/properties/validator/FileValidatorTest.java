package edu.mayo.mprc.swift.configuration.server.properties.validator;

import edu.mayo.mprc.utilities.FileUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public final class FileValidatorTest {

	@Test
	public void validateExistingDir() {
		final String tempDir = FileUtilities.DEFAULT_TEMP_DIRECTORY.getAbsolutePath();

		final FileValidator fileValidator = new FileValidator(null, true, true, false);

		Assert.assertNull(fileValidator.validate(tempDir), "Validation of existing directory failed.");
	}

	@Test
	public void validateAndFixExistingDir() {
		final File file = new File(FileUtilities.DEFAULT_TEMP_DIRECTORY, "test" + System.currentTimeMillis());

		try {
			final FileValidator fileValidator = new FileValidator(null, true, true, false);

			Assert.assertNotNull(fileValidator.validate(file.getAbsolutePath()), "Validation of existing directory failed. Validator must produce error message.");

			fileValidator.fix(file.getAbsolutePath());

			Assert.assertNull(fileValidator.validate(file.getAbsolutePath()), "Directory validation failed after fixing.");
		} finally {
			FileUtilities.cleanupTempFile(file);
		}
	}

	@Test
	public void validateAndFixNotExistingFile() {
		final File file = new File(FileUtilities.DEFAULT_TEMP_DIRECTORY, "test" + System.currentTimeMillis());

		final FileValidator fileValidator = new FileValidator(null, true, false, false);

		Assert.assertNotNull(fileValidator.validate(file.getAbsolutePath()), "Validation of existing file failed. Validator must produce error message.");

		fileValidator.fix(file.getAbsolutePath());

		Assert.assertNotNull(fileValidator.validate(file.getAbsolutePath()), "File validation after fixing failed. Validator should not create file.");
	}

	@Test
	public void validateFileIsNotDir() {
		final File file = FileUtilities.createTempFolder();

		try {
			final FileValidator fileValidator = new FileValidator(null, true, false, false);

			Assert.assertNotNull(fileValidator.validate(file.getAbsolutePath()), "Validation of existing file failed. Validator must produce error message.");
		} finally {
			FileUtilities.cleanupTempFile(file);
		}
	}
}
