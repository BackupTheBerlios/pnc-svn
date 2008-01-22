package com.mathias.hemoroids;

import java.awt.*;

public class MissileShip extends GunShip
{
	private static final int MAX_MISSILES = 6;
	private static final int INIT_MISSILES = 2;

	Missile[] miss;
	int missiles=0;
	boolean missileactiveated=false;

	MissileShip()
	{
		miss=new Missile[MAX_MISSILES];
		for(int i=0;i<MAX_MISSILES;i++)
			miss[i]=new Missile();
	}
	public void init()
	{
		missileactiveated=false;
		missiles=INIT_MISSILES;
		super.init();
	}
	public void update()
	{
		for(int i=0;i<MAX_MISSILES;i++)
			miss[i].update();
		super.update();
	}
	public void draw(Graphics g)
	{
		for(int i=0;i<MAX_MISSILES;i++)
			miss[i].draw(g);
		super.draw(g);
	}
  public int isColliding(Flying f)
	{
		int dam;
		for(int i=0;i<MAX_MISSILES;i++)
			if((dam=miss[i].isColliding(f))!=0)
				return dam;
		return super.isColliding(f);
	}
	public void keyDown(Flying t)
	{
		if(!active)
			return;
		//
		if(Keys.z && active && missiles>0 && counter==0 && missileactiveated)
		{
			for(int i=0;i<MAX_MISSILES;i++)
				if(!miss[i].active)
				{
					miss[i].init(this,t,true);
					break;
				}
			missiles--;
		}
		//
		super.keyDown();
		//
		if(Keys.n5 && missiles>0)
		{
			missileactiveated=true;
			Audio.mount.play();
			cannon=new NoGun();
			cannon2=new NoGun();
//			cannon3=new NoGun();
		}
		if(Keys.n0||Keys.n1||Keys.n2||Keys.n3||Keys.n4)
			missileactiveated=false;
	}
}
