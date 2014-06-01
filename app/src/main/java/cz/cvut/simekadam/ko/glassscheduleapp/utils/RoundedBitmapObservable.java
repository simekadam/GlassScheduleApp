package cz.cvut.simekadam.ko.glassscheduleapp.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;

import cz.cvut.simekadam.ko.glassscheduleapp.R;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by simekadam on 20/04/14.
 */
public class RoundedBitmapObservable implements Observable.OnSubscribe<Bitmap> {

	private final int RADIUS = 70;
	private Bitmap mBitmap;
	private Context mContext;
	private String mInitials;

	private RoundedBitmapObservable(Context context, Bitmap bitmap, String initials) {
		mBitmap = bitmap;
		mContext = context;
		mInitials = initials;
	}


	public static Observable<Bitmap> renderRoundedBitmap(Context context, Bitmap bitmap, String initials){
		return Observable.create(new RoundedBitmapObservable(context, bitmap, initials));
	}

	@Override
	public void call(Subscriber<? super Bitmap> subscriber) {
		Bitmap croppedBitmap = getCroppedBitmap(mBitmap, RADIUS, mInitials);
		subscriber.onNext(croppedBitmap);
	}


	public Bitmap getCroppedBitmap(Bitmap bmp, int radius, String initials) {
		initials = initials.toUpperCase();
		Bitmap sbmp;
		int oldWidth = bmp.getWidth();
		int oldWHeight = bmp.getHeight();
		int smallerSize = (oldWidth < oldWHeight) ? oldWidth : oldWHeight;

		sbmp = Bitmap.createBitmap(bmp, (oldWidth - smallerSize) / 2, (oldWHeight - smallerSize) / 2, smallerSize, smallerSize);

		if (smallerSize != radius) {
			sbmp = Bitmap.createScaledBitmap(sbmp, radius, radius, true);

		} else
			sbmp = bmp;
		Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
			sbmp.getHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(output);
		canvas.drawColor(Color.TRANSPARENT);
		final Paint paint = new Paint();
		final Paint paint2 = new Paint();
		final Paint initialsPaint = new Paint();
		final Paint initialsShadowPaint = new Paint();


		final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint2.setAntiAlias(true);
		paint2.setFilterBitmap(true);
		paint2.setDither(true);
		paint2.setAlpha(1);

		paint2.setStrokeWidth(2);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		paint2.setColor(mContext.getResources().getColor(R.color.light_gray));
		paint2.setStyle(Paint.Style.STROKE);

		initialsShadowPaint.setAntiAlias(true);
		initialsPaint.setStyle(Paint.Style.FILL);
		initialsShadowPaint.setShader(new RadialGradient(sbmp.getWidth() / 2, sbmp.getHeight()/2, sbmp.getWidth() / 2 - 2, mContext.getResources().getColor(R.color.translucent_black), Color.TRANSPARENT, Shader.TileMode.CLAMP));


		Typeface fontRoboThin = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Thin.ttf");

		initialsPaint.setColor(Color.WHITE);
		initialsPaint.setStyle(Paint.Style.FILL);
		initialsPaint.setTextSize(40);
		initialsPaint.setAntiAlias(true);
		initialsPaint.setTypeface(fontRoboThin);



		canvas.drawBitmap(sbmp, rect, rect, paint);

		canvas.drawRect(rect, paint2);

		canvas.drawRect(rect, initialsShadowPaint);

		float measureText = initialsPaint.measureText(initials);

		canvas.drawText(initials, sbmp.getWidth() / 2 - measureText/2,sbmp.getHeight()/2 + 15, initialsPaint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawCircle(sbmp.getWidth() / 2, sbmp.getHeight() / 2,
			sbmp.getWidth() / 2 - 1, paint);
		return output;
	}
}



