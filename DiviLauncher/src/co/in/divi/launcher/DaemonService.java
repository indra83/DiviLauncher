package co.in.divi.launcher;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import co.in.divi.launcher.lockscreen.UnlockScreenActivity;

public class DaemonService extends Service {
	private static final String	TAG					= DaemonService.class.getName();

	DevicePolicyManager			mDPM;
	ComponentName				mDeviceAdmin;
	Notification				notice;
	PowerManager				powerManager;
	AdminPasswordManager		adminPasswordManager;

	DaemonThread				daemonThread		= null;

	// state data
	private boolean				isLockingEnabled	= false;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdmin = new ComponentName(this, DiviDeviceAdmin.class);
		if (mDPM.isAdminActive(mDeviceAdmin))
			mDPM.setCameraDisabled(mDeviceAdmin, true);
		adminPasswordManager = AdminPasswordManager.getInstance();
		isLockingEnabled = mDPM.isAdminActive(mDeviceAdmin) && Util.isMyLauncherDefault(DaemonService.this);
		registBroadcastReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		notice = new NotificationCompat.Builder(this).setContentTitle("Divi Launcher").setContentText("Blah")
				.setSmallIcon(R.drawable.divi_logo).build();
		if (daemonThread == null || !daemonThread.isAlive()) {
			Log.d(TAG, "creating new thread");
			daemonThread = new DaemonThread();
			daemonThread.start();
		}

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		daemonThread.interrupt();
		unregisterReceiver();
	}

	private BroadcastReceiver	mPowerKeyReceiver	= null;

	private void registBroadcastReceiver() {
		final IntentFilter theFilter = new IntentFilter();
		/** System Defined Broadcast */
		theFilter.addAction(Intent.ACTION_SCREEN_ON);
		// theFilter.addAction(Intent.ACTION_SCREEN_OFF);

		mPowerKeyReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String strAction = intent.getAction();
				if (Config.DEBUG_DAEMON)
					Log.d(TAG, "got event!!");
				if (!isLockingEnabled)
					return;
				if (strAction.equals(Intent.ACTION_SCREEN_OFF)) {
					Log.d(TAG, "screen off");
				} else if (strAction.equals(Intent.ACTION_SCREEN_ON)) {
					Log.d(TAG, "screen on");
					ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
					List<RunningTaskInfo> tasks = am.getRunningTasks(10);
					if (tasks.size() > 0) {
						String activityName = tasks.get(0).topActivity.flattenToString();
						Log.d(TAG, "top act: " + activityName);
						// ignore if its notification activity..
						if (activityName.equalsIgnoreCase("co.in.divi/co.in.divi.activity.InstructionNotificationActivity"))
							return;
					}
					showLockScreen();
				}
			}
		};

		getApplicationContext().registerReceiver(mPowerKeyReceiver, theFilter);
	}

	private void unregisterReceiver() {
		try {
			getApplicationContext().unregisterReceiver(mPowerKeyReceiver);
		} catch (IllegalArgumentException e) {
			mPowerKeyReceiver = null;
		}
	}

	private void showLockScreen() {
		Intent intent = new Intent(this, UnlockScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivity(intent);
	}

	class DaemonThread extends Thread {
		@Override
		public void run() {
			// all variables here!
			int count = 0;
			long diff;
			String pkgName;
			isLockingEnabled = mDPM.isAdminActive(mDeviceAdmin) && Util.isMyLauncherDefault(DaemonService.this);
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasks = am.getRunningTasks(10);
			while (true) {
				count++;
				if (Config.DEBUG_DAEMON)
					Log.d(TAG, "in loop... - " + count);
				// check if screen on
				boolean isScreenOn = powerManager.isScreenOn();
				if (count % 3 == 0) {
					isLockingEnabled = mDPM.isAdminActive(mDeviceAdmin) && Util.isMyLauncherDefault(DaemonService.this);
					if (Config.DEBUG_DAEMON)
						Log.d(TAG, "checking locking enabled - " + isLockingEnabled);
				}
				if (isScreenOn) {
					tasks = am.getRunningTasks(10);
					if (tasks.size() > 0 && isLockingEnabled) {
						diff = System.currentTimeMillis() - adminPasswordManager.getLastAuthorizedTime();
						// Log.d(TAG, "diff - " + diff);
						if (diff < Config.SETTINGS_ACCESS_TIME) {
							// disable everything for 2 mins.
							if (Config.DEBUG_DAEMON)
								Log.w(TAG, "kiosk mode off!");
						} else {
							pkgName = tasks.get(0).topActivity.getPackageName();
							if (!(pkgName.equals(Config.APP_DIVI_LAUNCHER) || pkgName.equals(Config.APP_DIVI_MAIN))) {
								lockNow();
								// try {
								// Thread.sleep(Config.LOCK_RECHECK_DELAY);
								// } catch (InterruptedException e) {
								// Log.w(TAG, "we are interrupted!", e);
								// }
								// // wait and do the check again to ensure we are not in between transitions.
								// tasks = am.getRunningTasks(10);
								// if (tasks.size() > 0) {
								// pkgName = tasks.get(0).topActivity.getPackageName();
								// if (!(pkgName.equals(Config.APP_DIVI_LAUNCHER) ||
								// pkgName.equals(Config.APP_DIVI_MAIN))) {
								// lockNow();
								// }
								// }
							}
						}
					}
					if (Config.DEBUG_DAEMON) {
						for (RunningTaskInfo task : tasks) {
							Log.d(TAG, "task:" + task.id + ",top:" + task.topActivity.getPackageName());
						}
					}
					if (Config.DEBUG_DAEMON)
						Log.d(TAG, "===============================================================");
				}
				try {
					Thread.sleep(Config.SLEEP_TIME);
				} catch (InterruptedException e) {
					Log.w(TAG, "we are interrupted!", e);
					break;
				}
			}
		}
	}

	private void lockNow() {
		Intent i = new Intent(DaemonService.this, HomeActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		mDPM.lockNow();
	}
}
