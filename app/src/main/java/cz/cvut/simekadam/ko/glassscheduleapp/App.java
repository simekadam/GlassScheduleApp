package cz.cvut.simekadam.ko.glassscheduleapp;

import android.app.Application;
import android.content.Context;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;
import cz.cvut.simekadam.ko.glassscheduleapp.net.OkHttpStack;
import cz.cvut.simekadam.ko.glassscheduleapp.utils.BitmapLruCache;

/**
 * Created by simekadam on 20/04/14.
 */
public class App extends Application{


	private static ImageLoader mImageLoader;
	private static App sInstance;

	public static App getInstance() {
		if(sInstance == null) {
			sInstance = new App();
		}
		return sInstance;
	}

	public App() {
		super();
		sInstance = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
	}

	public ImageLoader getImageLoader(){
		if(mImageLoader == null){
			mImageLoader = new ImageLoader(Volley.newRequestQueue(getApplicationContext(), new OkHttpStack(new OkHttpClient())),  new BitmapLruCache(1024));
		}
		return mImageLoader;
	}

}
