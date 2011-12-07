package edu.mayo.mprc.swift.configuration.server;

import edu.mayo.mprc.common.client.GWTServiceException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.database.DatabaseFactory;
import edu.mayo.mprc.swift.configuration.client.model.*;
import edu.mayo.mprc.swift.search.SwiftSearcher;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.xtandem.XTandemWorker;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class ConfigurationDataTest {

	private static ConfigurationData data;
	private static final String USERNAME = "username";
	private static final String TEST = "test";

	@BeforeClass
	public static void setup() throws GWTServiceException {
		data = new ConfigurationData();
		data.loadDefaultConfig();
	}

	@Test
	public static void shouldProduceDefault() {
		final ApplicationModel model = data.getModel();

		Assert.assertEquals(model.getDaemons().size(), 1, "There should be one daemon");
		final DaemonModel daemon = model.getDaemons().get(0);

		Assert.assertEquals(daemon.getName(), "main", "The daemon is called 'main'");

		Assert.assertEquals(daemon.getTempFolderPath(), "var/tmp", "Default temp folder is in var/tmp");

		Assert.assertEquals(daemon.getChildren().size(), 4, "Daemon has Swift, Database, WebUI and Messenger modules by default");

		final ResourceModel swiftModule = daemon.getChildren().get(2);
		Assert.assertEquals(swiftModule.getProperty("fastaPath"), "var/fasta", "The default value has to be set");

		final ResourceConfig databaseConfig = getMainDaemon().getResources().get(0);
		final String databaseId = data.getId(databaseConfig);
		Assert.assertEquals(swiftModule.getProperty("database"), databaseId, "The database has to refer to actual database module");

		SwiftSearcher.Config swiftSearcherConfig = (SwiftSearcher.Config) getMainDaemon().getServices().get(0).getRunner().getWorkerConfiguration();
		Assert.assertEquals(swiftSearcherConfig.getDatabase(), databaseConfig, "The database config does not match");
	}

	@Test
	public static void shouldSaveConfig() {
		File folder = FileUtilities.createTempFolder();
		data.saveConfig(folder);
		Assert.assertTrue(new File(folder, "conf/").exists(), "Configuration folder must exist");
		Assert.assertTrue(new File(folder, "conf/swift.xml").exists(), "Swift.xml config must exist");
		Assert.assertTrue(
				new File(folder, "main-run.bat").exists()
						|| new File(folder, "main-run.sh").exists(), "Main executable must exist");
		FileUtilities.quietDelete(folder);
	}

	@Test
	public static void shouldSetProperty() {
		final DatabaseFactory.Config dbConfig = (DatabaseFactory.Config) getMainDaemon().getResources().get(0);
		final UiChangesReplayer uiChangesReplayer = data.setProperty(dbConfig, USERNAME, TEST, false);
		uiChangesReplayer.replay(new UiChanges() {
			@Override
			public void setProperty(String resourceId, String propertyName, String newValue) {
				Assert.assertEquals(propertyName, USERNAME);
				Assert.assertEquals(newValue, TEST);
			}

			@Override
			public void displayPropertyError(String resourceId, String propertyName, String error) {
				Assert.fail("this method should not be called");
			}
		});
		final ResourceModel databaseModel = data.getModel().getDaemons().get(0).getChildren().get(0);
		Assert.assertEquals(databaseModel.getProperty(USERNAME), TEST);
	}

	@Test
	public static void shouldChangeDaemonParams() throws GWTServiceException {
		data.createChild(data.getId(getMainDaemon()), XTandemWorker.TYPE);

		// Tandem is the last service
		final List<ServiceConfig> services = getMainDaemon().getServices();
		final ResourceConfig tandem = services.get(services.size() - 1).getRunner().getWorkerConfiguration();

		// Switch the daemon to Linux
		data.setProperty(getMainDaemon(), DaemonConfig.OS_NAME, "Linux", false);
		final UiChangesReplayer changes = data.setProperty(getMainDaemon(), DaemonConfig.OS_ARCH, "x86", false);

		// Config has to change
		checkTandemExecutable(tandem, "bin/tandem/linux_redhat_tandem/tandem.exe");

		// The change has to be reflected to the UI
		changes.replay(new UiChanges() {
			@Override
			public void setProperty(String resourceId, String propertyName, String newValue) {
				Assert.assertEquals(propertyName, XTandemWorker.TANDEM_EXECUTABLE);
				Assert.assertEquals(newValue, "bin/tandem/linux_redhat_tandem/tandem.exe");
			}

			@Override
			public void displayPropertyError(String resourceId, String propertyName, String error) {
				Assert.fail();
			}
		});

		// Change to Windows
		data.setProperty(getMainDaemon(), DaemonConfig.OS_NAME, "Windows", false);
		data.setProperty(getMainDaemon(), DaemonConfig.OS_ARCH, "32-bit", false);

		// Config has to change
		checkTandemExecutable(tandem, "bin/tandem/win32_tandem/tandem.exe");
	}

	private static void checkTandemExecutable(ResourceConfig tandem, String expected) {
		final Map<String, String> tandemConfig = tandem.save(null);
		Assert.assertEquals(tandemConfig.get(XTandemWorker.TANDEM_EXECUTABLE), expected);
	}

	private static DaemonConfig getMainDaemon() {
		return data.getConfig().getDaemons().get(0);
	}
}
