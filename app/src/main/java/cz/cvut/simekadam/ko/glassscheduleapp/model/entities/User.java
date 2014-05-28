package cz.cvut.simekadam.ko.glassscheduleapp.model.entities;

import java.util.List;

/**
 * Created by simekadam on 20/04/14.
 */
public class User {

	private String name;
	private String avatarUrl;
	private String initials;
	private int id;


	public User(String name,String initials, String avatarUrl, int id) {
		this.initials = initials;
		this.name = name;
		this.avatarUrl = avatarUrl;
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}


	public String getAvatarUrl() {
		return avatarUrl;
	}

	public int getId() {
		return id;
	}
}
