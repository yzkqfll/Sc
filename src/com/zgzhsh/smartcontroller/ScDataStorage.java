package com.zgzhsh.smartcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ScDataStorage {

	Context mContext;
	SharedPreferences mSharedPreferences;
	String mFileName;
	Editor mEditor;

	public ScDataStorage(Context context, String fileName) {
		mContext = context;
		mFileName = fileName;

		mSharedPreferences = mContext.getSharedPreferences(mFileName,
				Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
	}

	public void setValue(String key, String value) {
		mEditor.putString(key, value);
		mEditor.commit();
	}

	public void setValue(String key, int value) {
		mEditor.putInt(key, value);
		mEditor.commit();
	}

	public void setValue(String key, boolean value) {
		mEditor.putBoolean(key, value);
		mEditor.commit();
	}

	public String getValue(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}

	public int getValue(String key, int defValue) {
		return mSharedPreferences.getInt(key, defValue);
	}

	public boolean getValue(String key, boolean defValue) {
		return mSharedPreferences.getBoolean(key, defValue);
	}

}
