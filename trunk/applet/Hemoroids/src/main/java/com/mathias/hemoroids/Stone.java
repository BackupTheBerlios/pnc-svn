package com.mathias.hemoroids;

import java.awt.*;

public class Stone extends Flying
{
  static final int MIN_ROCK_SIDES =  8;     // Asteroid shape and size ranges.
  static final int MAX_ROCK_SIDES = 12;
  static final int MIN_ROCK_SIZE  = 20;
  static final int MAX_ROCK_SIZE  = 40;
  static final int MIN_ROCK_COLOR  = 50;
  static final int MAX_ROCK_COLOR  = 150;

	static final int BIG = 0;
	static final int MEDIUM = 1;
	static final int LITTLE = 2;

	static final int BIG_SHIELD = 100;
	static final int MEDIUM_SHIELD = 60;
	static final int LITTLE_SHIELD = 10;

	static final double BIG_SIZE = 1;
	static final double MEDIUM_SIZE = .6;
	static final double LITTLE_SIZE = .3;
	static final double MIN_EXPLOSION_SIZE = .03;
	static final double MAX_EXPLOSION_SIZE = .1;

	static final int SPOTS = 3;

  // Asteroid data.
  static int Speed;    // Asteroid speed.


	Tank[] tnk;
	DamageText dmge;
	double shield,maxshield;
	public int size;
	boolean bounced;

	Stone()
	{
		bounced=false;
		dmge=new DamageText();
//		tnk=new Tank[SPOTS];
		int c=MIN_ROCK_COLOR+(int)(Math.random()*(MAX_ROCK_COLOR-MIN_ROCK_COLOR));
		cr=c;cg=c;cb=c;
	}
	Stone(Color cc)
	{
		bounced=false;
		dmge=new DamageText();
//		tnk=new Tank[SPOTS];
		cr=cc.getRed()-(int)Math.random()*100;
		cg=cc.getGreen()-(int)Math.random()*100;
		cb=cc.getBlue()-(int)Math.random()*100;
	}
	public void init()
	{
		if(active)
			return;
		size=BIG;
		shield=BIG_SHIELD;
		maxshield=BIG_SHIELD;
		build(BIG_SIZE);
    // Place the asteroid at one edge of the screen.
    if(Math.random() < 0.5) 
		{
			currentX = -width / 2;
      if(Math.random() < 0.5)
				currentX = width / 2;
			currentY = Math.random() * height;
		}
    else 
		{
			currentX = Math.random() * width;
      currentY = -height / 2;
      if (Math.random() < 0.5)
				currentY = height / 2;
		}
		update();
		active = true;
	}
	public boolean init(Stone f)
	{
		if(active)
			return false;
		switch(f.size)
		{
		case BIG:
			size=MEDIUM;
			shield=MEDIUM_SHIELD;
			maxshield=MEDIUM_SHIELD;
			build(MEDIUM_SIZE);
			break;
		case MEDIUM:
			size=LITTLE;
			shield=LITTLE_SHIELD;
			maxshield=LITTLE_SHIELD;
			build(LITTLE_SIZE);
			break;
		case LITTLE:
			active=false;
			return true;
		}
		currentX=f.currentX;
		currentY=f.currentY;
//		render();
		update();
		active = true;
		return true;
	}
  public void build(double asize)
  {
    int i, j;
    int s;
    double theta, r;
    int x, y;
    // Create random shapes
		shape = new Polygon();
		//min sides - max sides
		s = MIN_ROCK_SIDES + (int)(Math.random() * (MAX_ROCK_SIDES - MIN_ROCK_SIDES));
		for (j = 0; j < s; j ++)
		{
			theta = 2 * Math.PI / s * j;
			r = asize * (MIN_ROCK_SIZE + (int) (Math.random() * (MAX_ROCK_SIZE - MIN_ROCK_SIZE)));
      x = (int) -Math.round(r * Math.sin(theta));
      y = (int)  Math.round(r * Math.cos(theta));
      shape.addPoint(x, y);
		}
		buildSprite();
    // Create random rotation
    deltaAngle = (Math.random() - 0.5) / 10;
    // Set a random motion for the asteroid.
    deltaX = Math.random() * Speed;
    if (Math.random() < 0.5)
			deltaX = -deltaX;
		deltaY = Math.random() * Speed;
    if (Math.random() < 0.5)
			deltaY = -deltaY;
  }
  public void update()
  {
		super.update();
		dmge.update();
  }
	public void draw(Graphics g)
	{
		super.draw(g);
		dmge.draw(g);
	}
	public boolean dead(double d)
	{
		shield-=d;
		dmge.display(shield/maxshield,currentX+width/2-30,currentY+height/2-30);
		if(shield/maxshield<=0)
			return true;
		return false;
	}
	public boolean explode(Flying f,int i)
	{
		if(active)
			return false;
		build(MIN_EXPLOSION_SIZE + (MAX_EXPLOSION_SIZE - MIN_EXPLOSION_SIZE) * Math.random());
		angle=f.angle;
    deltaAngle = (Math.random() * 2 * Math.PI - Math.PI) / 15;
    currentX = f.currentX;
    currentY = f.currentY;
    deltaX = f.shape.xpoints[i] * Math.random();
    deltaY = f.shape.ypoints[i] * Math.random();
		update();
    active = true;
		fadeout();
		return true;
	}
}

