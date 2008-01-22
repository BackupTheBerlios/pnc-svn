package com.mathias.hemoroids;

import java.awt.*;

public class Shield extends Util
{
	static final int SHIELD_FREQ = 3;
	static final int SHIELD_SIZE = 25;
	//shield shape
	Shield()
	{
		super(Color.yellow);
		shape.addPoint(-SHIELD_SIZE,-SHIELD_SIZE);
		shape.addPoint(-SHIELD_SIZE,SHIELD_SIZE);
		shape.addPoint(SHIELD_SIZE,SHIELD_SIZE);
		shape.addPoint(SHIELD_SIZE,-SHIELD_SIZE);
		buildSprite();
	}
	public void init()
	{
		Audio.shield.play();
		fr=cr;
		fg=cg;
		fb=cb;
		fadeout(SHIELD_FREQ);
		active=true;
	}
	public void draw(Graphics g)
	{
		if(active)
		{
			g.setColor(new Color(fr,fg,fb));
			g.drawPolygon(sprite);
		}
	}
	public void update(Ship s)
	{
		super.update(s,active);
	}
}
