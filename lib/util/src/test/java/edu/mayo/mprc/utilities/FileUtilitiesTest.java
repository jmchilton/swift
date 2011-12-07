package edu.mayo.mprc.utilities;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.mayo.mprc.MprcException;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public final class FileUtilitiesTest {

	private static final Logger LOGGER = Logger.getLogger(FileUtilitiesTest.class);

	@Test
	public void testGetRelativePathsUnixy() {
		Assert.assertEquals("stuff/xyz.dat", FileUtilities.getRelativePath(
				"/var/data/", "/var/data/stuff/xyz.dat", "/", true, false));
		Assert.assertEquals("../../b/c", FileUtilities.getRelativePath(
				"/a/x/y/", "/a/b/c", "/", true, false));
		Assert.assertEquals("../../b/c/", FileUtilities.getRelativePath(
				"/a/x/y", "/a/b/c/", "/", true, false));
		Assert.assertEquals("../../b/c", FileUtilities.getRelativePath(
				"/a/x/y", "/a/b/c", "/", true, false));
		Assert.assertEquals("../../b/c", FileUtilities.getRelativePath(
				"/m/n/o/a/x/y/", "/m/n/o/a/b/c", "/", true, false));
	}

	@Test
	public void testGetRelativePathFileToFile() {
		String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
		String base = "C:\\windows\\Speech\\Common\\sapisvr.exe";

		String relPath = FileUtilities.getRelativePath(base, target, "\\", false, false);
		Assert.assertEquals("..\\..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
	}

	@Test
	public void testGetRelativePathDirectoryToFile() {
		String target = "c:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
		String base = "C:\\Windows\\Speech\\Common";

		String relPath = FileUtilities.getRelativePath(base, target, "\\", false, false);
		Assert.assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
	}

	@Test(expectedExceptions = {MprcException.class})
	public void testGetRelativePathDifferentDriveLetters() {
		String target = "D:\\sources\\recovery\\RecEnv.exe";
		String base = "C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\";

		//  Should just return the target path because of the incompatible roots.
		String relPath = FileUtilities.getRelativePath(base, target, "\\", false, false);
	}

	@Test
	public void testShouldReturnNullIfNotParent() {
		String target = "c:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
		String base = "C:\\Windows\\Speech\\Common";

		String relPath = FileUtilities.getRelativePath(base, target, "\\", false, true);
		Assert.assertNull(relPath);
	}


	@Test
	public void testGetDateBasedDirectory() {
		File tempFolder = FileUtilities.createTempFolder();

		try {
			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);

			String logPath = new File(tempFolder, ""
					+ calendar.get(Calendar.YEAR) + File.separator
					+ (calendar.get(Calendar.MONTH) + 1) + File.separator
					+ calendar.get(Calendar.DAY_OF_MONTH)).getAbsolutePath();

			LOGGER.info("Expected log path: " + logPath);

			File file = FileUtilities.getDateBasedDirectory(tempFolder, date);
			Assert.assertEquals(logPath, file.getAbsolutePath(), "Log path was not created as expected.");
		} finally {
			FileUtilities.cleanupTempFile(tempFolder);
		}
	}

	@Test(groups = "windows")
	public void shouldCanonicalizeSpecialPaths() {
		if (!FileUtilities.isWindowsPlatform()) {
			return;
		}

		// On windows, C: actually means "current directory on drive C:", which can be confusing for users.
		Assert.assertFalse(new File("C:").isAbsolute(), "C: has to be reported as relative - it is cwd for disk C:");
		Assert.assertTrue(new File("C:/").isAbsolute(), "C:/ has to be reported as absolute");

		// We need to use canonicalDirectoryPath, otherwise the translation fails
		Assert.assertEquals(FileUtilities.canonicalDirectoryPath(new File("C:")), "/C:/", "The canonical directory path does not match");
	}

	@Test
	public void shouldObtainExtension() {
		Assert.assertEquals(FileUtilities.getExtension("foo.txt"), "txt");
		Assert.assertEquals(FileUtilities.getExtension("a/b/c.jpg"), "jpg");
		Assert.assertEquals(FileUtilities.getExtension("a/b/c"), "");
	}

	@Test
	public void shouldObtainGzippedExtension() {
		Assert.assertEquals(FileUtilities.getGzippedExtension("foo.txt"), "txt");
		Assert.assertEquals(FileUtilities.getGzippedExtension("foo.gz"), "gz");
		Assert.assertEquals(FileUtilities.getGzippedExtension("foo.tar.gz"), "tar.gz");
		Assert.assertEquals(FileUtilities.getGzippedExtension("a/b/c.jpg"), "jpg");
		Assert.assertEquals(FileUtilities.getGzippedExtension("a/b/c.jpg.gz"), "jpg.gz");
		Assert.assertEquals(FileUtilities.getGzippedExtension("a/b/c"), "");
	}

	@Test
	public void shouldStripExtension() {
		Assert.assertEquals(FileUtilities.stripExtension("foo.txt"), "foo");
		Assert.assertEquals(FileUtilities.stripExtension("a/b/c.jpg"), "a/b/c");
		Assert.assertEquals(FileUtilities.stripExtension("a/b/c"), "a/b/c");
		Assert.assertEquals(FileUtilities.stripExtension("a/b/c.tar.gz"), "a/b/c.tar");
	}

	@Test
	public void shouldDestroyContents() throws IOException {
		File file = File.createTempFile("test", ".txt");
		Files.write("hello world", file, Charsets.UTF_8);
		Assert.assertTrue(file.length() > 0);
		Files.copy(file, file);
		Assert.assertTrue(file.length() == 0);
		FileUtilities.deleteNow(file);
	}

	@Test
	public void shouldListFolderContentsShort() throws IOException {
		File folder = FileUtilities.createTempFolder();
		FileUtilities.ensureFolderExists(new File(folder, "1"));

		ArrayList<File> dirs = new ArrayList<File>();
		ArrayList<File> files = new ArrayList<File>();

		FileUtilities.listFolderContents(folder, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		}, dirs, files);

		Assert.assertEquals(dirs.size(), 1);
		Assert.assertEquals(files.size(), 0);

		FileUtilities.deleteNow(folder);
	}

	@Test
	public void shouldListFolderContentsLong() throws IOException {
		File folder = FileUtilities.createTempFolder();
		for (int i = 1; i < 50; i++) {
			FileUtilities.ensureFolderExists(new File(folder, "a" + String.valueOf(i)));
		}
		FileUtilities.ensureFolderExists(new File(folder, "b"));

		ArrayList<File> dirs = new ArrayList<File>();
		ArrayList<File> files = new ArrayList<File>();

		FileUtilities.listFolderContents(folder, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("a");
			}
		}, dirs, files);

		Assert.assertEquals(dirs.size(), 50);
		Assert.assertEquals(files.size(), 0);

		FileUtilities.deleteNow(folder);
	}

	@Test
	public void shouldUnixizeWindowsPath() {
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("c:/hello/world"), "c/hello/world");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("D:/test"), "d/test");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("c:"), "c/");  // Extra slash - we know it is a directory
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("Z:/"), "z/");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath(""), "");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("hi"), "hi");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("weird:path"), "weird:path");
	}

	@Test
	public void shouldUnixizePrefixedWindowsPath() {
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("/c:/hello/world"), "c/hello/world");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("/D:/test"), "d/test");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("/c:"), "c/");  // Extra slash - we know it is a directory
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("/Z:/"), "z/");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("/"), "/");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("/hi"), "/hi");
		Assert.assertEquals(FileUtilities.unixizeWindowsAbsolutePath("/weird:path"), "/weird:path");
	}
}
