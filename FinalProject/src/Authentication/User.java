package Authentication;

import util.CryptoStuff;

public class User {
	private String name;
	private String userName;
	private String passHash;
	private UserType userType;
	
	public User(String realName, String user, String pass, UserType type, boolean isHashed){
		name = realName;
		userName = user;
		if(!isHashed){
			passHash = CryptoStuff.sha256(pass);
		}else{
			passHash = pass;
		}
		userType = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassHash() {
		return passHash;
	}

	public void setPassHash(String passHash) {
		this.passHash = passHash;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

}
