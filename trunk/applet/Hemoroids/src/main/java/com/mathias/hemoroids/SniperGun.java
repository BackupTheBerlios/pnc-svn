package com.mathias.hemoroids;


public class SniperGun extends NoGun
{
	static final int SPEED = 30;
	static final int LENGTH = 400;
	static final int ACCURACY = 5;

	boolean ready=true;

	SniperGun(int x, int y)
	{
		super(x,y);
		shape.addPoint(-1 +x,0 +y);
		shape.addPoint(-1 +x,20 +y);
		shape.addPoint(1 +x,20 +y);
		shape.addPoint(1 +x,0 +y);
		buildSprite();
	}
	public int fire()
	{
		//fire shot
		if(away()<10 && ready)
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
