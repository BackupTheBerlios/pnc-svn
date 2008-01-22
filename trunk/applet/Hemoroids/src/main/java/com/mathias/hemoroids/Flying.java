package com.mathias.hemoroids;

import java.awt.*;
import java.net.*;
import java.util.*;

public class Flying
{
	static final int FADEFREQ = 10;
	static final double DEPART_SPEED = 10;

  static int width;   // Dimensions of the graphics area.
  static int height;

  public boolean active;                // Active flag.
  public double  currentX, currentY;    // Current position on screen.
  public double  oldX, oldY;    // Current position on screen.

	protected int cr;
	protected int cg;
	protected int cb;
	protected int fadeadd;
	protected boolean fade_in;
	protected int fr;
	protected int fg;
	protected int fb;
  protected Polygon shape;                // Initial sprite shape, centered at the origin (0,0).
  protected double  angle;                // Current angle of rotation.
  protected double  deltaAngle;           // Amount to change the rotation angle.
  protected double  deltaX, deltaY;       // Amount to change the screen position.
  protected Polygon sprite;               // Final location and shape of sprite after applying rotation and
																				// moving to screen position. Used for drawing on the screen and
																				// in detecting collisions.
  Flying()
  {
    active = false;
		cr=255;
		cg=255;
		cb=255;
		fadeadd=FADEFREQ;
		fade_in=true;
		fr=0;
		fg=0;
		fb=0;
    shape = new Polygon();
    angle = 0.0;
    deltaAngle = 0.0;
    currentX = 0.0;
    currentY = 0.0;
    deltaX = 0.0;
    deltaY = 0.0;
    sprite = new Polygon();
  }
  Flying(Color cc)
  {
    active = false;
		cr=cc.getRed();
		cg=cc.getGreen();
		cb=cc.getBlue();
		fadeadd=FADEFREQ;
		fade_in=true;
		fr=0;
		fg=0;
		fb=0;
    shape = new Polygon();
    angle = 0.0;
    deltaAngle = 0.0;
    currentX = 0.0;
    currentY = 0.0;
    deltaX = 0.0;
    deltaY = 0.0;
    sprite = new Polygon();
  }
	public void update()
	{
		if(active)
			if(fade_in)
			{
				if(fr<cr)
					if((fr+=fadeadd)>255)
						fr=255;
				if(fg<cg)
					if((fg+=fadeadd)>255)
						fg=255;
				if(fb<cb)
					if((fb+=fadeadd)>255)
						fb=255;
			}	
			else
			{
				if(fr>0)
					if((fr-=fadeadd)<0)
						fr=0;
				if(fg>0)
					if((fg-=fadeadd)<0)
						fg=0;
				if(fb>0)
					if((fb-=fadeadd)<0)
						fb=0;
				if(fr==0 && fg==0 && fb==0)
					active=false;
			}	
		advance();
		render();
	}
	public void update(boolean adv)
	{
		if(active)
			if(fade_in)
			{
				if(fr<cr)
					if((fr+=fadeadd)>255)
						fr=255;
				if(fg<cg)
					if((fg+=fadeadd)>255)
						fg=255;
				if(fb<cb)
					if((fb+=fadeadd)>255)
						fb=255;
			}	
			else
			{
				if(fr>0)
					if((fr-=fadeadd)<0)
						fr=0;
				if(fg>0)
					if((fg-=fadeadd)<0)
						fg=0;
				if(fb>0)
					if((fb-=fadeadd)<0)
						fb=0;
				if(fr==0 && fg==0 && fb==0)
					active=false;
			}	
		if(adv)
			advance();
		render();
	}
  public void draw(Graphics g)
  {
		if(active)
		{
			g.setColor(new Color(fr, fg, fb));
			g.fillPolygon(sprite);
		}
  }
	public void scale(double size)
	{
		for(int i=0;i<shape.npoints;i++)
		{
			shape.xpoints[i]*=size;
			shape.ypoints[i]*=size;
		}
	}
	public void move(int x, int y)
	{
		for(int i=0;i<shape.npoints;i++)
		{
			shape.xpoints[i]+=x;
			shape.ypoints[i]+=y;
		}
	}
  public void advance()
  {
    angle += deltaAngle;
    if(angle < 0)
      angle += 2 * Math.PI;
    if(angle > 2 * Math.PI)
      angle -= 2 * Math.PI;
    currentX += deltaX;
    if(currentX < -width / 2)
      currentX += width;
    if(currentX > width / 2)
      currentX -= width;
    currentY -= deltaY;
    if(currentY < -height / 2)
      currentY += height;
    if(currentY > height / 2)
      currentY -= height;
		oldX=currentX;
		oldY=currentY;
  }
  public void render()
  {
		sprite = new Polygon();
		for (int i = 0; i < shape.npoints; i++)
			sprite.addPoint((int) Math.round(shape.xpoints[i] * Math.cos(angle) + shape.ypoints[i] * Math.sin(angle)) + (int) Math.round(currentX) + width / 2,
                 (int) Math.round(shape.ypoints[i] * Math.cos(angle) - shape.xpoints[i] * Math.sin(angle)) + (int) Math.round(currentY) + height / 2);
  }
/*  public void render()
  {
		for (int i = 0; i < shape.npoints; i++)
		{
			sprite.xpoints[i]=(int) (Math.round(shape.xpoints[i] * Math.cos(angle) + shape.ypoints[i] * Math.sin(angle)) + Math.round(currentX) + width / 2);
			sprite.ypoints[i]=(int) (Math.round(shape.ypoints[i] * Math.cos(angle) - shape.xpoints[i] * Math.sin(angle)) + Math.round(currentY) + height / 2);
		}
  }*/
	public void buildSprite()
	{
/*		sprite=new Polygon();
		for(int i = 0; i < shape.npoints; i++)
			sprite.addPoint(0,0);
		render();*/
	}
  public int isColliding(Flying f)
  {
    int i;
		if(!active || !f.active)
			return 0;
    for (i = 0; i < f.sprite.npoints; i++)
      if (sprite.inside(f.sprite.xpoints[i], f.sprite.ypoints[i]))
        return 1;
    for (i = 0; i < sprite.npoints; i++)
      if (f.sprite.inside(sprite.xpoints[i], sprite.ypoints[i]))
        return 1;
    return 0;
  }
	public void stop()
	{
		active=false;
	}
	public void fadein()
	{
		fade_in=true;
		fadeadd=FADEFREQ;
		fr=0;
		fg=0;
		fb=0;
		active=true;
	}
	public void fadein(int fa)
	{
		fade_in=true;
		fadeadd=fa;
	}
	public void fadeout()
	{
		fr=cr;
		fg=cg;
		fb=cb;
		fade_in=false;
		fadeadd=FADEFREQ;
	}
	public void fadeout(int fa)
	{
		fade_in=false;
		fadeadd=fa;
	}
	public void keyDown()
	{
	}
	public void keyUp()
	{
	}
	public void explode()
	{
		deltaX=Math.random()*DEPART_SPEED-DEPART_SPEED;
		deltaY=Math.random()*DEPART_SPEED-DEPART_SPEED;
    deltaAngle = (Math.random() * 2 * Math.PI - Math.PI) / 15;
		fadeout();
	}
	public void toColor()
	{
		fr=cr;
		fg=cg;
		fb=cb;
	}
	public double getSpeed()
	{
		return Math.sqrt(deltaX*deltaX+deltaY*deltaY);
	}
}

