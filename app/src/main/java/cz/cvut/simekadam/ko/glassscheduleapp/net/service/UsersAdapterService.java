package cz.cvut.simekadam.ko.glassscheduleapp.net.service;

import java.util.List;

import android.content.Context;

import cz.cvut.simekadam.ko.glassscheduleapp.model.entities.User;
import retrofit.RestAdapter;
import rx.Observable;

/**
 * Created by simekadam on 29/05/14.
 */
public class UsersAdapterService {

	private final UsersService mUsersService;

	public UsersAdapterService(Context context) {
		mUsersService = new RestAdapter.Builder()
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setEndpoint("http://api.randomuser.me/")
			.build()
			.create(UsersService.class);
	}

	public Observable<List<User>> getUserObservable(int count){
		return mUsersService.getUsers(count);
	}

}
