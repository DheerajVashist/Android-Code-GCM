package com.techila.travelfeedback;

import java.math.MathContext;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GetLocation {

	Context context;
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	private static final float MIN_LAST_READ_ACCURACY = 500.0f;
	private static final float MIN_DISTANCE = 10.0f;
	private static final long ONE_MIN = 1000 * 60;
	private static final long TWO_MIN = ONE_MIN * 2;
	private static final long FIVE_MIN = ONE_MIN * 5;
	private static final long MEASURE_TIME = 1000 * 30;
	private static final long POLLING_FREQ = 1000 * 10;
	private static final float MIN_ACCURACY = 25.0f;
	private Location mLocation;

	public GetLocation(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		determineInitialReading();
	}

	public Location getCurrentLocation() {

		if (mLocationManager == null) {
			return null;
		}
		mLocation = bestLastKnownLocation(MIN_LAST_READ_ACCURACY, FIVE_MIN);

		if (mLocation != null) {
			return mLocation;
		}

		mLocationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onLocationChanged(Location location) {

				// Determine whether new location is better than current best
				// estimate
				if (mLocation == null
						|| location.getAccuracy() < mLocation.getAccuracy()) {

					// Update best Estimate
					mLocation = location;
					if (mLocation.getAccuracy() < MIN_ACCURACY)
						mLocationManager.removeUpdates(mLocationListener);
				}
			}
		};

		return mLocation;
	}

	public Location bestLastKnownLocation(float minLastReadAccuracy, long maxAge) {

		Location bestResult = null;
		float bestAccuracy = Float.MAX_VALUE;
		Long bestAge = Long.MIN_VALUE;

		List<String> matchingProvider = mLocationManager.getAllProviders();

		for (String provider : matchingProvider) {
			Location location = mLocationManager.getLastKnownLocation(provider);

			if (location != null) {

				float accuracy = location.getAccuracy();
				long time = location.getTime();

				if (accuracy < bestAccuracy) {
					
					bestResult = location;
					bestAccuracy = accuracy;
					bestAge = time;
					
				}
			}
		}

		if (bestAccuracy > minLastReadAccuracy
				|| (System.currentTimeMillis() - bestAge) > maxAge) {
			return null;
		} else {
			return bestResult;
		}
	}

	// Determine whether initial reading is
	// "good enough". If not, register for
	// further location updates

	public void determineInitialReading() {

		if (mLocation == null
				|| mLocation.getAccuracy() > MIN_LAST_READ_ACCURACY
				|| mLocation.getTime() < System.currentTimeMillis() - TWO_MIN) {

			// Register for network location updates
			if (null != mLocationManager
					.getProvider(LocationManager.NETWORK_PROVIDER)) {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, POLLING_FREQ,
						MIN_DISTANCE, mLocationListener);
			}

			// Register for GPS location updates
			if (null != mLocationManager
					.getProvider(LocationManager.GPS_PROVIDER)) {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, POLLING_FREQ,
						MIN_DISTANCE, mLocationListener);
			}
			
			// Schedule a runnable to unregister location listeners
			Executors.newScheduledThreadPool(1).schedule(new Runnable() {

				@Override
				public void run() {

					Log.i("Location", "location updates cancelled");

					mLocationManager.removeUpdates(mLocationListener);

				}
			}, MEASURE_TIME, TimeUnit.MILLISECONDS);
			
		}

	}

	public void unRegisterLocationListener() {
		mLocationManager.removeUpdates(mLocationListener);
	}

}
