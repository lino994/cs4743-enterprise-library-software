package Authentication;

public class Session {
	public static int nextId = 1;
	
	private int sessionId;
	private User user;
	private UserType sessionType;
	
	public Session(User user){
		this.user = user;
		sessionId = nextId++;
		sessionType = user.getUserType();
	}

	public static int getNextId() {
		return nextId;
	}

	public static void setNextId(int nextId) {
		Session.nextId = nextId;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public UserType getSessionType() {
		return sessionType;
	}

	public void setSessionType(UserType sessionType) {
		this.sessionType = sessionType;
	}
	
	
}
