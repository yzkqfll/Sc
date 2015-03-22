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

import java.net.InetAddress;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

public class ScWifiAdmin {
	private static final int BUILD_VERSION_JELLYBEAN = 17;

	/**
	 * Called activity context
	 */
	private Context mContext = null;

	public ScWifiAdmin(Context c) {
		mContext = c;
	}

	public boolean isConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI); /* ACCESS_NETWORK_STATE */

		return networkInfo.isConnected();
	}

	public String getSSID() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifiManager.getConnectionInfo(); /* ACCESS_WIFI_STATE */
		String ssid = wInfo.getSSID();

		if (!isConnected())
			return null;

		if (ssid.equals("<unknown ssid>") || ssid.equals("0x")) {
			System.out.println("[WifiAdmin] ssid is " + ssid);
			System.out.println("[WifiAdmin] change to \"\"");
			return "";
		}

		if (Build.VERSION.SDK_INT >= BUILD_VERSION_JELLYBEAN) {
			if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
				ssid = ssid.substring(1, ssid.length() - 1);
			}
		}

		return ssid;
	}

	public String getGateway() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		int gateway = wifiManager.getDhcpInfo().gateway;

		return (String.format("%d.%d.%d.%d", (gateway & 0xff),
				(gateway >> 8 & 0xff), (gateway >> 16 & 0xff),
				(gateway >> 24 & 0xff))).toString();
	}

	public boolean isWifiEnabled() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);

		return wifiManager.isWifiEnabled();
	}

	public boolean setWifiEnabled() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);

		if (!wifiManager.isWifiEnabled())
			return wifiManager.setWifiEnabled(true);

		return true;
	}

	public boolean setWifiDisabled() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);

		if (wifiManager.isWifiEnabled())
			return wifiManager.setWifiEnabled(false);

		return true;
	}

	public int getWifiState() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);

		return wifiManager.getWifiState();
	}

	private List<ScanResult> getScanResults(boolean print) {

		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		List<ScanResult> scanResults;

		wifiManager.startScan(); /* CHANGE_WIFI_STATE */
		scanResults = wifiManager.getScanResults();

		if (print) {
			for (int i = 0; i < scanResults.size(); i++)
				System.out.println(Integer.valueOf(i + 1).toString() + ":"
						+ (scanResults.get(i)).toString());
		}

		return scanResults;
	}

	public ScanResult scanAndFindSSID(String ssid) {
		List<ScanResult> scanResults = getScanResults(false);

		for (int i = 0; i < scanResults.size(); i++) {
			if (scanResults.get(i).SSID.equals(ssid))
				return scanResults.get(i);
		}

		return null;
	}

	private List<WifiConfiguration> getWifiConfigurations(boolean print) {

		final WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		final List<WifiConfiguration> wConfigurations;

		wifiManager.startScan(); /* CHANGE_WIFI_STATE */

		wConfigurations = wifiManager.getConfiguredNetworks();

		if (print) {
			for (int i = 0; i < wConfigurations.size(); i++) {
				WifiConfiguration wifiConfiguration = wConfigurations.get(i);
				System.out.println(Integer.valueOf(i + 1).toString() + ":"
						+ wifiConfiguration.toString());
			}
		}

		return wConfigurations;
	}

	public boolean disconnectFromAP() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);

		if (!isConnected())
			System.out.println("[WifiAdmin] not connected yet");

		return wifiManager.disconnect();
	}

	public boolean connectToApWithoutKey(String ssid) {

		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiConfiguration wConfiguration = new WifiConfiguration();
		int netId;

		ScanResult scanResult = scanAndFindSSID(ssid);
		if (scanResult == null) {
			System.out.printf("[WifiAdmin] ssid <%s> does not exist\n", ssid);
			return false;
		}

		/*
		 * wConfiguration.allowedAuthAlgorithms.clear();
		 * wConfiguration.allowedGroupCiphers.clear();
		 * wConfiguration.allowedKeyManagement.clear();
		 * wConfiguration.allowedPairwiseCiphers.clear();
		 * wConfiguration.allowedProtocols.clear();
		 */
		wConfiguration.SSID = "\"" + ssid + "\"";
		wConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

		netId = wifiManager.addNetwork(wConfiguration);
		if (netId < 0) {
			System.out.println("[WifiAdmin] fail to addNetwork " + ssid);
			return false;
		}

		if (!wifiManager.enableNetwork(netId, true)) {
			System.out.println("[WifiAdmin] fail to enableNetwork");
			return false;
		}

		return true;
	}

	public boolean connectToAp(String ssid) {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> wConfigurations;

		ssid = "\"" + ssid + "\"";

		wifiManager.startScan(); /* CHANGE_WIFI_STATE */

		wConfigurations = wifiManager.getConfiguredNetworks();
		for (int i = 0; i < wConfigurations.size(); i++) {
			if (wConfigurations.get(i).SSID.equals(ssid)) {
				wifiManager.enableNetwork(wConfigurations.get(i).networkId,
						true);
				return true;
			}
		}

		return false;
	}

	public boolean deleteApConfiguration(String ssid) {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> wConfigurations;

		ssid = "\"" + ssid + "\"";

		wConfigurations = wifiManager.getConfiguredNetworks();
		for (int i = 0; i < wConfigurations.size(); i++) {
			if (wConfigurations.get(i).SSID.equals(ssid)) {
				wifiManager.disableNetwork(wConfigurations.get(i).networkId);
				return true;
			}
		}

		return false;
	}

	public String getIp() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		int ip = wifiManager.getDhcpInfo().ipAddress;

		return (String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
				(ip >> 16 & 0xff), (ip >> 24 & 0xff))).toString();
	}

	public String getGateWayIp() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		int ip = wifiManager.getDhcpInfo().gateway;

		return (String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
				(ip >> 16 & 0xff), (ip >> 24 & 0xff))).toString();
	}

}
