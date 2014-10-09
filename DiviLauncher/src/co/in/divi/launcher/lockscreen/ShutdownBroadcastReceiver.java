package co.in.divi.launcher.lockscreen;

import co.in.divi.launcher.Config;
import co.in.divi.launcher.DiviDeviceAdmin;
import co.in.divi.launcher.Util;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {
	private static final String	TAG	= ShutdownBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Config.DEBUG)
			Log.d(TAG, "intent: " + intent.getAction());
		try {
			DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName mDeviceAdmin = new ComponentName(context, DiviDeviceAdmin.class);
			if (mDPM.isAdminActive(mDeviceAdmin)) {
				Util.setUnknownPassword(mDPM, mDeviceAdmin, 10);
			}
		} catch (Exception e) {
			Log.w("DiviLauncher", "error resetting password:", e);
		}

	}
}
