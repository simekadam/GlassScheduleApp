package cz.cvut.simekadam.ko.glassscheduleapp.views;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.android.glass.timeline.DirectRenderingCallback;
import cz.cvut.simekadam.ko.glassscheduleapp.R;
import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.Event;
import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.User;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by simekadam on 20/04/14.
 */
public class ScheduleView implements DirectRenderingCallback {

	/**
	 * The refresh rate, in frames per second, of the compass.
	 */
	private static final int REFRESH_RATE_FPS = 45;

	/**
	 * The duration, in milliseconds, of one frame.
	 */
	private static final long FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS;
	private final Context mContext;
	private final FrameLayout mLayout;
	public Subscriber<List<User>> mUserListSubscriber = new Subscriber<List<User>>() {
		@Override
		public void onCompleted() {

		}

		@Override
		public void onError(Throwable e) {
				Log.d("", "events error" + e.getMessage());
		}

		@Override
		public void onNext(List<User> users) {
			Observable.from(users).subscribe(new Action1<User>() {
				@Override
				public void call(User user) {
					Log.d("user added", user.toString());
					mScheduleFrameView.addUser(user);
				}
			});
		}
	};
	public Subscriber<List<Event>> mEventListSubscriber = new Subscriber<List<Event>>() {
		@Override
		public void onCompleted() {

		}

		@Override
		public void onError(Throwable e) {
			Log.d("", "events error" + e.getMessage());
		}

		@Override
		public void onNext(List<Event> events) {
			Observable.from(events).subscribe(new Action1<Event>() {
				@Override
				public void call(Event event) {
					mTaskScheduleView.addEvent(event);
				}
			});
		}
	};
	private List<Event> mEvents;
	private SurfaceHolder mHolder;
	private List<Subscriber<? super SurfaceHolder>> mSubscribers;
	@InjectView(R.id.frame)
	ScheduleFrameView mScheduleFrameView;
	@InjectView(R.id.taskView)
	TaskScheduleView mTaskScheduleView;
	private float mXpos;
	private float mYpos;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private RenderThread mRenderThread;
	private Observable<SurfaceHolder> mHolderObservable = Observable.create(new Observable.OnSubscribe<SurfaceHolder>() {
		@Override
		public void call(Subscriber<? super SurfaceHolder> subscriber) {
			mSubscribers.add(subscriber);
		}
	});

	public ScheduleView(Context context) {
		//view init
		mContext = context;
		mSubscribers = new LinkedList<>();
		LayoutInflater inflater = LayoutInflater.from(context);
		mLayout = (FrameLayout)inflater.inflate(R.layout.schedule_layout, null);
		ButterKnife.inject(this, mLayout);

	}

	public Observable<SurfaceHolder> getHolderObservable() {
		return mHolderObservable;
	}

	public void setHeading(float heading) {
		mTaskScheduleView.setOffset(heading);
	}

	public void setAbsoluteHeading(float absoluteHeading){
		mTaskScheduleView.setAbsoluteOffset(absoluteHeading);
	}

	public Subscriber<List<User>> getUserListSubscriber() {
		return mUserListSubscriber;
	}

	public Subscriber<List<Event>> getEventListSubscriber() {
		return mEventListSubscriber;
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
		for (Subscriber<? super SurfaceHolder> subscriber : mSubscribers) {
			subscriber.onNext(holder);
		}
		mRenderThread = new RenderThread();
		mRenderThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mSurfaceWidth = width;
		mSurfaceHeight = height;
		doLayout();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	@Override
	public void renderingPaused(SurfaceHolder surfaceHolder, boolean b) {

	}


	private void doLayout() {
		// Measure and update the layout so that it will take up the entire surface space
		// when it is drawn.
		int measuredWidth = View.MeasureSpec.makeMeasureSpec(mSurfaceWidth,
			View.MeasureSpec.EXACTLY);
		int measuredHeight = View.MeasureSpec.makeMeasureSpec(mSurfaceHeight,
			View.MeasureSpec.EXACTLY);

		mLayout.measure(measuredWidth, measuredHeight);
		mLayout.layout(0, 0, mLayout.getMeasuredWidth(), mLayout.getMeasuredHeight());
	}

	/**
	 * Repaints the view.
	 */
	private synchronized void repaint() {
		Canvas canvas = null;
		try {
			canvas = mHolder.lockCanvas();
		} catch (RuntimeException e) {

		}
		if (canvas != null) {
			mLayout.draw(canvas);
			try {
				mHolder.unlockCanvasAndPost(canvas);
			} catch (RuntimeException e) {
			}
		}
	}


	private class RenderThread extends Thread {
		private boolean mShouldRun;


		public RenderThread() {
			mShouldRun = true;
		}


		private synchronized boolean shouldRun() {
			return mShouldRun;
		}


		public synchronized void quit() {
			mShouldRun = false;
		}

		@Override
		public void run() {
			while (shouldRun()) {
				long frameStart = SystemClock.elapsedRealtime();
				repaint();
				long duration = SystemClock.elapsedRealtime() - frameStart;
//				Log.d("draw", "drawing "+duration);

				long sleepTime = FRAME_TIME_MILLIS - duration;
				if (sleepTime > 0) {
					SystemClock.sleep(sleepTime);
				}
			}
		}
	}

}
