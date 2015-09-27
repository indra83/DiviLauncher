package co.in.divi.llauncher;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.HashSet;
import java.util.List;

import in.co.divi.llauncher.R;

public class HomeActivity extends Activity {
    public static final String TAG = HomeActivity.class.getSimpleName();

    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdminRcvr;

    private TextView settingsStatusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_home);
        settingsStatusText = (TextView) findViewById(R.id.settingsStatusText);
        findViewById(R.id.hideButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new GetPackageDetailsTask().execute();
                new HideAppsTask().execute();
            }
        });
        findViewById(R.id.printButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(0);
                long diff = System.currentTimeMillis() - now;
                Log.d(TAG, "time to fetch list:" + diff);
                for (PackageInfo packageInfo : packageInfoList)
                    Log.d(TAG, " - " + packageInfo.packageName);
//                new HideAppsTask().execute();
            }
        });
        findViewById(R.id.hideSettingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                mDPM.setApplicationHidden(mDeviceAdminRcvr, SpecialAppNames.SETTINGS, true);
                long diff = System.currentTimeMillis() - now;
                Log.d(TAG, "time to hide:" + diff);
            }
        });
        findViewById(R.id.unhideSettingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                mDPM.setApplicationHidden(mDeviceAdminRcvr, SpecialAppNames.SETTINGS, false);
                long diff = System.currentTimeMillis() - now;
                Log.d(TAG, "time to unhide:" + diff);
                startActivity(getPackageManager().getLaunchIntentForPackage(SpecialAppNames.SETTINGS));
            }
        });
        findViewById(R.id.disableADB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.USB_MASS_STORAGE_ENABLED, "0");
                mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.ADB_ENABLED, "0");
                setSettingsStatusText();
            }
        });
        findViewById(R.id.enableADB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.USB_MASS_STORAGE_ENABLED, "1");
                mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.ADB_ENABLED, "1");

                try {
                    mDPM.setSecureSetting(mDeviceAdminRcvr, Settings.Secure.INSTALL_NON_MARKET_APPS, "1");
                }catch (Exception e) {
                    Log.w(TAG,"error setting non-mkt-apps",e);
                    throw e;
                }
                setSettingsStatusText();
            }
        });


        mDPM = (DevicePolicyManager) this
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminRcvr = new ComponentName(this, DAR.class);
    }

    private void setSettingsStatusText() {
        try {
            ContentResolver cr = getContentResolver();
            StringBuilder sb = new StringBuilder();
            sb.append("adb:").append(Settings.Global.getInt(cr, Settings.Global.ADB_ENABLED)).append("\n");
            sb.append("usb:").append(Settings.Global.getInt(cr, Settings.Global.USB_MASS_STORAGE_ENABLED)).append("\n");


            sb.append("auto-time:").append(Settings.Global.getInt(cr, Settings.Global.AUTO_TIME)).append("\n");
            sb.append("auto-time-zone:").append(Settings.Global.getInt(cr, Settings.Global.AUTO_TIME_ZONE)).append("\n");
            sb.append("wifi-sleep:").append(Settings.Global.getInt(cr, Settings.Global.WIFI_SLEEP_POLICY)).append("\n");

            sb.append("non-market-apps:").append(Settings.Secure.getInt(cr, Settings.Secure.INSTALL_NON_MARKET_APPS)).append("\n");
            settingsStatusText.setText(sb);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getPrintInfo(PackageInfo packageInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(packageInfo.packageName).append("\n");
        if (packageInfo.activities != null)
            for (ActivityInfo activityInfo : packageInfo.activities) {
                sb.append(activityInfo.name).append("; ");
            }
        if (packageInfo.providers != null)
            for (ProviderInfo providerInfo : packageInfo.providers) {
                sb.append(providerInfo.name).append("; ");
            }
        sb.append("\nsystem? ").append(packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM);
        sb.append("\n\n\n");
        return sb.toString();
    }

    class HideAppsTask extends AsyncTask<Void, String, Void> {
        HashSet<String> allowedPackages;

        @Override
        protected void onPreExecute() {
            allowedPackages = new HashSet<>();
            for (String pkg : AllowedSystemAppsProvider.ALLOWED_APPS)
                allowedPackages.add(pkg);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (PackageInfo packageInfo : packageInfoList) {
                if (!allowedPackages.contains(packageInfo.packageName)) {
                    Log.d(TAG, "hiding app " + packageInfo.packageName);
                    mDPM.setApplicationHidden(mDeviceAdminRcvr, packageInfo.packageName, true);
                }
            }
            return null;
        }
    }

    class GetPackageDetailsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (PackageInfo packageInfo : packageInfoList) {
                Log.d(TAG, "processing : " + packageInfo.packageName);
                int flags = PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_SERVICES | PackageManager.GET_UNINSTALLED_PACKAGES;
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(packageInfo.packageName, flags);
                    Log.d(TAG, "package info:" + getPrintInfo(pInfo));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
