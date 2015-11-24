package com.makeinfo.smslockwipe;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by Basil on 10/8/2015.
 */
public class CallForwardActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callforward);

        callforward("*21*9747864855#");
    }


    private void callforward(String callForwardString) {
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager)
                this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        Intent intentCallForward = new Intent(Intent.ACTION_CALL);
        Uri mmiCode = Uri.fromParts("tel", callForwardString, "#");
        intentCallForward.setData(mmiCode);
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        startActivity(intentCallForward);
    }

    private class PhoneCallListener extends PhoneStateListener {
        private boolean isPhoneCalling = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended, need detect flag
                // from CALL_STATE_OFFHOOK
                if (isPhoneCalling) {
                    // restart app
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    isPhoneCalling = false;
                }
            }
        }
    }
}
