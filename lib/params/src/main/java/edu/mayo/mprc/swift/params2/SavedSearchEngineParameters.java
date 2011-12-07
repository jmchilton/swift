package edu.mayo.mprc.swift.params2;

import edu.mayo.mprc.database.EvolvableBase;
import edu.mayo.mprc.workspace.User;

/**
 * A parameter set that was saved and named by a user.
 */
public class SavedSearchEngineParameters extends EvolvableBase {
	private String name;
	private User user;
	private SearchEngineParameters parameters;

	public SavedSearchEngineParameters() {
	}

	public SavedSearchEngineParameters(String name, User user, SearchEngineParameters parameters) {
		this.name = name;
		this.user = user;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public SearchEngineParameters getParameters() {
		return parameters;
	}

	public void setParameters(SearchEngineParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SavedSearchEngineParameters)) {
			return false;
		}

		SavedSearchEngineParameters that = (SavedSearchEngineParameters) o;

		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
			return false;
		}
		if (getParameters() != null ? !getParameters().equals(that.getParameters()) : that.getParameters() != null) {
			return false;
		}
		if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
		result = 31 * result + (getParameters() != null ? getParameters().hashCode() : 0);
		return result;
	}
}
