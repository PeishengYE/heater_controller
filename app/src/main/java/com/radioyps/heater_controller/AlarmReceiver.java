package com.radioyps.heater_controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by yep on 01/07/16.
 */
public class AlarmReceiver extends BroadcastReceiver {





    public static String CmdGetTemperature = "get_temp";
    public static String CmdGetSwtichStatus =        "get_switch";
    public static String CmdSetSwitchON  =      "switch_on";
    public static String CmdSetSwitchOFF =        "switch_off";

        SharedPrefs mSharedPrefs = new SharedPrefs();
        private static AlarmReceiverObserver alarmReceiverObserver;
        private final static String LOG_TAG = "AlarmReceiver";

    public static String TemperatureSensorAddress = "192.168.12.248";
    //private static String HeaterControllerAddress = "192.168.12.202";
    public static String HeaterControllerAddress = "192.168.12.248";


    public final static int CmdGetTemperatureInt = 0x13;
    public final static int CmdGetSwtichStatusInt =  0x14;
    public final static int CmdSetSwitchONInt  =    0x11;
    public final static int CmdSetSwitchOFFInt =    0x12;
    public static int currentCmd = CmdGetSwtichStatusInt;
    public  static int currentCmdFromSetting = 0;
    public AlarmReceiver(){
        super();

    }

    public AlarmReceiver(AlarmReceiverObserver myAlarmReceiverObserver){
        alarmReceiverObserver = myAlarmReceiverObserver;
    }

    public static void setCurrentCmdFromUI(int input){
        currentCmdFromSetting = input;
    }

    public static int getCurrentCmd(){ return  currentCmd;}

    private int setCurrentCmd()
    {
        if(currentCmdFromSetting != 0){
            currentCmd = currentCmdFromSetting;
            currentCmdFromSetting = 0;
            return currentCmd;
        }

        if(currentCmd == CmdGetSwtichStatusInt){
            currentCmd = CmdGetTemperatureInt;
        }else {
         currentCmd = CmdGetSwtichStatusInt;
        }

        return currentCmd;
    }


    private String [] getCurrentCmdArray(){

        String [] cmd = null;
        setCurrentCmd();
        switch(currentCmd){
            case CmdGetSwtichStatusInt:
               cmd = new String[] {HeaterControllerAddress, CmdGetSwtichStatus};
                break;
            case CmdGetTemperatureInt:
                cmd = new String[] {TemperatureSensorAddress, CmdGetTemperature};
                break;
            case CmdSetSwitchOFFInt:
                cmd = new String[] {HeaterControllerAddress, CmdSetSwitchOFF};
                break;
            case CmdSetSwitchONInt:
                cmd = new String[] {HeaterControllerAddress, CmdSetSwitchON};
                break;
            default:
                break;

        }
        return cmd;
    }


    @Override
        public void onReceive(Context context, Intent intent) {

             // For example
            if(!MainActivity.getTaskRunningStatus()){
                String[] cmd = getCurrentCmdArray();
                    if(cmd[1].equalsIgnoreCase(CmdGetTemperature)){
                        Toast.makeText(context,
                                context.getString(R.string.temp_updating_in_progress), Toast.LENGTH_LONG).show();
                    }else if(cmd[1].equalsIgnoreCase(CmdGetSwtichStatus)){
                        Toast.makeText(context,
                                context.getString(R.string.switch_status_updating_in_progress), Toast.LENGTH_LONG).show();
                    }else if(cmd[1].equalsIgnoreCase(CmdSetSwitchOFF)){
                        Toast.makeText(context,
                                context.getString(R.string.sending_off_in_progress), Toast.LENGTH_LONG).show();
                    }else if(cmd[1].equalsIgnoreCase(CmdSetSwitchON)){
                        Toast.makeText(context,
                                context.getString(R.string.sending_on_in_progress), Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(context,
                                context.getString(R.string.error), Toast.LENGTH_LONG).show();
                    }

                    alarmReceiverObserver.sendCommand(cmd);


            }else{
                Log.d(LOG_TAG, "onReceiver()>> task is still runing, ignore any alarm");
            }



        }

    }


