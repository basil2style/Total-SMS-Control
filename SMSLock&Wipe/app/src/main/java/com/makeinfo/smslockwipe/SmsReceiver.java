package com.makeinfo.smslockwipe;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;


public class SmsReceiver extends BroadcastReceiver {
    // Statics
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String EXTRA_SMS_PDUS = "pdus";

    SmsManager smsManager = SmsManager.getDefault();
    CellLocation location;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {

            Bundle extras = intent.getExtras();
            if (extras != null) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                SmsMessage[] messages = getMessagesFromIntent(intent);
                for (SmsMessage sms : messages) {
                    String body = sms.getMessageBody();
                    // TODO: whitelist/blacklist of allowed senders
                    // String address = sms.getOriginatingAddress();

                    boolean alarmEnabled = preferences.getBoolean(SetupActivity.PREFERENCES_ALARM_ENABLED,
                            context.getResources().getBoolean(R.bool.config_default_alarm_enabled));
                    boolean lockEnabled = preferences.getBoolean(SetupActivity.PREFERENCES_LOCK_ENABLED,
                            context.getResources().getBoolean(R.bool.config_lock_enabled));
                    boolean wipeEnabled = preferences.getBoolean(SetupActivity.PREFERENCES_WIPE_ENABLED,
                            context.getResources().getBoolean(R.bool.config_wipe_enabled));
                    boolean callforwardEnabled = preferences.getBoolean(SetupActivity.PREFERENCES_CALL_FORWARD_ENABLED,
                            context.getResources().getBoolean(R.bool.config_call_forwarding_enabled));
                    boolean batterylevelEnabled = preferences.getBoolean(SetupActivity.PREFERENCES_BATTERY_LEVEL_ENABLED,
                            context.getResources().getBoolean(R.bool.config_battery_level_enabled));
                    boolean celllocationEnabled = preferences.getBoolean(SetupActivity.PREFERENCES_CELL_LOCATION_ENABLED,
                            context.getResources().getBoolean(R.bool.config_battery_level_enabled));

                    String activationAlarmSms = preferences.getString(SetupActivity.PREFERENCES_ALARM_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_default_alarm_activation_sms));
                    String activationLockSms = preferences.getString(SetupActivity.PREFERENCES_LOCK_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_lock_activation_sms));
                    String activationWipeSms = preferences.getString(SetupActivity.PREFERENCES_WIPE_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_wipe_activation_sms));
                    String activationCallforwardSms = preferences.getString(SetupActivity.PREFERENCES_CALL_FORWARD_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_default_call_forwarding_activation_sms));
                    String activationBatterySms = preferences.getString(SetupActivity.PREFERENCES_BATTERY_LEVEL_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_default_battery_activation_sms));
                    String activationCellTowerSms = preferences.getString(SetupActivity.PREFERENCES_CELL_LOCATION_ACTIVATION_SMS,
                            context.getResources().getString(R.string.config_default_cell_tower_activation_sms));



                    if (alarmEnabled && body.startsWith(activationAlarmSms)) {
                        Intent alarmIntent = new Intent(context, AlarmDialogActivity.class);
                        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        context.startActivity(alarmIntent);
                    }

                    if (lockEnabled && body.startsWith(activationLockSms)) {
                        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        if (devicePolicyManager.isAdminActive(SetupActivity.DEVICE_ADMIN_COMPONENT)) {
                            String password = preferences.getString(SetupActivity.PREFERENCES_LOCK_PASSWORD,
                                    context.getResources().getString(R.string.config_lock_password));
                            if (body.length() > activationLockSms.length() + 1) {
                                password = body.substring(activationLockSms.length() + 1);
                            }
                            if (password.length() > 0) {
                                devicePolicyManager.resetPassword(password, 0);
                            }
                            devicePolicyManager.lockNow();
                        }
                    }

                    if (wipeEnabled && body.startsWith(activationWipeSms)) {
                        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        if (devicePolicyManager.isAdminActive(SetupActivity.DEVICE_ADMIN_COMPONENT)) {
                            devicePolicyManager.wipeData(0);
                        }
                    }

                    if (callforwardEnabled && body.startsWith(activationCallforwardSms)){
                        Intent forwardIntent = new Intent(context, CallForwardActivity.class);
                        forwardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        forwardIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        context.startActivity(forwardIntent);
                    }

                    if (batterylevelEnabled && body.startsWith(activationBatterySms)){
                        IntentFilter filter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                        Intent batt=context.getApplicationContext().registerReceiver(null,filter);
                        int temp=0;
                        int level = 99; // Default to some unknown/wild value
// registerReceiver method call could return null, so check that!
                        if (batt != null) {
                            level = batt.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                            temp  =batt.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
                        }
                        smsManager.sendTextMessage(sms.getOriginatingAddress(), null,String.valueOf("Battery Percent:"+level+"%"+" "+"Temperature :"+temp), null, null);
                    }

                    if (celllocationEnabled&& body.startsWith(activationCellTowerSms)){
                        TelephonyManager telm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                        int type = telm.getPhoneType();
                        String networkOperator = telm.getNetworkOperator();
                        int mcc = 0,mnc=0;
                        int cid=0,lac=0;

                        if(type==1){
                            GsmCellLocation gcellLocation = (GsmCellLocation) telm.getCellLocation();
                            try{
                                if (networkOperator != null || networkOperator.length()!=0) {
                                    cid = gcellLocation.getCid();
                                    lac = gcellLocation.getLac();
                                }
                            }catch (Exception e){

                            }
                            if (type==2){
                                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) telm.getCellLocation();
                                try{
                                    if (networkOperator !=null || networkOperator.length()!=0){
                                        cid = cdmaCellLocation.getBaseStationId();
                                        lac = cdmaCellLocation.getBaseStationLatitude();
                                    }
                                }catch (Exception e){

                                }

                            }
                        }

                        try{
                            if (networkOperator != null || networkOperator.length()!=0) {
                                mcc = Integer.parseInt(networkOperator.substring(0, 3));
                                mnc = Integer.parseInt(networkOperator.substring(3));
                            }
                        }catch (Exception e){
                            mcc =0;mnc=0;
                        }
                        smsManager.sendTextMessage(sms.getOriginatingAddress(), null,String.valueOf("CID:" + cid + " LAC :" + lac+" MCC :" + mcc + " MNC :" + mnc + " Phone Type :" + type + " IMEI :" + telm.getDeviceId()), null, null);
                    }

                }
            }
        }
    }


    private SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra(EXTRA_SMS_PDUS);
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }


}
