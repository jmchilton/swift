package edu.mayo.mprc.database;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public final class DaoBaseTest {
	private DaoBase base;
	private static final Logger LOGGER = Logger.getLogger(DaoBaseTest.class);

	public final class DaoBaseImpl extends DaoBase {
		public DaoBaseImpl(DatabasePlaceholder databasePlaceholder) {
			super(databasePlaceholder);
		}
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {

		LOGGER.getParent().setLevel(Level.DEBUG);
		TestDatabase db = new TestDatabase(Arrays.asList(
				"edu/mayo/mprc/database/TestSet.hbm.xml",
				"edu/mayo/mprc/database/TestSetMember.hbm.xml"),
				DatabaseUtilities.SchemaInitialization.CreateDrop);

		DatabasePlaceholder placeholder = new DatabasePlaceholder();
		db.setupDatabasePlaceholder(placeholder);
		base = new DaoBaseImpl(placeholder);
		base.begin();
	}

	@AfterClass
	public void tearDown() {
		base.commit();
	}

	private TestSet s(TestSet set) {
		LOGGER.debug("  // Saving set " + set.getSetName());
		base.save(set, Restrictions.eq("setName", set.getSetName()), true);
		LOGGER.debug("  \\ Saved set " + set.getSetName());
		return set;
	}

	private TestSetMember s(TestSetMember m) {
		LOGGER.debug("    // Saving member " + m.getMemberName());
		base.save(m, Restrictions.eq("memberName", m.getMemberName()), false);
		LOGGER.debug("    \\ Saved member " + m.getMemberName());
		return m;
	}

	private TestSet makeSet(String name, int firstMember, int lastMember) {
		TestSet set = new TestSet();
		set.setSetName(name);
		for (int i = firstMember; i <= lastMember; i++) {
			set.add(s(new TestSetMember("Member " + i)));
		}

		LOGGER.debug("// Updating set " + set.getSetName());
		set = base.updateSet(set, set.getMembers(), "members");
		LOGGER.debug("\\ Updated set " + set.getSetName());
		return set;
	}

	@Test
	public void testSaveSet() {
		TestSet s1 = makeSet("s1", 1, 7);

		Assert.assertNotNull(s1.getId());

		TestSet s2 = makeSet("s2", 1, 12);
		Assert.assertNotSame(s2.getId(), s1.getId(), "Sets differ in elements, two different objects");

		TestSet s3 = makeSet("s3", 1, 7);
		Assert.assertEquals(s3, s1, "These sets are meant to be the same");
		Assert.assertEquals(s3.getId(), s1.getId(), "Same set with same contents, save as same object");
	}

	@Test
	public void shouldSaveEmptySets() {
		TestSet s1 = makeSet("e1", 1, 0);

		Assert.assertNotNull(s1.getId());

		TestSet s2 = makeSet("e2", 1, 0);
		Assert.assertEquals(s2.getId(), s1.getId(), "Sets differ in name, save as same object");

		TestSet s3 = makeSet("e3", 1, 0);
		Assert.assertEquals(s3, s1, "These sets are meant to be the same");
		Assert.assertEquals(s3.getId(), s1.getId(), "Same set with same contents, save as same object");
	}

	@Test
	public void shouldHonorWritesWithinTransaction() {
		final long initialCount = (Long) base.getSession().createQuery("select count(t) from TestSet as t").uniqueResult();
		TestSet e1 = makeSet("extra1", 1, 2);
		TestSet e2 = makeSet("extra2", 1, 3);
		final long finalCount = (Long) base.getSession().createQuery("select count(t) from TestSet as t").uniqueResult();
		Assert.assertEquals(finalCount, initialCount + 2, "One item has to be added");
	}

}
