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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.R.integer;

class UserData {
	private byte[] mData;

	private String mPeerIP;
	private int mPeerPort;

	public UserData(String msg) {
		mData = msg.getBytes();
	}

	public UserData(byte[] data, int len) {
		mData = new byte[len];
		for (int i = 0; i < len; i++)
			mData[i] = data[i];
	}

	public UserData(byte[] data, int len, String ip, int port) {
		mData = new byte[len];
		for (int i = 0; i < len; i++)
			mData[i] = data[i];

		mPeerIP = ip;
		mPeerPort = port;
	}

	public byte[] getData() {
		return mData;
	}

	public int getLength() {
		return mData.length;
	}

	public String getPeerIP() {
		return mPeerIP;
	}

	public int getPeerPort() {
		return mPeerPort;
	}
}

public class ScUdpClient {

	private static final int BUF_LEN = 4096;
	byte[] recvBuffer = new byte[BUF_LEN];

	private DatagramSocket mSocket;

	private String mServerIp;
	private int mServerPort;

	public ScUdpClient(String server, int serverPort) {
		mServerIp = server;
		mServerPort = serverPort;

		try {
			/*
			 * mSocket = new DatagramSocket(9999,
			 * InetAddress.getByName("127.0.0.1"));
			 */
			mSocket = new DatagramSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.printf("[Udp Client] Create Udp Client: server is %s：%d\n",
				server, serverPort);
	}

	public boolean sendData(final UserData userData) {
		byte[] data = userData.getData();

		try {
			DatagramPacket packet = new DatagramPacket(data, data.length,
					InetAddress.getByName(mServerIp), mServerPort);

			if (ScConstants.PRINT_PKT_CONTENT) {
				System.out
						.printf("[Udp Client] [%s:%d] Send msg to %s:%d: <%s>, cnt %d\n",
								mSocket.getLocalAddress(), mSocket
										.getLocalPort(), // mSocket.getLocalSocketAddress()
								packet.getAddress().getHostAddress(), packet
										.getPort(), new String(data, 0,
										data.length), data.length);
				for (byte b : data) {
					System.out.printf("%x ", b);
				}
				System.out.println();
			}

			mSocket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public UserData recvData(boolean block, int ms) {
		DatagramPacket packet = new DatagramPacket(recvBuffer, BUF_LEN);
		UserData userData = null;

		try {
			if (!block)
				mSocket.setSoTimeout(ms);

			mSocket.receive(packet);

			userData = new UserData(packet.getData(), packet.getLength(),
					packet.getAddress().getHostAddress(), packet.getPort());

			if (ScConstants.PRINT_PKT_CONTENT) {
				System.out
						.printf("[Udp Client] [%s:%d] Get msg from %s:%d: <%s>, cnt %d\n",
								mSocket.getLocalAddress(),
								mSocket.getLocalPort(),
								packet.getAddress().getHostAddress(),
								packet.getPort(),
								new String(userData.getData(), 0, userData
										.getLength()), packet.getLength());
				for (byte b : userData.getData()) {
					System.out.printf("%x ", b);
				}
				System.out.println();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return userData;
	}

	public void close() {
		mSocket.close();
	}

}
