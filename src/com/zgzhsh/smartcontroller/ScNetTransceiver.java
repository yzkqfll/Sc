package com.zgzhsh.smartcontroller;

public class ScNetTransceiver {

	private String mPeerIp = null;
	private int mPort;
	private ScUdpClient mScUdpClient;

	public ScNetTransceiver(String ip, int port) {
		mPeerIp = ip;
		mPort = port;

		mScUdpClient = new ScUdpClient(mPeerIp, mPort);
	}

	public ScNetTransceiver(int port) {
		String ip = getPeerIp(port);

		mPeerIp = ip;
		mPort = port;
		mScUdpClient = new ScUdpClient(mPeerIp, mPort);
	}

	private String getPeerIp(int port) {

		UserData userData = encapPacket(ScConstants.PKT_TYPE_QUERY_IP,
				(byte) 0, "Query IP");
		ScUdpClient udpClient = new ScUdpClient("255.255.255.255", port);

		udpClient.sendData(userData);
		UserData recvUserData = udpClient.recvData(false, 1000);
		if (recvUserData != null)
			return recvUserData.getPeerIP();
		else
			return null;
	}

	private UserData encapPacket(byte type, byte subtype, String msg) {

		byte[] header = { 0x78, 0x56, 0x34, 0x12, type, subtype };

		byte[] packet = new byte[header.length + msg.length()];
		for (int i = 0; i < header.length; i++) {
			packet[i] = header[i];
		}
		for (int i = 0; i < msg.length(); i++) {
			packet[header.length + i] = msg.getBytes()[i];
		}

		return new UserData(packet, packet.length);
	}

	private UserData encapPacket(byte type, byte subtype, byte[] data) {

		byte[] header = { 0x78, 0x56, 0x34, 0x12, type, subtype };

		byte[] packet = new byte[header.length + data.length];
		for (int i = 0; i < header.length; i++) {
			packet[i] = header[i];
		}
		for (int i = 0; i < data.length; i++) {
			packet[header.length + i] = data[i];
		}

		return new UserData(packet, packet.length);
	}

	public boolean sendUdpPacket(byte type, byte subtype, String msg) {
		UserData userData = encapPacket(type, subtype, msg);

		mScUdpClient.sendData(userData);

		return true;
	}

	public boolean sendUdpPacket(byte type, byte subtype, byte[] data) {
		UserData userData = encapPacket(type, subtype, data);

		mScUdpClient.sendData(userData);

		return true;
	}

	public UserData recvUdpPacket(boolean block, int ms) {
		UserData userData = mScUdpClient.recvData(block, ms);

		return userData;
	}

}
