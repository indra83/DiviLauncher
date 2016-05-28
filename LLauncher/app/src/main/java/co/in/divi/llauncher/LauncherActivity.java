package co.in.divi.llauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import in.co.divi.llauncher.R;

/**
 * Created by indra83 on 9/28/15.
 */
public class LauncherActivity extends Activity {
    private static final String TAG = LauncherActivity.class.getSimpleName();

    Button provisionButton,bigBossButton;
    TextView versionText, allowedAppsText;

    LLApplication app;
    Handler handler = new Handler();
    Timer timer = new Timer();

    private long lastClickTime;
    private int clickCount;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        provisionButton = (Button) findViewById(R.id.provisionButton);
        bigBossButton = (Button) findViewById(R.id.bigBossButton);
        versionText = (TextView) findViewById(R.id.versionText);
        allowedAppsText = (TextView) findViewById(R.id.allowedAppsText);

        findViewById(R.id.logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDiviApp();
            }
        });

        provisionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LauncherActivity.this, ProvisionDeviceActivity.class));
            }
        });

        clickCount = 0;
        lastClickTime = 0;
        versionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastClickTime < 1000) {
                    clickCount++;
                    if (toast != null) toast.cancel();
                    if (clickCount >= 8) {
                        // Start admin activity
                        startActivity(new Intent(LauncherActivity.this, BigBossActivity.class));
                        clickCount = 0;
                        Toast.makeText(LauncherActivity.this,"Opening device management...",Toast.LENGTH_SHORT).show();
                    } else if (clickCount > 3) {
                        resetTimer();
                        int remaining = 8 - clickCount;
                        toast = Toast.makeText(LauncherActivity.this, "Open settings in " + remaining + " clicks.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    clickCount = 1;
                }
                lastClickTime = now;
            }
        });

        try {
            Util.fixBackgroundRepeat(findViewById(R.id.root));
        } catch (Exception e) {
            Log.w(TAG, "not important!", e);
        }

        String versionName = "--";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "error getting version name", e);
        }
        versionText.setText(versionName);

        app = (LLApplication) getApplication();


        ///// Testing!
   }

    @Override
    protected void onStart() {
        super.onStart();
        if (app.isDeviceProvisioned()) {
            provisionButton.setVisibility(View.GONE);
        } else {
            provisionButton.setVisibility(View.VISIBLE);
        }
        if(app.isBigBoss())
            bigBossButton.setVisibility(View.VISIBLE);
        else
        bigBossButton.setVisibility(View.GONE);

        bigBossButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LauncherActivity.this, BigBossActivity.class));
            }
        });

        setAllowedAppsText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    private void resetTimer() {
        if(timer!=null)
            timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                launchDiviApp();
            }
        }, Config.LAUNCH_DIVI_TIMER);
    }

    private void setAllowedAppsText() {
        HashSet<String> ignoreApps = new HashSet<>();
        for (String app : AllowedSystemAppsProvider.ALLOWED_APPS)
            ignoreApps.add(app);
        StringBuilder sb = new StringBuilder();
        for (PackageInfo pi : getPackageManager().getInstalledPackages(0)) {
            if (!ignoreApps.contains(pi.packageName)) {
                sb.append(pi.packageName).append("\n");
            }
        }
        allowedAppsText.setText(sb.toString());
    }

    private void launchDiviApp() {
        Intent i;
        PackageManager manager = getPackageManager();
        try {
            i = manager.getLaunchIntentForPackage("co.in.divi");
            if (i == null)
                throw new PackageManager.NameNotFoundException();
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "error launching divi", e);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LauncherActivity.this, "Error launching Divi", Toast.LENGTH_LONG).show();
                }
            });
            try {
                i = manager.getLaunchIntentForPackage("co.in.divi");
                if (i == null)
                    throw new PackageManager.NameNotFoundException();
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(i);
            } catch (Exception e2) {
                Log.e(TAG, "error launching divi (second time)", e);
                timer.cancel();// don't try again
            }
        }
    }
}
