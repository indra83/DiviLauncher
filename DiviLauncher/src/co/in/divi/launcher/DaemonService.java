package co.in.divi.launcher;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class DaemonService extends Service {
	private static final String					TAG						= DaemonService.class.getName();

	DevicePolicyManager							mDPM;
	ComponentName								mDeviceAdmin;
	Notification								notice;
	PowerManager								powerManager;
	AdminPasswordManager						adminPasswordManager;

	private ContentObserver						allowedAppsObserver;
	private ConcurrentHashMap<String, String>	allowedApps				= new ConcurrentHashMap<String, String>(108, 0.9f, 1);
	private FetchAllowedAppsTask				fetchAllowedAppsTask	= null;

	DaemonThread								daemonThread			= null;

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
		// registBroadcastReceiver();
		allowedAppsObserver = new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				Log.d(TAG, "content observer says it has new data");
				if (fetchAllowedAppsTask == null || fetchAllowedAppsTask.getStatus() == AsyncTask.Status.FINISHED) {
					Log.d(TAG, "starting fetch task");
					fetchAllowedAppsTask = new FetchAllowedAppsTask();
					fetchAllowedAppsTask.execute(new Void[0]);
				}
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		Intent i = new Intent(this, HomeActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		notice = new NotificationCompat.Builder(this).setContentTitle("Divi Launcher").setContentText("Blah")
				.setSmallIcon(R.drawable.divi_logo).setContentIntent(pi).setOngoing(true).build();

		startForeground(1337, notice);
		if (daemonThread == null || !daemonThread.isAlive()) {
			Log.d(TAG, "creating new thread");
			daemonThread = new DaemonThread();
			daemonThread.start();

			getContentResolver().registerContentObserver(Config.ALLOWED_APPS_URI, true, allowedAppsObserver);
		}

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		daemonThread.interrupt();
		// unregisterReceiver();
		getContentResolver().unregisterContentObserver(allowedAppsObserver);
	}

	class DaemonThread extends Thread {

		// state data
		private boolean	isLockingEnabled		= false;
		private long	suspiciousActivityStart	= 0L;

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
				if (count % 30 == 0) {
					isLockingEnabled = mDPM.isAdminActive(mDeviceAdmin) && Util.isMyLauncherDefault(DaemonService.this);
					if (Config.DEBUG_DAEMON)
						Log.d(TAG, "checking locking enabled - " + isLockingEnabled);
				}
				if (powerManager.isScreenOn()) {
					tasks = am.getRunningTasks(10);
					if (tasks.size() > 0 && isLockingEnabled) {
						diff = System.currentTimeMillis() - adminPasswordManager.getLastAuthorizedTime();
						// Log.d(TAG, "diff - " + diff);
						if (diff < Config.SETTINGS_ACCESS_TIME) {
							// disable everything for 2 mins.
							if (Config.DEBUG_DAEMON)
								Log.w(TAG, "kiosk mode off!");
							tasks = am.getRunningTasks(10);
							Log.d(TAG, "===============================================================");
							for (RunningTaskInfo task : tasks) {
								Log.d(TAG, "task:" + task.id + ",top:" + task.topActivity.getPackageName());
							}
							Log.d(TAG, "===============================================================");
						} else {
							pkgName = tasks.get(0).topActivity.getPackageName();
							if (!(pkgName.equals(Config.APP_DIVI_LAUNCHER) || pkgName.equals(Config.APP_DIVI_MAIN)
									|| pkgName.equals(Config.APP_INSTALLER) || pkgName.equals("zok.android.numbers")
									|| pkgName.equals("com.android.calculator2") || pkgName.equals("zok.android.dots") || pkgName
										.startsWith(Config.APP_DIVI_VM_PREFIX))) {
								if (pkgName.equals(Config.APP_INSTALLER) && adminPasswordManager.ignoreInstaller()) {
									// ignore installer - must be installing/updating divi
								} else {
									suspiciousActivityStart = System.currentTimeMillis();
									lockNow();
									try {
										for (RunningTaskInfo rt : tasks) {
											Log.d(TAG, "killing:" + rt.topActivity.getPackageName());
											if (rt.topActivity.getPackageName().equals(Config.APP_DIVI_LAUNCHER))
												continue;
											am.killBackgroundProcesses(rt.topActivity.getPackageName());
										}
									} catch (Exception e) {
										Log.w(TAG, "error killing bg process!");
									}
								}
							}
						}
					}
					// print
					if (Config.DEBUG_DAEMON) {
						for (RunningTaskInfo task : tasks) {
							Log.d(TAG, "task:" + task.id + ",top:" + task.topActivity.getPackageName());
						}
					}
					if (Config.DEBUG_DAEMON)
						Log.d(TAG, "===============================================================");
					// print
				}
				try {
					if (System.currentTimeMillis() - suspiciousActivityStart < Config.SUSPICIOUS_ACTIVITY_ALERT_TIME) {
						if (Config.DEBUG_DAEMON)
							Log.d(TAG, "small sleep");
						Thread.sleep(Config.SLEEP_TIME_SHORT);
					} else {
						if (Config.DEBUG_DAEMON)
							Log.d(TAG, "long sleep");
						Thread.sleep(Config.SLEEP_TIME_LONG);
					}

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

	class FetchAllowedAppsTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			Log.d(TAG, "starting fetch");
			Cursor cursor = getContentResolver().query(Config.ALLOWED_APPS_URI, null, null, null, null);
			Log.d(TAG, "got apps: " + cursor);
			allowedApps.clear();
			if (cursor != null) {
				Log.d(TAG, "got apps: " + cursor.getCount());
				int packageIndex = cursor.getColumnIndex(Config.ALLOWED_APP_PACKAGE_COLUMN);
				while (cursor.moveToNext()) {
					String pkg = cursor.getString(packageIndex);
					Log.d(TAG, "got allowed app: " + pkg);
					allowedApps.put(pkg, pkg);
				}
			}
			return null;
		}
	}
}
