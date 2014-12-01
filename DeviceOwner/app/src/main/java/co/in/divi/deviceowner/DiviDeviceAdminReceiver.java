package co.in.divi.deviceowner;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by indraneel on 23-11-2014.
 */
public class DiviDeviceAdminReceiver extends DeviceAdminReceiver {
    public static final String TAG = DiviDeviceAdminReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "onReceive:" + intent.getAction());
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
    }

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        super.onProfileProvisioningComplete(context, intent);
        Log.d(TAG, "onProfileProvisioningComplete");

        Intent i = new Intent(context, ProvisioningCompleteActivity.class);
        context.startActivity(i);
    }
}