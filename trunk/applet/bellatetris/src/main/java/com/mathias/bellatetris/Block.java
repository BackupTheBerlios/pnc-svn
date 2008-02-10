package com.mathias.bellatetris;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

public class Block {

	public int x;
	public int y;
	protected int width;
	protected int height;
	protected Color color;
	protected Image image;

	public Block(int x, int y, int width, int height, Image image, Color color){
		this(x, y, width, height);
		this.color = color;
		this.image = image;
	}

	public Block(int x, int y, int width, int height, Color color){
		this(x, y, width, height);
		this.color = color;
	}

	public Block(int x, int y, int width, int height, Image image){
		this(x, y, width, height);
		this.image = image;
	}
	
	private Block(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void paint(Graphics2D g, int x0, int y0){
		if(image != null){
			g.drawImage(image,  width*(x+x0), height*(y+y0), null);
		}else if(color != null){
			g.setColor(color);
			g.fillRect(width*(x+x0), height*(y+y0), width, height);
		}
	}

	public Block clone(){
		return new Block(x, y, width, height, image, color);
	}

	public Block clone(int x, int y){
		return new Block(x, y, width, height, image, color);
	}

}
