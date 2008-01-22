package com.mathias.bellatetris;

import java.awt.Color;
import java.awt.Graphics2D;

public class Block {

	protected final Color color;
	public int x;
	public int y;
	protected int width;
	protected int height;

	public Block(int x, int y, int width, int height, Color color){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
	}

	public void paint(Graphics2D g, int x0, int y0){
		g.setColor(color);
		g.fillRect(width*(x+x0), height*(y+y0), width, height);
	}

	public Block clone(){
		return new Block(x, y, width, height, color);
	}

	public Block clone(Color c){
		return new Block(x, y, width, height, c);
	}

	public Block clone(int x, int y){
		return new Block(x, y, width, height, color);
	}

}
