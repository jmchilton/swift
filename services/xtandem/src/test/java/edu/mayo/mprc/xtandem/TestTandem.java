package edu.mayo.mprc.xtandem;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.integration.Installer;
import edu.mayo.mprc.swift.params2.Instrument;
import edu.mayo.mprc.swift.params2.MassUnit;
import edu.mayo.mprc.swift.params2.Protease;
import edu.mayo.mprc.swift.params2.Tolerance;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.swift.params2.mapping.MockParamsInfo;
import edu.mayo.mprc.swift.params2.mapping.TestMappingContextBase;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class TestTandem {
	private static final Logger LOGGER = Logger.getLogger(TestTandem.class);

	private static File tempRootDir;

	private File tandemTemp;

	private static final String DATABASE_SHORT_NAME = "SprotYeast080226A";
	private File inputMgfFolder;
	private File inputMgfFile;
	private File fastaFolder;
	private File fastaFile;
	private File tandemInstallFolder;

	@BeforeClass()
	public void setup() throws IOException {
		tempRootDir = FileUtilities.createTempFolder();
		inputMgfFolder = Installer.mgfFiles(null, Installer.Action.INSTALL);
		inputMgfFile = new File(inputMgfFolder, "test.mgf");
		fastaFolder = Installer.yeastFastaFiles(null, Installer.Action.INSTALL);
		fastaFile = new File(fastaFolder, DATABASE_SHORT_NAME + ".fasta");
		tandemInstallFolder = Installer.tandem(null, Installer.Action.INSTALL);
	}

	@AfterClass()
	public void cleanup() {
		FileUtilities.cleanupTempFile(tempRootDir);
		Installer.mgfFiles(inputMgfFolder, Installer.Action.UNINSTALL);
		Installer.yeastFastaFiles(fastaFolder, Installer.Action.UNINSTALL);
		Installer.tandem(tandemInstallFolder, Installer.Action.UNINSTALL);
	}

	@Test
	public void runTandemWorker() {
		try {
			tandemTemp = new File(tempRootDir, "tandem");
			FileUtilities.ensureFolderExists(tandemTemp);

			File tandemOut = new File(tandemTemp, "out");
			FileUtilities.ensureFolderExists(tandemOut);


			File tandemParamFile = getTandemParams();

			String tandemExecutable = null;
			if (FileUtilities.isWindowsPlatform()) {
				tandemExecutable = new File(tandemInstallFolder, "tandem.exe").getAbsolutePath();
			} else if (FileUtilities.isLinuxPlatform()) {
				tandemExecutable = new File(tandemInstallFolder, "tandem.exe").getAbsolutePath();
			} else {
				Assert.fail("Unsupported platform to test X!Tandem on");
				return;
			}

			if (!new File(tandemExecutable).exists()) {
				LOGGER.warn("Could not find tandem executable in " + tandemExecutable + ", trying Tandem on the path.");
				tandemExecutable = "tandem.exe";
			}

			XTandemWorker.Config tandemConfig = new XTandemWorker.Config(tandemExecutable);

			final XTandemWorker.Factory factory = new XTandemWorker.Factory();
			Worker worker = factory.create(tandemConfig, null);

			final File resultFile = new File(tandemOut, "tandemResult.xml");

			XTandemWorkPacket workPacket = new XTandemWorkPacket(inputMgfFile, tandemParamFile, resultFile, tandemOut, fastaFile, false, "0", false);
			WorkPacketBase.simulateTransfer(workPacket);

			worker.processRequest(workPacket, new ProgressReporter() {
				public void reportStart() {
					LOGGER.info("Started processing");
				}

				public void reportProgress(ProgressInfo progressInfo) {
					LOGGER.info(progressInfo);
				}

				public void reportSuccess() {
					Assert.assertTrue(resultFile.length() > 0, "Tandem result file is empty.");
				}

				public void reportFailure(Throwable t) {
					throw new MprcException("Tandem worker failed to process work packet.", t);
				}
			});
		} catch (Exception e) {
			throw new MprcException("Tandem worker test failed.", e);
		} finally {
			FileUtilities.cleanupTempFile(tandemTemp);
		}
	}

	private File getTandemParams() throws IOException {
		XTandemMappingFactory mappingFactory = new XTandemMappingFactory();
		final Mappings mapping = mappingFactory.createMapping();
		mapping.read(mapping.baseSettings());

		MappingContext context = new TestMappingContextBase(new MockParamsInfo());

		// TODO: Excercise all mappings
		mapping.mapEnzymeToNative(context, new Protease("Trypsin (allow P)", "KR", ""));
		mapping.mapInstrumentToNative(context, Instrument.ORBITRAP);
		mapping.mapMissedCleavagesToNative(context, 2);
		mapping.mapPeptideToleranceToNative(context, new Tolerance(10, MassUnit.Ppm));
		mapping.mapFragmentToleranceToNative(context, new Tolerance(0.5, MassUnit.Da));

		File paramFile = new File(tandemTemp, mappingFactory.getCanonicalParamFileName());
		mapping.write(mapping.baseSettings(), Files.newWriter(paramFile, Charsets.UTF_8));

		return paramFile;
	}

}
