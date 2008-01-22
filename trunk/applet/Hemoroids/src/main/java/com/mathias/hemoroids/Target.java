package com.mathias.hemoroids;

import java.awt.*;

class Target
{
	private static final int SIZE = 40;

	private static final int lbn=4;
	private static final int[] lbx={0,-6,0,-8};//
	private static final int[] lby={-12,0,12,0};
	private static final int rbn=4;
	private static final int[] rbx={0,6,0,8};//
	private static final int[] rby={-12,0,12,0};

	private Util leftBracket, rightBracket;
	public Flying currentF,oldF;

	boolean active=false;

	Target()
	{
		leftBracket=new Util(lbx,lby,lbn,Color.red);
		leftBracket.move(-SIZE,0);
		leftBracket.toColor();
		rightBracket=new Util(rbx,rby,rbn,Color.red);
		rightBracket.move(SIZE,0);
		rightBracket.toColor();
	}
	public void lock(Flying f)
	{
		if(f!=null)
		{
			Audio.lock.play();
			currentF=f;
			active=true;
			update();
			leftBracket.active=currentF.active;
			rightBracket.active=currentF.active;
		}
	}
	public void stop()
	{
		oldF=currentF;
		active=false;
	}
	public void update()
	{
		if(active)
		{
			leftBracket.currentX=currentF.currentX;
			leftBracket.currentY=currentF.currentY;
			leftBracket.render();
			rightBracket.currentX=currentF.currentX;
			rightBracket.currentY=currentF.currentY;
			rightBracket.render();
			if(!currentF.active)
				active=false;
		}
	}
	public void draw(Graphics g)
	{
		if(active)
		{
			leftBracket.draw(g);
			rightBracket.draw(g);
		}
	}
}
