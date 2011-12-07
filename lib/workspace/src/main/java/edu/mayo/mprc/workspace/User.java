package edu.mayo.mprc.workspace;

import edu.mayo.mprc.database.EvolvableBase;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class User extends EvolvableBase implements Serializable {
	private static final long serialVersionUID = 20100601L;

	private String firstName;
	private String lastName;
	private String userPassword;
	private String userName;
	private String initials;
	private Map<String, String> preferences = new HashMap<String, String>();

	/**
	 * Binary representation of rights the user is given.
	 */
	private Long rights;

	/**
	 * The "right to not see" parameter editor. Specified negatively, as the default is to see the editor.
	 */
	private static final String PARAMETER_EDITOR_DISABLED = "param.editor.disabled";

	/**
	 * Users with this right can change the output path of their searches.
	 */
	private static final String OUTPUT_PATH_CHANGEABLE = "output.path.changeable";

	public User() {
	}

	public User(String firstName, String lastName, String userName, String userPassword) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.initials = firstName.charAt(0) + "" + lastName.charAt(0);
		this.userPassword = userPassword;
		this.rights = 0L;
	}

	public User(String firstName, String lastName, String userName, String initials, String userPassword) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.initials = initials;
		this.userPassword = userPassword;
		this.rights = 0L;
	}

	/**
	 * Copy constructor.
	 */
	public User(User copyFrom) {
		this.firstName = copyFrom.firstName;
		this.lastName = copyFrom.lastName;
		this.userName = copyFrom.userName;
		this.userPassword = copyFrom.userPassword;
		this.rights = copyFrom.rights;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getInitials() {
		if (initials != null && initials.length() > 0) {
			return initials;
		} else {
			return (firstName.charAt(0) + "" + lastName.charAt(0)).toLowerCase(Locale.ENGLISH);
		}
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}

	void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	/**
	 * @param key   Preference key to set.
	 * @param value Preference value to set. When set to <code>null</code>, the preference is removed.
	 */
	void addPreference(String key, String value) {
		if (value == null) {
			this.preferences.remove(key);
		} else {
			this.preferences.put(key, value);
		}
	}

	String preferenceValue(String key) {
		return this.preferences.get(key);
	}

	/**
	 * @deprecated use the getPreference/setPreference API
	 */
	public Long getRights() {
		if (rights == null) {
			return 0L;
		}
		return rights;
	}

	/**
	 * @deprecated use the getPreference/setPreference API
	 */
	public void setRights(Long rights) {
		this.rights = rights;
	}

	/**
	 * @return <code>true</code> if the user can use the parameter editor.
	 */
	public boolean isParameterEditorEnabled() {
		return preferenceValue(PARAMETER_EDITOR_DISABLED) == null;
	}

	public boolean isOutputPathChangeEnabled() {
		return preferenceValue(OUTPUT_PATH_CHANGEABLE) != null;
	}

	/**
	 * By default, the editor is enabled.
	 *
	 * @param enabled True if the user can edit parameter sets.
	 */
	public void setParameterEditorEnabled(boolean enabled) {
		addPreference(PARAMETER_EDITOR_DISABLED, enabled ? null : "1");
	}

	/**
	 * @param enabled Set to true to enable the users to change the output directory where Swift puts its results.
	 */
	public void setOutputPathChangeEnabled(boolean enabled) {
		addPreference(OUTPUT_PATH_CHANGEABLE, enabled ? "1" : null);
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof User)) {
			return false;
		}

		User that = (User) o;

		if (getFirstName() != null ? !getFirstName().equals(that.getFirstName()) : that.getFirstName() != null) {
			return false;
		}
		if (getLastName() != null ? !getLastName().equals(that.getLastName()) : that.getLastName() != null) {
			return false;
		}
		if (getUserName() != null ? !getUserName().equals(that.getUserName()) : that.getUserName() != null) {
			return false;
		}
		if (getUserPassword() != null ? !getUserPassword().equals(that.getUserPassword()) : that.getUserPassword() != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (getFirstName() != null ? getFirstName().hashCode() : 0);
		result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
		result = 31 * result + (getUserPassword() != null ? getUserPassword().hashCode() : 0);
		result = 31 * result + (getUserName() != null ? getUserName().hashCode() : 0);
		return result;
	}

	public String toString() {
		return MessageFormat.format("{0}: {1} {2} - {3}",
				this.getId(),
				this.getFirstName(),
				this.getLastName(),
				this.getUserName());
	}
}

