package edu.mayo.mprc.daemon.files;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileHolderTest {
	private static final Logger LOGGER = Logger.getLogger(FileHolderTest.class);

	// A sample class using the FileHolder
	static class TestClass extends FileHolder {
		private static final long serialVersionUID = 4837526649249198107L;

		private String string;
		private File file;
		private List<File> files = new ArrayList<File>(2);
		private TestClass child;
		private transient File notSerializable;
		private static File notSerializable2;

		public String getString() {
			return string;
		}

		public void setString(final String string) {
			this.string = string;
		}

		public File getFile() {
			return file;
		}

		public void setFile(final File file) {
			this.file = file;
		}

		public List<File> getFiles() {
			return files;
		}

		public void addFile(final File f) {
			files.add(f);
		}

		public TestClass getChild() {
			return child;
		}

		public void setChild(final TestClass child) {
			this.child = child;
		}

		public File getNotSerializable() {
			return notSerializable;
		}

		public void setNotSerializable(final File notSerializable) {
			this.notSerializable = notSerializable;
		}

		public static File getNotSerializable2() {
			return notSerializable2;
		}

		public static void setNotSerializable2(final File notSerializable2) {
			TestClass.notSerializable2 = notSerializable2;
		}
	}

	static class TestClass2 extends TestClass {
		private static final long serialVersionUID = -6085572690191248134L;
		private File file2;
		private Map<String, File> fileMap = new LinkedHashMap<String, File>();

		public File getFile2() {
			return file2;
		}

		public void setFile2(final File file2) {
			this.file2 = file2;
		}

		public void addFileToMap(final String key, final File value) {
			fileMap.put(key, value);
		}

		public Map<String, File> getFileMap() {
			return fileMap;
		}
	}


	int translatedTokens;

	@Test
	public void testSerialization() {
		final TestClass test = new TestClass();
		final File temp = FileUtilities.createTempFolder();
		Assert.assertFalse(temp.getAbsolutePath().contains("src"), "The temp folder cannot contain 'src': " + temp.getAbsolutePath());

		test.setFile(new File(temp, "src/test.txt"));
		test.addFile(new File(temp, "src/test1.txt"));
		test.addFile(new File(temp, "src/test2.txt"));
		test.setNotSerializable(new File(temp, "src/ns1.txt"));
		TestClass.setNotSerializable2(new File(temp, "src/ns2.txt"));
		test.setString("hello");

		final TestClass2 test2 = new TestClass2();
		test2.setFile(new File(temp, "src/hello.txt"));
		test2.setFile2(new File(temp, "src/world.txt"));
		test2.setString("world");
		test2.addFileToMap("test3", new File(temp, "src/test3.txt"));
		test2.addFileToMap("test4", new File(temp, "src/test4.txt"));
		test.setChild(test2);

		translatedTokens = 0;
		test.translateOnSender(new SenderTokenTranslator() {
			// We change all "src" to "dest" to simulate translation
			@Override
			public FileToken translateBeforeTransfer(final FileToken fileToken) {
				LOGGER.debug(fileToken.getTokenPath());
				translatedTokens++;
				String tokenPath = getFilePathFromToken(fileToken);
				tokenPath = tokenPath.replace("src", "dest");
				return FileTokenFactory.createAnonymousFileToken(new File(tokenPath));
			}
		});

		final MyFileTokenSynchronizer synchronizer = new MyFileTokenSynchronizer();
		test.translateOnReceiver(new ReceiverTokenTranslator() {
			@Override
			public File getFile(final FileToken fileToken) {
				return new File(getFilePathFromToken(fileToken));
			}
		}, synchronizer, null);

		Assert.assertEquals(translatedTokens, 7);
		assertFilesTranslated(test.getFile(), "dest/test.txt", temp);
		assertFilesTranslated(test.getFiles().get(0), "dest/test1.txt", temp);
		assertFilesTranslated(test.getFiles().get(1), "dest/test2.txt", temp);
		assertFilesTranslated(test.getChild().getFile(), "dest/hello.txt", temp);
		final TestClass2 testClass2 = (TestClass2) test.getChild();
		assertFilesTranslated(testClass2.getFile2(), "dest/world.txt", temp);
		assertFilesTranslated(testClass2.getFileMap().get("test3"), "dest/test3.txt", temp);
		assertFilesTranslated(testClass2.getFileMap().get("test4"), "dest/test4.txt", temp);
		assertFilesTranslated(test.getNotSerializable(), "src/ns1.txt", temp);
		assertFilesTranslated(TestClass.getNotSerializable2(), "src/ns2.txt", temp);

		test.uploadAndWait("file");
		Assert.assertNotNull(synchronizer.getUploadedToken());
	}

	private void assertFilesTranslated(final File file, final String expected, final File temp) {
		Assert.assertEquals(file.getAbsolutePath(), new File(temp, expected).getAbsolutePath());
	}

	private String getFilePathFromToken(final FileToken fileToken) {
		return fileToken.getTokenPath().substring("file:".length());
	}

	private static class MyFileTokenSynchronizer implements FileTokenSynchronizer {
		private FileToken uploadedToken;

		public FileToken getUploadedToken() {
			return uploadedToken;
		}

		@Override
		public void upload(final FileToken myToken) {
		}

		@Override
		public void uploadAndWait(final FileToken myToken) {
			if (uploadedToken == null) {
				uploadedToken = myToken;
			} else {
				throw new MprcException("Too many uploads");
			}
		}

		@Override
		public void download(final FileToken theirToken) {
		}

		@Override
		public void downloadAndWait(final FileToken theirToken) {
		}
	}
}
