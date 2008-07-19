package com.mathias.games.dogfight.common.items;

public class Explosion extends AbstractItem implements TtlItem {

	private static final long serialVersionUID = -8239239276704489695L;

	public int counter;

	public Explosion(){
		this(0, 0, 0);
	}

	public Explosion(int x, int y, int counter){
		super("", 0, x, y, 0, 0, 0);
		this.counter = counter;
		this.key = toString();
		this.action = Action.ONGOING;
	}

	@Override
	public boolean decreaseTtl() {
		return counter-- <= 0;
	}

	@Override
	public int getTtl() {
		return counter;
	}

}
