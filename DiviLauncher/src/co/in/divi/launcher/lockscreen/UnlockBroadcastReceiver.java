package co.in.divi.launcher.lockscreen;

import co.in.divi.launcher.DaemonService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UnlockBroadcastReceiver extends android.content.BroadcastReceiver {
	public static boolean	wasScreenOn	= true;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Boot complete", "on boot complete! - locking screen");
		showLockScreen(context);
		context.startService(new Intent(context, DaemonService.class));
	}

	private void showLockScreen(Context context) {
		Intent intent = new Intent(context, UnlockScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(intent);
	}
}