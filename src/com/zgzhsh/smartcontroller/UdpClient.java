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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClient {

	private static final String SERVER_IP = "127.0.0.1";
	private static final int SERVER_PORT = 9527;

	private static final int BUF_LEN = 4096;
	byte[] recvBuffer = new byte[BUF_LEN];

	private DatagramSocket mSocket;

	private String mServerIp = null;
	private int mServerPort;

	public UdpClient(String server, int serverPort) throws Exception {
		mServerIp = server;
		mServerPort = serverPort;

		/*
		 * mSocket = new DatagramSocket(9999,
		 * InetAddress.getByName("127.0.0.1"));
		 */
		mSocket = new DatagramSocket();

		System.out.printf("[Udp Client] server is %s：%d\n", server, serverPort);
	}

	public boolean sendData(final byte[] data) throws IOException {

		DatagramPacket packet = new DatagramPacket(data, data.length,
				InetAddress.getByName(mServerIp), mServerPort);

		System.out.printf(
				"[Udp Client] [%s:%d] Send msg to %s:%d <%s>, cnt %d\n",
				mSocket.getLocalAddress(),
				mSocket.getLocalPort(), // mSocket.getLocalSocketAddress()
				packet.getAddress().getHostAddress(), packet.getPort(),
				new String(data, 0, data.length), data.length);

		mSocket.send(packet);

		return true;
	}

	public byte[] recvData(boolean block) throws IOException {
		DatagramPacket packet = new DatagramPacket(recvBuffer, BUF_LEN);

		if (!block)
			mSocket.setSoTimeout(1000);

		mSocket.receive(packet);

		byte[] data = packet.getData();

		System.out.printf(
				"[Udp Client] [%s:%d] Get msg from %s:%d <%s>, cnt %d\n",
				mSocket.getLocalAddress(), mSocket.getLocalPort(), packet
						.getAddress().getHostAddress(), packet.getPort(),
				new String(data, 0, packet.getLength()), packet.getLength());

		return data;
	}

	public void close() {
		mSocket.close();
	}

}
