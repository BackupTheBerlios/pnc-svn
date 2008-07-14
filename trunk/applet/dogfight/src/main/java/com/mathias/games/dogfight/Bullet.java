package com.mathias.games.dogfight;

public class Bullet extends AbstractItem implements SolidItem, TtlItem {

	public static final char TYPE = 'B';
	
	public int counter = 130;

	public Bullet(){
		super();

		this.type = TYPE;
	}

	public Bullet(double angle, int x, int y, int speed) {
		super("", angle, x, y, 1, 1, speed);
		
		this.key = toString();
		this.type = TYPE;
	}

	@Override
	public String serialize() {
		return super.serialize();
	}

	@Override
	public Bullet deserialize(String[] split) {
		return (Bullet)super.deserialize(split);
	}

	public boolean decreaseTtl() {
		return counter-- <= 0;
	}

}
