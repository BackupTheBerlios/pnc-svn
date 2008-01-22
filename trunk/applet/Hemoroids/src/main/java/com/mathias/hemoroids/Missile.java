package com.mathias.hemoroids;

import java.awt.*;

public class Missile extends Flying
{
	private static final int MISSILE_SPEED = 7;
	private static final int MISSILE_DAMAGE = 500;
	private static final int MISSILE_TTL = 150;
	private static final int BOOST_TTL = MISSILE_TTL/5;

	//flamesshape
	private static final int[] fx={-4,0,4};//3
	private static final int[] fy={0,12,0};

  // Missle data.
  private int Counter;    // Counter for life of missle.
	private int BoostCounter;
	private Explosion exp;
	private Util flms;

	private double oldDeltaX;
	private double oldDeltaY;
	Flying targ;

  Missile()
  {
    // Create shape for the guided missle.
    shape.addPoint(0, -4);
    shape.addPoint(1, -3);
    shape.addPoint(1, 3);
    shape.addPoint(2, 4);
    shape.addPoint(-2, 4);
    shape.addPoint(-1, 3);
    shape.addPoint(-1, -3);
		buildSprite();
		exp=new Explosion(Color.orange);
		flms=new Util(fx,fy,3,Color.red);
		flms.scale(.5);
		flms.move(0,6);
  }
  public void init(Flying f,Flying t,boolean boostit)
  {
		targ=t;
		Audio.missile.loop();
    active = true;
    angle = f.angle;
    deltaAngle = 0.0;
    currentX = f.currentX;
    currentY = f.currentY;
    deltaX = MISSILE_SPEED * -Math.sin(angle);
    deltaY = MISSILE_SPEED *  Math.cos(angle);
    render();
		flms.update(this);
    Counter = MISSILE_TTL;
		if(boostit)
			BoostCounter = BOOST_TTL;
  }
  public void draw(Graphics g)
  {
		exp.draw(g);
		super.draw(g);
		flms.draw(g);
  }
  public void update()
  {
    int i;
    // Move the guided missle and check for collision with ship or photon. Stop it when its
    // counter has expired.
    if(active) 
		{
      if(--Counter <= 0)
        stop();
      else 
			{
				if(BoostCounter > 0)
					BoostCounter--;
				else
					if(targ!=null && targ.active)
					{
						guide(targ);
		        if(targ.isColliding(this)!=0 && active)
							stop();
					}
      }
    }
		exp.update();
		super.update();
		flms.update(this);
		oldDeltaX=deltaX;
		oldDeltaY=deltaY;
  }
  public void guide(Flying f)
  {
    double dx, dy, tangle;
    if (!f.active)
      return;
    // Find the angle needed to hit the ship.
    dx = f.currentX - currentX;
    dy = f.currentY - currentY;
    if (dx == 0 && dy == 0)
      tangle = 0;
    if (dx == 0) 
		{
      if (dy < 0)
        tangle = -Math.PI / 2;
      else
        tangle = Math.PI / 2;
    }
    else 
		{
      tangle = Math.atan(Math.abs(dy / dx));
      if (dy > 0)
        tangle = -tangle;
      if (dx < 0)
        tangle = Math.PI - tangle;
    }
    // Adjust angle for screen coordinates.
    angle = tangle - Math.PI / 2;
    // Change the missle's angle so that it points toward the ship.
    deltaX = deltaX * .9 + MISSILE_SPEED * -Math.sin(angle) * .1;
    deltaY = deltaY * .9 + MISSILE_SPEED *  Math.cos(angle) * .1;
  }
  public void stop()
  {
		Audio.missile.stop();
		exp.explode(this);
    active = false;
    Counter = 0;
  }
	public int isColliding(Flying f)
	{
		if(super.isColliding(f)!=0)
		{
			stop();
			return (int)(Math.random()*MISSILE_DAMAGE)+1;
		}
		return 0;
	}
}
