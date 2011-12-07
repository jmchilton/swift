package edu.mayo.mprc.dbcurator;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.database.DatabaseUtilities;
import edu.mayo.mprc.database.FileTokenToDatabaseTranslator;
import edu.mayo.mprc.database.FileType;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.CurationExecutor;
import edu.mayo.mprc.dbcurator.model.CurationStatus;
import edu.mayo.mprc.dbcurator.model.curationsteps.*;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDaoImpl;
import edu.mayo.mprc.fasta.filter.MatchMode;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;

import static org.testng.Assert.assertTrue;


/**
 * A set of test cases for testing a few different routine curations
 */
public final class CurationExecutorTest {

	private static final Logger LOGGER = Logger.getLogger(CurationExecutorTest.class);
	private static final String MODEL = "edu/mayo/mprc/dbcurator/model/";
	private static final String STEPS = MODEL + "curationsteps/";

	@Test(groups = {"fast", "integration"}, enabled = true)
	public void testSimpleCurationCreationAndExecution() {
		CurationDao curationDao = getTestCurationDao();

		curationDao.begin();
		try {

			File curationFolder = FileUtilities.createTempFolder();
			File localTempFolder = FileUtilities.createTempFolder();
			File curatorArchiveFolder = FileUtilities.createTempFolder();

			String url = "classpath:/edu/mayo/mprc/dbcurator/ShortTest.fasta.gz";

			Curation curation = new Curation();
			curation.setShortName("FirstTests");
			curation.setTitle("The first test cases");
			curation.setNotes("This is just a test curation that will be run as a unit test.");

			NewDatabaseInclusion inclusionStep = new NewDatabaseInclusion();
			inclusionStep.setUrl(url);
			assertTrue(inclusionStep.preValidate(curationDao).isOK(), "The NewDatabaseInclusion step failed validation.");

			ManualInclusionStep manualInclusion = new ManualInclusionStep();
			manualInclusion.setHeader(">MyManualInclusion_HUMAN");
			manualInclusion.setSequence("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

			//just a basic header filter will be used so only sequences with the string "_human" will be retained
			HeaderFilterStep filterStep = new HeaderFilterStep();
			filterStep.setTextMode(TextMode.REG_EX);
			filterStep.setCriteriaString("^>[^ ]+_HUMAN");
			filterStep.setMatchMode(MatchMode.ANY);
			assertTrue(filterStep.preValidate(curationDao).isOK(), "The filter failed prevalidation");

			//add a sequence scramble step to this database this will take the file write its contents and then append
			//the same sequences with sequences scrambled up.
			MakeDecoyStep scrambleStep = new MakeDecoyStep();
			scrambleStep.setManipulatorType(MakeDecoyStep.REVERSAL_MANIPULATOR);
			scrambleStep.setOverwriteMode(false); //append the reversed sequences to the file
			assertTrue(scrambleStep.preValidate(curationDao).isOK(), "The scramble step failed prevalidation.");

			//add the steps to the curation
			curation.addStep(manualInclusion, -1);
			curation.addStep(inclusionStep, -1); //add the step to the end of the curation
			curation.addStep(filterStep, -1);
			curation.addStep(scrambleStep, -1); //add manipulation step to the; //add the step to the end of the curation

			//get the object that we will use to keep track of the executor's progress
			CurationExecutor executor = new CurationExecutor(curation, false, curationDao, curationFolder, localTempFolder, curatorArchiveFolder);
			CurationStatus status = executor.execute();

			//every 5 seconds output the progress of the curation and any messages that were produced
			while (status.isInProgress()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					LOGGER.error(e);
				}
				LOGGER.info(status.getMessages());
				LOGGER.info("Step " + status.getCurrentStepNumber() + " is " + status.getCurrentStepProgress() + "% complete");
			}

			//if we had a failure then let's figure out why
			if (status.getFailedStepValidations() != null && status.getFailedStepValidations().size() > 0) {
				Assert.fail("There were errors executing the curation.\n" +
						CurationExecutor.failedValidationsToString(status.getFailedStepValidations()));
			}

			//get the final messages from the executor
			LOGGER.info(status.getMessages());

			LOGGER.info(curation.simpleDescription());
			curationDao.commit();
			FileUtilities.cleanupTempFile(curationFolder);
			FileUtilities.cleanupTempFile(localTempFolder);
			FileUtilities.cleanupTempFile(curatorArchiveFolder);
		} catch (Exception e) {
			curationDao.rollback();
			throw new MprcException(e);
		}
	}

	public static CurationDao getTestCurationDao() {
		FileType.initialize(new FileTokenToDatabaseTranslator() {
			@Override
			public String fileToDatabaseToken(File file) {
				return file.getAbsolutePath();
			}

			@Override
			public File databaseTokenToFile(String tokenPath) {
				return new File(tokenPath);
			}
		});

		final SessionFactory testSessionFactory = DatabaseUtilities.getTestSessionFactory(
				Arrays.asList(
						"edu/mayo/mprc/database/Change.hbm.xml",
						STEPS + "CurationStep.hbm.xml",
						STEPS + "DataSource.hbm.xml",
						STEPS + "HeaderTransform.hbm.xml",
						MODEL + "Curation.hbm.xml",
						MODEL + "SourceDatabaseArchive.hbm.xml"
				)
		);

		DatabasePlaceholder placeholder = new DatabasePlaceholder();
		CurationDao curationDao = new CurationDaoImpl(placeholder);
		placeholder.setSessionFactory(testSessionFactory);

		return curationDao;
	}
}
