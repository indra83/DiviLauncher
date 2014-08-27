package co.in.divi.launcher;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
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
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		showToast(context, "Divi Device Administration Disabled!");
		DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDPM.wipeData(0);
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		showToast(context, "Divi Device Administration - about to be disabled!");
		DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		Intent i = new Intent(context, HomeActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		mDPM.lockNow();
		return "Warning! Divi will be uninstalled!";
	}

}
