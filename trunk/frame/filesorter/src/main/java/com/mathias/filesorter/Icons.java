package com.mathias.filesorter;

import javax.swing.ImageIcon;

public enum Icons {

	FIND("find.gif"),
	REFRESH("refresh.gif");
	
	private String name;
	
	Icons(String name){
		this.name = name;
	}

	public ImageIcon getIcon() {
		return new ImageIcon(Icons.class.getResource("icons/" + name));
	}

}
