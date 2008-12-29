package com.mathias.android.acast.common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SearchItem implements Serializable {

	private String title;
	private String description;
	private String uri;

	public SearchItem(String title, String description, String uri) {
		this.title = title;
		this.description = description;
		this.uri = uri;
	}

	@Override
	public String toString() {
		return title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
