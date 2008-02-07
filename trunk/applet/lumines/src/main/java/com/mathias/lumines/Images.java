package com.mathias.lumines;

public enum Images {

	WOOD("images/wood.gif"),
	RAIN("images/rain.gif"),
	WOOD_FADE("images/wood_fade.gif"),
	RAIN_FADE("images/rain_fade.gif"),
	WALL("images/wall.gif"),
	LOADING("images/loading.gif"),
	GAMEOVER("images/gameover.gif"),
	MOON("images/moon.jpg");

	private final String filename;

	Images(String file){
		this.filename = file;
	}

	String getFilename(){
		return filename;
	}

	static int count(){
		return Images.values().length;
	}
}
