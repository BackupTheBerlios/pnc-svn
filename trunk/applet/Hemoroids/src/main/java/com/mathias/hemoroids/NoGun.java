package com.mathias.hemoroids;

import java.awt.*;

public class NoGun extends Flying
{
  static final int MAX_SHOTS = 20;

	int[] gfx={-2,0,2};//3
	int[] gfy={0,-6,0};

	Photon[] shots;
	Util gunflame;

	int ownX,ownY;

	NoGun()
	{
		gunflame=new Util(gfx,gfy,3,Color.red);
    //photons
		shots=new Photon[MAX_SHOTS];
    for (int i=0;i<MAX_SHOTS;i++)
      shots[i]=new Photon();
		active=false;
	}
	NoGun(int x,int y)
	{
		ownX=x;
		ownY=y;
		gunflame=new Util(gfx,gfy,3,Color.red);
		gunflame.move(ownX,ownY);
    //photons
		shots=new Photon[MAX_SHOTS];
    for (int i=0;i<MAX_SHOTS;i++)
      shots[i]=new Photon();
		active=true;
	}
	public void init()
	{
		gunflame.move(ownX,ownY);
		gunflame.update(this,true);
		for(int i=0;i<MAX_SHOTS;i++)
			shots[i].init();
		active=true;
	}
	public void update(Flying f)
	{
		//sprite
    angle = f.angle;
    deltaAngle = f.deltaAngle;
    currentX = f.currentX;
    currentY = f.currentY;
    deltaX = f.deltaX;
    deltaY = f.deltaY;
		//update values
		super.update(false);
		//gunflame
		gunflame.update(this);
		gunflame.fadeout(40);
		//shots
		for(int i=0;i<MAX_SHOTS;i++)
			shots[i].update();
	}
	public void draw(Graphics g)
	{
		gunflame.draw(g);
		//shots
		for(int i=0;i<MAX_SHOTS;i++)
			shots[i].draw(g);
		super.draw(g);
	}
  public int isColliding(Flying f)
	{
		int dam;
		for(int i=0;i<MAX_SHOTS;i++)
			if((dam=shots[i].isColliding(f))!=0)
			{
				shots[i].stop();
				return dam;
			}
		return 0;
	}
	public boolean busy()
	{
		for(int i=0;i<MAX_SHOTS;i++)
			if(shots[i].active)
				return true;
		return false;
	}
	public int fire()
	{
		return 0;
	}
	public boolean fire(int speed,int length,double accuracy)
	{
		//fire shot
		if(active)
			for(int i=0;i<MAX_SHOTS;i++)
				if(!shots[i].active)
				{
					gunflame.fadein(220);
					shots[i].fire(this,speed,length,accuracy);
					return true;
				}
		return false;
	}
	public int away()
	{
		int j=0;
		for(int i=0;i<MAX_SHOTS;i++)
			if(shots[i].active)
				j++;
		return j;
	}
	public void stop()
	{
		for(int i=0;i<MAX_SHOTS;i++)
			shots[i].stop();
		super.stop();
	}
	public void explode()
	{
		gunflame.explode();
    for (int i=0;i<MAX_SHOTS;i++)
      shots[i].explode();
		super.explode();
	}
}
