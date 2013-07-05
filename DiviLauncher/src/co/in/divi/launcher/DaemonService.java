package co.in.divi.launcher;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DaemonService extends Service {
	private static final String	TAG	= DaemonService.class.getName();

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The intent to launch when the user clicks the expanded notification
		// Intent homeIntent = new Intent(this, HomeActivity.class);
		// homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// PendingIntent pendIntent = PendingIntent.getActivity(this, 0, homeIntent, 0);
		Log.d(TAG, "starting service");

		Notification notice = new Notification.Builder(this).setContentTitle("Divi Launcher").setContentText("")
				.setSmallIcon(R.drawable.divi_logo).build();
		startForeground(1223, notice);

		new DaemonThread().start();

		return Service.START_STICKY;
	}

	class DaemonThread extends Thread {
		@Override
		public void run() {
			while (true) {
				ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				List<RunningTaskInfo> tasks = am.getRunningTasks(100);
				for (RunningTaskInfo task : tasks) {
					Log.d(TAG, "task:" + task.id + ",top:" + task.topActivity.getShortClassName());
				}
				Log.d(TAG,"===============================================================");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

}
