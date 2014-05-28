package cz.cvut.simekadam.ko.glassscheduleapp;

/**
 * Created by simekadam on 20/04/14.
 */
/*
 * Copyright (C) 2013 The Android Open Source Project
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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.hardware.*;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;


import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Collects and communicates information about the user's current orientation and location.
 */
public class OrientationManager {

	/**
	 * The minimum distance desired between location notifications.
	 */
	private static final long METERS_BETWEEN_LOCATIONS = 2;

	/**
	 * The minimum elapsed time desired between location notifications.
	 */
	private static final long MILLIS_BETWEEN_LOCATIONS = TimeUnit.SECONDS.toMillis(3);

	/**
	 * The maximum age of a location retrieved from the passive location provider before it is
	 * considered too old to use when the schedule first starts up.
	 */
	private static final long MAX_LOCATION_AGE_MILLIS = TimeUnit.MINUTES.toMillis(30);

	/**
	 * The sensors used by the schedule are mounted in the movable arm on Glass. Depending on how
	 * this arm is rotated, it may produce a displacement ranging anywhere from 0 to about 12
	 * degrees. Since there is no way to know exactly how far the arm is rotated, we just split the
	 * difference.
	 */
	private static final int ARM_DISPLACEMENT_DEGREES = 6;

	/**
	 * Classes should implement this interface if they want to be notified of changes in the user's
	 * location, orientation, or the accuracy of the schedule.
	 */
	public interface OnChangedListener {
		/**
		 * Called when the user's orientation changes.
		 *
		 * @param orientationManager the orientation manager that detected the change
		 */
		void onOrientationChanged(OrientationManager orientationManager);


		/**
		 * Called when the accuracy of the schedule changes.
		 *
		 * @param orientationManager the orientation manager that detected the change
		 */
		void onAccuracyChanged(OrientationManager orientationManager);
	}

	private final SensorManager mSensorManager;
	private final Set<OnChangedListener> mListeners;
	private final float[] mRotationMatrix;
	private final float[] mOrientation;

	private boolean mTracking;
	private float mHeading;
	private float mPitch;
	private GeomagneticField mGeomagneticField;
	private boolean mHasInterference;

	protected final BehaviorSubject<Float> mHeadingBehaviorSubject;
	protected final BehaviorSubject<Boolean> mInterferenceBehaviorSubject;

	/**
	 * The sensor listener used by the orientation manager.
	 */
	private SensorEventListener mSensorListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mHasInterference = (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
				mInterferenceBehaviorSubject.onNext(mHasInterference);
			}
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
				// Get the current heading from the sensor, then notify the listeners of the
				// change.
				SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
				SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
					SensorManager.AXIS_Z, mRotationMatrix);
				SensorManager.getOrientation(mRotationMatrix, mOrientation);

				// Store the pitch (used to display a message indicating that the user's head
				// angle is too steep to produce reliable results.
				mPitch = (float) Math.toDegrees(mOrientation[1]);

				// Convert the heading (which is relative to magnetic north) to one that is
				// relative to true north, using the user's current location to compute this.
				float magneticHeading = (float) Math.toDegrees(mOrientation[0]);
				mHeading = magneticHeading;

				mHeadingBehaviorSubject.onNext(mHeading);
			}
		}
	};

	/**
	 * The location listener used by the orientation manager.
	 */
	private LocationListener mLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			updateGeomagneticField(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// Don't need to do anything here.
		}

		@Override
		public void onProviderEnabled(String provider) {
			// Don't need to do anything here.
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Don't need to do anything here.
		}
	};

	/**
	 * Initializes a new instance of {@code OrientationManager}, using the specified context to
	 * access system services.
	 */
	public OrientationManager(SensorManager sensorManager) {
		mRotationMatrix = new float[16];
		mOrientation = new float[9];
		mSensorManager = sensorManager;
		mListeners = new LinkedHashSet<OnChangedListener>();

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);


		mHeadingBehaviorSubject = BehaviorSubject.create(getHeading());
		mInterferenceBehaviorSubject = BehaviorSubject.create(hasInterference());
		start();



	}



	/**
	 * Starts tracking the user's location and orientation. After calling this method, any
	 * {@link OnChangedListener}s added to this object will be notified of these events.
	 */
	public void start() {
		if (!mTracking) {
			mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_UI);

			// The rotation vector sensor doesn't give us accuracy updates, so we observe the
			// magnetic field sensor solely for those.
			mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_UI);






			mTracking = true;
		}
	}

	/**
	 * Stops tracking the user's location and orientation. Listeners will no longer be notified of
	 * these events.
	 */
	public void stop() {
		if (mTracking) {
			mSensorManager.unregisterListener(mSensorListener);
			mTracking = false;
		}
		mHeadingBehaviorSubject.onCompleted();
		mInterferenceBehaviorSubject.onCompleted();

	}

	/**
	 * Gets a value indicating whether there is too much magnetic field interference for the
	 * schedule to be reliable.
	 *
	 * @return true if there is magnetic interference, otherwise false
	 */
	public boolean hasInterference() {
		return mHasInterference;
	}



	/**
	 * Gets the user's current heading, in degrees. The result is guaranteed to be between 0 and
	 * 360.
	 *
	 * @return the user's current heading, in degrees
	 */
	public float getHeading() {
		return mHeading;
	}

	/**
	 * Gets the user's current pitch (head tilt angle), in degrees. The result is guaranteed to be
	 * between -90 and 90.
	 *
	 * @return the user's current pitch angle, in degrees
	 */
	public float getPitch() {
		return mPitch;
	}




	public Observable<Float> subscribeToHeading() {
		return mHeadingBehaviorSubject;
	}

	public Observable<Boolean> subscribeToInterference() {
		return mInterferenceBehaviorSubject;
	}





	/**
	 * Updates the cached instance of the geomagnetic field after a location change.
	 */
	private void updateGeomagneticField(Location location) {
		mGeomagneticField = new GeomagneticField((float) location.getLatitude(),
			(float) location.getLongitude(), (float) location.getAltitude(),
			location.getTime());
	}

	/**
	 * Use the magnetic field to compute true (geographic) north from the specified heading
	 * relative to magnetic north.
	 *
	 * @param heading the heading (in degrees) relative to magnetic north
	 * @return the heading (in degrees) relative to true north
	 */
	private float computeTrueNorth(float heading) {
		if (mGeomagneticField != null) {
			return heading + mGeomagneticField.getDeclination();
		} else {
			return heading;
		}
	}
}
