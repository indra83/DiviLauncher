package co.in.divi.llauncher;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

/**
 * Created by indra83 on 9/27/15.
 */
public class LaunchAppReceiver extends BroadcastReceiver {
    private static final String TAG = LaunchAppReceiver.class.getSimpleName();

    private static final String INTENT_EXTRA_PACKAGE = "INTENT_EXTRA_PACKAGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            LLApplication app = (LLApplication) context.getApplicationContext();
            DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName mDeviceAdminRcvr = new ComponentName(context, DAR.class);
            blockAllApps(app, mDPM, mDeviceAdminRcvr);

            String pkgToLaunch = intent.getStringExtra(INTENT_EXTRA_PACKAGE);
            if (pkgToLaunch != null) {
                Log.d(TAG, "unblocking - " + pkgToLaunch);
                mDPM.setApplicationHidden(mDeviceAdminRcvr, pkgToLaunch, false);

                Log.d(TAG, "launching - " + pkgToLaunch);
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(pkgToLaunch));
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            Log.w(TAG, "error launching app", e);
            Toast.makeText(context, "Error launching app", Toast.LENGTH_SHORT).show();
        }
    }

    private void blockAllApps(LLApplication app, DevicePolicyManager mDPM, ComponentName mDeviceAdminRcvr) {
        Log.d(TAG, "blocking all apps");
        PackageManager packageManager = app.getPackageManager();
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(0)) {
            if (!app.allowedPackages.contains(packageInfo.packageName)) {
                Log.d(TAG, "blocking: " + packageInfo.packageName);
                mDPM.setApplicationHidden(mDeviceAdminRcvr, packageInfo.packageName, true);
            }
        }
    }
}
