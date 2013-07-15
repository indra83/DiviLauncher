package co.in.divi.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class HomeActivity extends Activity {
	private static final String	TAG	= HomeActivity.class.getName();
    private static final String PREF_FILE = "prefs";

    private boolean tryTakingOver() {
        // check if already hacked, otherwise perpetuate the hacks yourself
        boolean undo = true;
        AllHacks takeOver = null;
        try {

            takeOver = new AllHacks( getSharedPreferences( PREF_FILE, MODE_PRIVATE ), this );
            if( takeOver.runHack() )
                return true;

        } catch ( SuProcWrapper.SuProcInAccessibleException ex ) {
            //ex.printStackTrace();

            Log.d( TAG, "This device is not rooted : " + ex.getReason() );
            undo = false;
        }
        // if something went wrong undo the whole thing?
        if( undo && takeOver != null )
            takeOver.undoHack();
        return false;

    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

        if( !tryTakingOver() )
            Log.d( TAG, "hack failed, app running in un-rooted mode" );
        else
            Log.d(TAG, "hack successful, app running in rooted mode");


		Util.fixBackgroundRepeat(findViewById(R.id.root));
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.d(TAG, "sending service start intent");
		startService(new Intent(this, DaemonService.class));
	}
}
