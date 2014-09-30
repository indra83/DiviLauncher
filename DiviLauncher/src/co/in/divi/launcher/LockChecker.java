package co.in.divi.launcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class LockChecker extends BroadcastReceiver {
	private static final String	TAG				= LockChecker.class.getSimpleName();

	private static final int	PERIOD			= 15000;						// 20 secs
	private static final int	INITIAL_DELAY	= 1000;						// 5 seconds

	@Override
	public void onReceive(Context context, Intent i) {
		Log.d(TAG, "onReceive");

		// ensure daemon is running
		context.startService(new Intent(context, DaemonService.class));
	}

	public static void scheduleAlarms(Context ctxt) {
		AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, LockChecker.class);
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + INITIAL_DELAY, PERIOD, pi);
	}
}
