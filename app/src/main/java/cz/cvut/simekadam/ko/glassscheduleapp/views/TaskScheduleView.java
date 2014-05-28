package cz.cvut.simekadam.ko.glassscheduleapp.views;

import java.util.LinkedList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import cz.cvut.simekadam.ko.glassscheduleapp.R;
import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.Event;
import cz.cvut.simekadam.ko.glassscheduleapp.utils.MathUtils;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by simekadam on 04/05/14.
 */
public class TaskScheduleView extends View {

	private final Paint mBottomShadowPaint;
	private float heading;
	private float mAnimatedHeading;
	private static final float MIN_DISTANCE_TO_ANIMATE = 15.0f;

	private List<Event> mEventList;
	private Paint mPaint;
	private Paint mTextPaint;
	private Paint mVerticalLinePaint;
	private ValueAnimator mAnimator;
	private boolean mCanvasUpToDate;

	public TaskScheduleView(Context context) {
		this(context, null, 0);
	}

	public TaskScheduleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TaskScheduleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mEventList = new LinkedList<>();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setColor(Color.BLUE);
		mPaint.setAlpha(160);
		mPaint.setAntiAlias(true);

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setShadowLayer(2, 0, 0, Color.GRAY);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setTextSize(30);
		mTextPaint.setAntiAlias(true);

		mVerticalLinePaint = new Paint();
		mVerticalLinePaint.setColor(Color.WHITE);
		mVerticalLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mVerticalLinePaint.setPathEffect(new DashPathEffect(new float[]{5,5}, 10));
		mVerticalLinePaint.setAntiAlias(true);

		mBottomShadowPaint = new Paint();
		mBottomShadowPaint.setShader(new LinearGradient(0, getHeight(), 0, getHeight()-100, getResources().getColor(R.color.translucent_black), Color.TRANSPARENT, Shader.TileMode.MIRROR));

		mAnimatedHeading = Float.NaN;
		mAnimator = new ValueAnimator();
		setupAnimator();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);



		float pixelsPerDegree = getWidth()/10f;
		float centerX = getWidth() / 2.0f;
		float centerY = getHeight() / 2.0f;



		canvas.save();
		float v = -heading * pixelsPerDegree + centerX;
		if( v < 0){
			v = 0;
		}else if(v > 640 * 4) {
			v = 640;
		}
			canvas.translate(v, 0);

				drawEvents(canvas);




		canvas.restore();

	}


	private void drawEvents(final Canvas canvas){
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

		canvas.save();

		final float height = canvas.getHeight()/4;
		final float width = canvas.getWidth()/8;

		for(int i = 0; i < 24; i++){
			canvas.drawLine(i*width, 0, i*width, getHeight(), mVerticalLinePaint);
		}
		Observable.from(mEventList).subscribe(new Action1<Event>() {
			@Override
			public void call(Event event) {
				for (int i = 0; i < 4; i++) {
					mPaint.setColor((getColor((event.getStartHour()+i)%6)));
					canvas.drawRect((event.getStartHour()+i) * width + 5, i * height + 10, (event.getEndHour()+i) * width - 5, (i + 1) * height - 10, mPaint);
					canvas.drawText(event.getName().substring(0, 5).toUpperCase(), (event.getStartHour()+i) * width + 35, i * height + 10 + height / 2, mTextPaint);
				}

			}
		});
		canvas.drawRect(0, getHeight()-100, getWidth(), getHeight(), mBottomShadowPaint);
		for(int i = 0; i < 24; i++){
			float v = mTextPaint.measureText(String.valueOf(i));
			canvas.drawText(String.valueOf(i), i * width-v/2, getHeight(), mTextPaint);
		}

		mCanvasUpToDate = true;
		canvas.restore();
	}


	public void addEvent(Event event) {
		mCanvasUpToDate = false;
		mEventList.add(event);
		invalidate();
	}

	public void setHeading(float heading) {
		this.heading = heading;
		animateTo(heading);

	}

	private int getColor(int climbCategory) {
		if(climbCategory == -1){
			climbCategory = (int)(Math.random()*5);
		}
		switch(climbCategory){
			case 0:
				return getResources().getColor(android.R.color.holo_blue_dark);
			case 1:
				return getResources().getColor(android.R.color.holo_green_dark);
			case 2:
				return getResources().getColor(android.R.color.holo_green_light);
			case 3:
				return getResources().getColor(android.R.color.holo_orange_light);
			case 4:
				return getResources().getColor(android.R.color.holo_orange_dark);
			default:
				return getResources().getColor(android.R.color.holo_red_dark);

		}
	}

	private void animateTo(float end) {
		// Only act if the animator is not currently running. If the user's orientation changes
		// while the animator is running, we wait until the end of the animation to update the
		// display again, to prevent jerkiness.
		if (!mAnimator.isRunning()) {
			float start = mAnimatedHeading;
			float distance = Math.abs(end - start);
			float reverseDistance = 360.0f - distance;
			float shortest = Math.min(distance, reverseDistance);

			if (Float.isNaN(mAnimatedHeading) || shortest < MIN_DISTANCE_TO_ANIMATE) {

				mAnimatedHeading = end;
				invalidate();
			} else {

				float goal;

				if (distance < reverseDistance) {
					goal = end;
				} else if (end < start) {
					goal = end + 360.0f;
				} else {
					goal = end - 360.0f;
				}

				mAnimator.setFloatValues(start, goal);
				mAnimator.start();
			}
		}
	}

	/**
	 * Sets up a {@link ValueAnimator} that will be used to animate the compass
	 * when the distance between two sensor events is large.
	 */
	private void setupAnimator() {
		mAnimator.setInterpolator(new LinearInterpolator());
		mAnimator.setDuration(250);

		// Notifies us at each frame of the animation so we can redraw the view.
		mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				mAnimatedHeading = MathUtils.mod((Float)mAnimator.getAnimatedValue(), 360.0f);
				invalidate();
			}
		});

		// Notifies us when the animation is over. During an animation, the user's head may have
		// continued to move to a different orientation than the original destination angle of the
		// animation. Since we can't easily change the animation goal while it is running, we call
		// animateTo() again, which will either redraw at the new orientation (if the difference is
		// small enough), or start another animation to the new heading. This seems to produce
		// fluid results.
		mAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animator) {
				animateTo(heading);
			}
		});
	}
}
