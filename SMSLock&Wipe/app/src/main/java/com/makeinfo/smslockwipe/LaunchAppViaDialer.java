package com.makeinfo.smslockwipe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Created by Basil on 10/9/2015.
 */
public class LaunchAppViaDialer extends BroadcastReceiver {

    //private static final String LAUNCHER_NUMBER =
    SharedPreferences pref;
    String dial_code ="1234567" ;

    @Override
    public void onReceive(Context context, Intent intent) {

     //    = context.getSharedPreferences("settings",Context.MODE_PRIVATE);

        //dial_code = pref.getString(SetupActivity.FAST_DAIL,context.getResources().getString(R.string.config_default_fast_dial_num));

        String phoneNubmer = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if (dial_code.equals(phoneNubmer)) {
            setResultData(null);
            Intent appIntent = new Intent(context, SetupActivity.class);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        }
    }
}
