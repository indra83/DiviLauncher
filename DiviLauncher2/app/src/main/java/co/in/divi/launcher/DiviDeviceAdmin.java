package co.in.divi.launcher;

import co.in.divi.launcher.lockscreen.BootBroadcastReceiver;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class DiviDeviceAdmin extends DeviceAdminReceiver {
	private static final String	TAG	= DiviDeviceAdmin.class.getSimpleName();

	void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (Config.DEBUG)
			Log.d(TAG, "got - " + intent.getAction());
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		showToast(context, "Divi Device Administration Enabled!");
		BootBroadcastReceiver.receivedBootEvent = true; // force this flag, otherwise we loose password.
		DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdmin = new ComponentName(context, DiviDeviceAdmin.class);
		Util.setKnownPassword(mDPM, mDeviceAdmin);
		// return to home
		Intent i = new Intent(context, HomeActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		// start daemon
		LockChecker.scheduleAlarms(context);
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		showToast(context, "Divi Device Administration Disabled!");
		if (System.currentTimeMillis() - AdminPasswordManager.getInstance().getLastAuthorizedTime() < Config.SETTINGS_ACCESS_TIME) {
			return;
		}
		if (Config.DEMO_MODE)
			return;
		showToast(context, "Not allowed!");
		DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdmin = new ComponentName(context, DiviDeviceAdmin.class);
		Util.setUnknownPassword(mDPM, mDeviceAdmin, 5);
		mDPM.lockNow();
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		showToast(context, "Divi Device Administration - about to be disabled!");
		if (System.currentTimeMillis() - AdminPasswordManager.getInstance().getLastAuthorizedTime() < Config.SETTINGS_ACCESS_TIME) {
			return "Go - Divi!";
		}
		return "Warning! - Continuing will wipe your device and remove Divi. Please contact your school principal to install Divi again!";
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		// showToast(context, "Password failed");
		// ComponentName daemonService = new ComponentName(context, DaemonService.class);
		// Log.d(TAG, "is daemon enabled? " + context.getPackageManager().getComponentEnabledSetting(daemonService));
		// context.startService(new Intent(context, DaemonService.class));
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		// showToast(context, "Password success");
	}

	@Override
	public void onPasswordExpiring(Context context, Intent intent) {
		// Log.d(TAG, "checking if our service is running...");
		if ((!BootBroadcastReceiver.receivedBootEvent) && (SystemClock.elapsedRealtime()<60*1000)) {
			Log.d(TAG, "Not running! - Probably in safe mode ? ");
			DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName mDeviceAdmin = new ComponentName(context, DiviDeviceAdmin.class);
			Util.setUnknownPassword(mDPM, mDeviceAdmin, 0);
			mDPM.lockNow();
		}
	}
}
