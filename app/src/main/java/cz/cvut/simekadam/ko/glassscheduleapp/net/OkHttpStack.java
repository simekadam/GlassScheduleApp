package cz.cvut.simekadam.ko.glassscheduleapp.net;

/**
 * Created by simekadam on 20/04/14.
 */
import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

/**
 * An {@link com.android.volley.toolbox.HttpStack HttpStack} implementation which
 * uses OkHttp as its transport.
 */
public class OkHttpStack extends HurlStack {
	private final OkHttpClient client;

	public OkHttpStack() {
		this(new OkHttpClient());
	}

	public OkHttpStack(OkHttpClient client) {
		Log.d("prdel", "test");
		if (client == null) {
			throw new NullPointerException("Client must not be null.");
		}
		this.client = client;
	}

	@Override protected HttpURLConnection createConnection(URL url) throws IOException {
		Log.d("test", "connecting");
		return client.open(url);
	}
}
