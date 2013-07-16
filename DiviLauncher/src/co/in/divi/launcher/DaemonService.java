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
import android.util.Log;

public class DaemonService extends Service {
	private static final String	TAG				= DaemonService.class.getName();

	private static final int	SLEEP_TIME		= 2000;							// polling interval

	DevicePolicyManager			mDPM;
	ComponentName				mDeviceAdmin;
	Notification				notice;
	PowerManager				powerManager;

	DaemonThread				daemonThread	= null;

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
		// registBroadcastReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		notice = new Notification.Builder(this).setContentTitle("Divi Launcher").setContentText("Blah").setSmallIcon(R.drawable.divi_logo)
				.build();
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
		// unregisterReceiver();
	}

	private BroadcastReceiver	mPowerKeyReceiver	= null;

	private void registBroadcastReceiver() {
		final IntentFilter theFilter = new IntentFilter();
		/** System Defined Broadcast */
		theFilter.addAction(Intent.ACTION_SCREEN_ON);
		theFilter.addAction(Intent.ACTION_SCREEN_OFF);

		mPowerKeyReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String strAction = intent.getAction();

				if (strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON)) {
					// > Your playground~!
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

	class DaemonThread extends Thread {
		@Override
		public void run() {
			while (true) {
				Log.d(TAG,"in loop...");
				// check if screen on
				boolean isScreenOn = powerManager.isScreenOn();
				if (isScreenOn) {
					ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
					List<RunningTaskInfo> tasks = am.getRunningTasks(100);
					if (tasks.size() > 0 && mDPM.isAdminActive(mDeviceAdmin)) {
						String pkgName = tasks.get(0).topActivity.getPackageName();
						if (!(pkgName.equals("co.in.divi.launcher") || pkgName.equals("co.in.divi") || pkgName
								.equals("com.android.settings"))) {
							Intent i = new Intent(DaemonService.this, HomeActivity.class);
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(i);
							mDPM.lockNow();
						}
					}
					for (RunningTaskInfo task : tasks) {
						Log.d(TAG, "task:" + task.id + ",top:" + task.topActivity.getShortClassName());
					}
					Log.d(TAG, "===============================================================");
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					Log.d(TAG, "we are interrupted!");
					break;
				}
			}
		}
	}
}
