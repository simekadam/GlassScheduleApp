package cz.cvut.simekadam.ko.glassscheduleapp.views;

import android.content.Context;
import android.graphics.*;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import cz.cvut.simekadam.ko.glassscheduleapp.App;
import cz.cvut.simekadam.ko.glassscheduleapp.R;

/**
 * Created by simekadam on 8/7/13.
 */
public class RoundedNetworkImageView extends NetworkImageView {

    static RoundedBitmapCache renderCache = new RoundedBitmapCache(4 * 1024 * 1024);
    private Bitmap roundedBitmap;
    private String mUrl;
    private boolean isLoading = false;
    private boolean useCache;


    private RenderTask renderTask;

    public RoundedNetworkImageView(Context context) {
        this(context, null, 0);
    }

    public RoundedNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        useCache = true;
    }

    public void disableCache() {
        this.useCache = false;
    }

    public void enableCache() {
        this.useCache = true;
    }




    public RenderTask getRenderTask() {
        return renderTask;
    }

    public Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
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

        final Paint paint = new Paint();
        final Paint paint2 = new Paint();

        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint2.setAntiAlias(true);
        paint2.setFilterBitmap(true);
        paint2.setDither(true);
        paint2.setAlpha(1);

        paint2.setStrokeWidth(4);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.WHITE);
        paint2.setColor(getContext().getResources().getColor(R.color.light_gray));
        paint2.setStyle(Paint.Style.STROKE);

//        canvas.drawOval(new RectF(0, 0, sbmp.getWidth()+4, sbmp.getHeight()+4), paint2);
        canvas.drawCircle(sbmp.getWidth() / 2, sbmp.getHeight() / 2,
                sbmp.getWidth() / 2 - 1, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);
        canvas.drawCircle(sbmp.getWidth() / 2, sbmp.getHeight() / 2,
                sbmp.getWidth() / 2 - 2, paint2);


        return output;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void setImageUrl(String url, ImageLoader imageLoader) {
        if (url.equals(mUrl) && isLoading) return;

        mUrl = url;
        if (renderTask != null) renderTask.cancel(true);

        if (url.equals("")) {
            super.setImageUrl(url, imageLoader);
            return;
        }
        Bitmap roundedBitmap = null;
        if (useCache) {
            roundedBitmap = RoundedNetworkImageView.getRenderCache().get(url);
        }
        if (roundedBitmap != null) {
            setSuperImageBitmap(roundedBitmap);
        } else {

            super.setImageUrl(url, imageLoader);
        }


    }

    public void setCachedImageUrl(String url, int resid) {
        if (url.equals(mUrl)) return;
        mUrl = url;
        if (isLoading) renderTask.cancel(true);

        super.setImageUrl("", App.getInstance().getImageLoader());

        Bitmap roundedBitmap = RoundedNetworkImageView.getRenderCache().get(url);

        if (roundedBitmap != null) {
            setSuperImageBitmap(roundedBitmap);
        } else {
            setImageResource(resid);
        }

    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        // if this is used frequently, may handle bitmaps explicitly
        // to reduce the intermediate drawable object
        if (bm == null) {

        } else {

            renderTask = new RenderTask();

            renderTask.execute(new ParamHolder(bm, this, mUrl));
            this.isLoading = true;
        }
    }

    public void setSuperImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
    }

    public static RoundedBitmapCache getRenderCache() {
        return RoundedNetworkImageView.renderCache;
    }

    public boolean isLoading() {
        return isLoading;
    }


    class ParamHolder {

        Bitmap bitmap;
        RoundedNetworkImageView networkImageView;
        String key;

        ParamHolder(Bitmap bitmap, RoundedNetworkImageView networkImageView, String key) {
            this.bitmap = bitmap;
            this.networkImageView = networkImageView;
            this.key = key;
        }
    }

    private class RenderTask extends AsyncTask<ParamHolder, Void, ParamHolder> {
        @Override
        protected ParamHolder doInBackground(ParamHolder... params) {

            Bitmap b = params[0].bitmap;
            if (b == null) {
                return params[0];
            }

            Bitmap bitmap = b.copy(Bitmap.Config.RGB_565, true);

            int w = getWidth(), h = getHeight();

            if (w == 0) {
                params[0].bitmap = null;

            } else {
                Bitmap roundBitmap = getCroppedBitmap(bitmap, w);
                params[0].bitmap = roundBitmap;
            }

            return params[0];
        }

        @Override
        protected void onPostExecute(final ParamHolder paramHolder) {
            if (paramHolder.bitmap == null || paramHolder.key == null) return;
            if (useCache) {
                RoundedNetworkImageView.getRenderCache().put(paramHolder.key, paramHolder.bitmap);
            } else {
                useCache = true;
            }
            if (mUrl.equals(paramHolder.key)) {
                final ImageView imageView = paramHolder.networkImageView;
                imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        imageView.setScaleX(0);
                        imageView.setScaleY(0);

                        imageView.animate().scaleX(1).scaleY(1).setInterpolator(new BounceInterpolator()).setDuration(300).start();
                        imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
                paramHolder.networkImageView.setSuperImageBitmap(paramHolder.bitmap);

            }

        }


    }

    public void cancel() {
        if (renderTask != null)
            this.renderTask.cancel(true);
        isLoading = false;
    }

    private static class RoundedBitmapCache extends LruCache<String, Bitmap> {


        public RoundedBitmapCache(int maxSize) {
            super(maxSize);

        }


    }


}
