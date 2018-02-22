package AuthenticationDB;

import javax.ejb.Remote;

import Authentication.Session;
import Authentication.User;
import Authentication.UserType;

@Remote
public interface AuthenticatorRemote {
	//public boolean hasAccess(UserType sessionId);
	public void addUsersToDB();
	public User getUserBySession(User user);
	public Session loginCheck(String uName, String pHash);
	public void logout(Session session);
}
