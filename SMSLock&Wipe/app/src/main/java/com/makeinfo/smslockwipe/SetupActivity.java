package com.makeinfo.smslockwipe;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

public class SetupActivity extends PreferenceActivity {
    private static final String PREFERENCES_DESCRIPTION_DIALOG_SHOWN = "description_dialog_shown";

    public static final String PREFERENCES_ALARM_ENABLED = "alarm_enabled";
    public static final String PREFERENCES_LOCK_ENABLED = "lock_enabled";
    public static final String PREFERENCES_WIPE_ENABLED = "wipe_enabled";
    public static final String PREFERENCES_CALL_FORWARD_ENABLED = "call_forwarding_enabled";
    public static final String PREFERENCES_BATTERY_LEVEL_ENABLED = "battery_level_enabled";
    public static final String PREFERENCES_CELL_LOCATION_ENABLED= "cell_location_enabled";

    public static final String PREFERENCES_ALARM_ACTIVATION_SMS = "alarm_activation_sms";
    public static final String PREFERENCES_LOCK_ACTIVATION_SMS = "lock_activation_sms";
    public static final String PREFERENCES_WIPE_ACTIVATION_SMS = "wipe_activation_sms";
    public static final String PREFERENCES_CALL_FORWARD_ACTIVATION_SMS = "call_forwarding_activation_sms";
    public static final String PREFERENCES_BATTERY_LEVEL_ACTIVATION_SMS = "battery_level_activation_sms";
    public static final String PREFERENCES_CELL_LOCATION_ACTIVATION_SMS = "cell_location_activation_sms";


    public static final String PREFERENCES_ALARM_VIBRATE = "alarm_vibrate";
    public static final String PREFERENCES_ALARM_DURATION = "alarm_duration";

    public static final String PREFERENCES_LOCK_PASSWORD = "lock_password";


    public static final ComponentName DEVICE_ADMIN_COMPONENT = new ComponentName(DeviceAdmin.class.getPackage().getName(), DeviceAdmin.class.getName());
    public static final int ACTIVATION_REQUEST = 1;

    private DevicePolicyManager mDevicePolicyManager;

    private CheckBoxPreference mLockEnabledPreference;
    private CheckBoxPreference mWipeEnabledPreference;


    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(PREFERENCES_DESCRIPTION_DIALOG_SHOWN, false)) {
            // TODO: HTML tutorial
        }

        Preference.OnPreferenceChangeListener deviceAdminPreferencesOnChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference.getKey().equals(PREFERENCES_LOCK_ENABLED) || preference.getKey().equals(PREFERENCES_WIPE_ENABLED)) {
                    if ((Boolean) newValue && !mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
                        // Activate device admin
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DEVICE_ADMIN_COMPONENT);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.device_admin_reason));
                        startActivityForResult(intent, ACTIVATION_REQUEST);
                    }
                    return true;
                }
                return false;
            }
        };
        mLockEnabledPreference = (CheckBoxPreference) findPreference(PREFERENCES_LOCK_ENABLED);
        mWipeEnabledPreference = (CheckBoxPreference) findPreference(PREFERENCES_WIPE_ENABLED);

        mLockEnabledPreference.setOnPreferenceChangeListener(deviceAdminPreferencesOnChangeListener);
        mWipeEnabledPreference.setOnPreferenceChangeListener(deviceAdminPreferencesOnChangeListener);

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        Preference cellTower = findPreference("cell_key");
        Preference battery = findPreference("battery_key");

        Preference settings = findPreference("settings");

        settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SetupActivity.this,SettingsActivity.class);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVATION_REQUEST:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.device_admin_reason, Toast.LENGTH_LONG).show();
                    mLockEnabledPreference.setChecked(false);
                    mWipeEnabledPreference.setChecked(false);
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mDevicePolicyManager.isAdminActive(DEVICE_ADMIN_COMPONENT)) {
            mLockEnabledPreference.setChecked(false);
            mWipeEnabledPreference.setChecked(false);
        }
    }
}
