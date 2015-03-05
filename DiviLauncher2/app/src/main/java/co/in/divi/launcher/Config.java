package co.in.divi.launcher;

import android.net.Uri;

public class Config {

	public static final boolean	DEBUG							= false;
	public static final boolean	DEBUG_DAEMON					= false;
	public static final boolean	DEMO_MODE						= false;

	public static final String	DEFAULT_PASSWORD				= "divi";
	public static final String	HARD_PASSWORD					= "superDogSnowy";

//	public static final String	APP_UPDATE_URL					= "https://dl.dropboxusercontent.com/u/5474079/demo/divi_app_update.json";
    public static final String	APP_UPDATE_URL					= "https://dl.dropboxusercontent.com/u/5474079/poranki/divi_app_update.json";

	public static final int		SETTINGS_ACCESS_TIME			= 2 * 60 * 1000;

	public static final int		SLEEP_TIME_LONG					= 500;
	public static final int		SLEEP_TIME_SHORT				= 300;
	public static final int		SUSPICIOUS_ACTIVITY_ALERT_TIME	= 30 * 1000;

	public static final int		LAUNCH_DIVI_TIMER				= 3 * 1000;
	public static final int		UNLOCK_SCREEN_RELOCK_DELAY		= 3 * 1000;

	public static final int		INSTALLER_ALLOW_DURATION		= 10 * 1000;

	// packages
	public static final String	APP_DIVI_MAIN					= "co.in.divi";
	public static final String	APP_DIVI_LAUNCHER				= "co.in.divi.launcher";
	public static final String	APP_ANDROID						= "android";
	public static final String	APP_ANDROID_SYSTEMUI			= "com.android.systemui";
	public static final String	APP_DIVI_VM_PREFIX				= "co.in.divi.vms";
	public static final String	APP_SETTINGS					= "com.android.settings";
	public static final String	APP_RESOLVER					= "com.android.internal.app.ResolverActivity";
	public static final String	APP_INSTALLER					= "com.android.packageinstaller";

	// allowed apps
	public static final Uri		ALLOWED_APPS_URI				= Uri.parse("content://co.in.divi.allowedapps/apps");
	public static final String	ALLOWED_APP_PACKAGE_COLUMN		= "package";

    public static final long THRESHOLD_USAGE_DURATION           = 2*1000; // 2 secs
}
