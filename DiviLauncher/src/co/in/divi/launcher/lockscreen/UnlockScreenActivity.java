package co.in.divi.launcher.lockscreen;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import co.in.divi.launcher.Config;
import co.in.divi.launcher.R;

public class UnlockScreenActivity extends Activity {
	private static final String	TAG					= UnlockScreenActivity.class.getSimpleName();

	Handler						handler;

	Runnable					lockScreenRunnable	= new Runnable() {

														@Override
														public void run() {
															try {
																Log.d(TAG, "locking now!");
																finish();
																((DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE))
																		.lockNow();
															} catch (Exception e) {
																Log.w(TAG, "error locking!	", e);
															}
														}
													};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.activity_unlockscreen);
		((Button) findViewById(R.id.unlockButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		handler = new Handler();
	}

	@Override
	protected void onResume() {
		super.onResume();
		hideBars();
		handler.removeCallbacks(lockScreenRunnable);
		handler.postDelayed(lockScreenRunnable, Config.UNLOCK_SCREEN_RELOCK_DELAY);
	}

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(lockScreenRunnable);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		hideBars();
	}

	private void hideBars() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			// WindowManager.LayoutParams.FLAG_FULLSCREEN);
			int newUiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
			getWindow().addFlags(newUiOptions);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		} else {
			int newUiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			newUiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
			newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
		}
	}
}
