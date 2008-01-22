package com.mathias.lumines;

import java.util.Random;

public class Block {

	private final static Random rand = new Random();

	public int x;
	public int y;
	public int image;
	public boolean remove = false;
	public boolean falling = false;

	public Block(int x, int y) {
		this(x, y, rand.nextInt(2));
	}

	public Block(int x, int y, boolean falling) {
		this(x, y, rand.nextInt(2));
		this.falling = falling;
	}

	public Block(int x, int y, int image) {
		this.x = x;
		this.y = y;
		this.image = image;
	}

	public boolean equals(Block b){
		return this.x == b.x && this.y == b.y;
	}
	
}
