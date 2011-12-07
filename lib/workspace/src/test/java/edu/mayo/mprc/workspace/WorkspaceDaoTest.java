package edu.mayo.mprc.workspace;

import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.database.DatabaseUtilities;
import org.hibernate.SessionFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public final class WorkspaceDaoTest {
	private WorkspaceDao workspaceDao;
	private SessionFactory factory;

	@BeforeClass
	public void setup() {
		factory = DatabaseUtilities.getTestSessionFactory(
				Arrays.asList(
						"edu/mayo/mprc/database/Change.hbm.xml",
						"edu/mayo/mprc/workspace/User.hbm.xml")
		);
		DatabasePlaceholder databasePlaceholder = new DatabasePlaceholder();
		databasePlaceholder.setSessionFactory(factory);
		workspaceDao = new WorkspaceDaoHibernate(databasePlaceholder);
	}

	@AfterClass
	public void teardown() {
		factory.close();
	}

	@Test
	public void getUserNamesTest() throws Throwable {
		workspaceDao.begin();
		try {
			List<User> users = workspaceDao.getUsers();
			workspaceDao.commit();
			Assert.assertTrue((users != null && users.size() == 0), "no user names should be found");
		} catch (Exception t) {
			workspaceDao.rollback();
			throw t;
		}
	}

	@Test(dependsOnMethods = "getUserNamesTest")
	public void listUsersTest() throws Throwable {
		workspaceDao.begin();
		try {
			Change change = new Change("Test user added", new Date());
			workspaceDao.addNewUser("Roman", "Zenka", "zenka.roman@mayo.edu", change);
			final List<User> list = workspaceDao.getUsers();
			workspaceDao.commit();
			Assert.assertEquals(list.size(), 1, "One user has to be defined");
			final User user = list.get(0);
			Assert.assertEquals(user.getFirstName(), "Roman");
		} catch (Exception t) {
			workspaceDao.rollback();
			throw t;
		}
	}

	@Test(dependsOnMethods = "listUsersTest")
	public void preferencesTest() throws Throwable {
		workspaceDao.begin();
		try {
			final List<User> list = workspaceDao.getUsers();
			final User user = list.get(0);
			user.addPreference("likes", "coffee");
			workspaceDao.commit();
		} catch (Exception t) {
			workspaceDao.rollback();
			throw t;
		}

		workspaceDao.begin();
		try {
			final List<User> list = workspaceDao.getUsers();
			final User user = list.get(0);
			Assert.assertEquals(user.getPreferences().get("likes"), "coffee");
			workspaceDao.commit();
		} catch (Exception t) {
			workspaceDao.rollback();
			throw t;
		}
	}
}
