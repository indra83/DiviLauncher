package co.in.divi.launcher;

import android.app.Activity;
import android.os.Bundle;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		Util.fixBackgroundRepeat(findViewById(R.id.root));
	}
}
