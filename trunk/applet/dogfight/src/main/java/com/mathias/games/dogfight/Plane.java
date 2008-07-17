package com.mathias.games.dogfight;

public class Plane extends AbstractItem implements SolidItem {

	private static final long serialVersionUID = 636309050285741622L;

	public int planeindex = 0;

	public Plane(){
		super();
	}

	public Plane(String name, double angle, int x, int y, int speed, int planeindex) {
		super(name, angle, x, y, 1, 1, speed);

		this.planeindex = planeindex;
	}

}
