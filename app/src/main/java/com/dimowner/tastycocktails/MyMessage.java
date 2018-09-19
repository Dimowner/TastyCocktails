package com.dimowner.tastycocktails;

public class MyMessage {

	private String id;
	private String text;

	public MyMessage() {
	}

	public MyMessage(String text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "MyMessage{" +
				"id='" + id + '\'' +
				", text='" + text + '\'' +
				'}';
	}
}
