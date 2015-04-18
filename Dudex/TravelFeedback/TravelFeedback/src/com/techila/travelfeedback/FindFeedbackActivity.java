package com.techila.travelfeedback;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.techila.travelfeedback.TrackApplication.TrackerName;
import com.techila.travelfeedback.dbhelper.FeedbackResultDb;
import com.techila.travelfeedback.pojo.FeedbackResponsePojo;

public class FindFeedbackActivity extends Activity implements
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
		AnimationListener {

	LoadMoreListView lst_of_feedback;
	FeedbackResultDb feedbackDB;

	EditText et_vehicle_number, et_comment, et_vehicle_st, et_veh_st_code,
			et_veh_num_code;
	EditText et_train_num, et_plance_code, et_plane_num;
	TextView tv_title;
	int count_call,drop_count;
	ArrayList<FeedbackResponsePojo> list;
	private final String URL = "http://phbjharkhand.in/TravelFeedBack/view_feedback_information.php";
	CustomAdapterForFindFeedback adapter;
	String vehicle_number;
	ConnectionDetector conn;
	String vehicleType;
	String error_code;
	String longitude, latitude;
	private String lastFormID="";
	Animation animate_up,animate_down;
	int anim_id = 0;
	boolean flag_count_call=false;
	
	// /////////////////////////////////////////////////
	// Location Listener Variables
	// ////////////////////////////////////////////////
	GoogleApiClient mGoogleApiClient;
	Location mLastLocation;
	LocationServices mLocationService;
	LocationRequest mLocationRequest;
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_feedback);

		Bundle extract = getIntent().getExtras();
		if (extract != null) {
			vehicleType = extract.getString("TravelType");
		}
		animate_up = AnimationUtils.loadAnimation(FindFeedbackActivity.this,
				R.anim.slide_up);
		
		animate_up.setAnimationListener(this);
		
		Tracker t = ((TrackApplication)getApplication()).getTracker(TrackerName.APP_TRACKER);
		
		
		
		
		if (isGoogleApiAvailable()) {
			createLocationRequest();
			buildGoogleApi();
		}
		feedbackDB = new FeedbackResultDb(this);
		list = new ArrayList<FeedbackResponsePojo>();
		lst_of_feedback = (LoadMoreListView) findViewById(R.id.lst_of_feedback);
		et_vehicle_number = (EditText) findViewById(R.id.et_vehicle_num);
		et_vehicle_st = (EditText) findViewById(R.id.et_vehicle_st);
		et_veh_st_code = (EditText) findViewById(R.id.et_vehicle_st_num);
		et_veh_num_code = (EditText) findViewById(R.id.et_vehicle_code);
		et_plane_num = (EditText) findViewById(R.id.et_plane_num);
		et_plance_code = (EditText) findViewById(R.id.et_plan_code);
		et_train_num = (EditText) findViewById(R.id.et_train_num);
		tv_title = (TextView) findViewById(R.id.tv_vehicle_type);

		LinearLayout ll_train = (LinearLayout) findViewById(R.id.ll_train_find);
		LinearLayout ll_plane = (LinearLayout) findViewById(R.id.ll_plane_find);
		LinearLayout ll_vehicle = (LinearLayout) findViewById(R.id.ll_vehicle_find);

		overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);

		lst_of_feedback.setOnLoadMoreListener(new OnLoadMoreListener() {

			@Override
			public void onLoadMore() {
				// TODO Auto-generated method stub
				if(conn.isConnectingToInternet()){
				new GetFeedback().execute();
				count_call++;				
				}else{
					lst_of_feedback.onLoadMoreComplete();
				}
			}
		});

		tv_title.setText(vehicleType);
		if (vehicleType.equals("Train")) {
			ll_train.setVisibility(View.VISIBLE);
			ll_plane.setVisibility(View.GONE);
			ll_vehicle.setVisibility(View.GONE);
		} else if (vehicleType.equals("Plane")) {
			tv_title.setText("Flight");
			ll_train.setVisibility(View.GONE);
			ll_plane.setVisibility(View.VISIBLE);
			ll_vehicle.setVisibility(View.GONE);
		} else {
			ll_train.setVisibility(View.GONE);
			ll_plane.setVisibility(View.GONE);
			ll_vehicle.setVisibility(View.VISIBLE);
		}

		conn = new ConnectionDetector(FindFeedbackActivity.this);
		adapter = new CustomAdapterForFindFeedback(FindFeedbackActivity.this,
				android.R.layout.simple_dropdown_item_1line, list);
		// for(int i = 0;i<5;i++){
		// FeedbackResponsePojo model = new FeedbackResponsePojo();
		// model.setComment("On an average");
		// model.setDate(22+"/0"+(i+1)+"/"+2015);
		// model.setUserName("A"+i);
		// model.setRating(""+(i+1));
		// list.add(model);
		// }

		lst_of_feedback.setAdapter(adapter);
		((TextView) findViewById(R.id.tv_back_on_find))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						finish();
						overridePendingTransition(R.anim.lefttoright,
								R.anim.righttoleft);
					}
				});
		((TextView) findViewById(R.id.tv_to_submit_feedback))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent it = new Intent(FindFeedbackActivity.this,
								SubmitFeedbackActivity.class);
						it.putExtra("TravelType", vehicleType);
						vehicle_number = getVehicleNumber();
						Log.d("Vehicle Number", "" + vehicle_number);
						it.putExtra("activity", "find_feedback");
						if (!vehicle_number.equals("")) {
							it.putExtra("vehicle_num", vehicle_number);
						}
						startActivityForResult(it, 1);
					}
				});
		addTextWatcher(et_vehicle_st);
		addTextWatcher(et_veh_st_code);
		addTextWatcher(et_veh_num_code);
		addTextWatcher(et_plance_code);
		addTextWatcher(et_plane_num);

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onPause();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		stopLocationRequest();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (mGoogleApiClient.isConnected()) {
			startLocationRequest();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("vehicle_num");
				vehicle_number = result;
				Log.d("Vehicle Type  ==== ", "" + vehicleType);
				setVehicleNumber(vehicle_number);
				new GetFeedback().execute();
			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result

			}
		}
	}

	private void setVehicleNumber(String vehicle_num) {
		String[] arr = vehicle_num.split(" ");
		if (vehicleType.equals("Train")) {
			Log.d("", "Train == " + vehicle_num);
			et_train_num.setText(vehicle_num);
		} else if (vehicleType.equals("Plane")) {
			et_plance_code.setText(arr[0]);
			et_plane_num.setText(arr[1]);
		} else {
			et_vehicle_st.setText(arr[0]);
			et_veh_st_code.setText(arr[1]);
			et_veh_num_code.setText(arr[2]);
			et_vehicle_number.setText(arr[3]);
		}

	}

	private void addTextWatcher(final EditText et) {
		et.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String str = s.toString();
				if (str.length() == 2) {
					switch (et.getId()) {
					case R.id.et_vehicle_st:
						et_veh_st_code.requestFocus();
						break;
					case R.id.et_vehicle_st_num:
						et_veh_num_code.requestFocus();
						break;
					case R.id.et_vehicle_code:
						et_vehicle_number.requestFocus();
						break;
					}
				}

				if (str.length() == 4) {
					if (et.getId() == R.id.et_plan_code) {
						et_plane_num.requestFocus();
					}
				}

			}
		});
	}

	private String getVehicleNumber() {
		if (vehicleType.equals("Train")) {
			if (et_train_num.getText().toString().equals("")) {

				return "";
			} else {
				vehicle_number = "" + et_train_num.getText().toString();
			}
		} else if (vehicleType.equals("Plane")) {
			if (et_plance_code.getText().toString().equals("")
					|| et_plane_num.getText().toString().equals("")) {
				return "";
			} else {
				vehicle_number = "" + et_plance_code.getText().toString() + " "
						+ et_plane_num.getText().toString();
			}
		} else {
			if (et_vehicle_number.getText().toString().equals("")
					|| et_vehicle_st.getText().toString().equals("")
					|| et_veh_st_code.getText().toString().equals("")
					|| et_veh_num_code.getText().toString().equals("")) {
				return "";
			} else {
				vehicle_number = "" + et_vehicle_st.getText().toString() + " "
						+ et_veh_st_code.getText().toString() + " "
						+ et_veh_num_code.getText().toString() + " "
						+ et_vehicle_number.getText().toString();
			}
		}

		return vehicle_number;
	}

	public void submitVehicleNumber(View vi) {

		switch (vi.getId()) {
		case R.id.btn_find_feedback:
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et_vehicle_number.getWindowToken(), 0);

			anim_id = 1;
			vehicle_number = getVehicleNumber();
			count_call = 0;
			drop_count=0;
			lastFormID="";


			if (conn.isConnectingToInternet()) {
				list.clear();
				new GetFeedback().execute();
			} else {
				if (!vehicle_number.equals("")) {
					list = feedbackDB.getFeedback(vehicle_number, vehicleType);
					if(list.size()==0){
						showEmptyMessage();
					}else{
					adapter = new CustomAdapterForFindFeedback(
							FindFeedbackActivity.this,
							android.R.layout.simple_dropdown_item_1line, list);
					lst_of_feedback.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					}
				} else {
					showInternetDialog();
				}
			}
			break;
		case R.id.btn_slip_down:
			((LinearLayout) findViewById(R.id.ll_for_find_fd))
			.setVisibility(View.VISIBLE);
			anim_id=2;
			drop_count=0;
			((LinearLayout) findViewById(R.id.ll_more))
			.setVisibility(View.GONE);
			animate_down = AnimationUtils.loadAnimation(FindFeedbackActivity.this,
					R.anim.slide_down);
			((LinearLayout) findViewById(R.id.ll_for_find_fd))
			.setAnimation(animate_down);
			
			break;
		default:
			break;
		}

	}

	private void showEmptyMessage() {
		AlertDialog.Builder build = new AlertDialog.Builder(
				FindFeedbackActivity.this);
		build.setTitle("No Rating");
		build.setMessage("Sorry we are not able to find any rating");
		build.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		build.show();
	}

	private class GetFeedback extends AsyncTask<Void, Void, Void> {

		ProgressDialog dialog;
		

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(FindFeedbackActivity.this);
			dialog.setMessage("Finding Feedback...");
			dialog.show();
			
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(URL);
			JSONObject data = new JSONObject();
			post.setHeader("content-type", "application/json");
			try {
				data.put("vehicleNo", vehicle_number);
				data.put("lat", latitude);
				data.put("lng", longitude);
				data.put("mainCategory", vehicleType);
				data.put("formID", lastFormID);
				if(list.size()!=0){					
					Log.d("Next Form ID ",""+list.get(0).getFormID());
					flag_count_call =true;
				}
				Log.d("", "" + data);
				StringEntity entity = new StringEntity(data.toString(),
						HTTP.UTF_8);
				post.setEntity(entity);
				HttpResponse response = client.execute(post);
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity ent = response.getEntity();
					String result = EntityUtils.toString(ent);
					Log.d("JSON Response", " " + result);
					JSONObject object = new JSONObject(result);
					JSONObject ob = object.getJSONObject("data");
					error_code = ob.getString("Error_Code");
					if (ob.getString("Error_Code").equals("1")) {

						JSONArray arr = ob.getJSONArray("result");

						for (int i = 0; i < arr.length(); i++) {
							
							JSONObject content = arr.getJSONObject(i);
							FeedbackResponsePojo model = new FeedbackResponsePojo();
							model.setComment(content.getString("userComment"));
							model.setDate(content.getString("createdDate"));
							model.setUserName(content.getString("userName"));
							model.setRating(""
									+ content.getString("userRating"));
							model.setVeh_number(content.getString("vehicleNo"));
							model.setFormID(content.getString("formID"));
							if(i==0){
								lastFormID = content.getString("formID");
							}
							Log.d("Form Id ", (i+1)+" = "+content.getString("formID"));
							list.add(model);
						}
						if (!vehicle_number.equals("") && count_call==0 ) {
							feedbackDB.insertFeedback(vehicle_number,
									vehicleType, list);
						}
					} else {

					}
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if (!error_code.equals("1") && !flag_count_call) {
				showEmptyMessage();
			}
			adapter.notifyDataSetChanged();
			if(drop_count==0){
				drop_count++;
			animate_up = AnimationUtils.loadAnimation(FindFeedbackActivity.this,
					R.anim.slide_up);
			animate_up.setAnimationListener(FindFeedbackActivity.this);
			((LinearLayout) findViewById(R.id.ll_for_find_fd))
					.setAnimation(animate_up);
			}
			if(flag_count_call){
				lst_of_feedback.onLoadMoreComplete();
			}
		}

	}

	private void showInternetDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("No Internet Connection");
		builder.setMessage("Please Connect to Internet");

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}

		});
		builder.show();
	}

	private boolean isGoogleApiAvailable() {

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST);
			} else {
				Toast.makeText(getApplicationContext(),
						"This device is not supported.", Toast.LENGTH_LONG)
						.show();
				finish();
			}
			return false;
		}
		return true;
	}

	private void buildGoogleApi() {
		mGoogleApiClient = new GoogleApiClient.Builder(
				FindFeedbackActivity.this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	private void startLocationRequest() {
		if (mGoogleApiClient != null) {
			LocationServices.FusedLocationApi.requestLocationUpdates(
					mGoogleApiClient, mLocationRequest, this);
		}
	}

	private void stopLocationRequest() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
	}

	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(1000 * 60 * 5);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
		mLocationRequest.setFastestInterval(1000 * 60);

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		mLastLocation = location;
		longitude = "" + mLastLocation.getLongitude();
		latitude = "" + mLastLocation.getLatitude();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			longitude = "" + mLastLocation.getLongitude();
			latitude = "" + mLastLocation.getLatitude();
			Log.d("Last Location ", " ===== Long = " + longitude + " === Lat "
					+ latitude);
		}
		startLocationRequest();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		mGoogleApiClient.connect();
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		
			((LinearLayout) findViewById(R.id.ll_for_find_fd))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ll_more))
					.setVisibility(View.VISIBLE);
		
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
				
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
				
	}

}
