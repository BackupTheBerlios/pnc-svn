package com.mathias.hemoroids;

import java.awt.*;

public class Stars
{
	static final int STAR_SIZE = 2;
	static final int GLOWINT = 20;
  // Background stars.
  int numStars;
	Point temp;
  Point[] stars;
	boolean glowing;
	int c;

	Stars(int width,int height)
	{
		glowing=false;
    // Generate starry background.
    numStars = width * height / 5000;
    stars = new Point[numStars];
    for (int i=0;i<numStars;i++)
      stars[i] = new Point((int) (Math.random() * width), (int) (Math.random() * height));
	}
	public void draw(Graphics g)
	{
    //stars.
		g.setColor(Color.white);
		for (int i = 0; i < numStars; i++)
			g.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
		g.setColor(new Color(c,c,c));
		g.drawLine(temp.x,temp.y-STAR_SIZE,temp.x,temp.y+STAR_SIZE);
		g.drawLine(temp.x-STAR_SIZE,temp.y,temp.x+STAR_SIZE,temp.y);
	}
	public void update()
	{
		if(!glowing)
		{
			temp=stars[(int)(Math.random()*numStars)];
			c=255;
			glowing=true;
		}
		else
			if((c-=GLOWINT)<0)
			{
				c=0;
				glowing=false;
			}
	}
}
