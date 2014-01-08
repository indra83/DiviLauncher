package co.in.divi.launcher;

public class Config {

	public static final String	VERSION					= "6 Jan 2013";

	public static final boolean	DEBUG					= true;
	public static final boolean	DEBUG_DAEMON			= true;

	public static final String	APP_UPDATE_URL			= "https://dl.dropboxusercontent.com/u/5474079/divi_app_update.json";

	public static final int		SETTINGS_ACCESS_TIME	= 2 * 60 * 1000;														// 2
																																// mins
	public static final int		SLEEP_TIME				= 1000;																// polling
																																// interval

	// packages
	public static final String	APP_DIVI_LAUNCHER		= "co.in.divi.launcher";
	public static final String	APP_DIVI_MAIN			= "co.in.divi";
	public static final String	APP_SETTINGS			= "com.android.settings";
	public static final String	APP_RESOLVER			= "com.android.internal.app.ResolverActivity";
	public static final String	APP_INSTALL				= "com.android.packageinstaller";

}
