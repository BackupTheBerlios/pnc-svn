package com.mathias.hemoroids;

import java.awt.*;

public class Photon extends Flying
{
  static final int PHOTON_SPEED = 10;
  static final int PHOTON_SIZE  = 20;
	static final int DIVERSION = 2;
	static final int PHOTON_DAMAGE = 20;
  // Photon data.
  static int Index;                           // Next available photon sprite.

  int Counter;    // Time counter for life of a photon.

  Photon()
  {
    // Create shape for the photon sprites.
    shape.addPoint(1, 1);
    shape.addPoint(1, -1);
    shape.addPoint(-1, 1);
    shape.addPoint(-1, -1);
		buildSprite();
  }
  public void draw(Graphics g)
  {
		if(active)
		{
			g.setColor(Color.green);
			g.drawPolygon(sprite);
		}
  }
  public void init()
  {
    active = false;
  }
  public void update()
  {
    // Move any active photons. Stop it when its counter has expired.
		if(active)
    {
			super.update();
			if(--Counter < 0)
				active = false;
		}
  }
  public void fire(NoGun g,int speed,int lenght,double accuracy)
  {
    currentX = g.currentX+(g.ownX*Math.cos(g.angle)+g.ownY*Math.sin(g.angle));
    currentY = g.currentY+(g.ownY*Math.cos(g.angle)-g.ownX*Math.sin(g.angle));
		deltaX = speed * -Math.sin(g.angle)+(Math.random()-.5)/accuracy+g.deltaX;
		deltaY = speed *  Math.cos(g.angle)+(Math.random()-.5)/accuracy+g.deltaY;
		Counter = lenght / speed;
    active = true;
		Audio.fire.play();
  }
	public void explode(Flying f)
	{
		currentX=f.currentX;
		currentY=f.currentY;
		super.explode();
		active=true;
	}
	public int isColliding(Flying f)
	{
		if(super.isColliding(f)!=0)
			return (int)(Math.random()*PHOTON_DAMAGE)+1;
		return 0;
	}
}

