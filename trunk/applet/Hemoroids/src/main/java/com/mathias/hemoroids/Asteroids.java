package com.mathias.hemoroids;

import java.awt.Graphics;

public class Asteroids
{
  static final int MAX_ROCKS =  20;          // asteroids and explosions.
	static final int MAX_SPLIT = 2;
	static final int MIN_DAMAGE = 20;
	static final int MAX_DAMAGE = 20;

	public int Stones=5;
	public int index=0;

	Explosion exp;
	Stone[] aster;

	Asteroids()
	{
		aster=new Stone[MAX_ROCKS];
		exp=new Explosion();
    for(int i=0;i<MAX_ROCKS;i++)
			aster[i]=new Stone();
	}
	public Flying getNext()
	{
		int i;
    for(i=index+1;i<MAX_ROCKS;i++)
			if(aster[i].active)
			{
				index=i;
				return aster[i];
			}
    for(i=0;i<MAX_ROCKS;i++)//<index
			if(aster[i].active)
			{
				index=i;
				return aster[i];
			}
		return null;
	}
	public void init()
	{
		exp.init();
		stop();
    for(int i=0;i<Stones;i++)
			aster[i].init();
	}
  public void update()
  {
		int i,j;
		for(i=0;i<MAX_ROCKS-1;i++)
			for(j=i+1;j<MAX_ROCKS;j++)
				if(aster[i].isColliding(aster[j])!=0)
				{
					if(Math.abs(aster[i].deltaX)>Math.abs(aster[i].deltaY))
						aster[i].deltaX*=-1;
					else
						aster[i].deltaY*=-1;
					if(Math.abs(aster[j].deltaX)>Math.abs(aster[j].deltaY))
						aster[j].deltaX*=-1;
					else
						aster[j].deltaY*=-1;
					//advance away
					aster[i].currentX=aster[i].oldX;
					aster[i].currentY=aster[i].oldY;
					aster[j].currentX=aster[j].oldX;
					aster[j].currentY=aster[j].oldY;
				}
		for(i=0;i<MAX_ROCKS;i++)
			aster[i].update();
		exp.update();
  }
  public void draw(Graphics g)
  {
    for(int i=0;i<MAX_ROCKS;i++)
			aster[i].draw(g);
		exp.draw(g);
  }
	public void isColliding(Shield s)
	{
    for(int i=0;i<MAX_ROCKS;i++)
			if(s.isColliding(aster[i])!=0)
			{
				if(Math.abs(aster[i].deltaX)>Math.abs(s.deltaX))
					aster[i].deltaX=-aster[i].deltaX;
				else
					aster[i].deltaX=-s.deltaX;
				if(Math.abs(aster[i].deltaY)>Math.abs(s.deltaY))
					aster[i].deltaY=-aster[i].deltaY;
				else
					aster[i].deltaY=-s.deltaY;
				aster[i].currentX=aster[i].oldX+s.deltaX;
				aster[i].currentY=aster[i].oldY+s.deltaY;
			}
	}
	public boolean isColliding(Flying skepp)
	{
		int dam;
		boolean flag=false;
    for(int i=0;i<MAX_ROCKS;i++)
			
			if((dam=skepp.isColliding(aster[i]))!=0)
			{
				if(aster[i].dead(dam))
				{
					exp.explode(aster[i]);
					aster[i].stop();
					int s=0;
					for(int j=0;j<MAX_ROCKS && s<MAX_SPLIT;j++)
						if(j!=i)
							if(aster[j].init(aster[i]))
								s++;
				}
				else
					Audio.crash.play();
				flag=true;
			}
		return flag;
	}
	public void stop()
	{
    for(int i=0;i<MAX_ROCKS;i++)
			aster[i].stop();
	}
	public boolean busy()
	{
    for(int i=0;i<MAX_ROCKS;i++)
			if(aster[i].active)
				return true;
		return false;
	}
}
