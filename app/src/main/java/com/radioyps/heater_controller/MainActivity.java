package com.radioyps.heater_controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.io.IOException;

public class MainActivity  extends AppCompatActivity implements AlarmReceiverObserver {



    private static final String LOG_TAG = MainActivity.class.getSimpleName();



    private static int connectPort =5018;


    private  TextView tempView;
    private  TextView cmdStatus;
    private TextView switchStatus;
    private  Button power_button;

	private String response="";
    private static boolean task_in_running = false;
    private AlarmReceiver alarm = null;
    private static long TIME_INTERVAL = 15*1000;
    private static long TIME_DELAY = 3*1000;

    private final static int BUTTON_STATUS_UNKNOWN = 0x14;
    private final static int BUTTON_STATUS_ON = 0x15;
    private final static int BUTTON_STATUS_OFF = 0x16;

    private final static String SEVER_REPLY_SWITCH_ON="switch is on";
    private final static String SEVER_REPLY_SWITCH_OFF="switch is off";
    private final static String NETWORK_ERROR = "network_error";

    private final static int SOCKET_TIMEOUT= 5000;

    private Context mContext = this;
    private  static String currnet_cmd = null;
    private static int button_status = BUTTON_STATUS_UNKNOWN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		tempView = (TextView) findViewById(R.id.temperatureView);
        cmdStatus = (TextView) findViewById(R.id.CmdStatusTextView);
        switchStatus = (TextView) findViewById(R.id.SwitchStatusTextView);
        power_button = (Button)findViewById(R.id.button);
        alarm = new AlarmReceiver(this);
        power_button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                setSwitchOnOffcmd();
            }
        });
        setSupportActionBar(toolbar);
        querySwitchStatus();
        //sendCommand("temp");
    }

    public void onStart() {
        super.onStart();
        //sendCommand("temp");
        querySwitchStatus();
        SetAlarm(this);
    }

    public  void onPause(){
        super.onPause();
        Log.i(LOG_TAG, "onPause()>> cacel alarm..");
        CancelAlarm(this);
    }

    public  void onDestroy(){
        super.onDestroy();
        CancelAlarm(this);
    }

    private void setSwitchOnOffcmd(){
        if(button_status == BUTTON_STATUS_ON){
            if (task_in_running == false){
                setSwitchOn();
            }else{
                AlarmReceiver.setCurrentCmdFromUI(AlarmReceiver.CmdSetSwitchONInt);
            }


        }else if(button_status == BUTTON_STATUS_OFF){


            if (task_in_running == false){
                setSwitchOff();
            }else{
                AlarmReceiver.setCurrentCmdFromUI(AlarmReceiver.CmdSetSwitchOFFInt);
            }
        }else{

        }
    }
    private void setSwitchOff(){
        String [] cmd = new String[] {AlarmReceiver.HeaterControllerAddress, AlarmReceiver.CmdSetSwitchOFF};
        sendCommand(cmd);
    }

    private void setSwitchOn(){
        String [] cmd = new String[] {AlarmReceiver.HeaterControllerAddress, AlarmReceiver.CmdSetSwitchON};
        sendCommand(cmd);
    }

    private void querySwitchStatus(){
        String [] cmd = new String[] {AlarmReceiver.HeaterControllerAddress, AlarmReceiver.CmdGetSwtichStatus};
        sendCommand(cmd);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendCommand(String []cmd){

        if(!isNetworkAvailable(this)){
            Log.i(LOG_TAG, "Network is down, ignore sending cmd");
             return;
        }


        if(task_in_running == false){
            Log.i(LOG_TAG, "Network is OK, Now sending " + cmd[1]);
            task_in_running = true;
            currnet_cmd = cmd[1];
            cmdStatus.setText(getString(R.string.cmd_in_progress));
            sendCmdOverTcpTask sendTask = new sendCmdOverTcpTask();
            sendTask.execute(cmd);

        }else{
            Log.i(LOG_TAG, "Network is OK, however task is running, ignore ");
        }
    }

    public static boolean getTaskRunningStatus(){
        return task_in_running;
    }



    public class sendCmdOverTcpTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = sendCmdOverTcpTask.class.getSimpleName();


        @Override
        protected String[] doInBackground(String... params) {


		Socket socket = null;
            if (params.length == 0) {
                return null;
            }

            Log.i(LOG_TAG, "AsyncTask get param[0]: " + params[0]
             + "param[1]: " + params[1]);
		try {

				response = "";

			socket = new Socket(params[0], connectPort);
            socket.setSoTimeout(SOCKET_TIMEOUT);

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream(1024);

			byte[] buffer = new byte[1024];

			int bytesRead;
			InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();


            outputStream.write(params[1].getBytes());
            outputStream.flush();

			/*
			 * notice: inputStream.read() will block if no data return
			 */
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
				response += byteArrayOutputStream.toString("UTF-8");
			}
            outputStream.close();
            inputStream.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response = "UnknownHostException: " + e.toString();
		} catch (SocketTimeoutException e) {

            Log.i(LOG_TAG, "sendCmdOverTcpTask()>> exception on TIMEOUT error " );
			e.printStackTrace();
            response = NETWORK_ERROR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//response = "IOException: " + e.toString();
            response = NETWORK_ERROR;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
private boolean checkCRC(String line){

    int good_crc;
    int bad_crc;
    good_crc = line.indexOf("YES", 0);
    bad_crc = line.indexOf("NO", 0);

    if(good_crc > 0)
        return true;


    return false;
}



        private String [] parseTemperature()
	{




        double temp_1=-1, temp_2=-1;
        String s_temp_1 ="", s_temp_2 ="";
        String[] result = new String[2];


		String lines[] = response.split("\\r?\\n");

        if(lines.length != 4){
            Log.i(LOG_TAG, "The received line have error, ignore !");
            Log.i(LOG_TAG, "lines:" + response);
            return null;
        }

        Log.i(LOG_TAG, "line 0: " + lines[0]);
        Log.i(LOG_TAG, "line 1: " + lines[1]);
        Log.i(LOG_TAG, "line 3: " + lines[2]);
        Log.i(LOG_TAG, "line 4: " + lines[3]);

        if(checkCRC(lines[0])){
        s_temp_1 = lines[1].split("=")[1];
        temp_1 = Double.parseDouble(s_temp_1);
        temp_1 /= 1000;

            result[0]= Double.toString(temp_1);


         }


        if(checkCRC(lines[2])){
        s_temp_2 = lines[3].split("=")[1];
        temp_2 = Double.parseDouble(s_temp_2);
        temp_2 /= 1000;
            result[1] = Double.toString(temp_2);
        }


        Log.i(LOG_TAG, " temp 1 = " + result[0]);
        Log.i(LOG_TAG, " temp 2 = " + result[1]);


		return result;
	}

        private void updateResponseForTemp(){

        String[] temp = parseTemperature();
        if(temp == null){
            task_in_running = false;
            return;
        }


        //tempView.setText(parseTemperature());

        //if((temp[0] != null)&&(temp[1] != null)){
        if((temp[0] != null)){
            tempView.setText(getString(R.string.sensor_1_place));
            tempView.append(": " + temp[0]);
            //tempView.append("\n");
            // tempView.append(R.string.sensor_2_place);
            // tempView.append(temp[1]);

            Log.i(LOG_TAG, "onPostExecute temp 1 = " + temp[0]);
            Log.i(LOG_TAG, "onPostExecute temp 2 = " + temp[1]);
        }


        }
        private void updateResponseForSwitchStatus() {
            Log.i(LOG_TAG, "updateResponseForSwitchStatus()>> response: " + response);
            /* set SwitchStatusTextView*/
            if(response.equalsIgnoreCase(SEVER_REPLY_SWITCH_ON)){

                switchStatus.setText(getString(R.string.current_switch_status)
                        + getString(R.string.power_on));
                power_button.setText(getString(R.string.button_power_off));
                button_status = BUTTON_STATUS_OFF;

            }else if(response.equalsIgnoreCase(SEVER_REPLY_SWITCH_OFF)){

                switchStatus.setText(getString(R.string.current_switch_status)
                        + getString(R.string.power_off));
                power_button.setText(getString(R.string.button_power_on));
                button_status = BUTTON_STATUS_ON;
            }else{
                cmdStatus.setText("Error on cmd");
                switchStatus.setText(getString(R.string.unknow_state));
                power_button.setText(getString(R.string.disable_button_state));
                button_status = BUTTON_STATUS_UNKNOWN;
            }
            /* set Button status*/
        }

        private void updateResponseForSwitchOnOff() {

            /* set cmdStatus Text view */
        }


        @Override
        protected void onPostExecute(String[] result) {


            task_in_running = false;
            cmdStatus.setText(getString(R.string.cmd_is_done));
            if(response.equalsIgnoreCase(NETWORK_ERROR)){
                StringBuilder toastString = new StringBuilder();
                String cmdType = null;
                if(currnet_cmd.equalsIgnoreCase(AlarmReceiver.CmdGetTemperature)){
                      cmdType = getResources().getString(R.string.query_temprature);
                      toastString.append(cmdType);
                }else if(currnet_cmd.equalsIgnoreCase(AlarmReceiver.CmdGetSwtichStatus)){
                    cmdType = getResources().getString(R.string.query_switch);
                    toastString.append(cmdType);
                }
                toastString.append(getResources().getString(R.string.network_error_try_again));
                Toast.makeText(mContext,toastString.toString(), Toast.LENGTH_LONG).show();
                return;
            }
            if(currnet_cmd.equalsIgnoreCase(AlarmReceiver.CmdGetTemperature)){
                updateResponseForTemp();
            }else if(currnet_cmd.equalsIgnoreCase(AlarmReceiver.CmdGetSwtichStatus)){
                updateResponseForSwitchStatus();

            }else if(currnet_cmd.equalsIgnoreCase(AlarmReceiver.CmdSetSwitchOFF)){
                updateResponseForSwitchStatus();
            }else if(currnet_cmd.equalsIgnoreCase(AlarmReceiver.CmdSetSwitchON)){
                updateResponseForSwitchStatus();
            }





        }



    }

    public static boolean isNetworkAvailable(Context context) {
        boolean isMobile = false, isWifi = false;
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] infoAvailableNetworks = connMgr.getAllNetworkInfo();
        if (infoAvailableNetworks != null) {
            for (NetworkInfo network : infoAvailableNetworks) {
                if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                    //Log.d(LOG_TAG ,"Received Network Status Changed 	5");
                    if (network.isConnected() && network.isAvailable())
                        isWifi = true;
                    //Log.d(LOG_TAG ,"Received Network Status Changed 	6 + "+isWifi);
                }

                if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                    boolean bol = network.getState() == NetworkInfo.State.CONNECTED;
                    //Log.d(LOG_TAG ,"Received Network Status Changed 	7 + " + network.isAvailable()  +" AND " + network.isConnected());
                    if (network.isAvailable() && network.isConnected()){
                        isMobile = true;
                    }
                    //Log.d(LOG_TAG ,"Received Network Status Changed 	8 +  "+ isMobile);
                }
            }
        }

        return isWifi;
    }


    public void SetAlarm(Context context) {
        //Toast.makeText(context, R.string.updating_in_progress, Toast.LENGTH_LONG).show(); // For example
        Log.d(LOG_TAG, "Set alarm!");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intnt = new Intent(context, AlarmReceiver.class);
        PendingIntent pendngIntnt = PendingIntent.getBroadcast(context, 0, intnt, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_DELAY, TIME_INTERVAL, pendngIntnt);
    }

    public void CancelAlarm(Context context) {
        Log.d(LOG_TAG, "Cancle alarm!");
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
