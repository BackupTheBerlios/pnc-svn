package com.mathias.hemoroids;


public class AutoGun extends NoGun
{
	static final int SPEED = 10;
	static final int LENGTH = 100;
	static final double ACCURACY = 0.5;

	AutoGun(int x, int y)
	{
		super(x,y);
		shape.addPoint(-1 +x,0 +y);
		shape.addPoint(-1 +x,10 +y);
		shape.addPoint(1 +x,10 +y);
		shape.addPoint(1 +x,0 +y);
		buildSprite();
		cr=150;
		cg=150;
		cb=150;
	}
	public int fire()
	{
		if(super.fire(SPEED,LENGTH,ACCURACY))
			return 1;
		return 0;
	}
}
