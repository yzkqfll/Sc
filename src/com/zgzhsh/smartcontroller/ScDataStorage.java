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
