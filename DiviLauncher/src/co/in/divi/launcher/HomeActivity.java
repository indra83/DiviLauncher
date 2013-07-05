package co.in.divi.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class HomeActivity extends Activity {
	private static final String	TAG	= HomeActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		Util.fixBackgroundRepeat(findViewById(R.id.root));
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "sending service start intent");
		startService(new Intent(this, DaemonService.class));
	}
}
