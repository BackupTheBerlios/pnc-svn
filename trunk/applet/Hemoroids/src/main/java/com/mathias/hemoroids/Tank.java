package com.mathias.hemoroids;

import java.awt.*;

public class Tank extends Flying
{

  Tank(int x, int y)
  {
    shape.addPoint(0 +x, 6 +y);
    shape.addPoint(-4 +x, 2 +y);
    shape.addPoint(-4 +x, -2 +y);
    shape.addPoint(0 +x, -6 +y);
    shape.addPoint(4 +x, -2 +y);
    shape.addPoint(4 +x, 2 +y);
		buildSprite();
		scale(.6);
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
    if(active)
      render();
		super.update();
  }
}
