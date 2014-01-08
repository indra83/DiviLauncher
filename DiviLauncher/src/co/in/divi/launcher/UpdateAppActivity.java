package co.in.divi.launcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateAppActivity extends Activity {
	private static final String	TAG						= "UpdateAppActivity";
	private static final String	INTENT_EXTRA_APK_PATH	= "path";

	TextView					apkDetails, logTV;
	ProgressDialog				pd;

	InstallTask					installTask;
	File						apkFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updateapp);
		apkDetails = (TextView) findViewById(R.id.apk_details);
		logTV = (TextView) findViewById(R.id.log);
	}

	@Override
	protected void onStart() {
		super.onStart();
		String apkPath = getIntent().getStringExtra(INTENT_EXTRA_APK_PATH);
		if (apkPath == null) {
			Toast.makeText(this, "APK file not sent.", Toast.LENGTH_LONG).show();
			return;
		}
		apkFile = new File(apkPath);
		if (!apkFile.exists()) {
			Toast.makeText(this, "APK file does not exist.", Toast.LENGTH_LONG).show();
			return;
		}
		try {
			apkDetails.setText(apkFile.getCanonicalPath() + " (" + apkFile.length() / (1024 * 1024) + " MB).");
		} catch (IOException e) {
			Toast.makeText(this, "Error reading apk file.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return;
		}

		installTask = new InstallTask();
		installTask.execute(new String[0]);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (pd != null)
			pd.cancel();
		if (installTask != null)
			installTask.cancel(true);
		finish();
	}

	class InstallTask extends AsyncTask<String, String, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			Process p = null;
			try {
				p = Runtime.getRuntime().exec("su");
				DataOutputStream outs = new DataOutputStream(p.getOutputStream());
				String cmd = "pm install " + apkFile.getAbsolutePath();
				outs.writeBytes(cmd + "\n");

				publishProgress(cmd);

				try {
					int ret = p.waitFor();
					publishProgress("return - " + ret);
				} catch (InterruptedException e) {
					e.printStackTrace();
					p.destroy();
					publishProgress("Error:" + e);
				}
				// String log = getInputString(p.getInputStream());
				return 0;

			} catch (IOException e) {
				e.printStackTrace();
				return 1;
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			logTV.setText(logTV.getText() + "\n" + values[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == 0) {
				Toast.makeText(UpdateAppActivity.this, "Success!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(UpdateAppActivity.this, "Failed update!", Toast.LENGTH_LONG).show();
			}
			finish();
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
}
