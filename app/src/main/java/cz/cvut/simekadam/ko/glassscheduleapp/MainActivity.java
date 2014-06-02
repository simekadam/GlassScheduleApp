package cz.cvut.simekadam.ko.glassscheduleapp;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.*;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import cz.cvut.simekadam.ko.glassscheduleapp.net.service.RestAdapterService;
import cz.cvut.simekadam.ko.glassscheduleapp.net.service.UsersAdapterService;
import cz.cvut.simekadam.ko.glassscheduleapp.views.ScheduleView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by simekadam on 25/04/14.
 */
public class MainActivity extends Activity {

	private static final String LIVE_CARD_ID = "schedule";
	private Observable<Float> headingObservable;
	private OrientationManager mOrientationManager;
	private TextToSpeech mSpeech;
	private ScheduleRenderer mRenderer;
	private ScheduleView mScheduleView;
	private RestAdapterService mRestService;
	private UsersAdapterService mUsersAdapterService;
	private GestureDetector mGestureDetector;
	private AudioManager maManager;
	private float mFloat;
	private boolean mTouch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SurfaceView v = (SurfaceView)LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_main, null);
		mFloat = Float.MAX_VALUE;

		mRestService = new RestAdapterService(getApplicationContext());
		mUsersAdapterService = new UsersAdapterService(getApplicationContext());
		// Even though the text-to-speech engine is only used in response to a menu action, we
		// initialize it when the application starts so that we avoid delays that could occur
		// if we waited until it was needed to start it up.

		SensorManager sensorManager =
			(SensorManager)getSystemService(Context.SENSOR_SERVICE);


		mOrientationManager = new OrientationManager(sensorManager);
		mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				// Do nothing.
			}
		});


		mScheduleView = new ScheduleView(getApplicationContext());
		mGestureDetector = createGestureDetector(this);

		v.getHolder().addCallback(mScheduleView);
		maManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);

		mScheduleView.getHolderObservable().subscribe(new Action1<SurfaceHolder>() {
			@Override
			public void call(SurfaceHolder surfaceHolder) {
				mRestService.getEventsObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mScheduleView.getEventListSubscriber());
			}
		});
		mScheduleView.getHolderObservable().subscribe(new Action1<SurfaceHolder>() {
			@Override
			public void call(SurfaceHolder surfaceHolder) {
				mUsersAdapterService.getUserObservable(4).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mScheduleView.getUserListSubscriber());
			}
		});


		headingObservable = mOrientationManager.subscribeToHeading().subscribeOn(AndroidSchedulers.mainThread());
		headingObservable.subscribe(new Action1<Float>() {
			@Override
			public void call(Float aFloat) {
				if(mTouch) return;
				if(mFloat == Float.MAX_VALUE){
					mFloat = aFloat;
				}else{
					Log.d("moving head", aFloat+"");
					mScheduleView.setAbsoluteHeading((aFloat) / 90 * (3 * 640));
				}

			}
		});
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(v);

	}

	@Override
	public void onDestroy() {


		mOrientationManager = null;

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mFloat = Float.MAX_VALUE;
	}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					//play the tap sound
					maManager.playSoundEffect(Sounds.TAP);
					//open the menu
					openOptionsMenu();
					return true;

				} else if (gesture == Gesture.TWO_TAP) {
					// do something on two finger tap
					return true;
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					// do something on right (forward) swipe
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					// do something on left (backwards) swipe
					return true;
				}
				return false;
			}
		});
		gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
			@Override
			public void onFingerCountChanged(int previousCount, int currentCount) {
				// do something on finger count changes
			}
		});
		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta, float velocity) {
				Log.d("scrolling", displacement+" "+delta+" "+velocity);
				// do something on scrolling
				if(mTouch)mScheduleView.setHeading(delta);
				return mTouch;
			}
		});

		return gestureDetector;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.reschedule:
				displaySpeechRecognizer();
				return true;
			case R.id.switchControls:
				switchControls();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void switchControls() {
		mTouch = !mTouch;
	}

	/*
		 * Send generic motion events to the gesture detector
		 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}

	/**
	 * A binder that gives other components access to the speech capabilities provided by the
	 * service.
	 */

	public void readHeadingAloud() {
			mSpeech.speak("Rescheduling tasks.", TextToSpeech.QUEUE_FLUSH, null);
		}


	private static final int SPEECH_REQUEST = 0;

	private void displaySpeechRecognizer() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, SPEECH_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	                                Intent data) {
		if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {

			List<String> results = data.getStringArrayListExtra(
				RecognizerIntent.EXTRA_RESULTS);
			String spokenText = results.get(0);
			Log.d("test", spokenText);
			readHeadingAloud();


			// Do something with spokenText.
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
