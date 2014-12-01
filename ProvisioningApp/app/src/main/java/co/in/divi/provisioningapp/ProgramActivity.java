package co.in.divi.provisioningapp;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Properties;


public class ProgramActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {

    private static final String TAG = "ProvisioningApp";
    private Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.setNdefPushMessageCallback(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start");


    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        Log.d(TAG, "got into createNdefMessage  ");
        try {
            Properties p = new Properties();
//            p.setProperty(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_SSID, "\"B54\"");
//            p.setProperty(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_PASSWORD, "qwertyuiop");
            p.setProperty(
                    DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                    "co.in.divi.deviceowner");
            p.setProperty(
                    DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION,
                    "https://dl.dropboxusercontent.com/u/5474079/app.apk");
            p.setProperty(
                    DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM,
                    "FdI3CMFHERhy5Wj-VXwg-A72has");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStream out = new ObjectOutputStream(bos);
            p.store(out, "");
            final byte[] bytes = bos.toByteArray();

            NdefMessage msg = new NdefMessage(NdefRecord.createMime(
                    DevicePolicyManager.MIME_TYPE_PROVISIONING_NFC, bytes));

            h.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "blah");
                    TextView tv = (TextView) findViewById(R.id.blah);
                    tv.setText(new String(bytes));
                }
            });

            return msg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
