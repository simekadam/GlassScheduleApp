package cz.cvut.simekadam.ko.glassscheduleapp.net.service;

import java.util.List;

import android.content.Context;

import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.Event;
import retrofit.RestAdapter;
import rx.Observable;

/**
 * Created by simekadam on 20/04/14.
 */
public class RestAdapterService{

	private ScheduleService mScheduleService;

	public RestAdapterService(Context context) {
		mScheduleService = new RestAdapter.Builder()
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setEndpoint("http://h3t.cz/glass/")
			.build()
			.create(ScheduleService.class);
	}


	public Observable<List<Event>> getEventsObservable(){
		return mScheduleService.getEvents();
	}


}
