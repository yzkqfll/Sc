package com.zgzhsh.smartcontroller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ScTvCtrl extends Activity implements OnClickListener,
		OnLongClickListener {

	static final int MSG_IR_STUDY_START = 1;
	static final int MSG_IR_STUDY_STOP = 2;
	static final int MSG_IR_STUDY_TIMEOUT = 3;
	static final int MSG_IR_STUDY_SUCCESS = 4;
	static final int MSG_IR_STUDY_EXCEP = 5;
	static final int MSG_IR_SEND_NEC = 6;
	static final int MSG_IR_SEND_OK = 7;
	static final int MSG_IR_SEND_FAILED = 8;
	static final int MSG_IR_SEND_EXCEP = 9;

	private Intent mIntent = null;

	private TextView mHdrTitle = null;
	private ImageButton mIbBack = null;
	private ImageButton mIbPower = null;
	private ImageButton mIbChUp = null;
	private ImageButton mIbChDown = null;
	private ImageButton mIbVolUp = null;
	private ImageButton mIbVolDown = null;

	private Dialog mDialog = null;
	private Button mDialogBtn = null;
	private TextView mDialogTitle = null;

	private ScInfraredAdmin mIrAdm = null;
	private boolean mStopIrStudy = false;
	private String mCurConstKey = null;

	private HandlerThread mIrSendHandlerThread;
	private Handler mSubHandler;

	final Handler mMainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_IR_STUDY_START:
				mDialog.show();
				break;
			case MSG_IR_SEND_EXCEP:
			case MSG_IR_STUDY_EXCEP:
				Toast.makeText(getApplicationContext(),
						getString(R.string.ir_comm_excep), Toast.LENGTH_LONG)
						.show();
				break;
			case MSG_IR_STUDY_TIMEOUT:
				Toast.makeText(getApplicationContext(),
						getString(R.string.irs_txt_timeout), Toast.LENGTH_LONG)
						.show();
				break;
			case MSG_IR_STUDY_SUCCESS:
				Toast.makeText(getApplicationContext(),
						getString(R.string.irs_txt_success), Toast.LENGTH_LONG)
						.show();
			case MSG_IR_STUDY_STOP:
				mDialog.dismiss();
				break;
			case MSG_IR_SEND_OK:
				Toast.makeText(getApplicationContext(),
						getString(R.string.irt_txt_send_ok), Toast.LENGTH_LONG)
						.show();
				break;
			case MSG_IR_SEND_FAILED:
				Toast.makeText(getApplicationContext(),
						getString(R.string.irt_txt_send_failed),
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_tv_ctrl);

		// get intent
		mIntent = this.getIntent();

		// create IR admin
		mIrAdm = new ScInfraredAdmin(ScTvCtrl.this,
				mIntent.getStringExtra("file_name"));

		initViews();

		setViewClickListeners();

		mIrSendHandlerThread = new HandlerThread("HandlerThread_irSend");
		mIrSendHandlerThread.start();
		mSubHandler = new Handler(mIrSendHandlerThread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {

				UserData udata = null;
				String pkt = null;

				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_IR_SEND_NEC:
					try {
						System.out.println("[IRSend]" + (String) msg.obj);

						mIrAdm.sendPacket(ScConstants.PKT_SUBTYPE_IR_SEND,
								(String) msg.obj);

						udata = mIrAdm.RecvPacket(false, 1000);

						if (null == udata) {
							mMainHandler.sendEmptyMessage(MSG_IR_SEND_EXCEP);
						} else {
							pkt = new String(udata.getData());
							if (!pkt.equals("IRSendNEC: OK")) {
								mMainHandler
										.sendEmptyMessage(MSG_IR_SEND_FAILED);
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					break;
				default:
					break;
				}
			}
		};
	}

	private void initViews() {
		// set title name
		mHdrTitle = (TextView) findViewById(R.id.tv_ctrl_title);
		mHdrTitle.setText(mIntent.getStringExtra("item_device_name"));

		// find back button
		mIbBack = (ImageButton) findViewById(R.id.tv_back_btn);

		// find dialog
		mDialog = new Dialog(ScTvCtrl.this, R.style.ScDialog);

		// find TV button
		initTVImageButton();
	}

	private void initTVImageButton() {
		mIbPower = (ImageButton) findViewById(R.id.tv_btn_pwr);
		mIbChUp = (ImageButton) findViewById(R.id.tv_btn_ch_up);
		mIbChDown = (ImageButton) findViewById(R.id.tv_btn_ch_down);
		mIbVolUp = (ImageButton) findViewById(R.id.tv_btn_vol_up);
		mIbVolDown = (ImageButton) findViewById(R.id.tv_btn_vol_down);
	}

	private void setViewClickListeners() {
		mIbBack.setOnClickListener(this);

		mIbPower.setOnClickListener(this);
		mIbPower.setOnLongClickListener(this);

		mIbChUp.setOnClickListener(this);
		mIbChUp.setOnLongClickListener(this);

		mIbChDown.setOnClickListener(this);
		mIbChDown.setOnLongClickListener(this);

		mIbVolUp.setOnClickListener(this);
		mIbVolUp.setOnLongClickListener(this);

		mIbVolDown.setOnClickListener(this);
		mIbVolDown.setOnLongClickListener(this);
	}

	/*
	 * To-do: avoid quick single click on TV key
	 */
	public void onClick(View v) {

		if (v.getId() == R.id.tv_back_btn) {
			ScTvCtrl.this.finish();
		} else {
			try {
				onClickKey(v.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void onClickKey(int rid) throws Exception {

		String key = getConstantKey(rid);
		String keyVal = mIrAdm.searchKey(key, "defVal");

		if (keyVal == null) {
			Toast.makeText(this, getString(R.string.irs_txt_empty),
					Toast.LENGTH_LONG).show();
		} else {
			Message msg = mSubHandler.obtainMessage(MSG_IR_SEND_NEC, 1, 1,
					keyVal);
			mSubHandler.sendMessage(msg);

		}
	}

	public boolean onLongClick(View v) {

		mCurConstKey = getConstantKey(v.getId());
		// String keyVal = mIrAdm.searchKey(mCurConstKey, "defVal");

		mDialog.setContentView(R.layout.tv_dialog);
		mDialog.setCanceledOnTouchOutside(false);

		mDialogBtn = (Button) mDialog.getWindow().findViewById(
				R.id.tv_dialog_btn);
		mDialogTitle = (TextView) mDialog.getWindow().findViewById(
				R.id.tv_dialog_title);

		mStopIrStudy = false;

		startIrStudy();

		mDialogBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mStopIrStudy = true;
			}
		});

		return false;
	}

	public String getConstantKey(int rid) {

		switch (rid) {
		case R.id.tv_btn_pwr:
			return ScConstants.IR_KEY_POWER;
		case R.id.tv_btn_ch_down:
			return ScConstants.IR_KEY_CH_DOWN;
		case R.id.tv_btn_ch_up:
			return ScConstants.IR_KEY_CH_UP;
		case R.id.tv_btn_vol_down:
			return ScConstants.IR_KEY_VOL_DOWN;
		case R.id.tv_btn_vol_up:
			return ScConstants.IR_KEY_VOL_UP;
		default:
			return null;
		}
	}

	/**
	 * Start IR Study:
	 * <p>
	 * 1) Setup a connect with board
	 * <p>
	 * 2) Enable board's input CC function
	 * <p>
	 * 3) Check board's study result
	 * <p>
	 * 4) Disable board's input CC function
	 * <p>
	 */
	private boolean startIrStudy() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String msg = null;
				boolean excep = false;
				UserData udata = null;

				try {

					// setup a connect
					mIrAdm.sendPacket(ScConstants.PKT_SUBTYPE_IR_CONNECT,
							"setup_connect");
					udata = mIrAdm.RecvPacket(false, 1000);
					if (null == udata) {
						System.out
								.println("[IR Study] connect err: no data received!");
						excep = true;
						return; // exception
					} else {
						msg = new String(udata.getData());
						if (!msg.equals("IRConnect: OK")) {
							System.out.println("[IR Study] connect ack err: "
									+ msg);
							return;
						}
					}

					Thread.sleep(100);

					// enable input capture
					mIrAdm.sendPacket(ScConstants.PKT_SUBTYPE_IR_ENABLE_ICC,
							"enable_icc");
					udata = mIrAdm.RecvPacket(false, 1000);
					if (null == udata) {
						System.out
								.println("[IR Study] enable err: no data received!");
						excep = true;
						return; // exception
					} else {
						msg = new String(udata.getData());
						if (!msg.equals("IREnableICC: OK")) {
							System.out.println("[IR Study] enable ack err: "
									+ msg);
							return;
						}
					}

					Thread.sleep(100);

					// update UI
					mMainHandler.sendEmptyMessage(MSG_IR_STUDY_START);

					// scan the board study result every second
					for (int i = 0; i < 15; i++) {

						for (int j = 0; j < 10; j++) {
							if (mStopIrStudy) {
								// Close board input capture
								mIrAdm.sendPacket(
										ScConstants.PKT_SUBTYPE_IR_DISABLE_ICC,
										"userClose");
								if (null == mIrAdm.RecvPacket(false, 1000)) {
									excep = true;
								}
								return;
							}
							Thread.sleep(100);
						}

						mIrAdm.sendPacket(ScConstants.PKT_SUBTYPE_IR_DECODE,
								"check_decode");
						udata = mIrAdm.RecvPacket(false, 1000);
						if (null == udata) {
							System.out
									.println("[IR Study] decode err: no data received!");
							excep = true;
							return; // exception
						} else {
							msg = new String(udata.getData());
							System.out.println(msg);

							String a[] = msg.split(" ");
							if (a[0].equals("IRDecode:") && !a[1].equals("ERR")) {

								System.out
										.println("[IRDecode] decode ok received!");

								// save to local flash
								mIrAdm.saveKey(mCurConstKey, a[1]);

								// update UI
								mMainHandler
										.sendEmptyMessage(MSG_IR_STUDY_SUCCESS);

								return; // Close input is already done by board
							}
						}
					}

					mIrAdm.sendPacket(ScConstants.PKT_SUBTYPE_IR_DISABLE_ICC,
							"timeoutClose");
					if (null == mIrAdm.RecvPacket(false, 1000)) {
						excep = true;
						return; // exception
					}

					mMainHandler.sendEmptyMessage(MSG_IR_STUDY_TIMEOUT);
					return;

				} catch (Exception e) {

					e.printStackTrace();

				} finally {

					if (excep) {
						mMainHandler.sendEmptyMessage(MSG_IR_STUDY_EXCEP);
					}
					mMainHandler.sendEmptyMessageAtTime(MSG_IR_STUDY_STOP, 200);
				}
			}

		}).start();

		return true;
	}

	public void clearData() {
		mIrAdm.clear();
	}

	public void onDestroy() {
		mIrSendHandlerThread.getLooper().quit();
		super.onDestroy();
	}
}
