package com.mathias.bella.lumines;

import java.util.Random;

public class Block {
	static Random rand = new Random();

	public int x;
	public int y;
	public int image;

	public Block(int x, int y) {
		this(x, y, rand.nextInt(Constants.BLOCK_IMAGES));
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
