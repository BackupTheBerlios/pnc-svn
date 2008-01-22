package com.mathias.hemoroids;


public class ShotGun extends NoGun
{
	static final int BURST = 10;
	static final int SPEED = 20;
	static final int LENGTH = 300;
	static final double ACCURACY = 0.3;

	ShotGun(int x, int y)
	{
		super(x,y);
		shape.addPoint(-2 +x,0 +y);
		shape.addPoint(-2 +x,10 +y);
		shape.addPoint(2 +x,10 +y);
		shape.addPoint(2 +x,0 +y);
		buildSprite();
	}
	public int fire()
	{
		int count=0;
		//fire shot
		if(!busy())
			for(int i=0;i<BURST;i++)
				if(super.fire(SPEED,LENGTH,ACCURACY))
					count++;
		return count;
	}
}
