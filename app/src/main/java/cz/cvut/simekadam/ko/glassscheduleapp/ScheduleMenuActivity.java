package cz.cvut.simekadam.ko.glassscheduleapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by simekadam on 25/04/14.
 */
public class ScheduleMenuActivity extends Activity {

	private MainActivity.CompassBinder mCompassService;
	private boolean mResumed;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof MainActivity.CompassBinder) {
				mCompassService = (MainActivity.CompassBinder)service;
				openOptionsMenu();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// Do nothing.
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(this, MainActivity.class), mConnection, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mResumed = true;
		openOptionsMenu();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mResumed = false;
	}

	@Override
	public void openOptionsMenu() {
		if (mResumed && mCompassService != null) {
			super.openOptionsMenu();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
//			case R.id.read_aloud:
//				mCompassService.readHeadingAloud();
//				return true;
//			case R.id.stop:
//				stopService(new Intent(this, CompassService.class));
//				return true;
//			default:
//				return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);

		unbindService(mConnection);

		// We must call finish() from this method to ensure that the activity ends either when an
		// item is selected from the menu or when the menu is dismissed by swiping down.
		finish();
	}
}
