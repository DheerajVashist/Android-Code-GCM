package com.techila.travelfeedback;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.techila.travelfeedback.dbhelper.VehicleNumberDb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	

	LayoutInflater inflater;
	AlertDialog.Builder builder;
	AlertDialog dialog;
	String trvl_type;
	View vi;
	ConnectionDetector cd;
	TextView tv_dlg_title;
	boolean flag=false;
	AdRequest ad;
	
	// ***********************Advertise variables*************************
		private AdView adview1, adview2;
		private InterstitialAd interstitialAd;
		private static final String AD_UNIT_ID = "ca-app-pub-9833789543858910/9282344381";
	// *************************finish***********************
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		new CreateDb().execute();
		
		TelephonyManager manager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = manager.getDeviceId();
		interstitialAd = new InterstitialAd(this);
		interstitialAd.setAdUnitId(AD_UNIT_ID);
		
		
		ad = new AdRequest.Builder().build();
	
		interstitialAd.loadAd(ad);
		
		cd = new ConnectionDetector(this);
		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    builder = new AlertDialog.Builder(MainActivity.this);
	    overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);
	    createDialog();
	}

	public void showPopUp(View vi) {
		switch (vi.getId()) {
		case R.id.btn_auto:
			showPopUp("Auto");
			break;
		case R.id.btn_bus:
			showPopUp("Bus");
			break;
		case R.id.btn_car:
			showPopUp("Car");
			break;
		case R.id.btn_plane:
			showPopUp("Plane");
			break;
		case R.id.btn_taxi:
			showPopUp("Taxi");
			break;
		case R.id.btn_train:
			showPopUp("Train");
			break;
		default:
			
			break;
		}
	}
	
	
//	@Override
//	public void onBackPressed() {
//
//		super.onBackPressed();
//		if (!cd.isConnectingToInternet()) {
//
//		} else {
//			interstitial.setAdListener(new AdListener() {
//				@Override
//				public void onAdLoaded() {
//					// TODO Auto-generated method stub
//					super.onAdLoaded();
//					displayAd();
//				}
//			});
//		}
//	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);
		if(cd.isConnectingToInternet()){
			interstitialAd.show();
			
		}
		
	}
	
	private void createDialog(){
		vi = inflater.inflate(R.layout.dialog_option_feedback, null);
		Button btn_findFeedback= (Button) vi.findViewById(R.id.btn_find_feedback);
		Button btn_submitFeedback = (Button)vi.findViewById(R.id.btn_submit_feed);
		tv_dlg_title = (TextView)vi.findViewById(R.id.dlg_title);
		btn_findFeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				Intent it = new Intent(MainActivity.this,FindFeedbackActivity.class);
				it.putExtra("TravelType", trvl_type);
				startActivity(it);
			}
		});
		btn_submitFeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				Intent it = new Intent(MainActivity.this,SubmitFeedbackActivity.class);
				it.putExtra("TravelType", trvl_type);
				it.putExtra("activity", "main");
				startActivity(it);
			}
		});
		
		builder.setView(vi);	
		dialog= builder.create();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
	private void showPopUp(String title){
		trvl_type = title;
		
//		  dialog.setTitle(title);
		  dialog.show();
			
		tv_dlg_title.setText(title);
	}
	
	
	class CreateDb extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
			VehicleNumberDb vehicelDb = VehicleNumberDb.getInstance(MainActivity.this);
			
			return null;
		}
		
		
		
	}
	
}
