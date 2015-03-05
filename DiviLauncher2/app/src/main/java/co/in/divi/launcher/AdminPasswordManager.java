package co.in.divi.launcher;

public class AdminPasswordManager {
    private static final String TAG = "AdminPasswordManager";

    private static AdminPasswordManager instance = null;

    public static AdminPasswordManager getInstance() {
        if (instance == null) {
            instance = new AdminPasswordManager();
        }
        return instance;
    }

    private long timestamp;

    private long installerIgnoreStartTime;

    private int triedUpdating;

    private AdminPasswordManager() {
        timestamp = 0L;
        installerIgnoreStartTime = 0L;
        triedUpdating = 0;
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

    public void setInstallerIgnoreStartTime() {
        installerIgnoreStartTime = System.currentTimeMillis();
    }

    public boolean ignoreInstaller() {
        return System.currentTimeMillis() - installerIgnoreStartTime < Config.INSTALLER_ALLOW_DURATION;
    }

    public boolean hasTriedUpdating() {
        return triedUpdating>3;
    }

    public void setTriedUpdating() {
        triedUpdating++;
    }

}
