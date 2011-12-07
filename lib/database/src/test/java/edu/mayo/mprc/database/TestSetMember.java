package edu.mayo.mprc.database;

public class TestSetMember extends PersistableBase {

	private String memberName;

	public TestSetMember() {
	}

	public TestSetMember(String memberName) {
		this.memberName = memberName;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof TestSetMember)) {
			return false;
		}

		TestSetMember that = (TestSetMember) o;

		if (getMemberName() != null ? !getMemberName().equals(that.getMemberName()) : that.getMemberName() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getMemberName() != null ? getMemberName().hashCode() : 0;
	}
}
