package cz.cvut.simekadam.ko.glassscheduleapp;

import java.util.List;

import android.content.Context;
import android.view.SurfaceHolder;

import com.google.android.glass.timeline.DirectRenderingCallback;
import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.User;
import cz.cvut.simekadam.ko.glassscheduleapp.net.service.RestAdapterService;
import cz.cvut.simekadam.ko.glassscheduleapp.views.ScheduleView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by simekadam on 20/04/14.
 */
public class ScheduleRenderer implements DirectRenderingCallback {

	private final Context mContext;
	ScheduleView mScheduleView;

	private boolean mInterference;
	private final OrientationManager.OnChangedListener mCompassListener =
		new OrientationManager.OnChangedListener() {

			@Override
			public void onOrientationChanged(OrientationManager orientationManager) {
				mScheduleView.setHeading(orientationManager.getHeading());


			}




			@Override
			public void onAccuracyChanged(OrientationManager orientationManager) {
				mInterference = orientationManager.hasInterference();

			}
		};

	public ScheduleRenderer(Context context, OrientationManager orientationManager
	                    ) {


		mContext = context;
		mScheduleView = new ScheduleView(mContext);


	}

	@Override
	public void renderingPaused(SurfaceHolder surfaceHolder, boolean b) {

	}

	@SuppressWarnings("ResourceType")
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		holder.addCallback(mScheduleView);


	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	public Observable<List<User>> updateUsers(RestAdapterService service){
		Observable<List<User>> userObservable = service.getUserObservable();
		userObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mScheduleView.getUserListSubscriber());
		return userObservable;
	}

}
