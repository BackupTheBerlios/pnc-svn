package com.mathias.hemoroids;

import java.awt.*;

public class Ufo extends Flying
{
	static final int MIN_UFO_SPEED = 2;
	static final int MAX_UFO_SPEED = 4;
	static final int MAX_UFO_SIZE = 2;
	static final int MIN_UFO_DIST = 160;
	static final int UFO_PASSES = 2;
	static final int MAX_MISSILES = 10;

	//roof & floor shape
	private static final int rfn = 3;
	private static final int[] rfx={-28,28,0};//3
	private static final int[] rfy={0,0,-10};

	//window shape
	private static final int WINDOWS = 3;
	private static final int wn = 4;
	private static final int[] wx={-3,3,3,-3};//4
	private static final int[] wy={-3,-3,3,3};

  // Flying saucer data.
  static int Counter;       // Time counter for each pass.
	static int PassesLeft;

	Missile[] miss;
	private Target targ;

	private Util roof;
	private Util floor;
	private Util[] wind;
	private Explosion exp;

  Ufo()
  {
		int i;
		cr=80;
		cg=80;
		cb=80;
    // Create shape for the flying saucer.
    shape.addPoint(-24, -4);
    shape.addPoint(-24, 4);
    shape.addPoint(24,4);
    shape.addPoint(24, -4);
		buildSprite();
		miss=new Missile[MAX_MISSILES];
		for(i=0;i<MAX_MISSILES;i++)
			miss[i]=new Missile();
		targ=new Target();
		roof=new Util(rfx,rfy,rfn,Color.gray);
		roof.move(0,-4);
		floor=new Util(rfx,rfy,rfn,Color.gray);
		floor.scale(-1);
		floor.move(0,4);
		wind=new Util[WINDOWS];
		for(i=0;i<WINDOWS;i++)
			wind[i]=new Util(wx,wy,wn,Color.white);
		wind[0].move(-11,0);
		wind[2].move(11,0);
		//explosion
		exp=new Explosion(Color.white);
  }
  public void init(Flying skepp)
  {
    // Randomly set flying saucer at left or right edge of the screen.
    active = true;
    currentX = -Flying.width / 2;
    currentY = Math.random() * Flying.height;
    deltaX = MIN_UFO_SPEED + Math.random() * (MAX_UFO_SPEED - MIN_UFO_SPEED);
    if (Math.random() < 0.5) {
      deltaX = -deltaX;
      currentX = Flying.width / 2;
    }
    deltaY = MIN_UFO_SPEED + Math.random() * (MAX_UFO_SPEED - MIN_UFO_SPEED);
    if (Math.random() < 0.5)
      deltaY = -deltaY;
    render();
		Audio.saucer.loop();
    // Set counter for this pass.
    Counter = (int) Math.floor(Flying.width / Math.abs(deltaX));
		PassesLeft=UFO_PASSES;
		targ.lock(skepp);
  }
  public void update(int max_away)
  {
    int i, d;
    if(active) 
		{
      if (--Counter <= 0)
				if(--PassesLeft > 0)
					init(targ.currentF);
				else
					stop();
      else
      {
        // On occassion, fire a missle at the ship if the saucer is not too close to it.
				d = (int) Math.max(Math.abs(currentX - targ.currentF.currentX), Math.abs(currentY - targ.currentF.currentY));
				if(d > MIN_UFO_DIST && targ.active && active && Math.random() < .02)
					for(i=0;i<MAX_MISSILES && i<max_away;i++)
						if(!miss[i].active)
						{
							miss[i].init(this,targ.currentF,false);
							break;
						}
       }
    }
		for(i=0;i<MAX_MISSILES;i++)
			miss[i].update();
		super.update();
		targ.update();
		roof.update(this);
		floor.update(this);
		for(i=0;i<WINDOWS;i++)
			wind[i].update(this);
		exp.update();
  }
  public void stop()
  {
    active = false;
    Counter = 0;
    PassesLeft = 0;
		Audio.saucer.stop();
		targ.stop();
		super.stop();
//		roof.stop();
/*		floor.stop();
		for(int i=0;i<WINDOWS;i++)
			wind[i].stop();*/
		exp.explode(this);
		exp.explode(roof);
		exp.explode(floor);
		for(int i=0;i<WINDOWS;i++)
			exp.explode(wind[i]);
  }
	public void draw(Graphics g)
	{
		int i;
		for(i=0;i<MAX_MISSILES;i++)
			miss[i].draw(g);
		super.draw(g);
		targ.draw(g);
		roof.draw(g);
		floor.draw(g);
		for(i=0;i<WINDOWS;i++)
			wind[i].draw(g);
		exp.draw(g);
	}
	public int isColliding(Ship s)
	{
		if(roof.isColliding(s)!=0)
		{
			stop();
			return 1;
		}
		if(floor.isColliding(s)!=0)
		{
			stop();
			return 1;
		}
		for(int i=0;i<WINDOWS;i++)
			if(wind[i].isColliding(s)!=0)
			{
				stop();
				return 1;
			}
		return 0;
	}
	public void isColliding(Shield s)
	{
		int i;
		for(i=0;i<MAX_MISSILES;i++)
			if(miss[i].isColliding(s)!=0)
				miss[i].stop();
		if(s.isColliding(this)!=0)
		{
			if(Math.abs(deltaX)>Math.abs(s.deltaX))
				deltaX=-deltaX;
			else
				deltaX=-s.deltaX;
			if(Math.abs(deltaY)>Math.abs(s.deltaY))
				deltaY=-deltaY;
			else
				deltaY=-s.deltaY;
			currentX=oldX+s.deltaX;
			currentY=oldY+s.deltaY;
		}
	}
}

