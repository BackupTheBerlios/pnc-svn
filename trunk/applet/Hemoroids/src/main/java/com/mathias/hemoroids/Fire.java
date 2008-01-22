package com.mathias.hemoroids;

import java.awt.*;

class Fire extends Util
{
	private static final int fn=4;
	private static final int[] fx={-10,-10,10,10};
	private static final int[] fy={-10,-10,10,10};
	Fire()
	{
		super(fx,fy,fn,Color.orange);
	}
}
