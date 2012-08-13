package edu.mayo.mprc.fasta;

import edu.mayo.mprc.integration.Installer;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A class to test the FASTAInputStream and FASTAOuputStream classes
 *
 * @author Eric J. Winter Date: Apr 19, 2007
 */
public final class FastaTest {
	private File fastaFileFolder;

	@BeforeClass
	public void installFiles() {
		fastaFileFolder = Installer.testFastaFiles(null, Installer.Action.INSTALL);
	}

	@AfterClass
	public void cleanupFiles() {
		Installer.testFastaFiles(fastaFileFolder, Installer.Action.UNINSTALL);
	}

	@Test
	public void testInputAndOutput() throws IOException {
		final File inFile = new File(fastaFileFolder, "test_in.fasta");
		final File outFile = File.createTempFile("test_out", ".fasta");

		Assert.assertNull(FASTAInputStream.isFASTAFileValid(inFile));

		final DBInputStream in = new FASTAInputStream(inFile);
		final DBOutputStream out = new FASTAOutputStream(outFile);

		in.beforeFirst();

		Assert.assertTrue(in.gotoNextSequence());

		final String header = in.getHeader();
		Assert.assertEquals(header, ">Q4U9M9|104K_THEAN 104 kDa microneme-rhoptry antigen precursor (p104) - Theileria annulata");
		final String sequence = in.getSequence();

		// Drop the > in header. It will be re-added.
		out.appendSequence(header.substring(1), sequence);
		out.appendRemaining(in);
		Assert.assertEquals(out.getFile(), outFile);

		out.close();
		in.close();

		assertTrue(outFile.exists());
		assertEquals(out.getSequenceCount(), 7);
		Assert.assertNull(TestingUtilities.compareFilesByLine(inFile, outFile));

		FileUtilities.cleanupTempFile(outFile);
	}

	@Test
	public void testInputAndOutputZipped() throws IOException {
		final File inFile = new File(fastaFileFolder, "test_in.fasta.gz");
		Assert.assertNull(FASTAInputStream.isFASTAFileValid(inFile));
		assertTrue(inFile.exists());

		final File outFile = File.createTempFile("test_out", ".fasta");

		final DBInputStream in = new FASTAInputStream(inFile);
		final DBOutputStream out = new FASTAOutputStream(outFile);

		in.beforeFirst();
		out.appendRemaining(in);

		out.close();
		in.close();

		assertTrue(inFile.exists());
		assertTrue(outFile.exists());
		assertEquals(out.getSequenceCount(), 7);

		FileUtilities.cleanupTempFile(outFile);
	}

	@Test
	public void shouldDetectDuplicateAccnums() throws IOException {
		final File inFile = new File(fastaFileFolder, "test_in_dups.fasta");
		final String errorMessage = FASTAInputStream.isFASTAFileValid(inFile);
		Assert.assertTrue(errorMessage.contains("Q4U9M9|104K_THEAN"), "Message [" + errorMessage + "] must mention duplicate accnum");
		Assert.assertTrue(errorMessage.toLowerCase(Locale.US).contains("duplicate"), "Must mention duplicity: " + errorMessage);
	}
}
