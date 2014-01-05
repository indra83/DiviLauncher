package co.in.divi.launcher;

public class AdminPasswordManager {
	private static final String			TAG			= "AdminPasswordManager";

	private static AdminPasswordManager	instance	= null;

	public static AdminPasswordManager getInstance() {
		if (instance == null) {
			instance = new AdminPasswordManager();
		}
		return instance;
	}

	private long	timestamp;

	private AdminPasswordManager() {
		timestamp = 0L;
	}

	public boolean isAuthorized(int challenge, int response) {
		return (((challenge * 997) + 7919) % 10000 == response);
	}

	public void setLastAuthorizedTime(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getLastAuthorizedTime() {
		return timestamp;
	}

}
