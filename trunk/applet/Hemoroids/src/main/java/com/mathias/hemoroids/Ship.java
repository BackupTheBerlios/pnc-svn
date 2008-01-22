package com.mathias.hemoroids;

import java.awt.*;

public class Ship extends Flying
{
	private static final int INIT_SHIELDS = 1;
	private static final int INIT_HYPERJUMPS = 1;
	private static final int START_COUNT = 25;
	private static final int SHIP_DAMAGE = 100;
	private static final int FLAME_DAMAGE = 50;

	//afterburnerflames shape
	private static final int[] fx={-4,0,4};//3
	private static final int[] fy={0,12,0};

  // Ship shape
	private static final int[] sx={0,-5,-5,-30,-30,-5,-5,-14,-14,-5,-5,5,5,14,14,5,5,30,30,5,5};//21
	private static int[] sy={-24,-14,-6,0,8,8,13,17,22,22,24,22,22,17,13,8,8,0,-6,-14};

	//cockpit shape
	private static final int[] cx={0,-4,-4,0,4,4};//6
	private static final int[] cy={6,2,-2,-6,-2,2};

	private Util flms;
	private Util cockpit;
	public Shield shield;
	private Explosion exp;
	public boolean dead;
	public int shields;
	public int hyperjumps;
	int counter;

  Ship()
  {
		//color
		cr=0;
		cg=0;
		cb=255;
    // Create shape for the ship sprite.
		//aeroplane
//		shape=new Polygon(sx,sy,21);
//		buildSprite();
    shape.addPoint(0, -24);
    shape.addPoint(-5, -14);
    shape.addPoint(-5, -6);
    shape.addPoint(-30, 0);
    shape.addPoint(-30, 8);
    shape.addPoint(-5, 8);
    shape.addPoint(-5, 13);
    shape.addPoint(-14, 17);
    shape.addPoint(-14, 22);
    shape.addPoint(-5, 22);
    shape.addPoint(-5, 24);
    shape.addPoint(5, 24);
    shape.addPoint(5, 22);
    shape.addPoint(14, 22);
    shape.addPoint(14, 17);
    shape.addPoint(5, 13);
    shape.addPoint(5, 8);
    shape.addPoint(30, 8);
    shape.addPoint(30, 0);
    shape.addPoint(5, -6);
    shape.addPoint(5, -14);
		buildSprite();
		scale(.5);

		cockpit=new Util(cx,cy,6,Color.white);
		cockpit.scale(.6);
		flms=new Util(fx,fy,3,Color.red);
		flms.move(0,12);
		shield=new Shield();
		exp=new Explosion(Color.blue);
  }
  public void init()
  {
    angle = 0.0;
    deltaAngle = 0.0;
    currentX = 0.0;
    currentY = 0.0;
    deltaX = 0.0;
    deltaY = 0.0;
    render();
		fadein();
		cockpit.update(this);
		flms.update(this);
		shield.update(this);
		exp.init();
		dead=false;
		shields=INIT_SHIELDS;
		hyperjumps=INIT_HYPERJUMPS;
		counter=START_COUNT;
    active=true;
  }

  public void update()
  {
    double dx, dy, limit;

		if(counter>0)
			counter--;
    // Rotate the ship if left or right cursor key is down.
    if (Keys.left)
    {
      angle += Math.PI / 13.0;
      if (angle > 2 * Math.PI)
        angle -= 2 * Math.PI;
    }
    if (Keys.right)
    {
      angle -= Math.PI / 13.0;
      if (angle < 0)
        angle += 2 * Math.PI;
    }
    // Fire thrusters if up or down cursor key is down. Don't let ship go past
    // the speed limit.
    dx = -Math.sin(angle);
    dy =  Math.cos(angle);
    limit = 0.8 * 20;
    if(Keys.up)
		{
      if (deltaX + dx > -limit && deltaX + dx < limit)
        deltaX += dx;
      if (deltaY + dy > -limit && deltaY + dy < limit)
        deltaY += dy;
			flms.active=true;
    }
		else
			flms.active=false;

    if(Keys.down)
		{
      deltaX/=1.3;
			deltaY/=1.3;
    }
    // Move the ship
		super.update();
		cockpit.update(this);
		flms.update(this);
		exp.update();
		shield.update(this);
		if(shield.active)
			counter=1;
  }
  public void hyperjump()
  {
		Audio.warp.play();
    currentX = Math.random() * width;
    currentY = Math.random() * height;
		fadein();
  }
  public void draw(Graphics g)
  {
		shield.draw(g);
		super.draw(g);
		cockpit.draw(g);
		flms.draw(g);
		exp.draw(g);
  }
  public int isColliding(Flying f)
	{
		int dam;
		if(super.isColliding(f)!=0)
		{
			//ship collided
			if(counter>0)
				return 0;
			exp.explode(this);
			exp.explode(cockpit);
			exp.explode(flms);
//			explode();
			dead=true;
			stop();
			return (int)(Math.random()*SHIP_DAMAGE)+1;
		}
		if(flms.isColliding(f)!=0 && counter==0)
			return (int)(Math.random()*FLAME_DAMAGE)+1;
		return 0;
	}
	public void stop()
	{
		Audio.thrusters.stop();
		super.stop();
	}
	public void fadein()
	{
		cockpit.fadein();
		super.fadein();
	}
	public void keyDown()
	{
		//hyperjump
		if(Keys.space && active)
			if(hyperjumps>0)
			{
				hyperjumps--;
				hyperjump();
			}
		//shield
		if(Keys.s && active)
			if(!shield.active)
				if(shields>0)
				{
					shields--;
					shield.init();
				}
		//
		if(Keys.up && active)
		{
			Audio.thrusters.loop();
			flms.fadein(30);
		}
	}
	public void keyUp()
	{
		//
		if(!Keys.up || !active)
		{
			Audio.thrusters.stop();
			flms.fadeout(30);
		}
	}
	public void explode()
	{
		flms.explode();
		cockpit.explode();
		super.explode();
	}
	public void matchSpeed(Flying f)
	{
		if(getSpeed()>f.getSpeed())
		{
			while(getSpeed()>f.getSpeed() && getSpeed()>1)
			{
				deltaX/=1.3;
				deltaY/=1.3;
			}
		}
		else
		{
			while(getSpeed()<f.getSpeed() && getSpeed()>1)
			{
				deltaX*=1.3;
				deltaY*=1.3;
			}
		}
	}
}
