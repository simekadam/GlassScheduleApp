package cz.cvut.simekadam.ko.glassscheduleapp.model.entities;

import java.util.Date;

import android.graphics.Color;

/**
 * Created by simekadam on 20/04/14.
 */
public class Event {

	private int startDate;
	private int endDate;
	private String name;
	private int color;
	private int userId;
	private int id;

	public Event(Integer startDate, Integer endDate,int id, int userId, String name) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.name = name;
		this.color = Color.BLUE;
		this.userId = userId;
		this.id = id;
	}

	public int getStartHour() {
		return startDate;
	}

	public int getEndHour() {
		return endDate;
	}

	public String getName() {
		return name;
	}

	public int getColor() {
		return color;
	}

	public int getUserId() {
		return userId;
	}

	public int getId() {
		return id;
	}

	public void setStartDate(int startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(int endDate) {
		this.endDate = endDate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setId(int id) {
		this.id = id;
	}
}
