package com.zgzhsh.smartcontroller;

import java.util.ArrayList;
import java.util.HashMap;

//import com.easylink.android.utils.EasyLinkConstants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ScDeviceAdd extends Activity implements OnClickListener {
	
	   private Button save_btn = null;
	   private ImageButton back_btn = null;
	   //private ImageView type_img = null;
	   private ListView lv = null;
	   private EditText edit_txt = null;
	   private int typen = 3;
       private String[] typeStr = new String[3];
	   private String sDevType= "";
	   private int preIdx = -1;
	   
	    
	   /** Called when the activity is first created. */
	   @Override
	   public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.sc_dev_add);
	      
	       /* init string resource */
		   String tv_str = getString(R.string.tv_str);
		   String air_str = getString(R.string.ac_str);
		   String tvbox_str= getString(R.string.tvb_str);
		   typeStr[0]= tv_str;
		   typeStr[1] = air_str;
		   typeStr[2] = tvbox_str;
		   
			/**
			 * Initialize all view componets in screen
			 */
			initViews();

			/**
			 * Initailize all click listeners to views
			 */
			setViewClickListeners();
			
			//setRadioGroupChangeListener();
	   }
	   
		/**
		 * Initialise all view components from xml
		 */
		private void initViews() {
			save_btn = (Button) findViewById(R.id.da_save_btn);
			back_btn = (ImageButton) findViewById(R.id.da_back_btn);
			//type_img = (ImageView) findViewById(R.id.da_item_image);
			edit_txt = (EditText) findViewById(R.id.da_dev_name_input);
			
			initListView();
		}
		
		private void initListView(){

			lv = (ListView) findViewById(R.id.da_type_list);
			ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	        for(int i=0;i<typen;i++)  
	        {  
	            HashMap<String, Object> map = new HashMap<String, Object>();  
	            map.put("image", R.drawable.ic_checked);
	            map.put("title", typeStr[i]);  
	            listItem.add(map); 
	        }
	        SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,   
	                R.layout.da_list_items,
	                new String[] {"image","title"},   
	                new int[] {R.id.da_item_image, R.id.da_item_title}  
	            );
	        lv.setAdapter(listItemAdapter);
	        
	        lv.setOnItemClickListener(new OnItemClickListener() {    
	            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
	                    long arg3) {  
	            	sDevType = typeStr[arg2];
	            	
	            	if(preIdx == -1)
	            	{
	            	   // show clicked item image view
	            	   ImageView curImg = (ImageView)arg1.findViewById(R.id.da_item_image);
	            	   curImg.setVisibility(View.VISIBLE);
	            	   preIdx = arg2;
	            	}
	            	else
	            	{
	            	   if(preIdx != arg2)
	            	   {
		            	  ImageView curImg = (ImageView)arg1.findViewById(R.id.da_item_image);
		            	  curImg.setVisibility(View.VISIBLE);
	            		   
	            	      ImageView preImg = (ImageView) arg0.getChildAt(preIdx).findViewById(R.id.da_item_image);
	            	      preImg.setVisibility(View.INVISIBLE);
	            	      
	            	      preIdx = arg2;
	            	   }
	            	}
	            }  
	        });
		}
	   
		/**
		 * Init the click listeners of all required views
		 */
		private void setViewClickListeners() {
			save_btn.setOnClickListener(this);
			back_btn.setOnClickListener(this);
		}
		
		/*
		 // radio button
		private void setRadioGroupChangeListener(){
			rg_type.setOnCheckedChangeListener(new DevTypeOnCheckedChangeListener());
		}
		
		private class DevTypeOnCheckedChangeListener implements OnCheckedChangeListener{
			public void onCheckedChanged(RadioGroup group, int checkedId){
				if(rb_tv.getId()==checkedId){
					sDevType=rb_tv.getText().toString();
				}
				else if(rb_air.getId()==checkedId){
					sDevType = rb_air.getText().toString();
				}
			}
		}
		*/
		
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.da_save_btn)
			{
				String devName = edit_txt.getText().toString();
				String input_name = getString(R.string.da_plz_input_name);
				String input_type = getString(R.string.da_plz_input_type);
				
				if(devName.equals(""))
				{
					Toast.makeText(this, input_name, Toast.LENGTH_LONG).show();
				}
				else if(sDevType.equals(""))
				{
					Toast.makeText(this, input_type, Toast.LENGTH_LONG).show();
				}
				else
				{
				  Intent in = getIntent();
				  in.putExtra("new_dev_result", sDevType+":"+devName); //result format: type:name
				  ScDeviceAdd.this.setResult(RESULT_OK, in);
				  ScDeviceAdd.this.finish();
				}
			}
			else if(v.getId() == R.id.da_back_btn)
			{
				ScDeviceAdd.this.finish();
			}
		}
		
	    /*
	    private CountDownTimer timer = new CountDownTimer(2000, 1000) {
	        @Override  
	        public void onFinish() {  
	        	wm.removeView(floatbtn);
	        }

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
			}  
	    }; 
	    */
	    
	    public void onDestroy(){
	    	super.onDestroy();
	    }

}
