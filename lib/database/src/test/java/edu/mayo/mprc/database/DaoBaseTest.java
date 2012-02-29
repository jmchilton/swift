package edu.mayo.mprc.database;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

public final class DaoBaseTest extends DaoTest {
	private DaoBase base;
	private static final Logger LOGGER = Logger.getLogger(DaoBaseTest.class);

	public final class DaoBaseImpl extends DaoBase {
		public DaoBaseImpl() {
		}

		@Override
		public Collection<String> getHibernateMappings() {
			return Arrays.asList(
					"edu/mayo/mprc/database/TestSet.hbm.xml",
					"edu/mayo/mprc/database/TestSetMember.hbm.xml",
					"edu/mayo/mprc/database/TestDouble.hbm.xml");
		}
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {

		LOGGER.getParent().setLevel(Level.DEBUG);
		base = new DaoBaseImpl();
		initializeDatabase(Arrays.asList(base));
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

	@Test
	public void shouldSaveNanDoubles() {
		TestDouble d1 = new TestDouble(10, 20);
		d1 = base.save(d1, testDoubleEqualityCriteria(d1), false);
		nextTransaction();
		TestDouble d2 = new TestDouble(10.0001, 19.9999);
		d2 = base.save(d2, testDoubleEqualityCriteria(d1), false);
		Assert.assertEquals(d2.getId(), d1.getId(), "Must be the same object");
	}

	private Criterion testDoubleEqualityCriteria(TestDouble d1) {
		return Restrictions.conjunction()
				.add(DaoBase.doubleEq("value1", d1.getValue1(), 0.1))
				.add(DaoBase.doubleEq("value2", d1.getValue2(), 0.1));
	}

}
