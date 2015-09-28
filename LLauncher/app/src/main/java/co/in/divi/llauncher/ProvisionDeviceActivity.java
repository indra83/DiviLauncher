package co.in.divi.llauncher;

import android.app.Activity;
import android.os.Bundle;

import in.co.divi.llauncher.R;

/**
 * Created by indra83 on 9/28/15.
 */
public class ProvisionDeviceActivity extends Activity{
    private static final String TAG = ProvisionDeviceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisiondevice);
    }
}
