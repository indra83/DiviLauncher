package co.in.divi.launcher;

import android.content.Context;
import android.provider.Settings.SettingNotFoundException;

public class OldSettingsManager extends VersionedSettingsManager {

	public OldSettingsManager(Context context) {
		this.context = context;
	}

	@Override
	public boolean getADBEnabled() {
		try {
			return android.provider.Settings.Secure.getInt(context.getContentResolver(),
					android.provider.Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED) > 0;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public void setADBEnabled(boolean enabled) {
		android.provider.Settings.Secure.putInt(context.getContentResolver(),
				android.provider.Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, enabled ? 1 : 0);
	}

	@Override
	public boolean isAutoTime() {
		try {
			return android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.AUTO_TIME) > 0;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public boolean isAutoZone() {
		try {
			return android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.AUTO_TIME_ZONE) > 0;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
			return true;
		}
	}

}
