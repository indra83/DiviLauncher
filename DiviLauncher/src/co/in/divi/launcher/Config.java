package co.in.divi.launcher;

public class Config {

	public static final boolean	DEBUG						= true;
	public static final boolean	DEBUG_DAEMON				= false;

	public static final String	APP_UPDATE_URL				= "https://dl.dropboxusercontent.com/u/5474079/dawn/divi_app_update.json";

	public static final int		SETTINGS_ACCESS_TIME		= 2 * 60 * 1000;															// 2
																																		// mins
	public static final int		SLEEP_TIME					= 500;																		// polling
																																		// interval
	public static final int		LOCK_RECHECK_DELAY			= 200;
	public static final int		LAUNCH_DIVI_TIMER			= 3 * 1000;
	public static final int		UNLOCK_SCREEN_RELOCK_DELAY	= 3 * 1000;

	// packages
	public static final String	APP_DIVI_LAUNCHER			= "co.in.divi.launcher";
	public static final String	APP_DIVI_MAIN				= "co.in.divi";
	public static final String	APP_SETTINGS				= "com.android.settings";
	public static final String	APP_RESOLVER				= "com.android.internal.app.ResolverActivity";
	public static final String	APP_INSTALL					= "com.android.packageinstaller";

}
