package com.mathias.games.dogfight;

public class Bullet extends AbstractItem implements SolidItem, TtlItem {

	private static final long serialVersionUID = 6558931384068119068L;

	public static final char TYPE = 'B';
	
	public int counter = 130;

	public Bullet(){
		this(0, 0, 0, 0);
	}

	public Bullet(double angle, int x, int y, int speed) {
		super("", angle, x, y, 1, 1, speed);
		
		this.key = toString();
		this.action = Action.ONGOING;
	}

	public boolean decreaseTtl() {
		return counter-- <= 0;
	}

}
