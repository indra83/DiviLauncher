package co.in.divi.llauncher;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by indra83 on 9/27/15.
 */
public class LLApplication extends Application{
    private static final String TAG = LLApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "in App!");

        Fabric.with(this, new Crashlytics());
    }
}
