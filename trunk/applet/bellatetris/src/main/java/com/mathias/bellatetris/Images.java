package com.mathias.bellatetris;

public enum Images {

	FOOD("images/floor.gif"),
	GREEN("images/green.gif"),
	LEAF("images/leaf.gif"),
	PASTELL("images/pastell.gif"),
	STONE("images/stone.gif"),
	TIGER("images/tiger.gif"),
	WATER("images/water.gif"),
	B("images/b.gif"),
	E("images/e.gif"),
	L("images/l.gif"),
	L2("images/l2.gif"),
	A("images/a.gif"),
	WALL("images/wall.gif"),
	BELLATETRIS("images/bellatetris_glow.gif");

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
