package com.mathias.games.dogfight;

public class Explosion extends AbstractItem implements TtlItem {

	public int counter;

	public Explosion(){
	}

	public Explosion(int x, int y, int counter){
		super("", 0, x, y, 0, 0, 0);
		this.counter = counter;
		this.key = toString();
	}

	public boolean decreaseTtl() {
		return counter-- <= 0;
	}

}
