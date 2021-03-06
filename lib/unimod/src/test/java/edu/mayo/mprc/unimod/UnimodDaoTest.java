package edu.mayo.mprc.unimod;

import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.database.DatabaseUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public final class UnimodDaoTest {
	private SessionFactory factory;
	private UnimodDao dao;
	private static final String HBM_DIR = "edu/mayo/mprc/unimod/";
	private Transaction transaction;
	private final DatabasePlaceholder databasePlaceholder = new DatabasePlaceholder();

	@BeforeMethod
	public void setupDb() {
		factory = DatabaseUtilities.getTestSessionFactory(Arrays.asList(
				HBM_DIR + "Mod.hbm.xml",
				HBM_DIR + "ModSet.hbm.xml",
				HBM_DIR + "ModSpecificity.hbm.xml",
				"edu/mayo/mprc/database/Change.hbm.xml"));
		databasePlaceholder.setSessionFactory(factory);
		dao = new UnimodDaoImpl(databasePlaceholder);
		databasePlaceholder.begin();
	}

	@AfterMethod
	public void teardown() {
		databasePlaceholder.commit();
		dao = null;
		factory.close();
	}

	public void nextTransaction() {
		databasePlaceholder.flushSession();
		databasePlaceholder.getSession().clear();
		databasePlaceholder.commit();
		databasePlaceholder.begin();
	}

	@Test
	public void shouldDoInitialInstall() throws IOException, SAXException {
		// Get fresh unimod
		Unimod unimod = new Unimod();
		unimod.parseUnimodXML(ResourceUtilities.getStream("classpath:edu/mayo/mprc/unimod/unimod.xml", UnimodDaoTest.class));

		// Save it to database
		final Change change = new Change("Initial Unimod Install", new Date());
		final UnimodUpgrade upgrade = dao.upgrade(unimod, change);

		Assert.assertEquals(upgrade.getOriginalItems(), 0, "No items previously in DB");
		Assert.assertEquals(upgrade.getCurentItems(), unimod.size(), "Current items are same as new unimod");
		Assert.assertEquals(upgrade.getItemsAdded(), unimod.size(), "All items are added");
		Assert.assertEquals(upgrade.getItemsModified(), 0, "Nothing is modified");
		Assert.assertEquals(upgrade.getItemsDeleted(), 0, "Nothing is deleted");

		final Mod CarbC1 = unimod.getByTitle("Carbamidomethyl");

		nextTransaction();

		// Check that we get the same data
		final Unimod currentUnimod = dao.load();
		Assert.assertEquals(currentUnimod.size(), unimod.size(), "Saved unimod has to be same size as previous one");
		for (Mod mod : currentUnimod.asSet()) {
			Assert.assertEquals(mod.getCreation(), change, "The creation field has to be set on all items");
		}

		final Mod CarbC2 = currentUnimod.getByTitle("Carbamidomethyl");
		Assert.assertEquals(CarbC1, CarbC2);
		Assert.assertTrue(CarbC1.getModSpecificities().equals(CarbC2.getModSpecificities()));

		nextTransaction();

		// Save it to database again
		final Change change2 = new Change("Unimod reinstall", new Date());
		final UnimodUpgrade upgrade2 = dao.upgrade(unimod, change2);

		Assert.assertEquals(upgrade2.getOriginalItems(), unimod.size(), "All items previously in DB");
		Assert.assertEquals(upgrade2.getCurentItems(), unimod.size(), "Current items are same");
		Assert.assertEquals(upgrade2.getItemsAdded(), 0, "No items are added");
		Assert.assertEquals(upgrade2.getItemsModified(), 0, "Nothing is modified");
		Assert.assertEquals(upgrade2.getItemsDeleted(), 0, "Nothing is deleted");
	}

	@Test
	public void shouldStoreUpdates() throws IOException, SAXException {
		// Get fresh unimod
		Unimod unimod = new Unimod();
		unimod.parseUnimodXML(ResourceUtilities.getStream("classpath:edu/mayo/mprc/unimod/unimod.xml", UnimodDaoTest.class));

		// Save it to database
		final Change change = new Change("Initial Unimod Install", new Date());
		final UnimodUpgrade upgrade = dao.upgrade(unimod, change);

		Assert.assertEquals(upgrade.getOriginalItems(), 0, "No items previously in DB");
		Assert.assertEquals(upgrade.getCurentItems(), unimod.size(), "Current items are same as new unimod");
		Assert.assertEquals(upgrade.getItemsAdded(), unimod.size(), "All items are added");
		Assert.assertEquals(upgrade.getItemsModified(), 0, "Nothing is modified");
		Assert.assertEquals(upgrade.getItemsDeleted(), 0, "Nothing is deleted");

		nextTransaction();

		// Get modified unimod
		Unimod unimod2 = new Unimod();
		unimod2.parseUnimodXML(ResourceUtilities.getStream("classpath:edu/mayo/mprc/unimod/unimod_modified.xml", UnimodDaoTest.class));

		// Save it to database
		final Change change2 = new Change("Unimod modification", new Date());
		final UnimodUpgrade upgrade2 = dao.upgrade(unimod2, change2);

		Assert.assertEquals(upgrade2.getOriginalItems(), unimod.size(), "All items previously in DB");
		Assert.assertEquals(upgrade2.getCurentItems(), unimod2.size(), "Current items are same as new unimod");
		Assert.assertEquals(upgrade2.getItemsAdded(), 1, "Mods added does not match");
		Assert.assertEquals(upgrade2.getItemsModified(), 3, "Mods/specificities modified does not match");
		Assert.assertEquals(upgrade2.getItemsDeleted(), 3, "Mods deleted does not match");

		nextTransaction();

		Unimod result = dao.load();
		final Mod carbamyl = result.getByTitle("Carbamyl");
		Assert.assertEquals(carbamyl.getCreation(), change2, "This mod has been changed");

		final Mod icatG = result.getByTitle("ICAT-G-ADDED");
		Assert.assertEquals(icatG.getCreation(), change2, "This mod has been added");
	}
}
