package co.in.divi.launcher;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DiviDeviceAdmin extends DeviceAdminReceiver {

	void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		showToast(context, "Divi Device Administration Enabled!");
		DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdmin = new ComponentName(context, DiviDeviceAdmin.class);
		mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
		mDPM.setPasswordMinimumLength(mDeviceAdmin, 4);
		mDPM.resetPassword(Config.DEFAULT_PASSWORD, 0);
		mDPM.setMaximumTimeToLock(mDeviceAdmin, 300 * 1000);
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		showToast(context, "Divi Device Administration Disabled!");
		if (System.currentTimeMillis() - AdminPasswordManager.getInstance().getLastAuthorizedTime() < Config.SETTINGS_ACCESS_TIME) {
			return;
		}

		// Not reliable!
		// showToast(context, "Wiping Device!!");
		// DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		// mDPM.wipeData(0);
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		showToast(context, "Divi Device Administration - about to be disabled!");
		// if (System.currentTimeMillis() - AdminPasswordManager.getInstance().getLastAuthorizedTime() <
		// Config.SETTINGS_ACCESS_TIME) {
		// return "Go Divi!";
		// }
		Intent i = new Intent(context, WipeActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		return "Warning! Divi will be uninstalled!";
	}

}
