package edu.mayo.mprc.integration;

import com.google.common.io.Files;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.HttpClientUtility;
import edu.mayo.mprc.utilities.StringUtilities;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Can install a required environment for testing purposes, using a given folder.
 * <p/>
 * The integration data is downloaded as a .zip file from a given URL to a particular location within the temporary
 * folder. This data is cached and reused as long as the md5 sum of the loaded file matches the expected one.
 * This approach is chosen instead of putting the testing data into the repository since the integration check data
 * is potentially huge and contains entire software packages.
 */
public class Installer {
	private static final String RESOURCE_URL = "https://github.com/downloads/romanzenka/swift/integration-resources.zip";
	private static final String RESOURCE_MD5 = "4a96b0517fa528234f0e630d63c64653";
	private static final long RESOURCE_LENGTH = 33578126;
	private static final String ROOT = "integration-resources";

	private static Date lastChecked = null;

	private static final String UNKNOWN_INSTALL_ACTION = "Unknown install action {0}";

	public enum Action {
		INSTALL,
		UNINSTALL,
	}

	private static final List<String> EXTRACT_MSN_FILES = Arrays.asList(
			"/extract_msn/extract_msn.exe",
			"/extract_msn/Fileio.dll",
			"/extract_msn/Fregistry.dll",
			"/extract_msn/MFC42U.DLL",
			"/extract_msn/MSVCP60.DLL",
			"/extract_msn/readme.txt",
			"/extract_msn/UnifiedFile.dll"
	);

	private static final List<String> OMSSA_LINUX = Arrays.asList(
			"!/omssa/linux/omssacl",
			"/omssa/linux/mods.xml",
			"!/omssa/linux/mgf2omssa.pl"
	);

	private static final List<String> OMSSA_WINDOWS = Arrays.asList(
			"/omssa/windows/omssacl.exe",
			"/omssa/windows/mods.xml",
			"/omssa/windows/msvcp80.dll",
			"/omssa/windows/msvcr80.dll",
			"/omssa/windows/OMSSA.xsd"
	);

	private static final List<String> FORMATDB_LINUX = Arrays.asList(
			"!/formatdb/linux/formatdb"
	);

	private static final List<String> FORMATDB_WINDOWS = Arrays.asList(
			"/formatdb/windows/formatdb.exe"
	);

	private static final List<String> TANDEM_LINUX = Arrays.asList(
			"!/tandem/tandem-linux-10-12-01-1/bin/tandem.exe"
	);

	private static final List<String> TANDEM_WINDOWS = Arrays.asList(
			"/tandem/tandem-win32-10-12-01-1/bin/tandem.exe"
	);

	private static final List<String> MSMSEVAL_WINDOWS = Arrays.asList(
			"/win/cygexpat-0.dll",
			"/win/cygwin1.dll",
			"/win/msmsEval.exe"
	);

	private static final List<String> MSMSEVAL_LINUX = Arrays.asList(
			"/linux_x86_64/msmsEval"
	);

	private static final String MYRIMATCH_EXE_WINDOWS = "myrimatch.exe";

	private static final String MYRIMATCH_EXE_LINUX = "myrimatch";

	private static final List<String> MYRIMATCH_WINDOWS = Arrays.asList(
			"agtsampleinforw.dll",
			"BaseCommon.dll",
			"BaseDataAccess.dll",
			"BaseError.dll",
			"BaseTof.dll",
			"BDal.CXt.Lc.dll",
			"BDal.CXt.Lc.Factory.dll",
			"BDal.CXt.Lc.Interfaces.dll",
			"BDal.CXt.Lc.UntU2.dll",
			"boost_date_time-vc80-mt-1_33_1-BDAL_20070424.dll",
			"boost_regex-vc80-mt-1_33_1-BDAL_20070424.dll",
			"boost_thread-vc80-mt-1_33_1-BDAL_20070424.dll",
			"Clearcore2.Data.AnalystDataProvider.dll",
			"Clearcore2.Data.CommonInterfaces.dll",
			"Clearcore2.Data.dll",
			"Clearcore2.Data.WiffReader.dll",
			"Clearcore2.InternalRawXYProcessing.dll",
			"Clearcore2.ProjectUtilities.dll",
			"Clearcore2.StructuredStorage.dll",
			"Clearcore2.Utility.dll",
			"CompassXtractMS.dll",
			"EULA.MHDAC",
			"EULA.MSFileReader",
			"fileio.dll",
			"FlexVariableTable.xml",
			"fregistry.dll",
			"HSReadWrite.dll",
			"ICRVariableTable.xml",
			"Interop.DataExplorer.dll",
			"Interop.EDAL.dll",
			"Interop.EDAL.SxS.manifest",
			"Interop.HSREADWRITELib.dll",
			"Interop.HSREADWRITELib.SxS.manifest",
			"libfftw3-3.dll",
			"libfftw3f-3.dll",
			"MassLynxRaw.dll",
			"MassSpecDataReader.dll",
			"MSFileReader.XRawfile2.dll",
			"MSFileReader.XRawfile2.SxS.manifest",
			MYRIMATCH_EXE_WINDOWS,
			"NTB-vc80-mt-1_5_97.dll"
	);

	private static final List<String> MYRIMATCH_LINUX = Arrays.asList(
			"!/myrimatch_2_1_87/linux/" + MYRIMATCH_EXE_LINUX
	);

	private static final String WRAPPER_SCRIPT = "unixXvfbWrapper.sh";

	private static final List<String> UNIX_XVFB_WRAPPER = Arrays.asList(
			"!/util/" + WRAPPER_SCRIPT
	);

	private static final List<String> FASTA_TEST = Arrays.asList(
			"/test_in.fasta",
			"/test_in.fasta.gz"
	);

	private static final List<String> FASTA_YEAST = Arrays.asList(
			"/SprotYeast080226A.fasta"
	);

	private static final List<String> MGF_TEST = Arrays.asList(
			"/test.mgf"
	);

	private static final List<String> RAW_FILES = Arrays.asList(
			"/test.RAW"
	);

	private static final int EXECUTABLE = 0x1ed; // 0755
	private static final String DIGEST_INSTANCE = "MD5";

	/**
	 * Install a given list of files into given folder.
	 *
	 * @param folder        Folder to install to.
	 * @param defaultFolder If folder is null, install into a temporary folder with this name.
	 * @param files         List of files to install. The files prefixed with exclamation mark need to have executable flag set.
	 * @return Folder where the list of files was installed.
	 */
	private static File installList(File folder, String defaultFolder, Collection<String> files) {
		folder = folderOrDefault(folder, defaultFolder);

		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(getIntegrationArchive());
		} catch (IOException e) {
			throw new MprcException("Could not open integration test archive", e);
		}

		try {
			return installList(folder, files, zipFile);
		} finally {
			try {
				zipFile.close();
			} catch (IOException ignore) {
				// SWALLOWED: we do not care at this point
			}
		}
	}

	private static File installList(File folder, Collection<String> files, ZipFile zipFile) {
		for (String file : files) {
			try {
				boolean makeExecutable = false;
				if (file.startsWith("!" )) {
					file = file.substring(1);
					makeExecutable = true;
				}
				final ZipEntry entry = zipFile.getEntry(ROOT + file);
				if (entry == null) {
					throw new MprcException(MessageFormat.format("Could not decompress entry {0} from integration test archive", ROOT + file));
				}

				final File actualFile = new File(folder, new File(file).getName());
				FileUtilities.writeStreamToFile(zipFile.getInputStream(entry), actualFile);
				if (makeExecutable) {
					FileUtilities.chmod(actualFile, EXECUTABLE, '+', false);
				}
			} catch (Exception e) {
				throw new MprcException("Could not install file: " + file + " to " + folder.getAbsolutePath(), e);
			}
		}
		return folder;
	}

	private static File folderOrDefault(File folder, String defaultFolder) {
		if (folder == null) {
			folder = FileUtilities.createTempFolder(null, defaultFolder, true);
		}
		return folder;
	}

	private static void uninstallList(File folder, Collection<String> files) {
		for (String file : files) {
			String name = new File(file).getName();
			FileUtilities.cleanupTempFile(new File(folder, name));
		}
		FileUtilities.quietDelete(folder);
		if (folder.exists()) {
			throw new MprcException("Could not uninstall files - the folder " + folder.getAbsolutePath() + " is not empty." );
		}
	}

	private static File processList(File folder, String defaultFolder, Collection<String> files, Action action) {
		switch (action) {
			case INSTALL:
				return installList(folder, defaultFolder, files);
			case UNINSTALL:
				uninstallList(folder, files);
				return folder;
			default:
				throw new MprcException(MessageFormat.format(UNKNOWN_INSTALL_ACTION, action.name()));
		}
	}

	/**
	 * Install extract_msn in a given folder.
	 *
	 * @param folder Folder to install extract_msn into. If null, temp folder is created.
	 * @return Folder where extract_msn got installed.
	 */
	public static File extractMsn(File folder, Action action) {
		return processList(folder, "extract_msn", EXTRACT_MSN_FILES, action);
	}

	public static File omssa(File folder, Action action) {
		return processList(folder, "omssa", FileUtilities.isWindowsPlatform() ? OMSSA_WINDOWS : OMSSA_LINUX, action);
	}

	public static File tandem(File folder, Action action) {
		return processList(folder, "tandem", getTandemFiles(), action);
	}

	public static File myrimatch(File folder, Action action) {
		final boolean win = FileUtilities.isWindowsPlatform();
		return processSingleFile(folder, "myrimatch",
				win ? MYRIMATCH_WINDOWS : MYRIMATCH_LINUX,
				win ? MYRIMATCH_EXE_WINDOWS : MYRIMATCH_EXE_LINUX,
				action);
	}

	public static File msmsEval(File folder, Action action) {
		return processList(folder, "msmsEval", FileUtilities.isWindowsPlatform() ? MSMSEVAL_WINDOWS : MSMSEVAL_LINUX, action);
	}

	public static File formatDb(File folder, Action action) {
		return processList(folder, "formatdb", FileUtilities.isWindowsPlatform() ? FORMATDB_WINDOWS : FORMATDB_LINUX, action);
	}

	public static File xvfbWrapper(File folder, Action action) {
		return processSingleFile(folder, "util", UNIX_XVFB_WRAPPER, WRAPPER_SCRIPT, action);
	}

	public static File testFastaFiles(File folder, Action action) {
		return processList(folder, "fasta", FASTA_TEST, action);
	}

	public static File yeastFastaFiles(File folder, Action action) {
		return processList(folder, "fasta", FASTA_YEAST, action);
	}

	public static File mgfFiles(File folder, Action action) {
		return processList(folder, "mgf", MGF_TEST, action);
	}

	public static File rawFiles(File folder, Action action) {
		return processList(folder, "raw", RAW_FILES, action);
	}

	public static byte[] checksum(File file) {
		try {
			return Files.getDigest(file, MessageDigest.getInstance(DIGEST_INSTANCE));
		} catch (IOException e) {
			throw new MprcException(MessageFormat.format("Cannot calculate {0} digest for file {1}", DIGEST_INSTANCE, file.getAbsolutePath()), e);
		} catch (NoSuchAlgorithmException e) {
			throw new MprcException(MessageFormat.format("Not supported message digest method {0}", DIGEST_INSTANCE), e);
		}
	}

	/**
	 * Download the integration resource archive.
	 *
	 * @return Path to the file containing the integration data.
	 */
	public static File getIntegrationArchive() {
		File resource = new File(FileUtilities.getDefaultTempDirectory(), "integration-" + RESOURCE_MD5 + ".zip" );

		if (lastChecked == null) {
			if (!resource.exists()) {
				HttpClientUtility.downloadUrlHttps(RESOURCE_URL, resource);
			}

			final long actualLength = resource.length();
			if (RESOURCE_LENGTH != actualLength) {
				throw new MprcException(MessageFormat.format("The downloaded integration file [{0}] does not have the expected size: expected {2}, got {2} bytes", resource.getAbsolutePath(), RESOURCE_LENGTH, actualLength));
			}

			final String checksum = StringUtilities.toHex(checksum(resource), "" );
			if (!RESOURCE_MD5.equals(checksum)) {
				throw new MprcException(MessageFormat.format("The downloaded integration file [{0}] does not have the expected {1} checksum {2}", resource.getAbsolutePath(), DIGEST_INSTANCE, RESOURCE_MD5));
			}

			lastChecked = new Date();
		}

		return resource;
	}

	private static List<String> getTandemFiles() {
		if (FileUtilities.isMacPlatform()) {
			return null;
		}
		return FileUtilities.isWindowsPlatform() ? TANDEM_WINDOWS : TANDEM_LINUX;
	}

	private static File processSingleFile(File folder, String defaultFolder, List<String> files, String mainFile, Action action) {
		switch (action) {
			case INSTALL:
				File output = processList(folder, defaultFolder, files, action);
				return new File(output, mainFile);
			case UNINSTALL:
				processList(folder.getParentFile(), defaultFolder, files, action);
				return folder.getParentFile();
			default:
				throw new MprcException(MessageFormat.format(UNKNOWN_INSTALL_ACTION, action.name()));
		}
	}
}
