package com.mathias.games.dogfight;



public class Plane extends AbstractItem implements SolidItem {
	
	public static final char TYPE = 'P';

	public int planeindex = 0;

	public Plane(){
		super();
		this.type = TYPE;
	}

	public Plane(String name, double angle, int x, int y, int speed, int planeindex) {
		super(name, angle, x, y, 1, 1, speed);

		this.planeindex = planeindex;
		this.type = TYPE;
	}

	@Override
	public String serialize() {
		return super.serialize()+","+planeindex;
	}

	@Override
	public Plane deserialize(String[] split){
		super.deserialize(split);
		planeindex = Integer.parseInt(split[8]);
		return this;
	}

	@Override
	public String toString() {
		return serialize();
	}

}
