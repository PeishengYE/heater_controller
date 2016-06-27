package com.radioyps.heater_controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.net.UnknownHostException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.io.IOException;
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
	TextView textResponse;
	String response="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textResponse = (TextView) findViewById(R.id.responsTextView);

        setSupportActionBar(toolbar);

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

    public void sendPowerOncmd(View view){

        Log.i(LOG_TAG, "send power on cmd");
		sendCmdOverTcpTask sendTask = new sendCmdOverTcpTask();
		sendTask.execute("power on");

    }

    public void sendPowerOffcmd(View view){
        Log.i(LOG_TAG, "send power off cmd");
		sendCmdOverTcpTask sendTask = new sendCmdOverTcpTask();
		sendTask.execute("power off");
    }


    public class sendCmdOverTcpTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = sendCmdOverTcpTask.class.getSimpleName();
	    String dstAddress = "192.168.12.202";
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






        @Override
        protected void onPostExecute(String[] result) {
            Log.i(LOG_TAG, "response: " + response);
				textResponse.setText(response);

        }



    }

    }
