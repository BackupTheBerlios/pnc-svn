package com.mathias.android.searchwidget;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Engine implements Serializable {

	public long id;

	public String title;

	public String uri;

	public String icon;

	public int button;

	public String description;

	public Engine(String title, String uri, String icon, int button,
			String description) {
		this(Constants.INVALID_ID, title, uri, icon, button, description);
	}

	public Engine(long id, String title, String uri, String icon,
			int button, String description) {
		this.id = id;
		this.title = title;
		this.uri = uri;
		this.icon = icon;
		this.button = button;
		this.description = description;
	}

}
