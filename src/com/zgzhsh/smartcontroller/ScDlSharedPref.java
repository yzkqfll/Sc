package com.zgzhsh.smartcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ScDlSharedPref {
    private Context context;
    private String PREF_DEVLIST = "pref_devlist";
    
    public ScDlSharedPref(Context context) {  
        super();  
        this.context = context;  
    }
  
    public void save(String str){  // format: "type/name,type/name"
        SharedPreferences devsp = context.getSharedPreferences("DeviceListSP", Context.MODE_PRIVATE);  
        Editor editor = devsp.edit();  
        editor.putString(PREF_DEVLIST, str);
        editor.commit(); //commit to xml file
    }  
      
    public String getdevs(){
        SharedPreferences devsp = context.getSharedPreferences("DeviceListSP", Context.MODE_PRIVATE);  
        String devinfos = devsp.getString(PREF_DEVLIST, "");
        return devinfos;
    } 
}
