package com.mathias.hemoroids;

import java.awt.*;

public class Explosion
{
  static final int MAX_SCRAP = 20;

  // Explosion data.
	Stone[] shit;
//	Scraps[] shit;

	Explosion()
	{
		shit=new Stone[MAX_SCRAP];
    for(int i=0;i<MAX_SCRAP;i++)
			shit[i]=new Stone();
/*		shit=new Scraps[MAX_SCRAP];
    for(int i=0;i<MAX_SCRAP;i++)
			shit[i]=new Scraps();*/
	}
	Explosion(Color cc)
	{
		shit=new Stone[MAX_SCRAP];
    for(int i=0;i<MAX_SCRAP;i++)
			shit[i]=new Stone(cc);
/*		shit=new Scraps[MAX_SCRAP];
    for(int i=0;i<MAX_SCRAP;i++)
			shit[i]=new Scraps();*/
	}
  public void init()
  {
    for(int i=0;i<MAX_SCRAP;i++)
//			shit[i].init();//Scraps
			shit[i].stop();//Stones
  }
  public void update()
  {
    // Move any active explosion debris. Stop explosion when its counter has expired.
    for(int i=0;i<MAX_SCRAP;i++)
			shit[i].update();
  }
  public void draw(Graphics g)
  {
    for(int i=0;i<MAX_SCRAP;i++)
			shit[i].draw(g);
  }
  public void explode(Flying f)
  {
    int i,j;
		Audio.explosion.play();
    f.render();
    for(i=0;i<f.shape.npoints;i++)
		{
	    for(j=0;j<MAX_SCRAP;j++)
				if(shit[j].explode(f,i))
					break;
    }
  }
}

