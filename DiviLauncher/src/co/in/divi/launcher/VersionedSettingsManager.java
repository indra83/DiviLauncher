package co.in.divi.launcher;

import android.content.Context;
import android.os.Build;

public abstract class VersionedSettingsManager {
	
	Context context;
	
	public static VersionedSettingsManager newInstance(Context context) {
		final int sdkVersion = Build.VERSION.SDK_INT;
		if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return new V17SettingsManager(context);
		}else {
			return new OldSettingsManager(context);
		}
	}
	
	public abstract boolean getADBEnabled();
	
	public abstract void setADBEnabled(boolean enabled);

}
