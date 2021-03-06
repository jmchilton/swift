package edu.mayo.mprc.swift.dbmapping;

public class TaskStateData {
	private Long id;
	private String description;

	public TaskStateData() {
	}

	public TaskStateData(String description) {
		this.description = description;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof TaskStateData)) {
			return false;
		}

		TaskStateData that = (TaskStateData) o;

		if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (getDescription() != null ? getDescription().hashCode() : 0);
		return result;
	}


	public String toString() {
		return "TaskStateData{" +
				"id=" + getId() +
				", description='" + getDescription() + '\'' +
				'}';
	}
}
