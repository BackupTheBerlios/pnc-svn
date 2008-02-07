package com.mathias.bellatetris;

public enum Images {

	FOOD("images/floor.gif"),
	GREEN("images/green.gif"),
	LEAF("images/leaf.gif"),
	PASTELL("images/pastell.gif"),
	STONE("images/stone.gif"),
	TIGER("images/tiger.gif"),
	WATER("images/water.gif");

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
