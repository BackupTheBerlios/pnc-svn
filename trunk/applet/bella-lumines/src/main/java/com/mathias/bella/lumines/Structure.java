package com.mathias.bella.lumines;

public abstract class Structure {
	int x0, y0, x1, y1;

	boolean inside(int x, int y){
		return x <= x0 && x >= x1 && y <= y0 && y >= y1;
	}

	boolean inside(Structure s){
		return inside(s.x0, s.y0) && inside(s.x1, s.y0) && inside(s.x0, s.y1) && inside(s.x1, s.y1);
	}
}
