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
		File inFile = new File(fastaFileFolder, "test_in.fasta");
		File outFile = File.createTempFile("test_out", ".fasta");

		Assert.assertTrue(FASTAInputStream.isFASTAFileValid(inFile));

		DBInputStream in = new FASTAInputStream(inFile);
		DBOutputStream out = new FASTAOutputStream(outFile);

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
		File inFile = new File(fastaFileFolder, "test_in.fasta.gz");
		assertTrue(FASTAInputStream.isFASTAFileValid(inFile));
		assertTrue(inFile.exists());

		File outFile = File.createTempFile("test_out", ".fasta");

		DBInputStream in = new FASTAInputStream(inFile);
		DBOutputStream out = new FASTAOutputStream(outFile);

		in.beforeFirst();
		out.appendRemaining(in);

		out.close();
		in.close();

		assertTrue(inFile.exists());
		assertTrue(outFile.exists());
		assertEquals(out.getSequenceCount(), 7);

		FileUtilities.cleanupTempFile(outFile);
	}

}
