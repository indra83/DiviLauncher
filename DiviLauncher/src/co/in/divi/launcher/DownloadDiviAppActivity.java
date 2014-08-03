package co.in.divi.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stericson.RootTools.RootTools;

public class DownloadDiviAppActivity extends Activity {
	private static final String	TAG				= DownloadDiviAppActivity.class.getName();

	ProgressDialog				pd;
	UpdateAppTask				updateAppTask	= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateAppTask = new UpdateAppTask();
		updateAppTask.execute(new Void[0]);
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
		if (updateAppTask != null)
			updateAppTask.cancel(true);// cancel if we close while downloading...
	}

	class UpdateAppTask extends AsyncTask<Void, String, Integer> {
		File	apkFile;
		int		versionCode;

		@Override
		protected void onPreExecute() {
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
			} else {
				Toast.makeText(DownloadDiviAppActivity.this, "Connect to internet.", Toast.LENGTH_LONG).show();
				finish();
				cancel(false);
			}
			PackageInfo pInfo;
			try {
				pInfo = getPackageManager().getPackageInfo(Config.APP_DIVI_MAIN, 0);
				versionCode = pInfo.versionCode;
			} catch (NameNotFoundException e) {
				Log.d(TAG, "app not found!");
				versionCode = 0;
			}

			apkFile = new File(new File(Environment.getExternalStorageDirectory(), "temp"), "Divi.apk");
			pd = ProgressDialog.show(DownloadDiviAppActivity.this, "Please wait", "Updating Divi...");
		}

		@Override
		protected Integer doInBackground(Void... params) {
			if (apkFile.exists())
				apkFile.delete();
			apkFile.getParentFile().mkdirs();
			try {
				URL url = new URL(Config.APP_UPDATE_URL);
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();

				InputStream is = c.getInputStream();

				String response = Util.getInputString(is);
				Log.d(TAG, response);
				AppUpdateDescription updateDescription = new Gson().fromJson(response, AppUpdateDescription.class);

				if (updateDescription.versionCode > versionCode) {
					publishProgress("Found new apk, downloading...");
					FileOutputStream fos = new FileOutputStream(apkFile);
					url = new URL(updateDescription.apkUrl);
					c = (HttpURLConnection) url.openConnection();
					c.setRequestMethod("GET");
					c.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");

					c.connect();
					Log.d(TAG, "code: " + c.getResponseCode());
					is = c.getInputStream();
					byte[] buffer = new byte[1024 * 8];
					int len1 = 0;
					while ((len1 = is.read(buffer)) != -1) {
						fos.write(buffer, 0, len1);
						Log.d(TAG, " read " + len1);
					}
					fos.close();
					return 0;
				} else {
					return 1;
				}
			} catch (IOException e) {
				Log.w(TAG, "error downloading update apk", e);
				return 2;
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Toast.makeText(DownloadDiviAppActivity.this, values[0], Toast.LENGTH_LONG).show();
		}

		@Override
		protected void onPostExecute(Integer result) {
			finish();
			pd.cancel();
			// Start intent to begin update activity
			if (result == 1) {
				Toast.makeText(DownloadDiviAppActivity.this, "App is latest.", Toast.LENGTH_LONG).show();
				return;
			}
			if (result == 2) {
				Toast.makeText(DownloadDiviAppActivity.this, "Error downloading apk?", Toast.LENGTH_LONG).show();
				return;
			}
			if (RootTools.isAccessGiven()) {
				try {
					Intent i = new Intent();
					i.setAction("co.in.divi.launcher.UPDATE_APP");
					i.putExtra("path", apkFile.getAbsolutePath());
					startActivity(i);
					return;
				} catch (ActivityNotFoundException anfe) {
					Log.w(TAG, "app update activity not found", anfe);
				}
			}

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
			startActivity(intent);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
		int newUiOptions = uiOptions | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
		if (Build.VERSION.SDK_INT >= 18) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}
		getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
	}

	class AppUpdateDescription {
		public int		versionCode;
		public String	apkUrl;
	}
}
