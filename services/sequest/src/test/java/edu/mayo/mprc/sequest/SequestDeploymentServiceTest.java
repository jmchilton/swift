package edu.mayo.mprc.sequest;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.DaemonWorkerTester;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressListener;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.enginedeployment.DeploymentRequest;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;

import static org.testng.Assert.*;

@Test(groups = {"sequest"}, sequential = true)
public final class SequestDeploymentServiceTest {
	private static final Logger LOGGER = Logger.getLogger(SequestDeploymentServiceTest.class);

	@BeforeClass
	public void init() {
	}

	@DataProvider(name = "fastaResources")
	public Object[][] getResources() {
		return new Object[][]{
				{"uncompressed", "/test_in.fasta"},
//				{"compressed", "/test_in.fasta.gz"},
		};
	}


	class FakeDeploymentTest {
		public File sharedTestFolder;
		public File fakeFASTAFileToDeploy;
		public DeploymentRequest request;

		private static final String sequestparams = "/edu/mayo/mprc/swift/params/Orbitrap_Sprot_Latest_CarbC_OxM/sequest.params";
		private static final String makedbparams = "/edu/mayo/mprc/swift/params/Orbitrap_Sprot_Latest_CarbC_OxM/makedb.params";

		private void init(String resourcePath) {
			try {
				sharedTestFolder = null;//TestApplicationContext.getSharedTestFolder();
				fakeFASTAFileToDeploy = TestingUtilities.getTempFileFromResource(this.getClass(), resourcePath, sharedTestFolder);
				File fakeParamsFile = writeToFile(sequestparams, sharedTestFolder, "tmp_sequest.params");

				String shortName = FileUtilities.stripExtension(fakeFASTAFileToDeploy.getName());

				Curation toDeploy = new Curation();
				toDeploy.setShortName(shortName);
				toDeploy.setCurationFile(fakeFASTAFileToDeploy);

				request = new DeploymentRequest(this.getClass().getSimpleName(), toDeploy.getFastaFile());
				request.addProperty(SequestDeploymentService.SEQUEST_PARAMS_FILE, fakeParamsFile);
			} catch (IOException e) {
				throw new MprcException(e);
			}
		}

		public File writeToFile(String resourceOnClasspath, File directoryToWriteTo, String name) {
			InputStream is = this.getClass().getResourceAsStream(resourceOnClasspath);
			File file = new File(directoryToWriteTo, name);
			try {
				FileUtilities.writeStreamToFile(is, file);
				file.deleteOnExit();
			} catch (IOException e) {
				throw new MprcException(e);
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
					//SWALLOWED: unnecessary...
				}
			}
			return file;
		}

		public void writeMakedbParams(String testName) {
			writeToFile(makedbparams, fakeFASTAFileToDeploy.getParentFile(), FileUtilities.stripExtension(testName) + ".makedb.params");
		}

		public FakeDeploymentTest(String resourcePath) {
			init(resourcePath);
		}
	}

	@Test(enabled = false, groups = {"linux"}, dataProvider = "fastaResources")
	public void performDeployment_Test(String testName, String resourcePath) throws IOException {

		LOGGER.info("Running SequesetDST for " + testName + "file.");

		SequestDeploymentService service = null; //getConfiguredSequestDeploymentService();

		FakeDeploymentTest fdt = new FakeDeploymentTest(resourcePath);

		File fakeFASTAFileToDeploy = fdt.fakeFASTAFileToDeploy;

		DeploymentRequest request = fdt.request;

		final SequestDeploymentResult[] results = new SequestDeploymentResult[1];

		DaemonWorkerTester tester = new DaemonWorkerTester(service);
		Object workToken = tester.sendWork(request, new ProgressListener() {

			public void requestEnqueued(String hostString) {
				LOGGER.debug("SequestDS request enqueued at " + hostString);
			}

			public void requestProcessingStarted() {
				LOGGER.debug("SequestDS processing started");
			}

			public void requestProcessingFinished() {
				LOGGER.debug("SequestDS processing finished");
			}

			public void requestTerminated(DaemonException e) {
				fail("Request terminated", e);
			}

			public void userProgressInformation(ProgressInfo progressInfo) {
				if (progressInfo instanceof SequestDeploymentResult) {
					results[0] = (SequestDeploymentResult) progressInfo;
				} else {
					LOGGER.info(progressInfo.toString());
				}
			}
		});

		while (!tester.isDone(workToken)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				fail("Interupted thread", e);
			}
		}

		SequestDeploymentResult result = results[0];

		assertNotNull(result, "No result was returned to the deployment failed.");

		for (String message : result.getMessages()) {
			LOGGER.info(message);
		}

		if (result.getCompositeException() != null) {
			LOGGER.error(result.getCompositeException());
		}

		File dgtFile = null;
		for (File sharedFile : result.getGeneratedFiles()) {
			if (sharedFile.getName().contains(".dgt")) {
				dgtFile = sharedFile;
			}
		}

		//perform assertions but we need to make sure that generated files are deleted.
		try {
			File hdrFile = result.getFileToSearchAgainst();
			assertNull(result.getCompositeException(), "There was at least one exception in performing sequest deployment");
			assertNotNull(result.getDeployedFile(), "No deployed file was returned.");
			assertNotNull(hdrFile);
			assertTrue(hdrFile.length() > 0);
			assertNotNull(dgtFile);
			assertTrue(dgtFile.length() > fakeFASTAFileToDeploy.length());
		} finally {
			for (File sharedFile : result.getGeneratedFiles()) {
				LOGGER.debug("deleting: " + sharedFile.getAbsolutePath());
				FileUtilities.cleanupTempFile(sharedFile);
			}
		}
	}

	private DeploymentRequest getFakeDeploymentRequest(File sharedTestFolder, File fakeFASTAFileToDeploy) throws IOException {
		InputStream is = this.getClass().getResourceAsStream("/edu/mayo/mprc/swift/params/Orbitrap_Sprot_Latest_CarbC_OxM/sequest.params");
		File fakeParamsFile = new File(sharedTestFolder, "tmp_sequest.params");
		try {
			FileUtilities.writeStreamToFile(is, fakeParamsFile);
			fakeParamsFile.deleteOnExit();
		} finally {
			if (is != null) {
				is.close();
			}
		}

		String shortName = FileUtilities.stripExtension(fakeFASTAFileToDeploy.getName());

		Curation toDeploy = new Curation();
		toDeploy.setShortName(shortName);
		toDeploy.setCurationFile(fakeFASTAFileToDeploy);

		DeploymentRequest request = new DeploymentRequest(this.getClass().getSimpleName(), toDeploy.getFastaFile());

		request.addProperty(SequestDeploymentService.SEQUEST_PARAMS_FILE, fakeParamsFile);
		return request;
	}


	private static final String INPUT_SEQUEST_PARAMS = "classpath:edu/mayo/mprc/sequest/sequest.params";
	private static final String FASTA_NAME = "foo.fasta";
	private static final String EXPECTED_MAKEDB_PARAMS = "classpath:edu/mayo/mprc/sequest/makedb.params.nocomments";
	//TODO make SequestToMakeDBConverter preserve comments.

	@Test(enabled = true)
	public void testConvertSequestToMakedb() throws Throwable {
		SequestMappings pic = new SequestMappings();
		pic.read(ResourceUtilities.getReader(INPUT_SEQUEST_PARAMS, this.getClass()));
		SequestToMakeDBConverter converter = new SequestToMakeDBConverter();
		StringBuilder outpic = converter.convertSequestToMakeDB(pic, new File(FASTA_NAME));
		String expected = FileUtilities.readIntoString(ResourceUtilities.getReader(EXPECTED_MAKEDB_PARAMS, pic), 1024 * 1024);
		expected = expected.replaceAll("database_name = /Users/cmason/mprc/maven/core/foo.fasta", Matcher.quoteReplacement("database_name = " + new File("foo.fasta").getAbsolutePath()));
		String obtained = outpic.toString();
		Assert.assertEquals(obtained, expected, "Generated makedb parameters do not match the expected ones");
	}

	@Test
	public void testSpecifiesNoEnzyme() throws IOException {
		File nsParamsFile = TestingUtilities.getTempFileFromResource(this.getClass(), "/edu/mayo/mprc/sequest/nonspecific_sequest.params", true, null);

		SequestDeploymentService service = new SequestDeploymentService();
		service.setConverter(new SequestToMakeDBConverter());

		boolean result = service.specifiesNoEnzyme(nsParamsFile);

		Assert.assertTrue(result, "Did not properly detect that a params file was non-specific enzyme.");

	}


}
