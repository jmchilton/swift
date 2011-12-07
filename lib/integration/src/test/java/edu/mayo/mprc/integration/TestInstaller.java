package edu.mayo.mprc.integration;

import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.StringUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class TestInstaller {
	@Test
	public static void shouldCalculateChecksum() throws IOException {
		File file = File.createTempFile("checksum", ".txt");
		try {
			FileUtilities.writeStringToFile(file, "A test message to checksum - 0123456789\n", true);
			final byte[] checksum = Installer.checksum(file);
			final String checksumString = StringUtilities.toHex(checksum, "");
			Assert.assertEquals(checksumString, "03c334af89cb0986372b38c94b392c2a");
		} finally {
			FileUtilities.cleanupTempFile(file);
		}
	}

	@Test
	public static void shouldDownload() {
		Assert.assertNotNull(Installer.getIntegrationArchive());
	}

	@Test(dependsOnMethods = {"shouldDownload"})
	public static void shouldInstallExtractMsn() {
		File folder = Installer.extractMsn(null, Installer.Action.INSTALL);
		Assert.assertTrue(folder.exists(), "extract_msn folder does not exist");
		Assert.assertTrue(new File(folder, "extract_msn.exe").exists(), "extract_msn.exe does not exist");
		Installer.extractMsn(folder, Installer.Action.UNINSTALL);
	}

	@Test(dependsOnMethods = {"shouldDownload"})
	public static void shouldInstallOmssa() {
		File folder = Installer.omssa(null, Installer.Action.INSTALL);
		Assert.assertTrue(folder.exists(), "Omssa folder does not exist");
		File ommsaExecutable;
		if (FileUtilities.isWindowsPlatform()) {
			ommsaExecutable = new File(folder, "omssacl.exe");
		} else {
			ommsaExecutable = new File(folder, "omssacl");
		}
		Assert.assertTrue(ommsaExecutable.exists(), "OMSSA executable does not exist");
		Installer.omssa(folder, Installer.Action.UNINSTALL);
	}

	@Test(dependsOnMethods = {"shouldDownload"})
	public static void shouldInstallFormatDb() {
		File folder = Installer.formatDb(null, Installer.Action.INSTALL);
		Assert.assertTrue(folder.exists(), "formatdb folder does not exist");
		File executable;
		if (FileUtilities.isWindowsPlatform()) {
			executable = new File(folder, "formatdb.exe");
		} else {
			executable = new File(folder, "formatdb");
		}
		Assert.assertTrue(executable.exists(), "formatdb executable does not exist");
		Installer.formatDb(folder, Installer.Action.UNINSTALL);
	}

	@Test(dependsOnMethods = {"shouldDownload"})
	public static void shouldInstallTandem() {
		File folder = Installer.tandem(null, Installer.Action.INSTALL);
		Assert.assertTrue(folder.exists(), "Tandem folder does not exist");
		File tandemExecutable;
		if (FileUtilities.isMacPlatform()) {
			tandemExecutable = new File(folder, "tandem");
		} else {
			tandemExecutable = new File(folder, "tandem.exe");
		}
		Assert.assertTrue(tandemExecutable.exists(), "Tandem executable does not exist");
		Installer.tandem(folder, Installer.Action.UNINSTALL);
	}

	@Test(dependsOnMethods = {"shouldDownload"})
	public static void shouldInstallXvfbWrapper() {
		File wrapper = Installer.xvfbWrapper(null, Installer.Action.INSTALL);
		Assert.assertTrue(wrapper.exists() && wrapper.isFile(), "Wrapper must be a file");
		Installer.xvfbWrapper(wrapper, Installer.Action.UNINSTALL);
		Assert.assertTrue(!wrapper.exists(), "File must be deleted");
	}

	@Test(dependsOnMethods = {"shouldDownload"})
	public static void shouldInstallTestFasta() {
		File folder = Installer.testFastaFiles(null, Installer.Action.INSTALL);
		Assert.assertTrue(folder.exists(), "Fasta file folder must exist");
		Installer.testFastaFiles(folder, Installer.Action.UNINSTALL);
		Assert.assertTrue(!folder.exists(), "Folder must be deleted");
	}

	@Test(dependsOnMethods = {"shouldDownload"})
	public static void shouldInstallYeastFasta() {
		File folder = Installer.yeastFastaFiles(null, Installer.Action.INSTALL);
		Assert.assertTrue(folder.exists(), "Fasta file folder must exist");
		Installer.yeastFastaFiles(folder, Installer.Action.UNINSTALL);
		Assert.assertTrue(!folder.exists(), "Folder must be deleted");
	}

	@Test(dependsOnMethods = {"shouldDownload"})
	public static void shouldInstallTestMgf() {
		File folder = Installer.mgfFiles(null, Installer.Action.INSTALL);
		Assert.assertTrue(folder.exists(), "MGF folder must exist");
		Installer.mgfFiles(folder, Installer.Action.UNINSTALL);
		Assert.assertTrue(!folder.exists(), "Folder must be deleted");
	}
}
