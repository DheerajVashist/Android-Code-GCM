package com.techila.travelfeedback;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Image;
import com.techila.travelfeedback.adapter.CustomAdapterForSpinner;
import com.techila.travelfeedback.dbhelper.VehicleNumberDb;

public class SubmitFeedbackActivity extends Activity implements
		ConnectionCallbacks, OnConnectionFailedListener,
		ResultCallback<People.LoadPeopleResult>, LocationListener {

	EditText et_vehicalNum, et_comment, et_vehicle_st, et_veh_st_code,
			et_veh_num_code;
	EditText et_train_num, et_plane_num;
	RatingBar rt_overall, rt_vehicle, rt_driver;
	float overall_rating, vehicle_rating, driver_rating;
	
	Spinner spn_subCategory;
	String mailId, vehicle_no, vehicle_type, vehicle_sub_type, str_comment,
			str_rating, str_loginType;

	String longitude, latitude;

	private final String URL = "http://phbjharkhand.in/TravelFeedBack/submit_feedback_form.php";
	
	boolean mShareButtonClicked =false;
	
	LayoutInflater inflater;

	// ////////////////////////////////////////////////////
	// Google Login detail Variables
	// ////////////////////////////////////////////////////
	private String scope = "https://www.googleapis.com/auth/profile.email";

	/* Client used to interact with Google APIs. */
	private GoogleApiClient googleApiClient;

	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;

	// ImageButton btn_gmail_logout;

	ArrayList<String> busCategory;
	ArrayList<String> carCategory;
	ArrayList<String> taxiCategory;
	ArrayList<String> autoCategory;
	ArrayList<String> trainCategory;
	ArrayList<String> planeCategory;

	/*
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;

	private boolean mSignInClicked;

	private ConnectionResult mConnectionResult;

	// /////////////////////////////////////////////////////
	// Google Integration End
	// ////////////////////////////////////////////////////

	// ///////////////////////////////////////////////////////
	// Facebook Login Detail Variables
	// ///////////////////////////////////////////////////////
	private static final String TAG = "LoginFragment";
	private static final String REQUESTING_LOCATION_UPDATES_KEY = "flag_location";
	private static final String LOCATION_KEY = "location";
	private static final String LAST_UPDATED_TIME_STRING_KEY = "time";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 0;
	private static final long UPDATE_INTERVAL = 1000 * 60 * 2;
	private static final long FATEST_INTERVAL = 100000;
	private static final float DISPLACEMENT = 100;
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	
	
	private final String[] PERMISSION = new String[] { "public_profile",
			"email" };

	// ////////////////////////////////////////////////////////
	// Facebook Integration End
	// ////////////////////////////////////////////////////////

	private SharedPreferences preference;
	private ConnectionDetector conn;
	Builder builder;
	AlertDialog dialog,dialog_for_share;
	boolean flag;
	String travelType, calling_activity;
	TextView tv_vehical_type;
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;
	private LocationRequest mLocationRequest;
	String mLastUpdateTime;
	boolean mRequestingLocationUpdates;
	VehicleNumberDb dbHelper;
	// LoginButton login_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		setContentView(R.layout.activity_submit_feedback);
		
		if (isConnectionToApiAvailable()) {
			createLocationRequest();
			buildGoogleApiClient();
		}
		dbHelper = VehicleNumberDb.getInstance(SubmitFeedbackActivity.this);
		ArrayList<String> arr = dbHelper.getTrainNumber();
		
		Log.d("Number Of Train","Size = "+arr.size());
		preference = getSharedPreferences("login_info", MODE_PRIVATE);
		// /////////////////////////////////////////////////////
		// Google + integration
		// ////////////////////////////////////////////////////
		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_PROFILE).build();

		// btn_gmail_logout = (ImageButton)
		// findViewById(R.id.btn_google_logout);
		tv_vehical_type = (TextView) findViewById(R.id.tv_vehical_type);
		// if (preference.getBoolean("googleLogin", false)) {
		//
		// btn_gmail_logout.setVisibility(View.VISIBLE);
		// } else {
		// btn_gmail_logout.setVisibility(View.GONE);
		// }
		spn_subCategory = (Spinner) findViewById(R.id.spn_sub_vehicle_type);
		// btn_gmail_logout.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// if (googleApiClient.isConnected()) {
		// Plus.AccountApi.clearDefaultAccount(googleApiClient);
		// googleApiClient.disconnect();
		// // googleApiClient.connect();
		// SharedPreferences.Editor edit = preference.edit();
		// edit.putBoolean("googleLogin", false);
		// edit.commit();
		// btn_gmail_logout.setVisibility(View.GONE);
		// }
		// }
		// });
		// ///////////////////////////////End Google Code/////////

		/* Find Travel Type From Bundle */
		et_comment = (EditText) findViewById(R.id.et_comment);
		et_vehicalNum = (EditText) findViewById(R.id.et_vehicle_number);
		et_vehicle_st = (EditText) findViewById(R.id.et_vehicle_st);
		et_veh_st_code = (EditText) findViewById(R.id.et_vehicle_st_num);
		et_veh_num_code = (EditText) findViewById(R.id.et_vehicle_code);
		et_plane_num = (EditText) findViewById(R.id.et_plane_num);
		
		et_train_num = (EditText) findViewById(R.id.et_train_num);

		rt_overall = (RatingBar) findViewById(R.id.rt_average);
		rt_driver = (RatingBar)findViewById(R.id.rt_driver);
		rt_vehicle=(RatingBar)findViewById(R.id.rt_of_vehicle);
		spn_subCategory = (Spinner) findViewById(R.id.spn_sub_vehicle_type);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Bundle bundle = getIntent().getExtras();
		try {
			tv_vehical_type.setText(bundle.getString("TravelType"));
			travelType = bundle.getString("TravelType");
			vehicle_type = travelType;
			calling_activity = bundle.getString("activity");
			vehicle_no = bundle.getString("vehicle_num");
			if (vehicle_no != null) {

				setVehicleNumber(vehicle_no);
			}
		} catch (Exception e) {

		}
		
		rt_driver.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				// TODO Auto-generated method stub
				driver_rating = rating;
				overall_rating = (driver_rating + vehicle_rating)/2; 
				rt_overall.setRating(overall_rating);
			}
		});
		rt_vehicle.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				// TODO Auto-generated method stub
				vehicle_rating = rating;
				overall_rating = (driver_rating + vehicle_rating)/2;
				rt_overall.setRating(overall_rating);
			}
		});
		
		CustomAdapterForSpinner adapter = null;
		// Initialize list of Sub Category
		LinearLayout ll_train = (LinearLayout) findViewById(R.id.ll_train);
		LinearLayout ll_plane = (LinearLayout) findViewById(R.id.ll_plane);
		LinearLayout ll_vehicle = (LinearLayout) findViewById(R.id.ll_vehicle);

		overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);
		if (travelType.equals("Auto")) {
			autoCategory = new ArrayList<String>();
			autoCategory.add("  City Auto");
			autoCategory.add("  Reserve Auto");
			autoCategory.add("  Share Auto");
			autoCategory.add("  Tour Auto");
			adapter = new CustomAdapterForSpinner(SubmitFeedbackActivity.this,
					android.R.layout.simple_spinner_item, autoCategory);
		} else if (travelType.equals("Bus")) {
			busCategory = new ArrayList<String>();
			busCategory.add("  Private Bus");
			busCategory.add("  Public Bus");
			busCategory.add("  City Bus");
			busCategory.add("  Tour Bus");
			adapter = new CustomAdapterForSpinner(SubmitFeedbackActivity.this,
					android.R.layout.simple_spinner_item, busCategory);
		} else if (travelType.equals("Car")) {
			carCategory = new ArrayList<String>();
			carCategory.add("  City Car");
			carCategory.add("  Tour Car");
			carCategory.add("  Private Car");
			carCategory.add("  Public Car");
			adapter = new CustomAdapterForSpinner(SubmitFeedbackActivity.this,
					android.R.layout.simple_spinner_item, carCategory);
		} else if (travelType.equals("Plane")) {
			tv_vehical_type.setText("Flight");
			planeCategory = new ArrayList<String>();
			planeCategory.add("  Private Plane");
			planeCategory.add("  KingFisher");
			planeCategory.add("  Sahara");
			planeCategory.add("  Air India");
			adapter = new CustomAdapterForSpinner(SubmitFeedbackActivity.this,
					android.R.layout.simple_spinner_item, planeCategory);
			ll_plane.setVisibility(View.VISIBLE);
			ll_train.setVisibility(View.GONE);
			ll_vehicle.setVisibility(View.GONE);
		} else if (travelType.equals("Taxi")) {
			taxiCategory = new ArrayList<String>();
			taxiCategory.add("  General Taxi");
			taxiCategory.add("  Prepaid Taxi");
			taxiCategory.add("  Cab");
			taxiCategory.add("  Share Taxi");
			adapter = new CustomAdapterForSpinner(SubmitFeedbackActivity.this,
					android.R.layout.simple_spinner_item, taxiCategory);
		} else if (travelType.equals("Train")) {
			trainCategory = new ArrayList<String>();
			trainCategory.add("  Express Train");
			trainCategory.add("  SuperFast Train");
			trainCategory.add("  Passenger Train");
			trainCategory.add("  Metro Train");
			et_train_num.requestFocus();
			adapter = new CustomAdapterForSpinner(SubmitFeedbackActivity.this,
					android.R.layout.simple_spinner_item, trainCategory);
			ll_plane.setVisibility(View.GONE);
			ll_train.setVisibility(View.VISIBLE);
			ll_vehicle.setVisibility(View.GONE);
		}

		spn_subCategory.setAdapter(adapter);

		spn_subCategory.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (travelType.equals("Train")) {
					vehicle_sub_type = trainCategory.get(position);
				} else if (travelType.equals("Car")) {
					vehicle_sub_type = carCategory.get(position);
				} else if (travelType.equals("Taxi")) {
					vehicle_sub_type = taxiCategory.get(position);
				} else if (travelType.equals("Auto")) {
					vehicle_sub_type = autoCategory.get(position);
				} else if (travelType.equals("Plane")) {
					vehicle_sub_type = planeCategory.get(position);
				} else if (travelType.equals("Bus")) {
					vehicle_sub_type = busCategory.get(position);
				}
				if (travelType.equals("Train")) {
					if (et_train_num.getText().toString().equals("")) {
						et_train_num.requestFocus();
					} else {
						et_comment.requestFocus();
						hideSoftInput();
						
					}
				} else if (travelType.equals("Plane")) {
					if (et_plane_num.getText().toString().equals("")) {
						et_plane_num.requestFocus();
					} else {
						et_comment.requestFocus();
						hideSoftInput();
					}
				} else {
					if (et_vehicle_st.getText().toString().equals("")) {
						et_vehicle_st.requestFocus();
					} else if (et_veh_st_code.getText().toString().equals("")) {
						et_veh_st_code.requestFocus();
					} else if (et_veh_num_code.getText().toString().equals("")) {
						et_veh_num_code.requestFocus();
					} else if (et_vehicalNum.getText().toString().equals("")) {
						et_vehicalNum.requestFocus();
					} else {
						et_comment.requestFocus();
						hideSoftInput();
					}
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		// share_btn = (LoginButton) findViewById(R.id.authButton);
		// if (preference.getBoolean("loggedIn", false)) {
		// login_btn.setVisibility(View.VISIBLE);
		// } else {
		// login_btn.setVisibility(View.GONE);
		// }
		conn = new ConnectionDetector(this);
		builder = new AlertDialog.Builder(SubmitFeedbackActivity.this);
		builder.setTitle("Login/Logout");
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vi = inflater.inflate(R.layout.dialog_for_submit_feedback, null);

		vi.findViewById(R.id.sign_in_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						str_loginType = "Gmail";
						if (!googleApiClient.isConnecting()) {

							mSignInClicked = true;
							resolveSignInError();
						}
					}
				});
		builder.setView(vi);

		LoginButton authButton = (LoginButton) vi.findViewById(R.id.authButton);
		authButton.setReadPermissions(Arrays.asList(PERMISSION));

		builder.setPositiveButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});

		addTextWatcher(et_vehicle_st);
		addTextWatcher(et_veh_st_code);
		addTextWatcher(et_veh_num_code);

		((TextView) findViewById(R.id.tv_back_on_submit))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						finish();
						overridePendingTransition(R.anim.lefttoright,
								R.anim.righttoleft);
					}
				});

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	private void hideSoftInput(){
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et_comment.getWindowToken(), 0);

	}
	
	private boolean isConnectionToApiAvailable() {

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);
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
						et_vehicalNum.requestFocus();
						break;
					}
				}

//				if (str.length() == 4) {
//					if (et.getId() == R.id.et_plan_code) {
//						et_plane_num.requestFocus();
//					}
//				}
			}
		});
	}

	private void resolveSignInError() {
		// TODO Auto-generated method stub
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				startIntentSenderForResult(mConnectionResult.getResolution()
						.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {

				mIntentInProgress = false;
				googleApiClient.connect();
			}
		}
	}

	private void setVehicleNumber(String vehicle_num) {
		String[] arr = vehicle_num.split(" ");
		if (vehicle_type.equals("Train")) {
			et_train_num.setText(vehicle_num);
		} else if (vehicle_type.equals("Plane")) {			
			et_plane_num.setText(vehicle_num);
		} else {
			et_vehicle_st.setText(arr[0]);
			et_veh_st_code.setText(arr[1]);
			et_veh_num_code.setText(arr[2]);
			et_vehicalNum.setText(arr[3]);
		}
	}

	protected void onStart() {
		super.onStart();
		googleApiClient.connect();
		if (mGoogleApiClient.isConnected()) {
			startLocationUpdates();
		}else{
			mGoogleApiClient.connect();
		}
	}

	protected void onStop() {
		super.onStop();

		if (googleApiClient.isConnected()) {
			googleApiClient.disconnect();
		}

		if (mGoogleApiClient.isConnected()) {
			stopLocationUpdates();
		}
	}

	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FATEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

	}

	@Override
	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		// if (session != null && (session.isOpened() || session.isClosed())) {
		// onSessionStateChange(session, session.getState(), null);
		// }		
		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RC_SIGN_IN) {
			mIntentInProgress = false;

			if (!googleApiClient.isConnecting()) {
				googleApiClient.connect();
				// btn_gmail_logout.setVisibility(View.VISIBLE);
			}
		}

		uiHelper.onActivityResult(requestCode, resultCode, data,
				new FacebookDialog.Callback() {
					@Override
					public void onError(FacebookDialog.PendingCall pendingCall,
							Exception error, Bundle data) {
						Log.d("Submit Feedback",
								String.format("Error: %s", error.toString()));
					}

					@Override
					public void onComplete(
							FacebookDialog.PendingCall pendingCall, Bundle data) {
						if (FacebookDialog.getNativeDialogDidComplete(data)) {
							Toast.makeText(SubmitFeedbackActivity.this,
									"Feedback Shared Successfully",
									Toast.LENGTH_SHORT).show();
							dialog_for_share.dismiss();
							
						} else {
							Toast.makeText(SubmitFeedbackActivity.this,
									"Feedback not Shared", Toast.LENGTH_SHORT)
									.show();
						}
						finish();
						SubmitFeedbackActivity.this.finish();
						Log.d("Submit Feedback", "Success!");
					}
				});

	}

	protected void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		// TODO Auto-generated method stub
		SharedPreferences.Editor edit = preference.edit();

		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
			str_loginType = "Facebook";
			Request request = Request.newMeRequest(session,
					new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user,
								Response response) {
							// TODO Auto-generated method stub
							if(!mShareButtonClicked){
							if (user != null) {
								Log.d("", "user.getName : " + user.getName());
								// Log.d("","user.getUsername : " +
								// user.getUsername());
								Log.d("", "user.getId : " + user.getId());
								mailId = user.getProperty("email").toString();
								Log.d("", "user.email : " + mailId);
								SharedPreferences.Editor ed = preference.edit();
								ed.putString("mailId", mailId);
								ed.commit();
								if (!(et_vehicalNum.getText().toString()
										.equals("") || et_comment.getText()
										.toString().equals(""))) {
									new SubmitResponse().execute();
								}
							}
						 }
						}
					});
			Request.executeBatchAsync(request);
			edit.putBoolean("loggedIn", true);
			edit.putString("loginType", str_loginType);

			// login_btn.setVisibility(View.VISIBLE);
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
			edit.putBoolean("loggedIn", false);
			edit.putString("loginType", "");
			edit.putString("mailId", "");
			// login_btn.setVisibility(View.GONE);
		}
		edit.commit();
		try {
			if(dialog!=null)
			if (dialog.isShowing()) {
				dialog.dismiss();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void submitFeedback(View vi) {

		switch (vi.getId()) {
		case R.id.btn_submit_feedback:
			if (conn.isConnectingToInternet()) {

				if (vehicle_type.equals("Train")) {
					if(et_train_num.getText().toString().length()<5){
						wrongVehicleNumber();
						return;
					}else{
					if (et_train_num.getText().toString().equals("")
							|| et_comment.getText().toString().equals("")) {
						showEmptyDialog();
						return;
					} else {
						vehicle_no = "" + et_train_num.getText().toString();
					}
					}
				} else if (vehicle_type.equals("Plane")) {
					if(et_plane_num.getText().toString().length()<5){
						wrongVehicleNumber();
						return;
					}else{
					if (et_plane_num.getText().toString().equals("")
							|| et_comment.getText().toString().equals("")) {
						showEmptyDialog();
						return;
					} else {
						vehicle_no =  et_plane_num.getText().toString();
					}
					}
				} else {
					if(et_vehicalNum.getText().toString().length()<4
							||et_vehicle_st.getText().toString().length()<2
							||et_veh_st_code.getText().toString().length()<2
							||et_veh_num_code.getText().toString().length()<2){
						wrongVehicleNumber();
						return;
					}else{
					if (et_vehicalNum.getText().toString().equals("")
							|| et_vehicle_st.getText().toString().equals("")
							|| et_comment.getText().toString().equals("")
							|| et_veh_st_code.getText().toString().equals("")
							|| et_veh_num_code.getText().toString().equals("")) {
						showEmptyDialog();
						return;
					} else {
						vehicle_no = "" + et_vehicle_st.getText().toString()
								+ " " + et_veh_st_code.getText().toString()
								+ " " + et_veh_num_code.getText().toString()
								+ " " + et_vehicalNum.getText().toString();
					}
					}
				}
				if (preference.getBoolean("loggedIn", false)
						|| preference.getBoolean("googleLogin", false)) {

					str_comment = et_comment.getText().toString();
					str_rating = "" + rt_overall.getRating();

					mailId = "" + preference.getString("mailId", "");
					str_loginType = "" + preference.getString("loginType", "");
					new SubmitResponse().execute();

				} else {

					str_comment = et_comment.getText().toString();
					str_rating = "" + rt_overall.getRating();
					login();

				}
			} else {
				showInternetDialog();
			}
			break;
		default:
			break;
		}
	}

	private void showEmptyDialog() {
		AlertDialog.Builder build = new AlertDialog.Builder(
				SubmitFeedbackActivity.this);
		build.setTitle("Warning");
		build.setMessage("Fill All the information");
		build.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});

		build.show();
	}
	
	private void wrongVehicleNumber() {
		AlertDialog.Builder build = new AlertDialog.Builder(
				SubmitFeedbackActivity.this);
		build.setTitle("Warning");
		build.setMessage("Enter valid Vehicle Number");
		build.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});

		build.show();
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

	private void login() {

		if (!flag) {
			dialog = builder.show();
			flag = true;
		} else {
			dialog.show();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		stopLocationUpdates();
		uiHelper.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
				mRequestingLocationUpdates);
		savedInstanceState.putParcelable(LOCATION_KEY, mLastLocation);
		savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY,
				mLastUpdateTime);
		super.onSaveInstanceState(savedInstanceState);
		uiHelper.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		if (!mIntentInProgress) {
			// Store the ConnectionResult so that we can use it later when the
			// user clicks
			// 'sign-in'.
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}
	}

	public void shareFeedback(View vi) {

		if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			// Publish the post using the Share Dialog
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
					this)
					.setLink(
							"https://play.google.com/store/apps/details?id=com.techila.travelfeedback")
					.setName("TravelFeedback")
					.setCaption(
							"Feedback of  " + vehicle_type + " Number :"
									+ vehicle_no)
					.setDescription(str_comment)
					.setPicture(
							"http://phbjharkhand.in/TravelFeedBack/tfa_icon_144.png")
					.build();

			// Toast.makeText(SubmitFeedbackActivity.this, "Share Dialog",
			// Toast.LENGTH_SHORT).show();
			uiHelper.trackPendingDialogCall(shareDialog.present());

		} else {
			// Fallback. For example, publish the post using the Feed Dialog
			// Toast.makeText(SubmitFeedbackActivity.this, "Feed Dialog",
			// Toast.LENGTH_SHORT).show();
			publishFeedDialog();
		}

	}

	private void publishFeedDialog() {
		Bundle params = new Bundle();
		params.putString("name", "TravelFeedback");
		params.putString("caption", "Feedback of  " + vehicle_type
				+ " Number :" + vehicle_no);
		params.putString("description", str_comment);
		params.putString("link",
				"https://play.google.com/store/apps/details?id=com.techila.travelfeedback");
		params.putString("picture",
				"http://phbjharkhand.in/TravelFeedBack/tfa_icon_144.png");
		try{
			 mShareButtonClicked =true;
			Session session = Session.getActiveSession();
			       if (!session.isOpened() && !session.isClosed()) {
			           session.openForRead(new Session.OpenRequest(this).setCallback(callback));
			       } else {
			           Session.openActiveSession(SubmitFeedbackActivity.this, true, callback);
			       }
			   
			WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
				SubmitFeedbackActivity.this, Session.getActiveSession(), params))
				.setOnCompleteListener(new OnCompleteListener() {

					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						// TODO Auto-generated method stub
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								Toast.makeText(SubmitFeedbackActivity.this,
										"Posted Successfully",
										Toast.LENGTH_SHORT).show();
								finish();
							} else {
								// User clicked the Cancel button
								Toast.makeText(
										SubmitFeedbackActivity.this
												.getApplicationContext(),
										"Publish cancelled", Toast.LENGTH_SHORT)
										.show();
								finish();
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							// User clicked the "x" button
							Toast.makeText(
									SubmitFeedbackActivity.this
											.getApplicationContext(),
									"Publish cancelled", Toast.LENGTH_SHORT)
									.show();
							finish();
						} else {
							// Generic, ex: network error
							Toast.makeText(
									SubmitFeedbackActivity.this
											.getApplicationContext(),
									"Error posting story", Toast.LENGTH_SHORT)
									.show();
							finish();
						}
					}

				}).build();
		feedDialog.show();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mSignInClicked = false;
		try {
			getLocation();
			startLocationUpdates();
			// Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG)
			// .show();
			if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(googleApiClient);
				String personName = currentPerson.getDisplayName();
				String email = Plus.AccountApi.getAccountName(googleApiClient);
				Log.d("User Name", " " + personName + "  emailID  = " + email);
				mailId = email;
				Log.d("Vehicle Number "," "+vehicle_no);
				str_loginType = "Gmail";
				str_comment =et_comment.getText().toString();
				str_rating = ""+rt_overall.getRating();
				str_loginType = "Gmail";
				SharedPreferences.Editor edit = preference.edit();
				edit.putBoolean("googleLogin", true);
				edit.putString("loginType", "Gmail");
				edit.putString("mailId", email);
				edit.commit();
				Image personPhoto = currentPerson.getImage();
				String personGooglePlusProfile = currentPerson.getUrl();
			}

			
			if (dialog != null) {
				if (dialog.isShowing()) {

					dialog.dismiss();
					new SubmitResponse().execute();
				}
			}
		} catch (Exception e) {

		}
	}

	private void getLocation() {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			longitude = "" + mLastLocation.getLongitude();
			latitude = "" + mLastLocation.getLatitude();

			Log.d("Locations Are = ", latitude + "Lat ===== Long " + longitude);

		} else {
			Log.d("",
					"(Couldn't get the location. Make sure location is enabled on the device)");
			Toast.makeText(
					SubmitFeedbackActivity.this,
					"Couldn't get the location. Make sure location is enabled on the device",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void startLocationUpdates() {
		if (mGoogleApiClient != null) {
			LocationServices.FusedLocationApi.requestLocationUpdates(
					mGoogleApiClient, mLocationRequest, this);
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		// Toast.makeText(this, "Connection Suspended",
		// Toast.LENGTH_LONG).show();
		mGoogleApiClient.connect();
	}

	@Override
	public void onResult(LoadPeopleResult arg0) {
		// TODO Auto-generated method stub

	}

	private class SubmitResponse extends AsyncTask<Void, Void, Void> {

		ProgressDialog progress;
		String error_code;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			try {
				progress = new ProgressDialog(SubmitFeedbackActivity.this);
				progress.setMessage("Submitting...");
				progress.show();
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(URL);

			post.setHeader("content-type", "application/json");

			JSONObject data = new JSONObject();

			try {
				data.put("userName", mailId);
				data.put("vehicleNo", vehicle_no);
				data.put("mainCategory", vehicle_type);
				data.put("subCategory", vehicle_sub_type);
				data.put("userComment", str_comment);
				data.put("userRating", str_rating);
				data.put("loginType", str_loginType);
				if (mLastLocation != null) {
					data.put("lng", "" + longitude);
					data.put("lat", "" + latitude);
				} else {
					data.put("lng", "");
					data.put("lat", "");
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
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			try {
				if (progress.isShowing()) {
					progress.dismiss();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (calling_activity.equals("find_feedback")) {
					Intent it = new Intent();
					it.putExtra("vehicle_num", vehicle_no);
					if (error_code.equals("1")) {
						setResult(RESULT_OK, it);

					} else {
						setResult(RESULT_CANCELED, it);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			if (error_code.equals("1")) {
				Toast.makeText(SubmitFeedbackActivity.this,
						"Submitted Successfully ", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(SubmitFeedbackActivity.this,
						"Submission Failed ", Toast.LENGTH_SHORT).show();
			}
			createShareDialog();
		}

	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
	}

	private void createShareDialog() {
		AlertDialog.Builder build = new AlertDialog.Builder(
				SubmitFeedbackActivity.this);
		AlertDialog ad = build.create();
		ad.requestWindowFeature(Window.FEATURE_NO_TITLE);
		View vi = inflater.inflate(R.layout.dialog_layout_for_share, null);
		build.setView(vi);

		build.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						finish();
						overridePendingTransition(R.anim.lefttoright,
								R.anim.righttoleft);
					}
				});

		dialog_for_share=build.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		mLastLocation = location;
		mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
		longitude = "" + location.getLongitude();
		latitude = "" + location.getLatitude();
	}

}
