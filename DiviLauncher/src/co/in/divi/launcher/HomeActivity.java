package co.in.divi.launcher;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
import android.provider.Settings;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	private static final String	TAG			= HomeActivity.class.getName();

	VersionedSettingsManager	settingsManager;
	DevicePolicyManager			mDPM;
	ComponentName				mDeviceAdmin;

	Handler						handler;
	Timer						timer		= new Timer();

	AlertDialog					settingsAD, updateAD;

	int							backCount	= 0;

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

		try {
			Util.fixBackgroundRepeat(findViewById(R.id.root));
		} catch (Exception e) {
			Log.w(TAG, "not important!", e);
		}

		// set alram
		LockChecker.scheduleAlarms(this);

		findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AdminPasswordManager.getInstance().hasTriedUpdating()) {
					if (mDPM.isAdminActive(mDeviceAdmin))
						mDPM.lockNow();
					return;
				}
				AdminPasswordManager.getInstance().setTriedUpdating();
				Intent i = new Intent();
				i.setAction("co.in.divi.launcher.DOWNLOAD_APP");
				startActivity(i);
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
									if (mDPM.isAdminActive(mDeviceAdmin))
										mDPM.lockNow();
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

		String versionName = "--";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.w(TAG, "error getting version name", e);
		}
		((TextView) findViewById(R.id.version)).setText(versionName);
	}

	@Override
	public void onBackPressed() {
		backCount++;
		if (backCount % 10 == 3)
			Toast.makeText(this, ":-)", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onStart() {
		super.onStart();

		try {
			StringBuilder statusTextBuilder = new StringBuilder();
			if (settingsManager.getADBEnabled()) {
				statusTextBuilder.append("ADB  : <font color='red'>On!</font><br/>");
			} else {
				statusTextBuilder.append("ADB  : <font color='green'>Ok</font><br/>");
			}
			if (settingsManager.isAutoTime() && !settingsManager.isAutoZone()) {
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
		if (!mDPM.isAdminActive(mDeviceAdmin)) {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Set Divi Device Administration");
			startActivity(intent);
			finish();
			return;
		}
		if (!Util.isMyLauncherDefault(this)) {
			Toast.makeText(this, "Make Divi the default home screen", Toast.LENGTH_LONG).show();
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			startActivity(i);
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
					try {
						i = manager.getLaunchIntentForPackage("co.in.divi");
						if (i == null)
							throw new PackageManager.NameNotFoundException();
						i.addCategory(Intent.CATEGORY_LAUNCHER);
						startActivity(i);
					} catch (Exception e2) {
						Log.e(TAG, "error launching divi (second time)", e);
						timer.cancel();// don't try again
					}
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
