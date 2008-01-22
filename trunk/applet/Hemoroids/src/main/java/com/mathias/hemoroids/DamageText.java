package com.mathias.hemoroids;

import java.awt.*;

public class DamageText
{
	static final int TIMEONSCREEN = 10;

	private int damage;
	private int x,y;
	private boolean active;
	private int counter;
	DamageText()
	{
		active=false;
	}
	public void display(double d,double xx,double yy)
	{
		damage=(int)(d*100);
		if(damage<0)
			damage=0;
		x=(int)xx;
		y=(int)yy;
		counter=TIMEONSCREEN;
		active=true;
	}
	public void update()
	{
		if(active)
			if(--counter<=0)
				active=false;
	}
	public void draw(Graphics g)
	{
		if(active)
		{
			g.setColor(Color.white);
			g.drawString(Integer.toString(damage)+" %",x,y);
		}
	}
}
