package co.in.divi.llauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = BootBroadcastReceiver.class.getSimpleName();

    public static boolean receivedBootEvent = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "on boot complete!");
        receivedBootEvent = true;
        LLApplication app = (LLApplication) context.getApplicationContext();
        if (app.isDeviceProvisioned() && !app.isBigBoss()) {
            Log.d(TAG, "resetting app hiding");
            Intent i = new Intent();
            i.setAction(Config.LAUNCH_ACTION);
            context.sendBroadcast(i);
        }
    }
}