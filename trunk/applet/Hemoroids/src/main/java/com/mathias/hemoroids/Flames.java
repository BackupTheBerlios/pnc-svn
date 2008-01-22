package com.mathias.hemoroids;

import java.awt.*;

public class Flames extends Flying
{

  Flames(int x, int y)
  {
		cr=255;
		cg=0;
		cb=0;
    shape.addPoint(-4 +x, 0 +y);
    shape.addPoint(0 +x, 12 +y);
    shape.addPoint(4 +x, 0 +y);
		buildSprite();
  }
  public void update(Flying f)
  {
    angle = f.angle;
    deltaAngle = f.deltaAngle;
    currentX = f.currentX;
    currentY = f.currentY;
    deltaX = f.deltaX;
    deltaY = f.deltaY;
		active=f.active;
		render();
		super.update();
  }
}
