package com.mathias.android.boardgame.tetris;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Block {
	
	public int x;
	public int y;
	protected int width;
	protected int height;
	protected int color;
	protected Bitmap image;

	private final Paint mPaint = new Paint();

	public Block(int x, int y, int width, int height, Bitmap image, int color){
		this(x, y, width, height);
		this.color = color;
		this.image = image;
	}

	public Block(int x, int y, int width, int height, int color){
		this(x, y, width, height);
		this.color = color;
	}

	public Block(int x, int y, int width, int height, Bitmap image){
		this(x, y, width, height);
		this.image = image;
	}
	
	private Block(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void paint(Canvas g, int x0, int y0){
		if(image != null){
			g.drawBitmap(image,  width*(x+x0), height*(y+y0), null);
		}else{
			g.drawColor(color);
			g.drawRect(width*(x+x0), height*(y+y0), width, height, mPaint);
		}
	}

	public Block clone(){
		return new Block(x, y, width, height, image, color);
	}

	public Block clone(int x, int y){
		return new Block(x, y, width, height, image, color);
	}

}
