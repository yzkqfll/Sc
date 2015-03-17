/*
 * Copyright (C) 2015 Ganesh Mahendran <opensource.ganesh@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zgzhsh.smartcontroller;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ScNetConfig extends Activity {
	/**
	 * WIFI controller
	 */
	private ScWifiAdmin wifiAdmin = null;
	
	/**
	 * Home AP SSID input text 
	 */
	private EditText mSsidEditText = null;
	/**
	 * Home AP password input text  
	 */
	private EditText mPasswdEditText = null;
	/**
	 * Home AP name
	 */
	private EditText mDevNameEditText = null;
	
	/**
	 * Start Net Config:<p>
	 *   1) Disconnect from Home AP<p>
	 *   2) Connect to Board soft AP<p>
	 *   3) Send UDP packet encapsulated with SSID/PASSWD to board<p>
	 *   4) Disconnect from Board soft AP<p>
	 *   5) Connect to Home AP<p>
	 *   6) Send UDP broadcast packet in Home AP local LAN<p>
	 *   7) Get IP of Board(as station)<p>
	 */
	private Button mStartButton = null;
	/**
	 * Progress bar when configuration
	 */
	private ProgressBar mProgressBar = null;
	
	/**
	 * Flag to avoid duplicated pressing "start" button
	 */
	private boolean mIsConfiguring = false;
	private boolean mUpdateUI  = false;
	
	private Timer timer = new Timer();
	
	/**
	 * Check whether the WIFI is connected
	 * @return
	 */
	private boolean checkWifiLink(boolean showToast) {
		if (wifiAdmin.isConnected())
			return true;
				
		if (showToast) {
			Toast t = Toast.makeText(getApplicationContext(), 
					"手机未连接WIFI", Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();	
		}
										
		return false;
	}
	
	/**
	 * Check the length of password user typed
	 * @return
	 */
	private boolean checkPasswd(boolean showToast) {
		String passwd = mPasswdEditText.getText().toString().trim();
		if (passwd.length() > 0)
			return true;

		//System.out.println("[NetConfig] mPasswdEditText is empty");
		
		if (showToast) {
			Toast t = Toast.makeText(getApplicationContext(), 
					"请输入密码", Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();	
		}						

		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.sc_net_conf);		
		
		wifiAdmin = new ScWifiAdmin(this);
		
		initUI();
							
		timerUpdateUI();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		registerReceiver(broadcastReceiver, intentFilter);				
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		
		super.onDestroy();
	}
	
	private boolean updateUI(boolean inConfiguring) {
		if (inConfiguring) {
			System.out.println("[NetConfig] Enable process bar");
			mStartButton.setBackgroundResource(R.drawable.nc_btn_pressed);
			mProgressBar.setVisibility(ProgressBar.VISIBLE);						
			mStartButton.setText(getResources().getString(R.string.nc_stop_text));
		} else {
			System.out.println("[NetConfig] Disable process bar");
			mStartButton.setBackgroundResource(R.drawable.nc_btn);
			mProgressBar.setVisibility(ProgressBar.INVISIBLE);
			mStartButton.setText(getResources().getString(R.string.nc_start_text));	
		}
	
		return true;
	}
	
	private boolean setUiUpdateFlag(boolean inConfiguring) {
		if (inConfiguring) 
			mIsConfiguring = true;			
		else 
			mIsConfiguring = false;
		
		mUpdateUI = true;
		
		return true;
	}
	
	
	/**
	 * Initialize UI
	 */
	private void initUI() {
		/**
		 * Get views
		 */
		mSsidEditText = (EditText)findViewById(R.id.nc_ssid_input);
		mPasswdEditText = (EditText)findViewById(R.id.nc_passwd_input);
		mDevNameEditText = (EditText)findViewById(R.id.nc_dev_name_input);
		mStartButton = (Button)findViewById(R.id.nc_start);
		
		mProgressBar = (ProgressBar)findViewById(R.id.nc_progress);		
		mProgressBar.bringToFront();
		
		/**
		 * Initialize views
		 */
		mDevNameEditText.setEnabled(false);
		
		mSsidEditText.setText(wifiAdmin.getSSID());		
		mSsidEditText.setEnabled(false);
		mSsidEditText.setFocusable(false);	
		
		/**
		 * Bind listener with button  
		 */
		setViewClickListeners();
		
	}

	private void setViewClickListeners() {
		mStartButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				switch (v.getId()) {
				case R.id.nc_start:					
					System.out.println("[NetConfig] Start button clicked");
					
					if (!mIsConfiguring) {																
						if (!checkWifiLink(true))
							break;
									
						setUiUpdateFlag(true);
						
						if (!checkPasswd(false))
							System.out.println("[NetConfig] home AP passwd is null");
												
						sendSSIDtoBoard();

					} else {
												
						stopSendSSIDtoBoard();						
					}

					break;

				default:
					break;
				}
				
			}
		});			
	}
	
	

	void timerUpdateUI() {
			
		int periodicDelay = 1000; // 1 sec.
		int timeInterval = 1000; // 1 sec.

		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {	
						if (mUpdateUI) {
							mUpdateUI = false;
							updateUI(mIsConfiguring);							
						}
							
					}
				});
			}
		}, periodicDelay, timeInterval);
		
	}	
	
	private boolean sendSSIDtoBoard() {
		Thread thread = new Thread(new Runnable() {					
			@Override
			public void run() {					
					                                              
				wifiAdmin.disconnectFromAP();		
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (wifiAdmin.isConnected())
					System.out.println("[NetConfig] sendSSIDtoBoard(): still connected?");
				
				/**
				 * Try to connect to Board(as soft AP)
				 */
				if (!wifiAdmin.connectToApWithoutKey(ScConstants.BOARD_AP_SSID)) {
					System.out.println("[NetConfig] connectToApWithoutKey() failed");
					setUiUpdateFlag(false);
					return;
				}
				
				int cnt = 0;
				while (!wifiAdmin.isConnected()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (cnt++ > 10)
						break;
				}
				if (!wifiAdmin.isConnected()) {
					System.out.printf("[NetConfig] fail to connect to %s\n", ScConstants.BOARD_AP_SSID);
					setUiUpdateFlag(false);
					return;
				}
				System.out.println("[NetConfig] connect to " + ScConstants.BOARD_AP_SSID);
										
				String ssid = mSsidEditText.getText().toString().trim();
				String passwd = mPasswdEditText.getText().toString().trim();				
				String msg = String.format("###%s:%s$", ssid, passwd);
				
				try {
					/**
					 * Send ssid:passwd of home AP to Board(as soft AP)
					 */
					UdpClient udpClient;
					udpClient = new UdpClient(wifiAdmin.getGateway(), ScConstants.BOARD_AP_UDP_SERVER_PORT);
										
					udpClient.sendData(msg.getBytes());			
					udpClient.recvData(false);
														
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/**
				 * Reconnect to home AP
				 */
				wifiAdmin.connectToAp(ssid);
				
				setUiUpdateFlag(false);
			}
		});
		
		thread.start();
		         		
		return true;
	}
	
	private boolean stopSendSSIDtoBoard() {
		Thread thread = new Thread(new Runnable() {					
			@Override
			public void run() {					
					                                              
				wifiAdmin.disconnectFromAP();		
															
				String ssid = mSsidEditText.getText().toString().trim();
				System.out.println("[NetConfig] stopSendSSIDtoBoard(): connect to " + ssid);
				
				/**
				 * Reconnect to home AP
				 */
				wifiAdmin.connectToAp(ssid);
				
				setUiUpdateFlag(false);
			}
		});
		
		thread.start();
		         		
		return true;
	}
	
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, android.content.Intent intent) {
			final String action = intent.getAction();
			
			if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
				System.out.println("[NetConfig] SUPPLICANT_CONNECTION_CHANGE_ACTION");
			}
			
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {															
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				
				if (info.getType() == ConnectivityManager.TYPE_WIFI) {	

					if (info.isConnected()) {
						//System.out.printf("[NetConfig] broadcast: connected to %s\n" + wifiAdmin.getSSID());
						System.out.println("[NetConfig] broadcast: connected to " + wifiAdmin.getSSID());						
					} else {
						System.out.printf("[NetConfig] broadcast: disconnected\n");						
					}
					
					if (!mIsConfiguring)
						mSsidEditText.setText(wifiAdmin.getSSID());
					/*
					if (info.getDetailedState() == DetailedState.CONNECTED) {
						WifiManager wManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						WifiInfo wInfo = wManager.getConnectionInfo();
												
						mSsidEditText.setText(wInfo.getSSID());
					} else {						
						mSsidEditText.setText("");	
					}
					*/					
				}			
			}
		};
	};
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sc_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
