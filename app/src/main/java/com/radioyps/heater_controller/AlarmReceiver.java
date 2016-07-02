package com.radioyps.heater_controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.widget.Toast;
import com.radioyps.heater_controller.MainActivity;
import com.radioyps.heater_controller.AlarmReceiverObserver;
/**
 * Created by yep on 01/07/16.
 */
public class AlarmReceiver extends BroadcastReceiver {



        //1 * 24 * 60 *60 * 1000; // Hour*Minutes*Seconds*MilliSeconds  FOR EXAMPLE := 1Hour = (60 * 60 * 1000)MilliSeconds
        public static boolean waitWhileSendingDataUsage = false;
        SharedPrefs mSharedPrefs = new SharedPrefs();
        private static AlarmReceiverObserver alarmReceiverObserver;

    public AlarmReceiver(){
        super();

    }

    public AlarmReceiver(AlarmReceiverObserver myAlarmReceiverObserver){
        alarmReceiverObserver = myAlarmReceiverObserver;
    }


    @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(context, R.string.updating_in_progress, Toast.LENGTH_LONG).show(); // For example
            Log.d("DWNetMon", "ALARMMmmmmm");
            if(!waitWhileSendingDataUsage) {
                waitWhileSendingDataUsage = true;
                if (MainActivity.isNetworkAvailable(context)) {
                    Log.d("DWNetMon", "ALARMMmmmmm:Sending Data Usage");
                    mSharedPrefs.setSharedPrefsString("sendDataStatsToServer", "true", context);

                } else {
                    Log.d("DWNetMon", "ALARMMmmmmm: Ignore sendDataStatsToServer still rocessing");
                    mSharedPrefs.setSharedPrefsString("sendDataStatsToServer", "true", context);
                    waitWhileSendingDataUsage = false;
                }
            }

        alarmReceiverObserver.sendCommand("get temp");
        }

    }


