package edu.mayo.mprc.messaging;

import edu.mayo.mprc.MprcException;

import java.net.URI;

final class UserInfo {
	private String userName;
	private String password;

	public UserInfo(URI uri) {
		String userInfo = uri.getUserInfo();
		if ((userInfo == null) || userInfo.equals("")) {
			this.userName = null;
			this.password = null;
		} else {

			int index = userInfo.indexOf(':');
			if (index < 0) {
				throw new MprcException("The URI does not contain proper user name:password pair: " + uri.toString());
			}
			this.userName = userInfo.substring(0, index);
			this.password = userInfo.substring(index + 1);
		}
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
}
