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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.net.UnknownHostException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.io.IOException;
import com.radioyps.heater_controller.AlarmReceiver;
public class MainActivity  extends AppCompatActivity implements AlarmReceiverObserver {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
	TextView textResponse;
	String response="";
    private boolean task_in_running = false;
    AlarmReceiver alarm = null;
    private static long TIME_INTERVAL = 15*1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textResponse = (TextView) findViewById(R.id.responsTextView);
        alarm = new AlarmReceiver(this);
        setSupportActionBar(toolbar);
        sendCommand("temp");
    }

    public void onStart() {
        super.onStart();
        sendCommand("temp");
        SetAlarm(this);
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

    public void sendCommand(String cmd){

        if(!isNetworkAvailable(this)){
            Log.i(LOG_TAG, "Network is down, ignore sending cmd");
             return;
        }

        Log.i(LOG_TAG, "Network is OK, Now sending " + cmd);
        if(task_in_running == false){
            task_in_running = true;
            sendCmdOverTcpTask sendTask = new sendCmdOverTcpTask();
            sendTask.execute(cmd);

        }
    }



    public void sendPowerOncmd(View view){

        Log.i(LOG_TAG, "send power on cmd");
        sendCommand("power on");

    }

    public void sendPowerOffcmd(View view){
        Log.i(LOG_TAG, "send power off cmd");
        sendCommand("power off");
    }


    public class sendCmdOverTcpTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = sendCmdOverTcpTask.class.getSimpleName();
	    String dstAddress = "192.168.12.248";
	    int dstPort =5013;

        @Override
        protected String[] doInBackground(String... params) {


		Socket socket = null;
            if (params.length == 0) {
                return null;
            }


		try {

				response = "";
			socket = new Socket(dstAddress, dstPort);

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream(1024);

			byte[] buffer = new byte[1024];

			int bytesRead;
			InputStream inputStream = socket.getInputStream();

			/*
			 * notice: inputStream.read() will block if no data return
			 */
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
				response += byteArrayOutputStream.toString("UTF-8");
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response = "UnknownHostException: " + e.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response = "IOException: " + e.toString();
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
private boolean check_crc(String line){

    int good_crc;
    int bad_crc;
    good_crc = line.indexOf("YES", 0);
    bad_crc = line.indexOf("NO", 0);

    if(good_crc > 0)
        return true;


    return false;
}



        private String [] get_temp ()
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

        if(check_crc(lines[0])){
        s_temp_1 = lines[1].split("=")[1];
        temp_1 = Double.parseDouble(s_temp_1);
        temp_1 /= 1000;

            result[0]= Double.toString(temp_1);


         }


        if(check_crc(lines[2])){
        s_temp_2 = lines[3].split("=")[1];
        temp_2 = Double.parseDouble(s_temp_2);
        temp_2 /= 1000;
            result[1] = Double.toString(temp_2);
        }


        Log.i(LOG_TAG, " temp 1 = " + result[0]);
        Log.i(LOG_TAG, " temp 2 = " + result[1]);


		return result;
	}




        @Override
        protected void onPostExecute(String[] result) {
            String[] temp = get_temp();
            if(temp == null){
                task_in_running = false;
                return;
            }


             //textResponse.setText(get_temp());

            //if((temp[0] != null)&&(temp[1] != null)){
            if((temp[0] != null)){
                textResponse.setText(R.string.sensor_1_place);
            textResponse.append(": "+ temp[0]);
            //textResponse.append("\n");
           // textResponse.append(R.string.sensor_2_place);
           // textResponse.append(temp[1]);

                Log.i(LOG_TAG, "onPostExecute temp 1 = " + temp[0]);
                Log.i(LOG_TAG, "onPostExecute temp 2 = " + temp[1]);
            }

            task_in_running = false;

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
        Toast.makeText(context, "Set Alarm!", Toast.LENGTH_LONG).show(); // For example
        Log.d("DWNetMon", "Set alarm!");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intnt = new Intent(context, AlarmReceiver.class);
        PendingIntent pendngIntnt = PendingIntent.getBroadcast(context, 0, intnt, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, TIME_INTERVAL, pendngIntnt);
    }

    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
