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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ScNetConfig extends Activity {

	static final int MSG_START_NET_CONFIG = 1;
	static final int MSG_STOP_NET_CONFIG = 2;
	static final int MSG_TOAST_WIFI_NOT_CONNECTED = 3;
	static final int MSG_TOAST_WIFI_NEED_CONNECT_TO_HOMEAP = 4;
	static final int MSG_TOAST_WIFI_CONNECT_TO_BOARD_TIMEOUT = 5;
	static final int MSG_TOAST_WIFI_BOARD_AP_NOT_EXISTED = 6;
	static final int MSG_TOAST_SUCCEED_TO_CONFIG_NET = 7;
	static final int MSG_TOAST_FAIL_TO_CONFIG_NET = 8;

	/**
	 * WIFI controller
	 */
	private ScWifiAdmin mWifiAdmin = null;

	/**
	 * Home AP SSID input text
	 */
	private EditText mHomeSsidText = null;
	/**
	 * Home AP password input text
	 */
	private EditText mHomePasswdText = null;
	/**
	 * Home AP name
	 */
	private EditText mDevNameText = null;

	private String mHomeSsid;
	private String mHomePasswd;

	private Button mStartNetConfigBtn = null;

	/**
	 * Progress bar when configuration
	 */
	private ProgressBar mProgressBar = null;

	/**
	 * Flag to avoid duplicated pressing "start" button
	 */
	private volatile boolean mInProcess = false;
	private volatile boolean mStopNetConfig = false;

	private Lock mLock = new ReentrantLock();

	private Timer timer = new Timer();

	private ScDataStorage mDataStorage;

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Toast t;

			switch (msg.what) {
			case MSG_START_NET_CONFIG:

				mInProcess = true;
				System.out
						.println("[NetConfig][MSG] Start net config: Update UI");
				mStartNetConfigBtn
						.setBackgroundResource(R.drawable.nc_btn_pressed);
				mProgressBar.setVisibility(ProgressBar.VISIBLE);
				mStartNetConfigBtn.setText(getResources().getString(
						R.string.nc_stop_text));

				break;

			case MSG_STOP_NET_CONFIG:
				mInProcess = false;
				System.out
						.println("[NetConfig][MSG] Stop net config: Update UI");
				mStartNetConfigBtn.setBackgroundResource(R.drawable.nc_btn);
				mProgressBar.setVisibility(ProgressBar.INVISIBLE);
				mStartNetConfigBtn.setText(getResources().getString(
						R.string.nc_start_text));

				break;

			case MSG_TOAST_WIFI_NOT_CONNECTED:
				if (!mHandler.hasMessages(MSG_TOAST_WIFI_NOT_CONNECTED)) {
					t = Toast.makeText(getApplicationContext(), "手机未连接WIFI",
							Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
				}
				break;

			case MSG_TOAST_WIFI_NEED_CONNECT_TO_HOMEAP:
				t = Toast.makeText(getApplicationContext(), "请连接家中无线路由器",
						Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();

				break;

			case MSG_TOAST_WIFI_CONNECT_TO_BOARD_TIMEOUT:
				t = Toast.makeText(getApplicationContext(), "配置超时",
						Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();

				break;

			case MSG_TOAST_WIFI_BOARD_AP_NOT_EXISTED:
				t = Toast.makeText(getApplicationContext(), "请确认智能遥控器已经打开",
						Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();

				break;

			case MSG_TOAST_SUCCEED_TO_CONFIG_NET:
				t = Toast.makeText(getApplicationContext(), "成功配置智能遥控器",
						Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();

				break;

			case MSG_TOAST_FAIL_TO_CONFIG_NET:
				t = Toast.makeText(getApplicationContext(), "配置智能遥控器失败",
						Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();

				break;

			default:
				break;
			}

			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_net_conf);

		mWifiAdmin = new ScWifiAdmin(this);

		mDataStorage = new ScDataStorage(this, ScConstants.NET_CONFIG_FILE);

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

	/**
	 * Initialize UI
	 */
	private void initUI() {
		/**
		 * Get views
		 */
		mHomeSsidText = (EditText) findViewById(R.id.nc_ssid_input);
		mHomePasswdText = (EditText) findViewById(R.id.nc_passwd_input);
		mDevNameText = (EditText) findViewById(R.id.nc_dev_name_input);
		mStartNetConfigBtn = (Button) findViewById(R.id.nc_start);

		mProgressBar = (ProgressBar) findViewById(R.id.nc_progress);
		mProgressBar.bringToFront();

		/**
		 * Initialize views
		 */
		mDevNameText.setEnabled(false);

		mHomeSsidText.setText(mWifiAdmin.getSSID());
		mHomeSsidText.setEnabled(false);
		mHomeSsidText.setFocusable(false);

		/**
		 * Bind listener with button
		 */
		setViewClickListeners();

	}

	private void setViewClickListeners() {
		mStartNetConfigBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				switch (v.getId()) {
				case R.id.nc_start:

					if (!mInProcess) {
						System.out.println("[NetConfig] User Start net config");
						startNetConfig();
					} else {
						System.out
								.println("[NetConfig] User wants to stop net config");
						mStopNetConfig = true;
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
		int timeInterval = 500; // 1 sec.

		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {

					}
				});
			}
		}, periodicDelay, timeInterval);
	}

	public static String getBoardStaIP() {
		// ScWifiAdmin scWifiAdmin = new ScWifiAdmin(c);

		String msg = String.format("###%s", "getIP");

		ScUdpClient udpClient = new ScUdpClient("255.255.255.255",
				ScConstants.BOARD_STA_UDP_SERVER_PORT);

		udpClient.sendData(new UserData(msg));
		UserData userData = udpClient.recvData(false);

		return userData.getPeerIP();
	}

	public static int getBoardStaUdpPort() {

		String msg = String.format("###%s", "getIP");

		ScUdpClient udpClient = new ScUdpClient("255.255.255.255",
				ScConstants.BOARD_STA_UDP_SERVER_PORT);

		udpClient.sendData(new UserData(msg));
		UserData userData = udpClient.recvData(false);

		return userData.getPeerPort();
	}

	/**
	 * Start Net Config:
	 * <p>
	 * 1) Disconnect from Home AP
	 * <p>
	 * 2) Connect to Board soft AP
	 * <p>
	 * 3) Send UDP packet encapsulated with SSID/PASSWD to board
	 * <p>
	 * 4) Disconnect from Board soft AP
	 * <p>
	 * 5) Connect to Home AP
	 * <p>
	 * 6) Send UDP broadcast packet in Home AP local LAN
	 * <p>
	 * 7) Get IP of Board(as station)
	 * <p>
	 */
	private boolean startNetConfig() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				mHandler.sendEmptyMessage(MSG_START_NET_CONFIG);

				try {
					System.out.println("[NetConfig] Check wifi link...");
					if (!mWifiAdmin.isConnected()) {
						mHandler.sendEmptyMessage(MSG_TOAST_WIFI_NOT_CONNECTED);
						return;
					}

					/**
					 * Get Home AP SSID:PASSWD
					 */
					String ssid = mWifiAdmin.getSSID().trim();
					if (ssid.equals(ScConstants.BOARD_AP_SSID)) {
						mHandler.sendEmptyMessage(MSG_TOAST_WIFI_NEED_CONNECT_TO_HOMEAP);
						return;
					}
					mHomeSsid = ssid;

					mHomePasswd = mHomePasswdText.getText().toString().trim();
					mHomePasswd = "liufangnan2008";
					System.out.printf(
							"[NetConfig] Home AP：[%s], password [%s]\n",
							mHomeSsid, mHomePasswd);

					/**
					 * 1. Disconnect from Home AP
					 */
					System.out.println("[NetConfig] Disconnect from Home AP ["
							+ mHomeSsid + "]");
					mWifiAdmin.disconnectFromAP();

					Thread.sleep(200);

					if (mWifiAdmin.isConnected())
						System.out
								.println("[NetConfig] Fail to disconnect from Home AP ["
										+ mHomeSsid + "] ??");

					if (mStopNetConfig)
						return;

					/**
					 * 2. Connect to Board
					 */
					System.out.println("[NetConfig] Try to connect to Board ["
							+ ScConstants.BOARD_AP_SSID + "]");
					if (!mWifiAdmin
							.connectToApWithoutKey(ScConstants.BOARD_AP_SSID)) {
						System.out
								.println("[NetConfig] Fail to connect to Board ["
										+ ScConstants.BOARD_AP_SSID + "]");
						mHandler.sendEmptyMessage(MSG_TOAST_WIFI_BOARD_AP_NOT_EXISTED);
						return;
					}

					Thread.sleep(100);
					for (int i = 0; i < 100; i++) {
						if (mStopNetConfig)
							return;

						if (mWifiAdmin.isConnected())
							break;

						Thread.sleep(100);
					}

					if (!mWifiAdmin.isConnected()) {
						System.out
								.println("[NetConfig] Timeout: fail to connect to Board ["
										+ ScConstants.BOARD_AP_SSID + "]");
						mHandler.sendEmptyMessage(MSG_TOAST_WIFI_CONNECT_TO_BOARD_TIMEOUT);
						return;
					}

					if (!mWifiAdmin.getSSID().equals(ScConstants.BOARD_AP_SSID)) {
						System.out.println("[NetConfig] Connected to "
								+ mWifiAdmin.getSSID() + "??, exit");
						return;
					}

					System.out.println("[NetConfig] Connected to Board ["
							+ ScConstants.BOARD_AP_SSID + "]");

					/**
					 * 3. Send UDP packet(ssid:passwd) to Board
					 */
					String msg = String.format("###%s:%s$", mHomeSsid,
							mHomePasswd);
					System.out.printf("[NetConfig] Send ###%s:%s$ to Board\n",
							mHomeSsid, mHomePasswd);

					ScUdpClient udpClient = new ScUdpClient(mWifiAdmin
							.getGateway(), ScConstants.BOARD_AP_UDP_SERVER_PORT);

					udpClient.sendData(new UserData(msg));
					UserData userData = udpClient.recvData(false);
					if (new String(userData.getData()).equals("OK")) {
						System.out.println("[NetConfig] Net Config Completed");
						mHandler.sendEmptyMessage(MSG_TOAST_SUCCEED_TO_CONFIG_NET);

						mDataStorage.setValue("ConfigCompleted", true);
					} else {
						System.out.println("[NetConfig] Net Config Failed");
						mHandler.sendEmptyMessage(MSG_TOAST_FAIL_TO_CONFIG_NET);

						mDataStorage.setValue("ConfigCompleted", false);
					}

				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					mStopNetConfig = false;

					mWifiAdmin.disconnectFromAP();
					System.out.println("[NetConfig] Reconnect to Home AP ["
							+ mHomeSsid + "]");
					mWifiAdmin.connectToAp(mHomeSsid);

					/*
					 * System.out.println("[NetConfig] Delete WIFI AP" +
					 * ScConstants.BOARD_AP_SSID + "configuration"); if
					 * (!mWifiAdmin
					 * .deleteApConfiguration(ScConstants.BOARD_AP_SSID))
					 * System.
					 * out.printf("[NetConfig] WIFI AP [%s] does not exist\n",
					 * ScConstants.BOARD_AP_SSID);
					 */

					mHandler.sendEmptyMessageAtTime(MSG_STOP_NET_CONFIG, 1000);
				}

			}
		}).start();

		return true;
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context,
				android.content.Intent intent) {
			final String action = intent.getAction();

			if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
				System.out
						.println("[NetConfig] SUPPLICANT_CONNECTION_CHANGE_ACTION");
			}

			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo info = intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

				if (info.getType() == ConnectivityManager.TYPE_WIFI) {

					if (info.isConnected()) {
						// System.out.printf("[NetConfig] broadcast: connected to %s\n"
						// + wifiAdmin.getSSID());
						System.out
								.println("[NetConfig] broadcast: connected to "
										+ mWifiAdmin.getSSID());
					} else {
						// System.out.printf("[NetConfig] broadcast: disconnected\n");
					}

					if (!mInProcess)
						mHomeSsidText.setText(mWifiAdmin.getSSID());
					/*
					 * if (info.getDetailedState() == DetailedState.CONNECTED) {
					 * WifiManager wManager = (WifiManager)
					 * context.getSystemService(Context.WIFI_SERVICE); WifiInfo
					 * wInfo = wManager.getConnectionInfo();
					 * 
					 * mSsidEditText.setText(wInfo.getSSID()); } else {
					 * mSsidEditText.setText(""); }
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
