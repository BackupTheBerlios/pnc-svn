package com.mathias.games.dogfight;

public class Explosion extends AbstractItem implements TtlItem {
	
	public static final char TYPE = 'E';

	public int counter;

	public Explosion(){
		this.type = TYPE;
	}

	public Explosion(int x, int y, int counter){
		super("", 0, x, y, 0, 0, 0);
		this.counter = counter;
		this.key = toString();
		this.type = TYPE;
	}

	@Override
	public String serialize() {
		return super.serialize()+","+counter;
	}

	@Override
	public Explosion deserialize(String[] split) {
		super.deserialize(split);
		if(split.length != 7){
			return null;
		}
		counter = Integer.parseInt(split[6]);
		return this;
	}

	public boolean decreaseTtl() {
		return counter-- <= 0;
	}

}
