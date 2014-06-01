package cz.cvut.simekadam.ko.glassscheduleapp.model.entities;


/**
 * Created by simekadam on 20/04/14.
 */
public class User {

	private String gender;
	private Name name;
	private Location location;
	private String email;
	private String picture;

	public User(String gender, Name name, Location location, String email, String picture) {
		this.gender = gender;
		this.name = name;
		this.location = location;
		this.email = email;
		this.picture = picture;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("name: ").append(getName()).append("\n");
		sb.append("gender: ").append(getGender()).append("\n");
		sb.append("location: ").append(getLocation()).append("\n");
		sb.append("email: ").append(getEmail()).append("\n");
		sb.append("picture: ").append(getPicture()).append("\n");
		return sb.toString();
	}
}
