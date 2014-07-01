package co.in.divi.launcher;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	private static final String	TAG		= HomeActivity.class.getName();

	VersionedSettingsManager	settingsManager;
	DevicePolicyManager			mDPM;
	ComponentName				mDeviceAdmin;

	Handler						handler;
	Timer						timer	= new Timer();

	/************************ DEVELOPMENT ************************/
	CheckBox					adb;
	CheckBox					admin;

	AlertDialog					settingsAD, updateAD;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdmin = new ComponentName(this, DiviDeviceAdmin.class);
		settingsManager = VersionedSettingsManager.newInstance(this);
		setContentView(R.layout.activity_home);

		findViewById(R.id.logo).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Config.DEBUG)
					Log.d(TAG, "launching divi");
				launchDivi();
			}
		});

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
		findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int challenge = new Random(System.currentTimeMillis()).nextInt(10000);
				final EditText input = new EditText(HomeActivity.this);
				input.setInputType(InputType.TYPE_CLASS_NUMBER);
				input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });
				timer.cancel();
				updateAD = new AlertDialog.Builder(HomeActivity.this).setTitle("Enter password")
						.setMessage("Enter the key for challenge: " + challenge).setView(input)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								int response = -1;
								try {
									response = Integer.parseInt(input.getText().toString());
								} catch (Exception e) {
								}
								if (AdminPasswordManager.getInstance().isAuthorized(challenge, response)) {
									AdminPasswordManager.getInstance().setLastAuthorizedTime(System.currentTimeMillis());
									Intent i = new Intent();
									i.setAction("co.in.divi.launcher.DOWNLOAD_APP");
									startActivity(i);
								} else {
									Toast.makeText(HomeActivity.this, "Authorization failed", Toast.LENGTH_SHORT).show();
									resetTimer();
								}
							}
						}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								resetTimer();
							}
						}).show();
			}
		});
		findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int challenge = new Random(System.currentTimeMillis()).nextInt(10000);
				final EditText input = new EditText(HomeActivity.this);
				input.setInputType(InputType.TYPE_CLASS_NUMBER);
				input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });
				timer.cancel();
				settingsAD = new AlertDialog.Builder(HomeActivity.this).setTitle("Enter password")
						.setMessage("Enter the key for challenge: " + challenge).setView(input)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								int response = -1;
								try {
									response = Integer.parseInt(input.getText().toString());
								} catch (Exception e) {
								}
								if (AdminPasswordManager.getInstance().isAuthorized(challenge, response)) {
									AdminPasswordManager.getInstance().setLastAuthorizedTime(System.currentTimeMillis());
									startActivity(new Intent(Settings.ACTION_SETTINGS));
								} else {
									Toast.makeText(HomeActivity.this, "Authorization failed", Toast.LENGTH_SHORT).show();
									resetTimer();
								}
							}
						}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								resetTimer();
							}
						}).show();
			}
		});
		findViewById(R.id.lock).setOnClickListener(new View.OnClickListener() {
			@SuppressLint("NewApi")
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

		((TextView) findViewById(R.id.version)).setText(Config.VERSION);

		// hide all
		if (true) {
			findViewById(R.id.reboot).setVisibility(View.GONE);
			findViewById(R.id.crash).setVisibility(View.GONE);
			findViewById(R.id.settings).setVisibility(View.VISIBLE);
			findViewById(R.id.lock).setVisibility(View.GONE);
			findViewById(R.id.gallery).setVisibility(View.GONE);
			findViewById(R.id.adb).setVisibility(View.GONE);
			findViewById(R.id.admin).setVisibility(View.GONE);
		}

		// show version
		String versionName = "--";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.w(TAG, "error getting version name", e);
		}
		((TextView) findViewById(R.id.version)).setText(versionName);
	}

	@Override
	protected void onStart() {
		super.onStart();

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
				if (Config.DEBUG)
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
		try {
			StringBuilder statusTextBuilder = new StringBuilder();
			if (settingsManager.getADBEnabled()) {
				statusTextBuilder.append("ADB  : <font color='red'>On!</font><br/>");
			} else {
				statusTextBuilder.append("ADB  : <font color='green'>Ok</font><br/>");
			}
			if(settingsManager.isAutoTime() && !settingsManager.isAutoZone()) {
				statusTextBuilder.append("Time : <font color='green'>Ok</font><br/>");
			} else {
				statusTextBuilder.append("Time : <font color='red'>Off!</font><br/>");
			}
			
			((TextView) findViewById(R.id.status)).setText(Html.fromHtml(statusTextBuilder.toString()));
		} catch (Exception e) {
			Log.w(TAG, "error showing status text", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		hideBars();
		if (!Util.isMyLauncherDefault(this)) {
			Toast.makeText(this, "Make Divi the default home screen", Toast.LENGTH_LONG).show();
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			startActivity(i);
			finish();
			// make sure we don't get into infinite lock screen
			mDPM.removeActiveAdmin(mDeviceAdmin);
			return;
		}
		if (!mDPM.isAdminActive(mDeviceAdmin)) {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Set Divi Device Administration");
			startActivity(intent);
			finish();
			return;
		}
		resetTimer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		timer.cancel();
		if (settingsAD != null && settingsAD.isShowing())
			settingsAD.dismiss();
		if (updateAD != null && updateAD.isShowing())
			updateAD.dismiss();
	}

	private void resetTimer() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				launchDivi();
			}
		}, Config.LAUNCH_DIVI_TIMER);
	}

	private void launchDivi() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Intent i;
				PackageManager manager = getPackageManager();
				try {
					i = manager.getLaunchIntentForPackage("co.in.divi");
					if (i == null)
						throw new PackageManager.NameNotFoundException();
					i.addCategory(Intent.CATEGORY_LAUNCHER);
					startActivity(i);
				} catch (Exception e) {
					Log.e(TAG, "error launching divi", e);
					Toast.makeText(HomeActivity.this, "Error launching Divi", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		hideBars();
	}

	private void hideBars() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			// WindowManager.LayoutParams.FLAG_FULLSCREEN);
			int newUiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
			getWindow().addFlags(newUiOptions);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		} else {
			int newUiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			newUiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
			newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
		}
	}
}
