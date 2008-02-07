package com.mathias.lumines;

public enum Sounds {

	LOCK("audio/lock.au");

	private final String filename;

	Sounds(String file){
		this.filename = file;
	}

	String getFilename(){
		return filename;
	}

	static int count(){
		return Sounds.values().length;
	}

}
