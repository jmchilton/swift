package edu.mayo.mprc.database;

import java.util.HashSet;
import java.util.Set;

public class TestSet extends PersistableBase {

	private String setName;
	private Set<TestSetMember> members;

	public TestSet() {
		members = new HashSet<TestSetMember>();
	}

	public String getSetName() {
		return setName;
	}

	public void setSetName(String setName) {
		this.setName = setName;
	}

	public Set<TestSetMember> getMembers() {
		return members;
	}

	public void setMembers(Set<TestSetMember> members) {
		this.members = members;
	}

	public void add(TestSetMember member) {
		members.add(member);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		TestSet testSet = (TestSet) obj;

		if (getMembers() != null ? !getMembers().equals(testSet.getMembers()) : testSet.getMembers() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getMembers() != null ? getMembers().hashCode() : 0;
	}
}
