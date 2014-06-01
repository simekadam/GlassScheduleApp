package cz.cvut.simekadam.ko.glassscheduleapp.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.android.volley.Cache;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import cz.cvut.simekadam.ko.glassscheduleapp.App;
import cz.cvut.simekadam.ko.glassscheduleapp.R;
import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.User;
import cz.cvut.simekadam.ko.glassscheduleapp.utils.RoundedBitmapObservable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by simekadam on 04/05/14.
 */
public class ScheduleFrameView extends View {
	private List<Bitmap> mImages;
	private HashMap<String, Integer> mPositions;
	private Paint mPhotoPaint;
	private Paint mLeftShadowPaint;
	private ImageLoader mImageLoader;

	private Context mContext;

	public ScheduleFrameView(Context context) {
		this(context, null, 0);
	}

	public ScheduleFrameView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScheduleFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		mPositions = new HashMap<>();
		mImages = new LinkedList<>();
		mPhotoPaint = new Paint();
		mImageLoader = App.getInstance().getImageLoader();
		mLeftShadowPaint = new Paint();
		mLeftShadowPaint.setShader(new LinearGradient(0, 0, 170, getHeight(), getResources().getColor(R.color.translucent_black), Color.TRANSPARENT, Shader.TileMode.MIRROR));


	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Bitmap bitmap;
		canvas.drawRect(0, 0, 170, canvas.getHeight(), mLeftShadowPaint);

		for (int i = 0; i < mImages.size();i++) {
				bitmap = mImages.get(i);
				if(bitmap != null){
					canvas.drawBitmap(bitmap, 15, 10 + i * 90, mPhotoPaint);
				}
		}
	}

	public void addUser(User user){
		if(user.getPicture()==null){
			return;
		}
		mPositions.put(user.getPicture(), mPositions.size());
		mImageLoader.get(user.getPicture(), new ProfileImageLoadedListener(user));

	}

	private void renderUserAvatar(Bitmap bitmap,final String requestUrl, User user) {
		String initials = user.getName().getFirst().substring(0,1)+user.getName().getLast().substring(0,1);
		Observable<Bitmap> bitmapObservable = RoundedBitmapObservable.renderRoundedBitmap(mContext, bitmap, initials);
		bitmapObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Bitmap>() {
			@Override
			public void call(Bitmap bitmap) {
				mImages.add(bitmap);

				invalidate();
			}
		}, new Action1<Throwable>() {
			@Override
			public void call(Throwable throwable) {
//				Log.d("test", throwable.getMessage());
			}
		});
	}

	private class ProfileImageLoadedListener implements ImageLoader.ImageListener {

		private User mUser;

		private ProfileImageLoadedListener(User user) {
			mUser = user;
		}

		@Override
		public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
			renderUserAvatar(response.getBitmap(), response.getRequestUrl(), mUser);
		}

		@Override
		public void onErrorResponse(VolleyError error) {

		}
	}

}
