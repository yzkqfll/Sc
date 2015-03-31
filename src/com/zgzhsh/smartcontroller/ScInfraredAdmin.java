package com.zgzhsh.smartcontroller;

import android.content.Context;

public class ScInfraredAdmin {

	// decode result
	private byte[] mDecResult;

	// infrared protocol
	private String mProtocol;

	// study succeed or failed
	private boolean mStudyStat;

	// save to local flash
	private ScDataStorage mDataStor;

	// remote control
	private ScNetTransceiver mNetTransceiver;

	public ScInfraredAdmin(Context context) {

		mDataStor = new ScDataStorage(context, "Data");

		mNetTransceiver = new ScNetTransceiver("192.168.1.105", 9528);
	}

	public boolean isStudySucceed() {
		return mStudyStat;
	}

	public void setStudyState(boolean value) {
		mStudyStat = value;
	}

	public String getProtocol() {
		return mProtocol;
	}

	public void setProtocol(String value) {
		mProtocol = value;
	}

	public String getKey(String key, String defValue) {
		return mDataStor.getValue(key, defValue);
	}

	public void saveKey(String key, String value) {
		mDataStor.setValue(key, value);
	}

	public String searchKey(String key, String defValue) {
		String retVal = getKey(key, defValue);

		if (retVal == defValue)
			return null;
		else
			return retVal;
	}

	public void sendPacket(byte type, String msg) {

		mNetTransceiver.sendUdpPacket(ScConstants.PKT_TYPE_INFRA, type, msg);
	}

	public UserData RecvPacket(boolean block) {

		return mNetTransceiver.recvUdpPacket(false);
	}

	public byte[] getDecResult() {
		return mDecResult;
	}

	public void setDecResult(byte[] data) {
		mDecResult = data;
	}
}