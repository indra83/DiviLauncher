package co.in.divi.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.View;

public class Util {

	public static void setKnownPassword(DevicePolicyManager mDPM, ComponentName mDeviceAdmin) {
		mDPM.resetPassword(Config.DEFAULT_PASSWORD, 0);
		mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, 0);
		mDPM.setMaximumTimeToLock(mDeviceAdmin, 180 * 1000);
		mDPM.setPasswordExpirationTimeout(mDeviceAdmin, 10000);
	}

	public static void setUnknownPassword(DevicePolicyManager mDPM, ComponentName mDeviceAdmin, int maxFailAttempts) {
		mDPM.resetPassword(Config.HARD_PASSWORD, 0);
		mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, maxFailAttempts);
		mDPM.setPasswordExpirationTimeout(mDeviceAdmin, 10000);
	}

	public static boolean isMyLauncherDefault(Context context) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		String currentHomePackage = resolveInfo.activityInfo.packageName;
		final String myPackageName = context.getPackageName();
		if (Config.DEBUG) {
			Log.d(context.getPackageName(), "currentHomePackage:" + currentHomePackage);
			Log.d(context.getPackageName(), "myPackageName:" + myPackageName);
		}
		return currentHomePackage.equals(myPackageName);
	}

	public static void fixBackgroundRepeat(View view) {
		Drawable bg = view.getBackground();
		if (bg != null) {
			if (bg instanceof LayerDrawable) {
				// Log.d("temp", "Hooray! setting bitmap tile mode");
				BitmapDrawable bd = (BitmapDrawable) ((LayerDrawable) bg).getDrawable(0);
				bd.mutate(); // make sure that we aren't sharing state anymore
				bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
			} else if (bg instanceof StateListDrawable) {
				StateListDrawable stateDrwbl = (StateListDrawable) bg;
				stateDrwbl.mutate(); // make sure that we aren't sharing state
										// anymore

				ConstantState constantState = stateDrwbl.getConstantState();
				if (constantState instanceof DrawableContainerState) {
					DrawableContainerState drwblContainerState = (DrawableContainerState) constantState;
					final Drawable[] drawables = drwblContainerState.getChildren();
					for (Drawable drwbl : drawables) {
						if (drwbl instanceof LayerDrawable) {
							BitmapDrawable bd = (BitmapDrawable) ((LayerDrawable) drwbl).getDrawable(0);
							// bd.mutate(); // make sure that we aren't sharing
							// state anymore
							bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
						}
					}
				}
			}
		}
	}

	public static String getInputString(InputStream is) throws IOException {
		if (is == null)
			return "";
		BufferedReader in = new BufferedReader(new InputStreamReader(is), 8192);
		String line;
		StringBuilder sb = new StringBuilder();

		try {
			while ((line = in.readLine()) != null)
				sb.append(line);
		} finally {
			is.close();
		}
		return sb.toString();
	}

}
