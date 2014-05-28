package cz.cvut.simekadam.ko.glassscheduleapp.net.service;

import java.util.List;

import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.Event;
import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.User;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by simekadam on 20/04/14.
 */
public interface ScheduleService {


	@GET("/j/cdraSwieCq/")
	Observable<List<User>> getUsers();

	@POST("/j/cbpcnzVfKG/")
	Observable<List<Event>> getEvents();



}
