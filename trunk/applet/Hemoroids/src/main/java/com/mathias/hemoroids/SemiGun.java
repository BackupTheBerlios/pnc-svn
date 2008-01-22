package com.mathias.hemoroids;


public class SemiGun extends NoGun
{
	static final int SPEED = 8;
	static final int LENGTH = 120;
	static final double ACCURACY = .5;

	boolean ready=true;

	SemiGun(int x, int y)
	{
		super(x,y);
		shape.addPoint(-1 +x,0 +y);
		shape.addPoint(-1 +x,10 +y);
		shape.addPoint(1 +x,10 +y);
		shape.addPoint(1 +x,0 +y);
		buildSprite();
	}
	public int fire()
	{
		//fire shot
		if(ready)
		{
			if(super.fire(SPEED,LENGTH,ACCURACY))
			{
				ready=false;
				return 1;
			}
		}
		return 0;
	}
	public void update(Flying f)
	{
		if(Keys.z==false)
			ready=true;
		super.update(f);
	}
}
