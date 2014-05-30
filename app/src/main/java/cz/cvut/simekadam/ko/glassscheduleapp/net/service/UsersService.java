package cz.cvut.simekadam.ko.glassscheduleapp.net.service;

import java.util.List;

import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.User;
import retrofit.http.Field;
import retrofit.http.GET;
import rx.Observable;

/**
 * Created by simekadam on 29/05/14.
 */
public interface UsersService {

	@GET("/")
	Observable<List<User>> getUsers(@Field("results") int countOfResults);

}
