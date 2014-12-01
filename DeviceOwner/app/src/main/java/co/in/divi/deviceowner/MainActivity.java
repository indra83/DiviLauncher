package co.in.divi.deviceowner;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdminRcvr;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.status);

//        findViewById(R.id.activate).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
//                        mDeviceAdminRcvr);
//                startActivityForResult(intent, 243299);
//                finish();
//            }
//        });

        findViewById(R.id.unpin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLockTask();
            }
        });

        findViewById(R.id.pin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLockTask();
            }
        });

        findViewById(R.id.hide_apps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "" + mDPM.setApplicationHidden(mDeviceAdminRcvr, "com.android.launcher", true));
                Log.d(TAG, "" + mDPM.setApplicationHidden(mDeviceAdminRcvr, "com.android.settings", true));
                Log.d(TAG, "" + mDPM.setApplicationHidden(mDeviceAdminRcvr, "com.android.providers.downloads.ui", true));


                PackageManager pm = getPackageManager();
                for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
                    Log.d(TAG, "" + ai.packageName + "   -   " + mDPM.isApplicationHidden(mDeviceAdminRcvr, ai.packageName));
                }
            }
        });

        mDPM = (DevicePolicyManager) this
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminRcvr = new ComponentName(this, DiviDeviceAdminReceiver.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean active = mDPM.isAdminActive(mDeviceAdminRcvr);
        boolean owner = mDPM.isDeviceOwnerApp(getPackageName());

        String msg = "Device Admin: " + (active ? "Active" : "Not Active");
        msg = msg + "\nDevice Owner: " + (owner ? "Owner" : "Not Owner");

        textView.setText(msg);

        if (owner) {
            mDPM.setLockTaskPackages(mDeviceAdminRcvr, new String[]{"co.in.divi.deviceowner"});

            PackageManager pm = getPackageManager();
            for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
                Log.d(TAG, "" + ai.packageName + "   -   " + mDPM.isApplicationHidden(mDeviceAdminRcvr, ai.packageName));
            }
        }
    }
}
