package co.in.divi.llauncher;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

import in.co.divi.llauncher.R;

/**
 * Created by indra83 on 9/30/15.
 */
public class BigBossActivity extends Activity {
    private static final String TAG = BigBossActivity.class.getSimpleName();

    View otpContainer, actionsContainer;
    Button settingsButton, browserButton, marketButton, exitButton;

    TextView challengeText;
    EditText challengeResp;

    LLApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bigboss);
        otpContainer = findViewById(R.id.otpContainer);
        actionsContainer = findViewById(R.id.actionContainer);

        challengeText = (TextView) findViewById(R.id.challenge);
        challengeResp = (EditText) findViewById(R.id.otpResponse);
        settingsButton = (Button) findViewById(R.id.settings);
        browserButton = (Button) findViewById(R.id.browser);
        marketButton = (Button) findViewById(R.id.market);
        exitButton = (Button) findViewById(R.id.exit);


        app = (LLApplication) getApplication();
    }

    @Override
    protected void onStart() {
        super.onStart();
        otpContainer.setVisibility(View.GONE);
        actionsContainer.setVisibility(View.GONE);
        if (!app.isBigBoss()) {
            otpContainer.setVisibility(View.VISIBLE);
            final int challenge = new Random(System.currentTimeMillis()).nextInt(10000);
            challengeText.setText("" + challenge);
            challengeResp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (isAuthorized(challenge, Integer.parseInt(challengeResp.getText().toString()))) {
                            app.setBigBoss(true);
                            otpContainer.setVisibility(View.GONE);
                            actionsContainer.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        //ignore?
                    }
                }
            });
        } else
            actionsContainer.setVisibility(View.VISIBLE);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.isBigBoss()) {
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    ComponentName mDeviceAdminRcvr = new ComponentName(BigBossActivity.this, DAR.class);

                    mDPM.setApplicationHidden(mDeviceAdminRcvr, SpecialAppNames.SETTINGS, false);
                    startActivity(getPackageManager().getLaunchIntentForPackage(SpecialAppNames.SETTINGS));
                }
            }
        });
        browserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.isBigBoss()) {
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    ComponentName mDeviceAdminRcvr = new ComponentName(BigBossActivity.this, DAR.class);

                    mDPM.setApplicationHidden(mDeviceAdminRcvr, SpecialAppNames.BROWSER, false);
                    startActivity(getPackageManager().getLaunchIntentForPackage(SpecialAppNames.BROWSER));
                }
            }
        });
        marketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.isBigBoss()) {
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    ComponentName mDeviceAdminRcvr = new ComponentName(BigBossActivity.this, DAR.class);

                    mDPM.setApplicationHidden(mDeviceAdminRcvr, SpecialAppNames.MARKET, false);
                    startActivity(getPackageManager().getLaunchIntentForPackage(SpecialAppNames.MARKET));
                }
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.isBigBoss()) {
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    ComponentName mDeviceAdminRcvr = new ComponentName(BigBossActivity.this, DAR.class);

                    mDPM.setApplicationHidden(mDeviceAdminRcvr, SpecialAppNames.SETTINGS, true);
                    mDPM.setApplicationHidden(mDeviceAdminRcvr, SpecialAppNames.BROWSER, true);
                    mDPM.setApplicationHidden(mDeviceAdminRcvr, SpecialAppNames.MARKET, true);

                    app.setBigBoss(false);
                    finish();
                }
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }


    private boolean isAuthorized(int challenge, int response) {
        return (((challenge * 997) + 7919) % 10000 == response);
    }

}
