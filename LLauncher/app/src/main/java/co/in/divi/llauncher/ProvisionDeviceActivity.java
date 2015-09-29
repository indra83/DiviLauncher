package co.in.divi.llauncher;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;

import in.co.divi.llauncher.R;

/**
 * Created by indra83 on 9/28/15.
 */
public class ProvisionDeviceActivity extends Activity {
    private static final String TAG = ProvisionDeviceActivity.class.getSimpleName();

    TextView unknownSourcesText, accessibilityText, installDiviText, deviceOwnerText;
    Button unknownSourcesButton, accessibilityButton, installDiviButton, provisionButton;

    LLApplication app;

    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdminRcvr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisiondevice);
        unknownSourcesText = (TextView) findViewById(R.id.unknownSourcesText);
        accessibilityText = (TextView) findViewById(R.id.accessibilityText);
        installDiviText = (TextView) findViewById(R.id.installDiviText);
        deviceOwnerText = (TextView) findViewById(R.id.deviceOwnerText);

        unknownSourcesButton = (Button) findViewById(R.id.unknownSourcesButton);
        accessibilityButton = (Button) findViewById(R.id.accessibilityButton);
        installDiviButton = (Button) findViewById(R.id.installDiviButton);
        provisionButton = (Button) findViewById(R.id.provisionButton);

        app = (LLApplication) getApplication();

        mDPM = (DevicePolicyManager) this
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminRcvr = new ComponentName(this, DAR.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupUI();

        // TESTING ONLY!
        findViewById(R.id.clearDeviceOwner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDPM.setApplicationHidden(mDeviceAdminRcvr,SpecialAppNames.SETTINGS,false);
                mDPM.clearDeviceOwnerApp(getPackageName());
                setupUI();
            }
        });

    }

    private void setupUI() {
        boolean owner = mDPM.isDeviceOwnerApp(getPackageName());
        deviceOwnerText.setText("Device Owner? " + owner);
        boolean readyToProvision = owner;

        // non-market apps
        int nonMarketApps = 0;
        try {
            nonMarketApps = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (nonMarketApps == 0) {
            unknownSourcesButton.setText("Enable unknown sources");
            unknownSourcesButton.setEnabled(true);
            unknownSourcesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentSettings = new Intent();
                    intentSettings.setAction(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intentSettings);
                }
            });
            readyToProvision = false;
        } else {
            unknownSourcesButton.setText("Done");
            unknownSourcesButton.setEnabled(false);
        }

        // check divi installed
        PackageInfo diviInfo;
        try {
            diviInfo = getPackageManager().getPackageInfo(SpecialAppNames.DIVI, 0);
        } catch (PackageManager.NameNotFoundException e) {
            diviInfo = null;
        }
        if (diviInfo == null) {
            installDiviButton.setText("Install Divi & check");
            installDiviButton.setEnabled(true);
            installDiviButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupUI();
                }
            });
            readyToProvision = false;
        } else {
            installDiviButton.setEnabled(false);
            installDiviButton.setText("Divi Installed!");
        }

        // accessibility
        if (!isAccessibilitySettingsOn(getApplicationContext())) {
            accessibilityButton.setText("Turn on accessibility");
            accessibilityButton.setEnabled(true);
            accessibilityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
            });
            readyToProvision = false;
        } else {
            accessibilityButton.setText("Accessibility is on");
            accessibilityButton.setEnabled(false);
        }

        if (readyToProvision) {
            provisionButton.setEnabled(true);
            provisionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // set settings

                    //TODO: for debug!
                    mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.ADB_ENABLED, "1");

                    mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.USB_MASS_STORAGE_ENABLED, "0");
                    mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.AUTO_TIME, "1");
                    mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.AUTO_TIME_ZONE, "1");
                    mDPM.setGlobalSetting(mDeviceAdminRcvr, Settings.Global.WIFI_SLEEP_POLICY, "2");

                    try {
                        mDPM.setSecureSetting(mDeviceAdminRcvr, Settings.Secure.INSTALL_NON_MARKET_APPS, "1");
                    } catch (Exception e) {
                        Log.w(TAG, "error setting non-mkt-apps", e);
                    }

                    // block apps
                    new HideAppsTask().execute();
                }
            });
        } else {
            provisionButton.setEnabled(false);
        }

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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            app.setDeviceProvisioned(true);
            Toast.makeText(ProvisionDeviceActivity.this, "Device Provisioned!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // To check if service is enabled
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = Config.DIVI_ACCESSIBILITY_SERVICE;
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    Log.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }
}
