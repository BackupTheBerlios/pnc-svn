package com.mathias.hemoroids;

import java.awt.*;

public class Util extends Flying
{
	Util(Color cc)
	{
		cr=cc.getRed();
		cg=cc.getGreen();
		cb=cc.getBlue();
	}
	Util(int[] x,int[] y,int n,Color cc)
	{
		shape=new Polygon(x,y,n);
		buildSprite();
		cr=cc.getRed();
		cg=cc.getGreen();
		cb=cc.getBlue();
	}
  public void update(Flying f)
  {
    angle = f.angle;
    deltaAngle = f.deltaAngle;
    currentX = f.currentX;
    currentY = f.currentY;
    deltaX = f.deltaX;
    deltaY = f.deltaY;
    active = f.active;
		super.update(false);
  }
  public void update(Flying f,boolean factive)
  {
    angle = f.angle;
    deltaAngle = f.deltaAngle;
    currentX = f.currentX;
    currentY = f.currentY;
    deltaX = f.deltaX;
    deltaY = f.deltaY;
		active = factive;
		super.update(false);
  }
}
