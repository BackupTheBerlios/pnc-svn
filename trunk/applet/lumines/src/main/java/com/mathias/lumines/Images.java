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

	private final String file;

	Images(String file){
		this.file = file;
	}

	String getFile(){
		return file;
	}

	static int count(){
		return Images.values().length;
	}
}
