package com.mathias.games.dogfight;

import java.awt.Polygon;

import com.mathias.drawutils.Util;

public class IntersectsTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int x = 10;
		int y = 10;
		int w = 10;
		int h = 10;
		Polygon p = new Polygon();
		p.addPoint(x, y);
		p.addPoint(x+w, y);
		p.addPoint(x+w, y+h);
		p.addPoint(x, y+h);
		if(p.intersects(x+9, y+9, 1, 1)){
			Util.LOG("INTERSECTS");
		}else{
			Util.LOG("does NOT intersect");
		}
	}

}
