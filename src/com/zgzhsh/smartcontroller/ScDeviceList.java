package com.zgzhsh.smartcontroller;

import java.util.ArrayList;
import java.util.HashMap;
import com.zgzhsh.smartcontroller.ScDlSharedPref;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

public class ScDeviceList extends Activity implements OnClickListener {

	private ImageButton add_btn = null;
	private ListView device_lv = null;
	private SimpleAdapter listItemAdapter;
	private ArrayList<HashMap<String, Object>> listItem;
	private ScDlSharedPref devlist_sp;
	private Dialog mDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_dev_list);

		/**
		 * Initialize all view componets in screen
		 */
		initViews();

		/**
		 * Initailize all click listeners to views
		 */
		setViewClickListeners();

		listItem = new ArrayList<HashMap<String, Object>>();
		listItemAdapter = new SimpleAdapter(this, listItem,
				R.layout.dl_list_items,
				new String[] { "image", "type", "name" }, new int[] {
						R.id.dl_item_image, R.id.dl_item_type,
						R.id.dl_item_name });

		// load devices info
		devlist_sp = new ScDlSharedPref(ScDeviceList.this);
		onCreateDeviceInfoLoad();
	}

	private void onCreateDeviceInfoLoad() {
		String devinfos = devlist_sp.getdevs();
		if (devinfos != "") {
			String[] devs = devinfos.split(",");
			for (String str : devs) {
				String[] dev = str.split("/");
				addItem(dev[0] + ":" + dev[1]);
				device_lv.setAdapter(listItemAdapter);
			}
		}
	}

	/**
	 * Initialise all view components from xml
	 */
	private void initViews() {
		add_btn = (ImageButton) findViewById(R.id.dl_add_btn);
		device_lv = (ListView) findViewById(R.id.device_listView);
	}

	/**
	 * Init the click listeners of all required views
	 */
	private void setViewClickListeners() {
		add_btn.setOnClickListener(this);
		device_lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				TextView type = (TextView) arg1.findViewById(R.id.dl_item_type);
				TextView name = (TextView) arg1.findViewById(R.id.dl_item_name);

				String tv_str = getString(R.string.tv_str);
				String air_str = getString(R.string.ac_str);
				// String tvbox_str=
				// getResources().getString(R.string.tv_box_str);

				if (type.getText().equals(tv_str)) {

					Intent it = new Intent(ScDeviceList.this, ScTvCtrl.class);
					it.putExtra("item_device_name", name.getText().toString());
					startActivity(it);

				} else if (type.getText().equals(air_str)) {
					// startActivity(new Intent(ScDeviceList.this,
					// AirCtrlActivity.class));
				} else {
					System.out.println("Unknow device type");
					System.out.println(type.getText());
				}
			}
		});
		/*
		 * device_lv.setOnCreateContextMenuListener(new
		 * OnCreateContextMenuListener() { public void
		 * onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo
		 * menuInfo) { String str_ok = getString(R.string.sure); String str_del
		 * = getString(R.string.doudelete); menu.setHeaderTitle(str_del);
		 * menu.add(0, 0, 0, str_ok); //menu.add(0, 1, 0, str_cancel); } });
		 */
		device_lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// get item name
				TextView tv_name = (TextView) view
						.findViewById(R.id.dl_item_name);
				String str_name = tv_name.getText().toString();

				// init content
				mDialog = new Dialog(ScDeviceList.this, R.style.ScDialog);
				mDialog.setContentView(R.layout.dev_list_dialog);
				mDialog.show();

				// change content
				TextView tv_title = (TextView) mDialog.getWindow()
						.findViewById(R.id.dl_dialog_title);
				tv_title.setText(str_name);

				// set on dialog click
				final int idx = position;
				Button btn = (Button) mDialog.getWindow().findViewById(
						R.id.dl_dialog_del_dev_btn);
				btn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						deleteItem(idx);
						mDialog.dismiss();
					}
				});

				return true;
			}
		});
	}

	/*
	 * public boolean onContextItemSelected(MenuItem item) { ContextMenuInfo
	 * menuInfo = (ContextMenuInfo) item.getMenuInfo();
	 * AdapterView.AdapterContextMenuInfo info =
	 * (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); int id =
	 * (int)info.id; switch(item.getItemId()) { case 0:
	 * //System.out.println(id); deleteItem(id); break; default: break; } return
	 * super.onContextItemSelected(item); }
	 */

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dl_add_btn) {
			// if net config is done before, then i think no need to consider
			// wifi status here
			startDeviceAddAct();
		}
	}

	public void startDeviceAddAct() {
		String new_device = "";
		Intent it = new Intent(ScDeviceList.this, ScDeviceAdd.class);
		it.putExtra("new_dev_result", new_device);
		startActivityForResult(it, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			String new_dev = data.getStringExtra("new_dev_result");
			addItem(new_dev);
			device_lv.setAdapter(listItemAdapter);
			break;
		default:
			break;
		}
	}

	private void addItem(String new_dev) {
		// new_dev format: type:name
		HashMap<String, Object> item = new HashMap<String, Object>();
		String a[] = new_dev.split(":");
		String dev_type = a[0];
		String dev_name = a[1];

		String type_tv = getString(R.string.tv_str);
		String type_air = getString(R.string.ac_str);

		if (dev_type.equals(type_tv)) {
			item.put("image", R.drawable.ic_tv);
		} else if (dev_type.equals(type_air)) {
			item.put("image", R.drawable.ic_ac);
		}
		item.put("type", dev_type);
		item.put("name", dev_name);
		listItem.add(item);
		listItemAdapter.notifyDataSetChanged();
	}

	private void deleteItem(int id) {
		listItem.remove(id);
		listItemAdapter.notifyDataSetChanged();
	}

	public void onDestroy() {
		saveDevListInfo();
		super.onDestroy();
	}

	private void saveDevListInfo() { // format: "type/name,type/name"
		String devinfos = "";
		for (int i = 0; i < listItem.size(); i++) {
			String type = listItem.get(i).get("type").toString();
			String name = listItem.get(i).get("name").toString();
			String devinfo = type + "/" + name;
			if (devinfos == "")
				devinfos = devinfo;
			else
				devinfos += "," + devinfo;
		}
		devlist_sp.save(devinfos);
		// System.out.println(devinfos);
	}

}
