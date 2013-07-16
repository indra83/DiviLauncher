package co.in.divi.launcher;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class HomeActivity extends Activity {
	private static final String	TAG	= HomeActivity.class.getName();

	VersionedSettingsManager	settingsManager;
	DevicePolicyManager			mDPM;
	ComponentName				mDeviceAdmin;

	/************************ DEVELOPMENT ************************/
	CheckBox					adb;
	CheckBox					admin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdmin = new ComponentName(this, DiviDeviceAdmin.class);
		settingsManager = VersionedSettingsManager.newInstance(this);
		setContentView(R.layout.activity_home);

		Util.fixBackgroundRepeat(findViewById(R.id.root));

		// set alram
		LockChecker.scheduleAlarms(this);

		/************************ DEVELOPMENT ************************/
		findViewById(R.id.reboot).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((PowerManager) getSystemService(POWER_SERVICE)).reboot("divi");
			}
		});
		findViewById(R.id.crash).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				throw new RuntimeException();
			}
		});
		findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Settings.ACTION_SETTINGS));
			}
		});
		findViewById(R.id.lock).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDPM.setCameraDisabled(mDeviceAdmin, true);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
					mDPM.setKeyguardDisabledFeatures(mDeviceAdmin, DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL);
				mDPM.lockNow();
			}
		});
		findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK);
				i.setType("image/*");
				startActivity(i);
			}
		});

		adb = (CheckBox) findViewById(R.id.adb);
		admin = (CheckBox) findViewById(R.id.admin);

		((TextView) findViewById(R.id.version)).setText("adb fix");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "sending service start intent");

		/************************ DEVELOPMENT ************************/
		// startService(new Intent(this, DaemonService.class));
		adb.setOnCheckedChangeListener(null);
		if (settingsManager.getADBEnabled())
			adb.setChecked(true);
		else
			adb.setChecked(false);
		adb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.d(TAG, "trying to change settings");
				settingsManager.setADBEnabled(isChecked);
			}
		});
		admin.setOnCheckedChangeListener(null);
		if (mDPM.isAdminActive(mDeviceAdmin))
			admin.setChecked(true);
		else
			admin.setChecked(false);
		admin.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.d(TAG, "trying to change device admin setting");
				if (isChecked) {
					// Launch the activity to have the user enable our admin.
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
					intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Set Divi Device Administration");
					startActivity(intent);
				} else {
					mDPM.removeActiveAdmin(mDeviceAdmin);
				}
			}
		});
	}
}
