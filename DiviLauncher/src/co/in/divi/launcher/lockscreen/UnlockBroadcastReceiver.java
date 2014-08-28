package co.in.divi.launcher.lockscreen;

import co.in.divi.launcher.Config;
import co.in.divi.launcher.DaemonService;
import co.in.divi.launcher.DiviDeviceAdmin;
import co.in.divi.launcher.LockChecker;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UnlockBroadcastReceiver extends android.content.BroadcastReceiver {
	public static boolean	wasScreenOn	= true;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Boot complete", "on boot complete! - locking screen");
		// showLockScreen(context);
		try {
			DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName mDeviceAdmin = new ComponentName(context, DiviDeviceAdmin.class);
			if (mDPM.isAdminActive(mDeviceAdmin)) {
				mDPM.resetPassword(Config.DEFAULT_PASSWORD, 0);
			}
		} catch (Exception e) {
			Log.w("DiviLauncher", "error resetting password:", e);
		}
		context.startService(new Intent(context, DaemonService.class));
		LockChecker.scheduleAlarms(context);
	}
}