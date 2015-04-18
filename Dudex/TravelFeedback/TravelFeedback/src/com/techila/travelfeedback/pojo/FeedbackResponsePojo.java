package com.techila.travelfeedback.pojo;

public class FeedbackResponsePojo {
	
	String userName;
	String date;
	String comment;
	String rating;
	String veh_number;
	String formID;
	
	public String getFormID() {
		return formID;
	}
	public void setFormID(String formID) {
		this.formID = formID;
	}
	public String getVeh_number() {
		return veh_number;
	}
	public void setVeh_number(String veh_number) {
		this.veh_number = veh_number;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	
}
