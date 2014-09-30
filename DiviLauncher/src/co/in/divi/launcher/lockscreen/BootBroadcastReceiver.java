package co.in.divi.launcher.lockscreen;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import co.in.divi.launcher.DaemonService;
import co.in.divi.launcher.DiviDeviceAdmin;
import co.in.divi.launcher.LockChecker;
import co.in.divi.launcher.Util;

public class BootBroadcastReceiver extends BroadcastReceiver {
	private static final String	TAG	= BootBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "on boot complete!");
		// showLockScreen(context);
		try {
			DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName mDeviceAdmin = new ComponentName(context, DiviDeviceAdmin.class);
			if (mDPM.isAdminActive(mDeviceAdmin)) {
				Util.setKnownPassword(mDPM, mDeviceAdmin);
			}
		} catch (Exception e) {
			Log.w("DiviLauncher", "error resetting password:", e);
		}
		context.startService(new Intent(context, DaemonService.class));
		LockChecker.scheduleAlarms(context);
	}
}