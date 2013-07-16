package co.in.divi.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings.SettingNotFoundException;

@SuppressLint("NewApi")
public class V17SettingsManager extends VersionedSettingsManager {

	public V17SettingsManager(Context context) {
		this.context = context;
	}

	@Override
	public boolean getADBEnabled() {
		try {
			return android.provider.Settings.Global.getInt(context.getContentResolver(),
					android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) > 0;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public void setADBEnabled(boolean enabled) {
		android.provider.Settings.Global.putInt(context.getContentResolver(),
				android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, enabled ? 1 : 0);
	}

}
