package com.makeinfo.smslockwipe;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by Basil on 10/8/2015.
 */
public class SettingsActivity extends PreferenceActivity {




    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("settings");
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        addPreferencesFromResource(R.xml.settings);
        CheckBoxPreference hideApp = (CheckBoxPreference) findPreference("hide_icon");


        //bundle = getSharedPreferences("settings",1);

        hideApp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean b = (Boolean) o;
                //hideAlert(b);
                if(b){
                    Toast.makeText(getApplicationContext(),String.valueOf("Hide enabled"),Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),String.valueOf("Dial 1234567 to open your app"),Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),String.valueOf("Hide disabled"),Toast.LENGTH_SHORT).show();
                }
                PackageManager pm = getPackageManager();
                pm.setComponentEnabledSetting(
                        new ComponentName(getApplicationContext(), "com.makeinfo.smslockwipe.Launcher"), b ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        ,
                        PackageManager.DONT_KILL_APP);
                return true;
            }
        });

    }

}

