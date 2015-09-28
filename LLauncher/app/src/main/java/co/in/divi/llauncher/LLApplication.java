package co.in.divi.llauncher;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.HashSet;

import io.fabric.sdk.android.Fabric;

/**
 * Created by indra83 on 9/27/15.
 */
public class LLApplication extends Application {
    private static final String TAG = LLApplication.class.getSimpleName();

    private static final String PREFS_FILE = "llauncher_prefs";
    private static final String PREF_PROVISIONED_FLAG = "PREF_PROVISIONED_FLAG";

    public HashSet<String> allowedPackages;

    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        allowedPackages = new HashSet<>();
        for (String allowedPkg : AllowedSystemAppsProvider.ALLOWED_APPS) {
            allowedPackages.add(allowedPkg);
        }

        prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
    }

    public boolean isDeviceProvisioned() {
        return prefs.getBoolean(PREF_PROVISIONED_FLAG, false);
    }

    public void setDeviceProvisioned(boolean isProvisioned) {
        prefs.edit().putBoolean(PREF_PROVISIONED_FLAG, isProvisioned).apply();
    }
}
