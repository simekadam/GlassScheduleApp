package cz.cvut.simekadam.ko.glassscheduleapp;

import java.util.zip.Inflater;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.google.android.glass.timeline.LiveCard;
import cz.cvut.simekadam.ko.glassscheduleapp.net.service.RestAdapterService;
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

	/**
	 * A binder that gives other components access to the speech capabilities provided by the
	 * service.
	 */
	public class CompassBinder extends Binder {
		/**
		 * Read the current heading aloud using the text-to-speech engine.
		 */
		public void readHeadingAloud() {
			mSpeech.speak("test", TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	private final CompassBinder mBinder = new CompassBinder();
	private Observable<Float> headingObservable;

	private OrientationManager mOrientationManager;
	private TextToSpeech mSpeech;

	private ScheduleRenderer mRenderer;
	private ScheduleView mScheduleView;
	private RestAdapterService mRestService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SurfaceView v = (SurfaceView)LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_main, null);



		mRestService = new RestAdapterService(getApplicationContext());

		// Even though the text-to-speech engine is only used in response to a menu action, we
		// initialize it when the application starts so that we avoid delays that could occur
		// if we waited until it was needed to start it up.

		SensorManager sensorManager =
			(SensorManager) getSystemService(Context.SENSOR_SERVICE);


		mOrientationManager = new OrientationManager(sensorManager);
//

			mRenderer = new cz.cvut.simekadam.ko.glassscheduleapp.ScheduleRenderer(this, mOrientationManager);
			mScheduleView = new ScheduleView(getApplicationContext());
			v.getHolder().addCallback(mRenderer);
			mRenderer.updateUsers(mRestService);
			mScheduleView.getHolderObservable().subscribe(new Action1<SurfaceHolder>() {
				@Override
				public void call(SurfaceHolder surfaceHolder) {
					mRestService.getUserObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mScheduleView.getUserListSubscriber());
				}
			});
			mScheduleView.getHolderObservable().subscribe(new Action1<SurfaceHolder>() {
				@Override
				public void call(SurfaceHolder surfaceHolder) {
					mRestService.getEventsObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mScheduleView.getEventListSubscriber());
				}
			});
			Intent menuIntent = new Intent(this, ScheduleMenuActivity.class);
			menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

			headingObservable = mOrientationManager.subscribeToHeading().subscribeOn(AndroidSchedulers.mainThread());
			headingObservable.subscribe(new Action1<Float>() {
				@Override
				public void call(Float aFloat) {
					mScheduleView.setHeading(aFloat);
				}
			});
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			setContentView(v);

	}





	@Override
	public void onDestroy() {



		mOrientationManager = null;

		super.onDestroy();
	}
}
