package co.in.divi.launcher;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

public class WipeActivity extends Activity {
	private static final String	TAG	= WipeActivity.class.getSimpleName();

	@Override
	protected void onStart() {
		super.onStart();

		ComponentName mDeviceAdmin = new ComponentName(this, DiviDeviceAdmin.class);
		DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (mDPM.isAdminActive(mDeviceAdmin)) {
			Log.d(TAG, "Still admin, wiping!!!");
			mDPM.wipeData(0);
		} else {
			Log.d(TAG, "wipe failed!");
		}

	}

}
