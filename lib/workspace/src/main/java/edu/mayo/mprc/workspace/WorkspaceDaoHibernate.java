package edu.mayo.mprc.workspace;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.util.*;

public final class WorkspaceDaoHibernate extends DaoBase implements WorkspaceDao, RuntimeInitializer {
	private static final Logger LOGGER = Logger.getLogger(WorkspaceDaoHibernate.class);

	public WorkspaceDaoHibernate() {
	}

	public WorkspaceDaoHibernate(DatabasePlaceholder databasePlaceholder) {
		super(databasePlaceholder);
	}

	@Override
	public Collection<String> getHibernateMappings() {
		return Arrays.asList(
				"edu/mayo/mprc/database/Change.hbm.xml",
				"edu/mayo/mprc/workspace/User.hbm.xml");
	}

	/**
	 * We are sorting users by first and then last name. Display the names in "first last" order for a natural, easy to
	 * scan list.
	 *
	 * @return List of users in "first last" order.
	 */
	@Override
	public List<User> getUsers() {
		try {
			return (List<User>)
					allCriteria(User.class)
							.addOrder(org.hibernate.criterion.Order.asc("firstName"))
							.addOrder(org.hibernate.criterion.Order.asc("lastName"))
							.setReadOnly(true)
							.list();
		} catch (Exception t) {
			throw new MprcException("Cannot obtain list of users", t);
		}
	}

	@Override
	public User getUserByEmail(String email) {
		try {
			return get(User.class, Restrictions.eq("userName", email).ignoreCase());
		} catch (Exception t) {
			throw new MprcException("Cannot find user with e-mail [" + email + "]", t);
		}
	}


	@Override
	public User addNewUser(final String firstName, final String lastName, final String email, final Change change) {
		try {
			User user = new User(firstName, lastName, email, "database");
			user = save(user, getUserEqualityCriteria(user), true);
			return user;
		} catch (Exception t) {
			throw new MprcException("Cannot create new user " + firstName + " " + lastName, t);
		}
	}

	private Criterion getUserEqualityCriteria(User user) {
		return Restrictions.eq("userName", user.getUserName());
	}

	@Override
	public String check(Map<String, String> params) {
		if (countAll(User.class) == 0) {
			return "At least one user has to be defined";
		}
		if (getUsersNoInitials().size() > 0) {
			return "There are users with no initials defined";
		}
		if (getUsersWithRights().size() > 0) {
			return "There are users with legacy user rights defined";
		}
		return null;
	}

	@Override
	public void initialize(Map<String, String> params) {
		if (countAll(User.class) == 0) {
			User user = new User("Mprc", "Test", "mprctest@localhost", "mt", "database");
			save(user, new Change("Creating a test user - no users were defined", new DateTime()), getUserEqualityCriteria(user), true);
		}

		addUserInitials();
		updateUserRights();
	}

	private void updateUserRights() {
		List<User> users = this.getUsersWithRights();
		if (users.size() > 0) {
			LOGGER.info("Updating user rights");
			for (User user : users) {
				user.setParameterEditorEnabled(user.isParameterEditorEnabled());
				user.setOutputPathChangeEnabled(user.isOutputPathChangeEnabled());
				LOGGER.info("User " + user.getUserName() + ": "
						+ (user.isParameterEditorEnabled() ? "Parameter editor, " : "")
						+ (user.isOutputPathChangeEnabled() ? "Output path, " : ""));
				user.setRights(null);
			}
		}
	}

	private void addUserInitials() {
		// Update initials
		List<User> users = getUsersNoInitials();
		if (users.size() > 0) {
			LOGGER.info("Updating user initials");
			int count = 0;

			for (User user : users) {
				user.setInitials((user.getFirstName().charAt(0) + "" + user.getLastName().charAt(0)).toLowerCase(Locale.ENGLISH));
				LOGGER.info("User " + (++count) + " updated. User initials: " + user.getInitials());
			}
		}
	}

	private List<User> getUsersNoInitials() {
		try {
			return (List<User>) allCriteria(User.class)
					.add(Restrictions.isNull("initials"))
					.list();
		} catch (Exception t) {
			throw new MprcException("Cannot obtain list of users", t);
		}
	}

	private List<User> getUsersWithRights() {
		try {
			return (List<User>)
					allCriteria(User.class)
							.add(Restrictions.isNotNull("rights"))
							.add(Restrictions.ne("rights", 0L))
							.list();
		} catch (Exception t) {
			throw new MprcException("Cannot obtain list of users", t);
		}
	}
}
