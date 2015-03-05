package co.in.divi.launcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class DaemonService extends Service {
    private static final String TAG = DaemonService.class.getSimpleName();

    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;
    private Notification notice;
    private PowerManager powerManager;
    private ActivityManager am;
    private AdminPasswordManager adminPasswordManager;

    private ContentObserver allowedAppsObserver;
    private Handler handler;
    private ConcurrentHashMap<String, String> allowedApps = new ConcurrentHashMap<String, String>(108, 0.9f, 1);
    private FetchAllowedAppsTask fetchAllowedAppsTask = null;
    private long lastLockRequestTime = 0;

    private DaemonThread daemonThread = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(this, DiviDeviceAdmin.class);
        if (mDPM.isAdminActive(mDeviceAdmin))
            mDPM.setCameraDisabled(mDeviceAdmin, true);
        adminPasswordManager = AdminPasswordManager.getInstance();
        // registBroadcastReceiver();
        allowedAppsObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.d(TAG, "content observer says it has new data");
                fetchAllowedApps();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (daemonThread == null || !daemonThread.isAlive()) {
            Intent i = new Intent(this, HomeActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
            notice = new NotificationCompat.Builder(this).setContentTitle("Divi Launcher").setContentText("Blah")
                    .setSmallIcon(R.drawable.divi_logo).setContentIntent(pi).setOngoing(true).build();

            startForeground(1337, notice);

            Log.d(TAG, "creating new thread");
            daemonThread = new DaemonThread();
            daemonThread.start();

            getContentResolver().registerContentObserver(Config.ALLOWED_APPS_URI, true, allowedAppsObserver);
            fetchAllowedApps();
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy");
        daemonThread.interrupt();
        // unregisterReceiver();
        getContentResolver().unregisterContentObserver(allowedAppsObserver);
    }

    class DaemonThread extends Thread {

        // state data
        private boolean isLockingEnabled = false;
        private long suspiciousActivityStart = 0L;
        private AppLog curLog = null;

        @Override
        public void run() {
            // all variables here!
            int count = 0;
            long diff;
            String pkgName, appName;
            boolean killAll = false;
            isLockingEnabled = mDPM.isAdminActive(mDeviceAdmin);
            List<RunningTaskInfo> tasks;
            while (true) {
                count++;
                if (Config.DEBUG_DAEMON)
                    Log.d(TAG, "in loop... - " + count);
                if (count % 30 == 0) {
                    Log.d(TAG, "in loop... - " + count);
                    isLockingEnabled = mDPM.isAdminActive(mDeviceAdmin);
                    if (Config.DEBUG_DAEMON)
                        Log.d(TAG, "checking locking enabled - " + isLockingEnabled);
                }
                if (powerManager.isScreenOn()) {// check if screen on
                    if (count % 2 == 0) {
                        collapseDrawers();
                    }
                    tasks = am.getRunningTasks(10);
                    if (Config.DEBUG_DAEMON)
                        Log.d(TAG, "got tasks:" + tasks.size());
                    if (tasks.size() > 0 && isLockingEnabled) {
                        diff = System.currentTimeMillis() - adminPasswordManager.getLastAuthorizedTime();
                        if (diff < Config.SETTINGS_ACCESS_TIME) {
                            // disable everything for 2 mins.
                            if (Config.DEBUG_DAEMON)
                                Log.w(TAG, "kiosk mode is off!");
                        } else {
                            pkgName = tasks.get(0).topActivity.getPackageName();
                            appName = tasks.get(0).topActivity.getClassName();
                            if (curLog != null && !curLog.appPackage.equals(pkgName)) {
                                curLog.checkAndPost();
                                curLog = null;
                            }
                            if (!(pkgName.equals(Config.APP_DIVI_LAUNCHER) || pkgName.equals(Config.APP_DIVI_MAIN) || pkgName
                                    .equals(Config.APP_INSTALLER))) {
                                if (allowedApps.containsKey(pkgName)) {
                                    // using an authorized 3p app..
                                    // check if we need to start new log
                                    if (curLog == null) {
                                        curLog = new AppLog(pkgName);
                                    }
                                } else {
                                    if (pkgName.equals(Config.APP_ANDROID) && !Util.isMyLauncherDefault(DaemonService.this)) {
                                        // ignore for now..
                                    } else {
                                        Log.e(TAG, "locking! - " + pkgName);
                                        for (RunningTaskInfo task : tasks) {
                                            Log.d(TAG, "task: " + task.id + ", base: " + task.baseActivity.getClassName() + ", top:"
                                                    + task.topActivity.getPackageName());
                                        }
                                        suspiciousActivityStart = System.currentTimeMillis();
                                        killAll = true;
                                        final String culpritAppName = pkgName + " - " + appName;
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                lockNow(culpritAppName);
                                            }
                                        }, 200);
                                    }
                                }
                            }
                        }
                    }
                    // print
                    if (Config.DEBUG_DAEMON && count % 30 == 0) {
                        for (RunningTaskInfo task : tasks) {
                            Log.d(TAG,
                                    "task: " + task.id + ", base: " + task.baseActivity.getClassName() + ", top:"
                                            + task.topActivity.getPackageName());
                        }
                        printAllowedApps();
                    }
                    if (Config.DEBUG_DAEMON)
                        Log.d(TAG, "===============================================================");
                    // print
                } else {
                    if (curLog != null) {
                        curLog.checkAndPost();
                        curLog = null;
                    }
                }
                try {
                    if (System.currentTimeMillis() - suspiciousActivityStart < Config.SUSPICIOUS_ACTIVITY_ALERT_TIME) {
                        if (Config.DEBUG_DAEMON)
                            Log.d(TAG, "small sleep");
                        Thread.sleep(Config.SLEEP_TIME_SHORT);
                    } else {
                        if (Config.DEBUG_DAEMON)
                            Log.d(TAG, "long sleep");
                        Thread.sleep(Config.SLEEP_TIME_LONG);
                    }
                } catch (InterruptedException e) {
                    Log.w(TAG, "we are interrupted!", e);
                    break;
                }
                if (killAll) {
                    killAll = false;
                    try {
                        for (RunningTaskInfo rt : am.getRunningTasks(40)) {
                            String pName = rt.topActivity.getPackageName();
                            if (pName.equals(Config.APP_DIVI_LAUNCHER) || pName.equals(Config.APP_DIVI_MAIN))
                                continue;
                            Log.d(TAG, "killing : " + pName);
                            am.killBackgroundProcesses(rt.topActivity.getPackageName());
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "error killing bg process!");
                    }
                }
            }
        }
    }

    private void lockNow(String culprit) {
        try {
            // close any dialogs
            sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            // check again
            List<RunningTaskInfo> tasks = am.getRunningTasks(10);
            String pkgName = null;
            if (tasks.size() > 0) {
                for (RunningTaskInfo task : tasks) {
                    Log.w(TAG,
                            "task: " + task.id + ", base: " + task.baseActivity.getClassName() + ", top:"
                                    + task.topActivity.getPackageName());
                }
                pkgName = tasks.get(0).topActivity.getPackageName();
                if (!(pkgName.equals(Config.APP_DIVI_LAUNCHER) || pkgName.equals(Config.APP_DIVI_MAIN) || pkgName
                        .equals(Config.APP_INSTALLER))) {
                    Log.w(TAG, "allowed apps: " + allowedApps.size());

                    for (String allowedApp : allowedApps.keySet())
                        Log.w(TAG, allowedApp);
                    if (allowedApps.containsKey(pkgName)) {
                        Log.d(TAG, "allowed app!");
                    } else {
                        Log.d(TAG, "locking! - " + culprit + ", cur:" + pkgName);
                        // go to home
                        Intent i = new Intent(DaemonService.this, HomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(i);
                        // don't lock the on first notice
                        long now = System.currentTimeMillis();
                        if (now - lastLockRequestTime > Config.SUSPICIOUS_ACTIVITY_ALERT_TIME)
                            Toast.makeText(this, "Unauthorized app! - " + culprit, Toast.LENGTH_LONG).show();
                        else
                            mDPM.lockNow();
                        lastLockRequestTime = now;
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "error locking!!", e);
        }
    }

    private void fetchAllowedApps() {
        if (fetchAllowedAppsTask == null || fetchAllowedAppsTask.getStatus() == AsyncTask.Status.FINISHED) {
            Log.d(TAG, "starting fetch task");
            fetchAllowedAppsTask = new FetchAllowedAppsTask();
            fetchAllowedAppsTask.execute(new Void[0]);
        }
    }

    class FetchAllowedAppsTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Log.d(TAG, "starting fetch");
                Cursor cursor = getContentResolver().query(Config.ALLOWED_APPS_URI, null, null, null, null);
                Log.d(TAG, "got apps: " + cursor);
                allowedApps.clear();
                if (cursor != null) {
                    Log.d(TAG, "got apps: " + cursor.getCount());
                    int packageIndex = cursor.getColumnIndex(Config.ALLOWED_APP_PACKAGE_COLUMN);
                    while (cursor.moveToNext()) {
                        String pkg = cursor.getString(packageIndex);
                        Log.d(TAG, "got allowed app: " + pkg);
                        allowedApps.put(pkg, pkg);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "error fetching allowed apps:", e);
            }
            return null;
        }
    }

    // collapse statusbar
    private void collapseDrawers() {
//                                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//                        sendBroadcast(it);
        try {
            Object service = getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method collapse = statusbarManager.getMethod("collapsePanels");
            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch (Exception e) {
            Log.w(TAG, "error closing panels", e);
        }
    }

    // debug!
    private void printAllowedApps() {
        for (String pkg : allowedApps.keySet())
            Log.d(TAG, "allowed app - " + pkg);
    }

    private class AppLog {
        public String appPackage;
        public long openedAt;
        public long duration;

        public AppLog(String pkgName) {
            appPackage = pkgName;
            openedAt = System.currentTimeMillis();
        }

        private void updateDuration() {
            this.duration = System.currentTimeMillis() - openedAt;
        }

        public void checkAndPost() {
            if (Config.DEBUG_DAEMON)
                Log.d(TAG, "posting app usage stat");
            updateDuration();
            if (duration > Config.THRESHOLD_USAGE_DURATION) {
                Intent postIntent = new Intent("co.in.divi.intent.APP_USAGE_BROADCAST");
                postIntent.putExtra("INTENT_EXTRA_APP_PACKAGE", appPackage);
                postIntent.putExtra("INTENT_EXTRA_OPENED_AT", openedAt);
                postIntent.putExtra("INTENT_EXTRA_DURATION", duration);
                sendBroadcast(postIntent);
            }
        }
    }
}
